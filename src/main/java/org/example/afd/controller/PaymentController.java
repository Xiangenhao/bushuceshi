package org.example.afd.controller;

import org.example.afd.model.Result;
import org.example.afd.service.PaymentService;
import org.example.afd.dto.PaymentDTO;
import org.example.afd.mapper.PaymentMapper;
import org.example.afd.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

/**
 * 统一支付控制器
 * 
 * 处理所有支付相关操作：
 * - 创建支付
 * - 查询支付状态
 * - 确认支付完成
 * - 取消支付
 * - 处理支付回调
 * - 获取支付记录
 * - 退款相关操作
 * 
 * @author AFD Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    /**
     * 创建支付
     * 
     * @param paymentRequest 支付请求参数
     * @param request HTTP请求
     * @return 支付参数（包含支付URL、参数等）
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createPayment(
            @RequestBody Map<String, Object> paymentRequest,
            HttpServletRequest request) {
        try {
            // 从请求头获取用户ID
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户 {} 创建支付，请求参数: {}", userId, paymentRequest);
            
            // 验证必需参数
            if (!paymentRequest.containsKey("orderNo")) {
                return Result.error("订单号不能为空");
            }
            if (!paymentRequest.containsKey("paymentMethod")) {
                return Result.error("支付方式不能为空");
            }
            
            String orderNo = paymentRequest.get("orderNo").toString();
            Integer paymentMethod = Integer.valueOf(paymentRequest.get("paymentMethod").toString());
            
            // 创建支付
            return paymentService.createPayment(orderNo, paymentMethod, userId);
            
        } catch (NumberFormatException e) {
            log.error("支付参数格式错误", e);
            return Result.error("支付参数格式错误");
        } catch (Exception e) {
            log.error("创建支付失败", e);
            return Result.error("创建支付失败");
        }
    }
    
    /**
     * 查询支付状态
     * 
     * @param paymentNo 支付流水号
     * @param request HTTP请求
     * @return 支付状态信息
     */
    @GetMapping("/status/{paymentNo}")
    public Result<Map<String, Object>> getPaymentStatus(
            @PathVariable String paymentNo,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户 {} 查询支付状态: {}", userId, paymentNo);
            
            return paymentService.getPaymentStatus(paymentNo, userId);
            
        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return Result.error("查询支付状态失败");
        }
    }
    
    /**
     * 确认支付完成
     * 
     * @param paymentNo 支付流水号
     * @param paymentData 支付回调数据
     * @param request HTTP请求
     * @return 确认结果
     */
    @PostMapping("/{paymentNo}/confirm")
    public Result<Boolean> confirmPayment(
            @PathVariable String paymentNo,
            @RequestBody Map<String, Object> paymentData,
            HttpServletRequest request) {
        try {
            log.info("确认支付完成，支付流水号: {}, 数据: {}", paymentNo, paymentData);
            
            return paymentService.confirmPayment(paymentNo, paymentData);
            
        } catch (Exception e) {
            log.error("确认支付失败", e);
            return Result.error("确认支付失败");
        }
    }
    
    /**
     * 取消支付
     * 
     * @param paymentNo 支付流水号
     * @param request HTTP请求
     * @return 取消结果
     */
    @PostMapping("/{paymentNo}/cancel")
    public Result<Boolean> cancelPayment(
            @PathVariable String paymentNo,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户 {} 取消支付: {}", userId, paymentNo);
            
            return paymentService.cancelPayment(paymentNo, userId);
            
        } catch (Exception e) {
            log.error("取消支付失败", e);
            return Result.error("取消支付失败");
        }
    }
    
    /**
     * 处理支付回调
     * 
     * @param paymentChannel 支付渠道（wechat/alipay/bank）
     * @param callbackData 回调数据
     * @return 处理结果
     */
    @PostMapping("/callback/{paymentChannel}")
    public Result<Boolean> handlePaymentCallback(
            @PathVariable String paymentChannel,
            @RequestBody Map<String, Object> callbackData) {
        try {
            log.info("收到支付回调，渠道: {}, 数据: {}", paymentChannel, callbackData);
            
            return paymentService.handlePaymentCallback(paymentChannel, callbackData);
            
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return Result.error("处理支付回调失败");
        }
    }
    
    /**
     * 获取用户支付记录
     * 
     * @param orderType 订单类型：1-购物订单，2-订阅订单，null-全部
     * @param page 页码
     * @param size 每页大小
     * @param request HTTP请求
     * @return 支付记录列表
     */
    @GetMapping("/records")
    public Result<List<PaymentDTO>> getUserPayments(
            @RequestParam(required = false) Integer orderType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户 {} 获取支付记录，订单类型: {}, 页码: {}, 大小: {}", 
                    userId, orderType, page, size);
            
            return paymentService.getUserPayments(userId, orderType, page, size);
            
        } catch (Exception e) {
            log.error("获取支付记录失败", e);
            return Result.error("获取支付记录失败");
        }
    }
    
    /**
     * 获取支付方式列表
     * 
     * @param orderType 订单类型：1-购物订单，2-订阅订单
     * @return 支付方式列表
     */
    @GetMapping("/methods")
    public Result<List<Map<String, Object>>> getPaymentMethods(
            @RequestParam(required = false) Integer orderType) {
        try {
            log.info("获取支付方式列表，订单类型: {}", orderType);
            
            return paymentService.getPaymentMethods(orderType);
            
        } catch (Exception e) {
            log.error("获取支付方式失败", e);
            return Result.error("获取支付方式失败");
        }
    }
    
    /**
     * 获取支付渠道列表（为Android客户端提供）
     * 
     * @return 支付渠道列表
     */
    @GetMapping("/channels")
    public Result<List<Map<String, Object>>> getPaymentChannels() {
        try {
            log.info("=== 客户端获取支付渠道列表 ===");
            
            Result<List<Map<String, Object>>> result = paymentService.getPaymentMethods(null);
            
            if (result.isSuccess()) {
                log.info("支付渠道列表获取成功，数量: {}", result.getData().size());
                for (Map<String, Object> channel : result.getData()) {
                    log.info("支付渠道详情: {}", channel);
                }
            } else {
                log.error("支付渠道列表获取失败: {}", result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取支付渠道列表失败", e);
            return Result.error("获取支付渠道列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 申请退款
     * 
     * @param refundRequest 退款申请参数
     * @param request HTTP请求
     * @return 退款申请结果
     */
    @PostMapping("/refund/apply")
    public Result<Map<String, Object>> applyRefund(
            @RequestBody Map<String, Object> refundRequest,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户 {} 申请退款，参数: {}", userId, refundRequest);
            
            // 验证必需参数
            if (!refundRequest.containsKey("orderNo")) {
                return Result.error("订单号不能为空");
            }
            if (!refundRequest.containsKey("refundAmount")) {
                return Result.error("退款金额不能为空");
            }
            if (!refundRequest.containsKey("refundReason")) {
                return Result.error("退款原因不能为空");
            }
            
            String orderNo = refundRequest.get("orderNo").toString();
            java.math.BigDecimal refundAmount = new java.math.BigDecimal(refundRequest.get("refundAmount").toString());
            String refundReason = refundRequest.get("refundReason").toString();
            
            return paymentService.applyRefund(orderNo, refundAmount, refundReason, userId);
            
        } catch (NumberFormatException e) {
            log.error("退款金额格式错误", e);
            return Result.error("退款金额格式错误");
        } catch (Exception e) {
            log.error("申请退款失败", e);
            return Result.error("申请退款失败");
        }
    }
    
    /**
     * 查询退款状态
     * 
     * @param refundNo 退款单号
     * @param request HTTP请求
     * @return 退款状态
     */
    @GetMapping("/refund/status/{refundNo}")
    public Result<Map<String, Object>> getRefundStatus(
            @PathVariable String refundNo,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户 {} 查询退款状态: {}", userId, refundNo);
            
            return paymentService.getRefundStatus(refundNo, userId);
            
        } catch (Exception e) {
            log.error("查询退款状态失败", e);
            return Result.error("查询退款状态失败");
        }
    }
    
    /**
     * 模拟支付成功回调 - 用于测试
     */
    @PostMapping("/mock/callback/success")
    public Result<Map<String, Object>> mockPaymentSuccess(@RequestBody Map<String, Object> request) {
        try {
            String paymentNo = (String) request.get("paymentNo");
            String thirdPartyOrderNo = (String) request.get("thirdPartyOrderNo");
            String transactionId = (String) request.get("transactionId");
            
            if (paymentNo == null) {
                return Result.error("支付单号不能为空");
            }
            
            log.info("=== 模拟支付成功回调 ===");
            log.info("支付单号: {}", paymentNo);
            log.info("第三方订单号: {}", thirdPartyOrderNo);
            log.info("交易流水号: {}", transactionId);
            
            // 构造回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("paymentNo", paymentNo);
            callbackData.put("status", "SUCCESS");
            callbackData.put("thirdPartyOrderNo", thirdPartyOrderNo != null ? thirdPartyOrderNo : "MOCK_" + paymentNo);
            callbackData.put("transactionId", transactionId != null ? transactionId : "TXN_" + paymentNo);
            callbackData.put("paymentTime", System.currentTimeMillis());
            
            // 调用支付服务处理回调
            Result<Boolean> result = paymentService.handlePaymentCallback("mock", callbackData);
            
            if (result.isSuccess()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("paymentNo", paymentNo);
                responseData.put("status", "SUCCESS");
                responseData.put("message", "支付成功");
                responseData.put("callbackData", callbackData);
                
                return Result.success("模拟支付成功", responseData);
            } else {
                return Result.error("处理支付回调失败: " + result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("模拟支付成功回调失败", e);
            return Result.error("模拟支付回调失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟支付失败回调 - 用于测试
     */
    @PostMapping("/mock/callback/failure")
    public Result<Map<String, Object>> mockPaymentFailure(@RequestBody Map<String, Object> request) {
        try {
            String paymentNo = (String) request.get("paymentNo");
            String failureReason = (String) request.get("failureReason");
            
            if (paymentNo == null) {
                return Result.error("支付单号不能为空");
            }
            
            log.info("=== 模拟支付失败回调 ===");
            log.info("支付单号: {}", paymentNo);
            log.info("失败原因: {}", failureReason);
            
            // 构造回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("paymentNo", paymentNo);
            callbackData.put("status", "FAILED");
            callbackData.put("failureReason", failureReason != null ? failureReason : "支付失败");
            callbackData.put("callbackTime", System.currentTimeMillis());
            
            // 调用支付服务处理回调
            Result<Boolean> result = paymentService.handlePaymentCallback("mock", callbackData);
            
            if (result.isSuccess()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("paymentNo", paymentNo);
                responseData.put("status", "FAILED");
                responseData.put("message", "支付失败");
                responseData.put("failureReason", failureReason);
                
                return Result.success("模拟支付失败", responseData);
            } else {
                return Result.error("处理支付回调失败: " + result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("模拟支付失败回调失败", e);
            return Result.error("模拟支付回调失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟退款成功回调 - 用于测试
     */
    @PostMapping("/mock/callback/refund")
    public Result<Map<String, Object>> mockRefundSuccess(@RequestBody Map<String, Object> request) {
        try {
            String paymentNo = (String) request.get("paymentNo");
            String refundAmount = (String) request.get("refundAmount");
            String refundReason = (String) request.get("refundReason");
            
            if (paymentNo == null || refundAmount == null) {
                return Result.error("支付单号和退款金额不能为空");
            }
            
            log.info("=== 模拟退款成功回调 ===");
            log.info("支付单号: {}", paymentNo);
            log.info("退款金额: {}", refundAmount);
            log.info("退款原因: {}", refundReason);
            
            // 构造退款回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("paymentNo", paymentNo);
            callbackData.put("refundAmount", refundAmount);
            callbackData.put("refundReason", refundReason);
            callbackData.put("refundStatus", "SUCCESS");
            callbackData.put("refundTime", System.currentTimeMillis());
            
            // 这里应该调用退款处理服务，暂时返回成功
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("paymentNo", paymentNo);
            responseData.put("refundAmount", refundAmount);
            responseData.put("refundStatus", "SUCCESS");
            responseData.put("message", "退款成功");
            
            log.info("模拟退款成功处理完成: paymentNo={}, amount={}", paymentNo, refundAmount);
            return Result.success("模拟退款成功", responseData);
            
        } catch (Exception e) {
            log.error("模拟退款回调失败", e);
            return Result.error("模拟退款回调失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户支付记录列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getUserPayments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            log.info("=== 获取用户支付记录列表 ===");
            log.info("用户ID: {}, 页码: {}, 每页数量: {}", userId, page, size);
            
            // 计算偏移量
            int offset = (page - 1) * size;
            
                         // 查询支付记录
             List<Payment> paymentList = paymentMapper.getUserPayments(userId, null, offset, size);
             
             // 查询总数
             int totalCount = paymentMapper.getUserPaymentCount(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("payments", paymentList);
            result.put("page", page);
            result.put("size", size);
            result.put("totalCount", totalCount);
            result.put("totalPages", (int) Math.ceil((double) totalCount / size));
            
            log.info("查询到{}条支付记录", paymentList.size());
            return Result.success("获取支付记录成功", result);
            
        } catch (Exception e) {
            log.error("获取支付记录失败", e);
            return Result.error("获取支付记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID，如果未找到返回null
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return null;
            }
            
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else {
                try {
                    return Long.parseLong(userIdObj.toString());
                } catch (NumberFormatException e) {
                    log.warn("用户ID格式错误: {}", userIdObj);
                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("获取用户ID失败", e);
            return null;
        }
    }
} 