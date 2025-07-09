package org.example.afd.service.impl;

import org.example.afd.entity.Order;
import org.example.afd.entity.Payment;
import org.example.afd.mapper.OrderMapper;
import org.example.afd.mapper.PaymentMapper;
import org.example.afd.mapper.PaymentChannelMapper;
import org.example.afd.model.Result;
import org.example.afd.service.PaymentService;
import org.example.afd.dto.PaymentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 统一支付服务实现类
 * 
 * 处理所有支付相关业务逻辑：
 * - 创建支付、查询支付状态
 * - 确认支付、取消支付
 * - 处理支付回调
 * - 退款相关操作
 * 
 * @author AFD Team
 * @version 2.0
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private PaymentChannelMapper paymentChannelMapper;
    
    /**
     * 创建支付
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createPayment(String orderNo, Integer paymentMethod, Long userId) {
        try {
            log.info("创建支付，订单号: {}, 支付方式: {}, 用户ID: {}", orderNo, paymentMethod, userId);
            
            // 查询订单信息
            Order order = orderMapper.getOrderByOrderNo(orderNo);
            if (order == null) {
                log.warn("订单不存在: {}", orderNo);
                return Result.error("订单不存在");
            }
            
            // 验证订单所有者
            if (!order.getUserId().equals(userId.intValue())) {
                log.warn("用户 {} 尝试支付他人订单: {}", userId, orderNo);
                return Result.error("无权限操作此订单");
            }
            
            // 验证订单状态
            if (!order.getOrderStatus().equals(1)) {
                log.warn("订单状态不允许支付，订单: {}, 状态: {}", orderNo, order.getOrderStatus());
                return Result.error("订单状态不允许支付");
            }
            
            // 检查是否已有支付记录
            Payment existingPayment = paymentMapper.getPaymentByOrderId(order.getOrderId());
            if (existingPayment != null && existingPayment.getPaymentStatus().equals(3)) {
                log.warn("订单已支付成功: {}", orderNo);
                return Result.error("订单已支付");
            }
            
            // 生成支付流水号
            String paymentNo = "PAY" + System.currentTimeMillis() + 
                              String.format("%04d", new Random().nextInt(10000));
            
            // 创建支付记录
            Payment payment = new Payment();
            payment.setPaymentNo(paymentNo);
            payment.setOrderId(order.getOrderId());
            payment.setUserId(userId.intValue());
            payment.setChannelId(paymentMethod); // 使用channelId替代paymentMethod
            payment.setPaymentAmount(order.getTotalAmount());
            payment.setPaymentStatus(1); // 待支付
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            
            // 保存支付记录
            paymentMapper.insertPayment(payment);
            
            // 构建支付参数
            Map<String, Object> paymentParams = new HashMap<>();
            paymentParams.put("paymentNo", paymentNo);
            paymentParams.put("orderNo", orderNo);
            paymentParams.put("amount", order.getTotalAmount());
            paymentParams.put("paymentMethod", paymentMethod);
            paymentParams.put("paymentMethodName", getPaymentMethodName(paymentMethod));
            
            // 根据支付方式生成不同的支付参数
            switch (paymentMethod) {
                case 1: // 微信支付
                    paymentParams.put("wechatParams", generateWechatPayParams(paymentNo, order));
                    break;
                case 2: // 支付宝
                    paymentParams.put("alipayParams", generateAlipayParams(paymentNo, order));
                    break;
                case 3: // 银行卡
                    paymentParams.put("bankParams", generateBankPayParams(paymentNo, order));
                    break;
                case 4: // 余额支付
                    paymentParams.put("balanceParams", generateBalancePayParams(paymentNo, order));
                    break;
                default:
                    return Result.error("不支持的支付方式");
            }
            
            log.info("支付创建成功，支付流水号: {}", paymentNo);
            return Result.success(paymentParams);
            
        } catch (Exception e) {
            log.error("创建支付失败", e);
            return Result.error("创建支付失败");
        }
    }
    
    /**
     * 查询支付状态
     */
    @Override
    public Result<Map<String, Object>> getPaymentStatus(String paymentNo, Long userId) {
        try {
            log.info("查询支付状态，支付流水号: {}, 用户ID: {}", paymentNo, userId);
            
            Payment payment = paymentMapper.getPaymentByPaymentNo(paymentNo);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }
            
            // 验证用户权限
            if (!payment.getUserId().equals(userId.intValue())) {
                log.warn("用户 {} 尝试查询他人支付: {}", userId, paymentNo);
                return Result.error("无权限查询此支付");
            }
            
            Map<String, Object> status = new HashMap<>();
            status.put("paymentNo", payment.getPaymentNo());
            status.put("paymentStatus", payment.getPaymentStatus());
            status.put("paymentStatusName", getPaymentStatusName(payment.getPaymentStatus()));
            status.put("paymentAmount", payment.getPaymentAmount());
            status.put("paymentTime", payment.getPaymentTime());
            status.put("createTime", payment.getCreateTime());
            
            return Result.success(status);
            
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return Result.error("查询支付状态失败");
        }
    }
    
    /**
     * 确认支付完成
     */
    @Override
    @Transactional
    public Result<Boolean> confirmPayment(String paymentNo, Map<String, Object> paymentData) {
        try {
            log.info("确认支付完成，支付流水号: {}, 数据: {}", paymentNo, paymentData);
            
            Payment payment = paymentMapper.getPaymentByPaymentNo(paymentNo);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }
            
            // 验证支付状态
            if (payment.getPaymentStatus().equals(3)) {
                log.warn("支付已完成: {}", paymentNo);
                return Result.success(true);
            }
            
            if (!payment.getPaymentStatus().equals(1) && !payment.getPaymentStatus().equals(2)) {
                log.warn("支付状态不允许确认，状态: {}", payment.getPaymentStatus());
                return Result.error("支付状态不允许确认");
            }
            
            // 更新支付状态
            payment.setPaymentStatus(3); // 支付成功
            payment.setPaymentTime(LocalDateTime.now());
            payment.setCallbackTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            
            // 设置第三方交易信息
            if (paymentData.containsKey("thirdPartyOrderNo")) {
                payment.setThirdPartyOrderNo(paymentData.get("thirdPartyOrderNo").toString());
            }
            if (paymentData.containsKey("thirdPartyTransactionId")) {
                payment.setThirdPartyTransactionId(paymentData.get("thirdPartyTransactionId").toString());
            }
            
            paymentMapper.updatePayment(payment);
            
            // 更新订单状态
            Order order = orderMapper.getOrderById(payment.getOrderId());
            if (order != null) {
                if (order.getOrderType().equals(2)) {
                    // 订阅订单，直接完成
                    order.setOrderStatus(4); // 已完成
                } else {
                    // 购物订单，改为待发货
                    order.setOrderStatus(2); // 待发货
                }
                order.setPaidAmount(payment.getPaymentAmount());
                order.setUpdateTime(LocalDateTime.now());
                orderMapper.updateOrder(order);
                
                log.info("订单状态已更新，订单ID: {}, 新状态: {}", order.getOrderId(), order.getOrderStatus());
            }
            
            log.info("支付确认成功: {}", paymentNo);
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("确认支付失败", e);
            return Result.error("确认支付失败");
        }
    }
    
    /**
     * 取消支付
     */
    @Override
    @Transactional
    public Result<Boolean> cancelPayment(String paymentNo, Long userId) {
        try {
            log.info("取消支付，支付流水号: {}, 用户ID: {}", paymentNo, userId);
            
            Payment payment = paymentMapper.getPaymentByPaymentNo(paymentNo);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }
            
            // 验证用户权限
            if (!payment.getUserId().equals(userId.intValue())) {
                log.warn("用户 {} 尝试取消他人支付: {}", userId, paymentNo);
                return Result.error("无权限操作此支付");
            }
            
            // 验证支付状态
            if (!payment.getPaymentStatus().equals(1) && !payment.getPaymentStatus().equals(2)) {
                log.warn("支付状态不允许取消，状态: {}", payment.getPaymentStatus());
                return Result.error("支付状态不允许取消");
            }
            
            // 更新支付状态
            payment.setPaymentStatus(5); // 已取消
            payment.setUpdateTime(LocalDateTime.now());
            paymentMapper.updatePayment(payment);
            
            log.info("支付取消成功: {}", paymentNo);
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("取消支付失败", e);
            return Result.error("取消支付失败");
        }
    }
    
    /**
     * 处理支付回调
     */
    @Override
    @Transactional
    public Result<Boolean> handlePaymentCallback(String paymentChannel, Map<String, Object> callbackData) {
        try {
            log.info("处理支付回调，渠道: {}, 数据: {}", paymentChannel, callbackData);
            
            // 从回调数据中提取支付流水号
            String paymentNo = extractPaymentNoFromCallback(paymentChannel, callbackData);
            if (paymentNo == null) {
                log.error("无法从回调数据中提取支付流水号");
                return Result.error("回调数据格式错误");
            }
            
            return confirmPayment(paymentNo, callbackData);
            
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return Result.error("处理支付回调失败");
        }
    }
    
    /**
     * 获取用户支付记录
     */
    @Override
    public Result<List<PaymentDTO>> getUserPayments(Long userId, Integer orderType, Integer page, Integer size) {
        try {
            log.info("获取用户支付记录，用户ID: {}, 订单类型: {}, 页码: {}, 大小: {}", 
                    userId, orderType, page, size);
            
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            int offset = (page - 1) * size;
            
            List<Payment> payments = paymentMapper.getUserPayments(userId.intValue(), orderType, offset, size);
            List<PaymentDTO> paymentDTOs = new ArrayList<>();
            
            for (Payment payment : payments) {
                PaymentDTO dto = convertToPaymentDTO(payment);
                paymentDTOs.add(dto);
            }
            
            return Result.success(paymentDTOs);
            
        } catch (Exception e) {
            log.error("获取用户支付记录失败", e);
            return Result.error("获取支付记录失败");
        }
    }
    
    /**
     * 获取支付方式列表
     */
    @Override
    public Result<List<Map<String, Object>>> getPaymentMethods(Integer orderType) {
        try {
            log.info("=== 开始获取支付方式列表 ===");
            log.info("订单类型: {}", orderType);
            
            // 从数据库获取启用的支付渠道
            log.info("正在查询启用的支付渠道...");
            List<Map<String, Object>> channels = paymentChannelMapper.getEnabledPaymentChannels();
            log.info("数据库查询完成，原始渠道数量: {}", channels != null ? channels.size() : 0);
            
            if (channels != null && !channels.isEmpty()) {
                log.info("原始支付渠道数据:");
                for (int i = 0; i < channels.size(); i++) {
                    Map<String, Object> channel = channels.get(i);
                    log.info("渠道[{}]: {}", i, channel);
                }
            }
            
            // 转换为前端需要的格式
            List<Map<String, Object>> methods = new ArrayList<>();
            for (Map<String, Object> channel : channels) {
                Map<String, Object> method = new HashMap<>();
                method.put("channelId", channel.get("channel_id"));
                method.put("code", channel.get("channel_code"));
                method.put("name", channel.get("channel_name"));
                method.put("iconUrl", channel.get("icon_url"));
                method.put("description", channel.get("description"));
                method.put("sortOrder", channel.get("sort_order"));
                method.put("enabled", true); // 只返回启用的渠道
                
                methods.add(method);
                log.info("转换后的支付方式: {}", method);
            }
            
            log.info("=== 支付方式列表获取完成 ===");
            log.info("最终返回 {} 个可用支付渠道", methods.size());
            return Result.success(methods);
            
        } catch (Exception e) {
            log.error("=== 获取支付方式失败 ===", e);
            return Result.error("获取支付方式失败");
        }
    }
    
    /**
     * 申请退款
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> applyRefund(String orderNo, BigDecimal refundAmount, 
                                                  String refundReason, Long userId) {
        try {
            log.info("申请退款，订单号: {}, 退款金额: {}, 退款原因: {}, 用户ID: {}", 
                    orderNo, refundAmount, refundReason, userId);
            
            // 查询订单
            Order order = orderMapper.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 验证用户权限
            if (!order.getUserId().equals(userId.intValue())) {
                log.warn("用户 {} 尝试申请他人订单退款: {}", userId, orderNo);
                return Result.error("无权限操作此订单");
            }
            
            // 验证订单状态
            if (!Arrays.asList(3, 4).contains(order.getOrderStatus())) {
                return Result.error("订单状态不允许退款");
            }
            
            // 验证退款金额
            if (refundAmount.compareTo(order.getPaidAmount()) > 0) {
                return Result.error("退款金额不能超过已支付金额");
            }
            
            // 生成退款单号
            String refundNo = "REF" + System.currentTimeMillis() + 
                             String.format("%04d", new Random().nextInt(10000));
            
            // 更新订单状态为退款中
            order.setOrderStatus(6); // 退款中
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateOrder(order);
            
            Map<String, Object> refundInfo = new HashMap<>();
            refundInfo.put("refundNo", refundNo);
            refundInfo.put("refundAmount", refundAmount);
            refundInfo.put("refundStatus", 1); // 退款申请中
            refundInfo.put("message", "退款申请已提交，请等待处理");
            
            log.info("退款申请成功，退款单号: {}", refundNo);
            return Result.success(refundInfo);
            
        } catch (Exception e) {
            log.error("申请退款失败", e);
            return Result.error("申请退款失败");
        }
    }
    
    /**
     * 查询退款状态
     */
    @Override
    public Result<Map<String, Object>> getRefundStatus(String refundNo, Long userId) {
        try {
            log.info("查询退款状态，退款单号: {}, 用户ID: {}", refundNo, userId);
            
            // 这里应该查询退款表，暂时模拟返回
            Map<String, Object> refundStatus = new HashMap<>();
            refundStatus.put("refundNo", refundNo);
            refundStatus.put("refundStatus", 2); // 退款成功
            refundStatus.put("refundStatusName", "退款成功");
            refundStatus.put("refundTime", LocalDateTime.now());
            
            return Result.success(refundStatus);
            
        } catch (Exception e) {
            log.error("查询退款状态失败", e);
            return Result.error("查询退款状态失败");
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 将Payment实体转换为PaymentDTO
     */
    private PaymentDTO convertToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setPaymentNo(payment.getPaymentNo());
        dto.setOrderId(payment.getOrderId());
        dto.setUserId(payment.getUserId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentMethodName(getPaymentMethodName(payment.getPaymentMethod()));
        dto.setPaymentChannel(payment.getPaymentChannel());
        dto.setPaymentAmount(payment.getPaymentAmount());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentStatusName(getPaymentStatusName(payment.getPaymentStatus()));
        dto.setThirdPartyOrderNo(payment.getThirdPartyOrderNo());
        dto.setThirdPartyTransactionId(payment.getThirdPartyTransactionId());
        dto.setPaymentTime(payment.getPaymentTime());
        dto.setCallbackTime(payment.getCallbackTime());
        dto.setCreateTime(payment.getCreateTime());
        dto.setUpdateTime(payment.getUpdateTime());
        
        return dto;
    }
    
    /**
     * 创建支付方式信息
     */
    private Map<String, Object> createPaymentMethodInfo(Integer method, String name, String channel, Boolean enabled) {
        Map<String, Object> info = new HashMap<>();
        info.put("method", method);
        info.put("name", name);
        info.put("channel", channel);
        info.put("enabled", enabled);
        return info;
    }
    
    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(Integer paymentMethod) {
        switch (paymentMethod) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "银行卡";
            case 4: return "余额支付";
            default: return "未知支付方式";
        }
    }
    
    /**
     * 获取支付渠道名称
     */
    private String getPaymentChannelName(Integer paymentMethod) {
        switch (paymentMethod) {
            case 1: return "wechat";
            case 2: return "alipay";
            case 3: return "bank";
            case 4: return "balance";
            default: return "unknown";
        }
    }
    
    /**
     * 获取支付状态名称
     */
    private String getPaymentStatusName(Integer paymentStatus) {
        switch (paymentStatus) {
            case 1: return "待支付";
            case 2: return "支付中";
            case 3: return "支付成功";
            case 4: return "支付失败";
            case 5: return "已取消";
            case 6: return "已退款";
            default: return "未知状态";
        }
    }
    
    /**
     * 生成微信支付参数
     */
    private Map<String, Object> generateWechatPayParams(String paymentNo, Order order) {
        Map<String, Object> params = new HashMap<>();
        params.put("appId", "wx_app_id");
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        params.put("package", "prepay_id=" + paymentNo);
        params.put("signType", "MD5");
        params.put("paySign", "mock_pay_sign");
        return params;
    }
    
    /**
     * 生成支付宝支付参数
     */
    private Map<String, Object> generateAlipayParams(String paymentNo, Order order) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderInfo", "mock_alipay_order_info");
        params.put("payUrl", "https://openapi.alipay.com/gateway.do");
        return params;
    }
    
    /**
     * 生成银行卡支付参数
     */
    private Map<String, Object> generateBankPayParams(String paymentNo, Order order) {
        Map<String, Object> params = new HashMap<>();
        params.put("bankList", Arrays.asList("ICBC", "ABC", "BOC", "CCB"));
        params.put("payUrl", "https://pay.bank.com/gateway");
        return params;
    }
    
    /**
     * 生成余额支付参数
     */
    private Map<String, Object> generateBalancePayParams(String paymentNo, Order order) {
        Map<String, Object> params = new HashMap<>();
        params.put("needPassword", true);
        params.put("balance", 1000.00); // 模拟用户余额
        return params;
    }
    
    /**
     * 从回调数据中提取支付流水号
     */
    private String extractPaymentNoFromCallback(String paymentChannel, Map<String, Object> callbackData) {
        switch (paymentChannel) {
            case "wechat":
                return callbackData.get("out_trade_no") != null ? 
                       callbackData.get("out_trade_no").toString() : null;
            case "alipay":
                return callbackData.get("out_trade_no") != null ? 
                       callbackData.get("out_trade_no").toString() : null;
            default:
                return callbackData.get("paymentNo") != null ? 
                       callbackData.get("paymentNo").toString() : null;
        }
    }
} 