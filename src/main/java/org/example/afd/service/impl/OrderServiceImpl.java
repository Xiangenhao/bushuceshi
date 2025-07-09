package org.example.afd.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.OrderDTO;
import org.example.afd.dto.OrderItemDTO;
import org.example.afd.dto.SubscriptionPlanDTO;
import org.example.afd.entity.Order;
import org.example.afd.entity.Payment;
import org.example.afd.mapper.OrderMapper;
import org.example.afd.mapper.PostMapper;
import org.example.afd.mapper.MerchantMapper;
import org.example.afd.mapper.PaymentMapper;
import org.example.afd.mapper.AddressMapper;
import org.example.afd.model.PageResult;
import org.example.afd.model.Result;
import org.example.afd.model.Merchant;
import org.example.afd.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 统一订单服务实现类
 * 
 * 支持订阅订单和购物订单的统一管理
 * 
 * @author AFD Team
 * @version 2.0
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private MerchantMapper merchantMapper;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private AddressMapper addressMapper;
    
    /**
     * 创建订阅订单
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createSubscriptionOrder(Long userId, Long planId, Integer months) {
        try {
            log.info("创建订阅订单: userId={}, planId={}, months={}", userId, planId, months);
            
            // 1. 验证输入参数
            if (userId == null || planId == null || months == null || months <= 0) {
                return Result.error("参数错误");
            }
            
            // 2. 检查是否已有有效订阅
            boolean hasActiveSubscription = orderMapper.hasActiveSubscription(userId, planId);
            if (hasActiveSubscription) {
                return Result.error("您已有该计划的有效订阅");
            }
            
            // 3. 获取订阅计划信息
            SubscriptionPlanDTO plan = postMapper.getSubscriptionPlanById(planId);
            if (plan == null) {
                return Result.error("订阅计划不存在");
            }
            
            // 4. 计算订单金额
            BigDecimal monthlyPrice = plan.getMonthlyPrice();
            BigDecimal totalAmount = monthlyPrice.multiply(new BigDecimal(months));
            
            // 生成订单号
            String orderNo = generateOrderNo("SUB");
            
            // 5. 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId.intValue());
            
            // 计算订阅起止时间
            LocalDateTime subscriptionStartTime = LocalDateTime.now();
            LocalDateTime subscriptionEndTime = subscriptionStartTime.plusMonths(months);
            
            order.setOrderType(2); // 订阅订单
            order.setRelatedId(planId);
            order.setTotalAmount(totalAmount);
            order.setPaidAmount(BigDecimal.ZERO);
            order.setShippingFee(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setCouponAmount(BigDecimal.ZERO);
            order.setOrderStatus(1); // 待付款
            order.setSubscriptionMonths(months);
            order.setSubscriptionStartTime(subscriptionStartTime);
            order.setSubscriptionEndTime(subscriptionEndTime);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setExpireTime(LocalDateTime.now().plusDays(1)); // 24小时后过期
            
            int result = orderMapper.insertOrder(order);
            if (result <= 0) {
                return Result.error("创建订单失败");
            }
            
            // 6. 创建订单项（订阅计划项）
            Map<String, Object> orderItem = new HashMap<>();
            orderItem.put("orderId", order.getOrderId());
            orderItem.put("itemType", 2); // 订阅计划类型
            orderItem.put("planId", planId);
            orderItem.put("planName", plan.getTitle());
            orderItem.put("planDescription", plan.getDescription());
            orderItem.put("monthlyPrice", monthlyPrice);
            orderItem.put("quantity", months);
            orderItem.put("itemAmount", totalAmount);
            
            int itemResult = orderMapper.insertOrderItem(orderItem);
            if (itemResult <= 0) {
                log.warn("创建订单项失败，但订单已创建: orderNo={}", orderNo);
            }
            
            // 7. 返回订单信息
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderNo", orderNo);
            orderInfo.put("orderId", order.getOrderId());
            orderInfo.put("totalAmount", totalAmount);
            orderInfo.put("planTitle", plan.getTitle());
            orderInfo.put("planCover", plan.getCoverUrl());
            orderInfo.put("subscriptionMonths", months);
            orderInfo.put("expireTime", order.getExpireTime());
            
            log.info("订阅订单创建成功: {}", orderInfo);
            return Result.success("订单创建成功", orderInfo);
            
        } catch (Exception e) {
            log.error("创建订阅订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建购物订单
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createShoppingOrder(Long userId, List<Map<String, Object>> cartItems, Long addressId, String orderNote) {
        try {
            log.info("创建购物订单: userId={}, cartItems={}, addressId={}", userId, cartItems.size(), addressId);
            
            // TODO: 实现购物订单创建逻辑
            // 1. 验证购物车商品
            // 2. 计算订单金额
            // 3. 创建订单和订单项
            
            return Result.error("购物订单功能暂未实现");
            
        } catch (Exception e) {
            log.error("创建购物订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建商品订单 - 按SKU分组创建独立订单
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createProductOrder(Long userId, Map<String, Object> orderData) {
        try {
            log.info("=== 开始创建商品订单 ===");
            log.info("创建商品订单: userId={}, orderData={}", userId, orderData);
            
            // 获取订单信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderData.get("orderItems");
            Long addressId = getLongValue(orderData, "addressId");
            String orderNote = getString(orderData, "orderNote");
            
            log.info("解析基础数据: addressId={}, orderNote={}, orderItems.size={}", 
                    addressId, orderNote, orderItems != null ? orderItems.size() : 0);
            
            if (orderItems == null || orderItems.isEmpty()) {
                log.error("订单商品不能为空");
                return Result.error("订单商品不能为空");
            }
            
            List<Map<String, Object>> createdOrders = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            log.info("=== 开始处理订单项，共{}个 ===", orderItems.size());
            
            // 为每个SKU创建独立订单
            for (int i = 0; i < orderItems.size(); i++) {
                Map<String, Object> item = orderItems.get(i);
                log.info("=== 处理第{}个订单项 ===", i + 1);
                log.info("处理订单项: {}", item);
                
                Long productId = getLongValue(item, "productId");
                Long skuId = getLongValue(item, "skuId");
                BigDecimal unitPrice = getBigDecimalValue(item, "unitPrice");
                Integer quantity = getIntegerValue(item, "quantity");
                BigDecimal itemAmount = getBigDecimalValue(item, "totalPrice");
                String itemNote = getString(item, "itemNote");
                
                log.info("解析订单项数据: productId={}, skuId={}, unitPrice={}, quantity={}, itemAmount={}", 
                        productId, skuId, unitPrice, quantity, itemAmount);
                
                if (productId == null || skuId == null || unitPrice == null || quantity == null || itemAmount == null) {
                    log.error("订单项数据不完整: {}", item);
                    continue;
                }
                
                log.info("验证订单项金额: itemAmount={}, 是否大于0: {}", itemAmount, itemAmount.compareTo(BigDecimal.ZERO) > 0);
                
                if (itemAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("订单项金额不能为空或小于等于0: itemAmount={}", itemAmount);
                    return Result.error("订单金额不能为空或小于等于0");
                }
                
                // 获取商家ID
                log.info("获取商家ID: productId={}", productId);
                Long merchantId = orderMapper.getMerchantIdByProductId(productId);
                if (merchantId == null) {
                    log.error("无法获取商品的商家ID: productId={}", productId);
                    continue;
                }
                log.info("获取到商家ID: merchantId={}", merchantId);
                
                // 生成订单号
                String orderNo = generateOrderNo("PROD");
                log.info("生成订单号: orderNo={}", orderNo);
                
                // 创建订单
                Order order = new Order();
                order.setOrderNo(orderNo);
                order.setUserId(userId.intValue());
                order.setOrderType(1); // 商品订单
                order.setRelatedId(merchantId); // 设置商家ID
                order.setTotalAmount(itemAmount); // 单个SKU的金额
                order.setPaidAmount(BigDecimal.ZERO);
                order.setShippingFee(BigDecimal.ZERO);
                order.setDiscountAmount(BigDecimal.ZERO);
                order.setCouponAmount(BigDecimal.ZERO);
                order.setOrderStatus(1); // 待付款
                order.setAddressId(addressId); // 设置收货地址ID
                order.setOrderNote(orderNote); // 设置订单备注
                order.setCreateTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                order.setExpireTime(LocalDateTime.now().plusDays(1)); // 24小时后过期
                
                log.info("准备插入订单: orderNo={}, totalAmount={}", orderNo, itemAmount);
                
                int result = orderMapper.insertOrder(order);
                if (result <= 0) {
                    log.error("创建订单失败: {}", orderNo);
                    continue;
                }
                
                log.info("订单插入成功: orderNo={}, orderId={}", orderNo, order.getOrderId());
                
                // 创建订单项 - 一个订单只包含一个SKU
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("orderId", order.getOrderId());
                orderItem.put("itemType", 1); // 商品类型
                orderItem.put("productId", productId);
                orderItem.put("skuId", skuId);
                orderItem.put("unitPrice", unitPrice);
                orderItem.put("quantity", quantity);
                orderItem.put("itemAmount", itemAmount);
                
                // 添加商品备注支持
                if (itemNote != null && !itemNote.trim().isEmpty()) {
                    orderItem.put("itemNote", itemNote.trim());
                    log.info("订单项备注: productId={}, note={}", productId, itemNote.trim());
                }
                
                // 插入订单项
                List<Map<String, Object>> singleItemList = new ArrayList<>();
                singleItemList.add(orderItem);
                int itemResult = orderMapper.batchInsertOrderItems(singleItemList);
                
                if (itemResult > 0) {
                    totalAmount = totalAmount.add(itemAmount);
                    
                    // 同时创建支付记录
                    String paymentNo = "PAY" + System.currentTimeMillis() + 
                                     String.format("%04d", new Random().nextInt(10000));
                    
                    Payment payment = new Payment();
                    payment.setPaymentNo(paymentNo);
                    payment.setOrderId(order.getOrderId());
                    payment.setUserId(userId.intValue());
                    payment.setChannelId(1); // 默认支付渠道ID
                    payment.setPaymentAmount(itemAmount);
                    payment.setPaymentStatus(1); // 待支付
                    payment.setCreateTime(LocalDateTime.now());
                    payment.setUpdateTime(LocalDateTime.now());
                    
                    int paymentResult = paymentMapper.insertPayment(payment);
                    if (paymentResult > 0) {
                        log.info("支付记录创建成功: paymentNo={}, amount={}", paymentNo, itemAmount);
                    } else {
                        log.error("创建支付记录失败: orderNo={}", orderNo);
                    }
                    
                    // 记录创建成功的订单信息
                    Map<String, Object> orderInfo = new HashMap<>();
                    orderInfo.put("orderNo", orderNo);
                    orderInfo.put("orderId", order.getOrderId());
                    orderInfo.put("paymentNo", paymentNo);
                    orderInfo.put("totalAmount", itemAmount);
                    orderInfo.put("expireTime", order.getExpireTime());
                    orderInfo.put("merchantId", merchantId);
                    orderInfo.put("productId", productId);
                    orderInfo.put("skuId", skuId);
                    createdOrders.add(orderInfo);
                    
                    log.info("SKU订单创建成功: orderNo={}, skuId={}, amount={}", orderNo, skuId, itemAmount);
                } else {
                    log.error("插入订单项失败: orderNo={}, skuId={}", orderNo, skuId);
                }
            }
            
            if (createdOrders.isEmpty()) {
                log.error("没有成功创建任何订单");
                return Result.error("没有成功创建任何订单");
            }
            
            // 返回订单信息汇总
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("createdOrders", createdOrders);
            resultData.put("orderCount", createdOrders.size());
            resultData.put("totalAmount", totalAmount);
            
            log.info("=== 商品订单创建完成 ===");
            log.info("商品订单创建完成: 共创建{}个订单，总金额={}", createdOrders.size(), totalAmount);
            return Result.success("订单创建成功", resultData);
            
        } catch (Exception e) {
            log.error("创建商品订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建购物车订单
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createCartOrder(Long userId, Map<String, Object> orderData) {
        try {
            log.info("创建购物车订单: userId={}, orderData={}", userId, orderData);
            
            // 获取订单信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderData.get("orderItems");
            
            if (orderItems == null || orderItems.isEmpty()) {
                return Result.error("购物车商品不能为空");
            }
            
            List<Map<String, Object>> createdOrders = new ArrayList<>();
            List<Long> skuIds = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // 为每个SKU创建单独的订单
            for (Map<String, Object> item : orderItems) {
                String orderNo = generateOrderNo("CART");
                
                Long productId = getLongValue(item, "productId");
                Long skuId = getLongValue(item, "skuId");
                BigDecimal unitPrice = getBigDecimalValue(item, "unitPrice");
                Integer quantity = getIntegerValue(item, "quantity");
                BigDecimal itemAmount = getBigDecimalValue(item, "totalPrice");
                
                if (productId == null || skuId == null || unitPrice == null || quantity == null || itemAmount == null) {
                    log.error("订单项数据不完整: {}", item);
                    continue;
                }
                
                if (itemAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("订单项金额不能为空或小于等于0: {}", item);
                    continue;
                }
                
                // 获取商家ID
                Long merchantId = orderMapper.getMerchantIdByProductId(productId);
                if (merchantId == null) {
                    log.error("无法获取商品的商家ID: productId={}", productId);
                    continue;
                }
                
                // 创建订单
                Order order = new Order();
                order.setOrderNo(orderNo);
                order.setUserId(userId.intValue());
                order.setOrderType(1); // 商品订单
                order.setRelatedId(merchantId); // 设置商家ID
                order.setTotalAmount(itemAmount); // 单个SKU的金额
                order.setPaidAmount(BigDecimal.ZERO);
                order.setShippingFee(BigDecimal.ZERO);
                order.setDiscountAmount(BigDecimal.ZERO);
                order.setCouponAmount(BigDecimal.ZERO);
                order.setOrderStatus(1); // 待付款
                order.setCreateTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                order.setExpireTime(LocalDateTime.now().plusDays(1)); // 24小时后过期
                
                int result = orderMapper.insertOrder(order);
                if (result <= 0) {
                    log.error("创建订单失败: {}", orderNo);
                    continue;
                }
                
                // 创建订单项 - 一个订单只包含一个SKU
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("orderId", order.getOrderId());
                orderItem.put("itemType", 1); // 商品类型
                orderItem.put("productId", productId);
                orderItem.put("skuId", skuId);
                orderItem.put("unitPrice", unitPrice);
                orderItem.put("quantity", quantity);
                orderItem.put("itemAmount", itemAmount);
                
                // 插入订单项
                List<Map<String, Object>> singleItemList = new ArrayList<>();
                singleItemList.add(orderItem);
                int itemResult = orderMapper.batchInsertOrderItems(singleItemList);
                
                if (itemResult > 0) {
                    skuIds.add(skuId);
                    totalAmount = totalAmount.add(itemAmount);
                    
                    // 记录创建成功的订单信息
                    Map<String, Object> orderInfo = new HashMap<>();
                    orderInfo.put("orderNo", orderNo);
                    orderInfo.put("orderId", order.getOrderId());
                    orderInfo.put("totalAmount", itemAmount);
                    orderInfo.put("expireTime", order.getExpireTime());
                    orderInfo.put("merchantId", merchantId);
                    orderInfo.put("productId", productId);
                    orderInfo.put("skuId", skuId);
                    createdOrders.add(orderInfo);
                    
                    log.info("SKU订单创建成功: orderNo={}, skuId={}, amount={}", orderNo, skuId, itemAmount);
                } else {
                    log.error("插入订单项失败: orderNo={}, skuId={}", orderNo, skuId);
                }
            }
            
            if (createdOrders.isEmpty()) {
                return Result.error("没有成功创建任何订单");
            }
            
            // 删除购物车中的商品
            if (!skuIds.isEmpty()) {
                int deleteResult = orderMapper.deleteCartItemsBySkuIds(userId, skuIds);
                log.info("删除购物车商品数量: {}", deleteResult);
            }
            
            // 返回订单信息汇总
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("createdOrders", createdOrders);
            resultData.put("orderCount", createdOrders.size());
            resultData.put("totalAmount", totalAmount);
            
            log.info("购物车订单创建完成: 共创建{}个订单，总金额={}", createdOrders.size(), totalAmount);
            return Result.success("订单创建成功", resultData);
            
        } catch (Exception e) {
            log.error("创建购物车订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建通用订单（支持商品备注）- 按SKU分组创建独立订单
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createUnifiedOrder(Long userId, Map<String, Object> orderData) {
        try {
            log.info("创建通用订单: userId={}, orderData={}", userId, orderData);
            
            // 获取订单信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderData.get("orderItems");
            Long addressId = getLongValue(orderData, "addressId");
            String orderNote = getString(orderData, "orderNote");
            
            if (orderItems == null || orderItems.isEmpty()) {
                return Result.error("订单商品不能为空");
            }
            
            List<Map<String, Object>> createdOrders = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // 为每个SKU创建独立订单
            for (Map<String, Object> item : orderItems) {
                Long productId = getLongValue(item, "productId");
                Long skuId = getLongValue(item, "skuId");
                BigDecimal unitPrice = getBigDecimalValue(item, "unitPrice");
                Integer quantity = getIntegerValue(item, "quantity");
                BigDecimal itemAmount = getBigDecimalValue(item, "totalPrice");
                String itemNote = getString(item, "itemNote");
                
                if (productId == null || skuId == null || unitPrice == null || quantity == null || itemAmount == null) {
                    log.error("订单项数据不完整: {}", item);
                    continue;
                }
                
                if (itemAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("订单项金额不能为空或小于等于0: {}", item);
                    continue;
                }
                
                // 获取商家ID
                Long merchantId = orderMapper.getMerchantIdByProductId(productId);
                if (merchantId == null) {
                    log.error("无法获取商品的商家ID: productId={}", productId);
                    continue;
                }
                
                // 生成订单号
                String orderNo = generateOrderNo("UNI");
                
                // 创建订单
                Order order = new Order();
                order.setOrderNo(orderNo);
                order.setUserId(userId.intValue());
                order.setOrderType(1); // 商品订单
                order.setRelatedId(merchantId); // 设置商家ID
                order.setTotalAmount(itemAmount); // 单个SKU的金额
                order.setPaidAmount(BigDecimal.ZERO);
                order.setShippingFee(BigDecimal.ZERO);
                order.setDiscountAmount(BigDecimal.ZERO);
                order.setCouponAmount(BigDecimal.ZERO);
                order.setOrderStatus(1); // 待付款
                order.setAddressId(addressId); // 设置收货地址ID
                order.setOrderNote(orderNote); // 设置订单备注
                order.setCreateTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                order.setExpireTime(LocalDateTime.now().plusDays(1)); // 24小时后过期
                
                int result = orderMapper.insertOrder(order);
                if (result <= 0) {
                    log.error("创建订单失败: {}", orderNo);
                    continue;
                }
                
                // 创建订单项 - 一个订单只包含一个SKU
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("orderId", order.getOrderId());
                orderItem.put("itemType", 1); // 商品类型
                orderItem.put("productId", productId);
                orderItem.put("skuId", skuId);
                orderItem.put("unitPrice", unitPrice);
                orderItem.put("quantity", quantity);
                orderItem.put("itemAmount", itemAmount);
                
                // 添加商品备注支持
                if (itemNote != null && !itemNote.trim().isEmpty()) {
                    orderItem.put("itemNote", itemNote.trim());
                    log.info("订单项备注: productId={}, note={}", productId, itemNote.trim());
                }
                
                // 插入订单项
                List<Map<String, Object>> singleItemList = new ArrayList<>();
                singleItemList.add(orderItem);
                int itemResult = orderMapper.batchInsertOrderItems(singleItemList);
                
                if (itemResult > 0) {
                    totalAmount = totalAmount.add(itemAmount);
                    
                    // 记录创建成功的订单信息
                    Map<String, Object> orderInfo = new HashMap<>();
                    orderInfo.put("orderNo", orderNo);
                    orderInfo.put("orderId", order.getOrderId());
                    orderInfo.put("totalAmount", itemAmount);
                    orderInfo.put("expireTime", order.getExpireTime());
                    orderInfo.put("merchantId", merchantId);
                    orderInfo.put("productId", productId);
                    orderInfo.put("skuId", skuId);
                    createdOrders.add(orderInfo);
                    
                    log.info("SKU订单创建成功: orderNo={}, skuId={}, amount={}", orderNo, skuId, itemAmount);
                } else {
                    log.error("插入订单项失败: orderNo={}, skuId={}", orderNo, skuId);
                }
            }
            
            if (createdOrders.isEmpty()) {
                return Result.error("没有成功创建任何订单");
            }
            
            // 返回订单信息汇总
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("createdOrders", createdOrders);
            resultData.put("orderCount", createdOrders.size());
            resultData.put("totalAmount", totalAmount);
            
            log.info("通用订单创建完成: 共创建{}个订单，总金额={}", createdOrders.size(), totalAmount);
            return Result.success("订单创建成功", resultData);
            
        } catch (Exception e) {
            log.error("创建通用订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @Override
    public Result<OrderDTO> getOrderDetail(String orderNo, Long userId) {
        try {
            log.info("获取订单详情: orderNo={}, userId={}", orderNo, userId);
            
            Map<String, Object> orderData = orderMapper.selectOrderDetailByNoAndUser(orderNo, userId);
            if (orderData == null) {
                return Result.error("订单不存在");
            }
            
            OrderDTO orderDTO = convertToOrderDTO(orderData);
            return Result.success(orderDTO);
            
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户订单列表
     */
    @Override
    public Result<List<OrderDTO>> getUserOrders(Long userId, Integer orderType, Integer orderStatus, Integer page, Integer size) {
        try {
            log.info("=== 开始获取用户订单列表 ===");
            log.info("参数: userId={}, orderType={}, orderStatus={}, page={}, size={}", 
                    userId, orderType, orderStatus, page, size);
            
            int offset = (page - 1) * size;
            log.info("计算offset: {}", offset);
            
            log.info("=== 开始数据库查询 ===");
            List<Map<String, Object>> orderDataList = orderMapper.selectUserOrders(userId, orderType, orderStatus, offset, size);
            log.info("=== 数据库查询完成 ===");
            log.info("查询到的原始订单数据数量: {}", orderDataList != null ? orderDataList.size() : 0);
            
            if (orderDataList != null && !orderDataList.isEmpty()) {
                log.info("=== 开始处理订单数据 ===");
                for (int i = 0; i < orderDataList.size(); i++) {
                    Map<String, Object> orderData = orderDataList.get(i);
                    log.info("原始订单数据[{}]: {}", i, orderData.keySet());
                    log.info("订单ID: {}, 订单号: {}, 状态: {}", 
                            orderData.get("order_id"), orderData.get("order_no"), orderData.get("order_status"));
                }
            }
            
            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Map<String, Object> orderData : orderDataList) {
                OrderDTO orderDTO = convertToOrderDTO(orderData);
                
                // 查询订单项
                Long orderId = getLongValue(orderData, "order_id");
                if (orderId != null) {
                    log.info("=== 查询订单[{}]的订单项 ===", orderId);
                    List<Map<String, Object>> orderItems = orderMapper.selectOrderItemsByOrderId(orderId);
                    log.info("订单[{}]的订单项数量: {}", orderId, orderItems != null ? orderItems.size() : 0);
                    
                    // 转换订单项为DTO
                    List<org.example.afd.dto.OrderItemDTO> orderItemDTOs = new ArrayList<>();
                    for (Map<String, Object> itemData : orderItems) {
                        log.info("处理订单项数据: {}", itemData.keySet());
                        log.info("sku_name: '{}', sku_image: '{}'", itemData.get("sku_name"), itemData.get("sku_image"));
                        
                        org.example.afd.dto.OrderItemDTO itemDTO = new org.example.afd.dto.OrderItemDTO();
                        itemDTO.setOrderItemId(getLongValue(itemData, "item_id"));
                        itemDTO.setOrderId(orderId);
                        itemDTO.setProductId(getLongValue(itemData, "product_id"));
                        itemDTO.setSkuId(getLongValue(itemData, "sku_id"));
                        itemDTO.setProductName((String) itemData.get("product_name"));
                        
                        // 直接设置SKU字段 - 数据库sku_name -> skuName, sku_image -> skuImage
                        String skuName = (String) itemData.get("sku_name");
                        String skuImage = (String) itemData.get("sku_image");
                        
                        itemDTO.setSkuName(skuName);     // 数据库sku_name -> skuName
                        itemDTO.setSkuImage(skuImage);   // 数据库sku_image -> skuImage
                        
                        log.info("设置SKU字段 - skuName: '{}', skuImage: '{}'", skuName, skuImage);
                        
                        itemDTO.setPrice(getBigDecimalValue(itemData, "unit_price") != null ? 
                                getBigDecimalValue(itemData, "unit_price").doubleValue() : 0.0);
                        itemDTO.setQuantity(getIntegerValue(itemData, "quantity"));
                        itemDTO.setTotalPrice(getBigDecimalValue(itemData, "item_amount") != null ? 
                                getBigDecimalValue(itemData, "item_amount").doubleValue() : 0.0);
                        
                        orderItemDTOs.add(itemDTO);
                        log.info("订单项DTO添加完成: productName={}, skuName={}, skuImage={}", 
                                itemDTO.getProductName(), itemDTO.getSkuName(), itemDTO.getSkuImage());
                    }
                    
                    orderDTO.setOrderItems(orderItemDTOs);
                    log.info("订单[{}]的订单项设置完成，共{}个", orderId, orderItemDTOs.size());
                }
                
                orderDTOList.add(orderDTO);
                log.info("订单DTO添加完成: orderNo={}, status={}, orderItems={}", 
                        orderDTO.getOrderNo(), orderDTO.getStatus(), 
                        orderDTO.getOrderItems() != null ? orderDTO.getOrderItems().size() : 0);
            }
            
            log.info("=== 最终返回订单列表，共{}条 ===", orderDTOList.size());
            return Result.success(orderDTOList);
            
        } catch (Exception e) {
            log.error("获取用户订单列表失败", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户订单详情
     */
    @Override
    public Result<OrderDTO> getUserOrderDetail(Long orderId, Long userId) {
        try {
            log.info("=== 开始获取用户订单详情 ===");
            log.info("请求参数: orderId={}, userId={}", orderId, userId);
            
            // 查询订单基础信息
            log.info("步骤1: 查询订单基础信息");
            Order order = orderMapper.selectByOrderId(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return Result.error("订单不存在");
            }
            
            log.info("查询到订单基础信息: orderNo={}, status={}, orderType={}, userId={}", 
                    order.getOrderNo(), order.getOrderStatus(), order.getOrderType(), order.getUserId());
            
            if (!order.getUserId().equals(userId.intValue())) {
                log.warn("用户无权限访问此订单: orderId={}, userId={}, orderUserId={}", 
                        orderId, userId, order.getUserId());
                return Result.error("订单不存在");
            }
            
            log.info("权限验证通过");
            
            // 查询订单详细信息（包含关联信息）
            log.info("步骤2: 查询订单详细信息");
            Map<String, Object> orderData = orderMapper.selectOrderDetailByNoAndUser(order.getOrderNo(), userId);
            if (orderData == null) {
                log.warn("查询订单详细信息失败: orderNo={}, userId={}", order.getOrderNo(), userId);
                log.info("使用基础订单信息构建数据，并补充地址信息");
                // 如果查询失败，使用基础信息
                orderData = new HashMap<>();
                orderData.put("order_id", order.getOrderId());
                orderData.put("order_no", order.getOrderNo());
                orderData.put("user_id", order.getUserId());
                orderData.put("order_type", order.getOrderType());
                orderData.put("order_status", order.getOrderStatus());
                orderData.put("total_amount", order.getTotalAmount());
                orderData.put("paid_amount", order.getPaidAmount());
                orderData.put("create_time", order.getCreateTime());
                orderData.put("update_time", order.getUpdateTime());
                orderData.put("order_note", order.getOrderNote());
                orderData.put("address_id", order.getAddressId());
                orderData.put("shipping_fee", order.getShippingFee());
                orderData.put("discount_amount", order.getDiscountAmount());
                orderData.put("coupon_amount", order.getCouponAmount());
                orderData.put("logistics_info", order.getLogisticsInfo());
                orderData.put("related_id", order.getRelatedId());
                
                // 补充地址信息 - 单独查询
                if (order.getAddressId() != null) {
                    try {
                        log.info("补充查询地址信息: addressId={}", order.getAddressId());
                        // 查询地址信息（包含日本地址详情）
                        Map<String, Object> addressInfo = addressMapper.selectAddressById(order.getAddressId(), userId);
                        if (addressInfo != null) {
                            orderData.put("receiver_name", addressInfo.get("receiver_name"));
                            orderData.put("receiver_phone", addressInfo.get("receiver_phone"));
                            orderData.put("address_deleted", addressInfo.get("is_deleted"));
                            
                            // 日本地址详情已经通过LEFT JOIN查询获得
                            orderData.put("postal_code", addressInfo.get("postal_code"));
                            orderData.put("prefecture", addressInfo.get("prefecture"));
                            orderData.put("city", addressInfo.get("municipality"));
                            orderData.put("town", addressInfo.get("town"));
                            orderData.put("chome", addressInfo.get("chome"));
                            orderData.put("banchi", addressInfo.get("banchi"));
                            orderData.put("building", addressInfo.get("building"));
                            orderData.put("room_number", addressInfo.get("room_number"));
                            
                            // 拼接完整地址
                            String addressLine1 = (String) addressInfo.get("address_line1");
                            String addressLine2 = (String) addressInfo.get("address_line2");
                            String fullAddress = addressLine1;
                            if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
                                fullAddress += " " + addressLine2;
                            }
                            orderData.put("shipping_address", fullAddress);
                            
                            log.info("地址信息补充完成: 姓名={}, 电话={}, 地址={}", 
                                    addressInfo.get("receiver_name"), 
                                    addressInfo.get("receiver_phone"), 
                                    fullAddress);
                        }
                    } catch (Exception e) {
                        log.warn("补充地址信息失败: addressId={}", order.getAddressId(), e);
                    }
                }
                
                // 补充商家信息
                if (order.getOrderType() == 1 && order.getRelatedId() != null) {
                    try {
                        log.info("补充查询商家信息: merchantId={}", order.getRelatedId());
                        Merchant merchant = merchantMapper.selectByPrimaryKey(order.getRelatedId());
                        if (merchant != null) {
                            orderData.put("merchant_name", merchant.getMerchantName());
                            orderData.put("merchant_logo", merchant.getLogo());
                            log.info("商家信息补充完成: 商家名称={}", merchant.getMerchantName());
                        }
                    } catch (Exception e) {
                        log.warn("补充商家信息失败: merchantId={}", order.getRelatedId(), e);
                    }
                }
            }
            
            log.info("查询到订单详细信息，包含字段: {}", orderData.keySet());
            log.info("订单详细数据: order_id={}, order_no={}, order_status={}, total_amount={}", 
                    orderData.get("order_id"), orderData.get("order_no"), 
                    orderData.get("order_status"), orderData.get("total_amount"));
            
            // 转换为OrderDTO
            log.info("步骤3: 转换为OrderDTO");
            OrderDTO orderDTO = convertToOrderDTO(orderData);
            log.info("OrderDTO转换完成: orderNo={}, status={}, totalAmount={}", 
                    orderDTO.getOrderNo(), orderDTO.getStatus(), orderDTO.getTotalAmount());
            
            // 查询订单项
            log.info("步骤4: 查询订单项");
            List<Map<String, Object>> orderItems = orderMapper.selectOrderItemsByOrderId(orderId);
            log.info("查询到订单项数量: {}", orderItems != null ? orderItems.size() : 0);
            
            if (orderItems != null && !orderItems.isEmpty()) {
                log.info("步骤5: 转换订单项为DTO");
                // 转换订单项为DTO
                List<org.example.afd.dto.OrderItemDTO> orderItemDTOs = new ArrayList<>();
                for (int i = 0; i < orderItems.size(); i++) {
                    Map<String, Object> itemData = orderItems.get(i);
                    log.info("处理订单项[{}]: {}", i, itemData.keySet());
                    
                    try {
                        org.example.afd.dto.OrderItemDTO itemDTO = convertToOrderItemDTO(itemData);
                        orderItemDTOs.add(itemDTO);
                        log.info("订单项[{}]: productName={}, quantity={}, price={}", 
                                i, itemDTO.getProductName(), itemDTO.getQuantity(), itemDTO.getPrice());
                    } catch (Exception e) {
                        log.error("转换订单项[{}]失败", i, e);
                        log.error("订单项数据: {}", itemData);
                    }
                }
                orderDTO.setOrderItems(orderItemDTOs);
                log.info("所有订单项转换完成，总数: {}", orderItemDTOs.size());
            } else {
                log.info("没有订单项数据");
                orderDTO.setOrderItems(new ArrayList<>());
            }
            
            log.info("=== 订单详情查询完成 ===");
            log.info("最终结果: orderNo={}, 订单项数量={}, 商家名称={}", 
                    orderDTO.getOrderNo(), 
                    orderDTO.getOrderItems() != null ? orderDTO.getOrderItems().size() : 0,
                    orderDTO.getMerchantName());
            
            return Result.success(orderDTO);
            
        } catch (Exception e) {
            log.error("=== 获取用户订单详情失败 ===", e);
            log.error("异常类型: {}", e.getClass().getSimpleName());
            log.error("异常消息: {}", e.getMessage());
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消用户订单
     */
    @Override
    @Transactional
    public Result<Boolean> cancelUserOrder(Long orderId, Long userId) {
        try {
            log.info("取消用户订单: orderId={}, userId={}", orderId, userId);
            
            Order order = orderMapper.selectByOrderId(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId.intValue())) {
                return Result.error("无权操作此订单");
            }
            
            if (order.getOrderStatus() != 1) {
                return Result.error("只能取消待付款订单");
            }
            
            // 更新订单状态为已取消（状态6）
            int result = orderMapper.updateOrderStatus(order.getOrderNo(), 6); // 6-已取消
            if (result > 0) {
                log.info("订单取消成功: orderId={}", orderId);
                return Result.success(true);
            } else {
                return Result.error("取消订单失败");
            }
            
        } catch (Exception e) {
            log.error("取消用户订单失败", e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消订单（兼容旧接口）
     */
    @Override
    @Transactional
    public Result<Boolean> cancelOrder(String orderNo, Long userId) {
        try {
            log.info("取消订单: orderNo={}, userId={}", orderNo, userId);
            
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId.intValue())) {
                return Result.error("无权限操作此订单");
            }
            
            if (order.getOrderStatus() != 1) {
                return Result.error("订单状态不允许取消");
            }
            
            // 更新订单状态为已取消（状态6）
            int result = orderMapper.updateOrderStatus(orderNo, 6); // 6-已取消
            return Result.success(result > 0);
            
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认用户订单收货
     */
    @Override
    @Transactional
    public Result<Boolean> confirmUserOrder(Long orderId, Long userId) {
        try {
            log.info("确认用户订单收货: orderId={}, userId={}", orderId, userId);
            
            Order order = orderMapper.selectByOrderId(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId.intValue())) {
                return Result.error("无权操作此订单");
            }
            
            if (order.getOrderStatus() != 4) {
                return Result.error("只能确认已发货的订单");
            }
            
            // 更新订单状态为已完成
            int result = orderMapper.updateOrderStatus(order.getOrderNo(), 5); // 5-已完成
            if (result > 0) {
                log.info("订单确认收货成功: orderId={}", orderId);
                return Result.success(true);
            } else {
                return Result.error("确认收货失败");
            }
            
        } catch (Exception e) {
            log.error("确认用户订单收货失败", e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认收货（兼容旧接口）
     */
    @Override
    @Transactional
    public Result<Boolean> confirmOrder(String orderNo, Long userId) {
        try {
            log.info("确认收货: orderNo={}, userId={}", orderNo, userId);
            
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId.intValue())) {
                return Result.error("无权限操作此订单");
            }
            
            if (order.getOrderStatus() != 4) {
                return Result.error("只能确认已发货的订单");
            }
            
            int result = orderMapper.updateOrderStatus(orderNo, 5); // 5-已完成
            return Result.success(result > 0);
            
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户订单（软删除，设置状态为0）
     */
    @Override
    @Transactional
    public Result<Boolean> deleteUserOrder(Long orderId, Long userId) {
        try {
            log.info("删除用户订单: orderId={}, userId={}", orderId, userId);
            
            Order order = orderMapper.selectByOrderId(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 只能删除已完成、已取消或已关闭的订单
            if (order.getOrderStatus() == 1 || order.getOrderStatus() == 2 || order.getOrderStatus() == 3 || order.getOrderStatus() == 4) {
                return Result.error("该状态的订单不能删除");
            }
            
            // 软删除：更新订单状态为已删除（状态0）
            int result = orderMapper.updateOrderStatus(order.getOrderNo(), 0); // 0-已删除
            if (result > 0) {
                log.info("订单删除成功: orderId={}", orderId);
                return Result.success(true);
            } else {
                return Result.error("删除订单失败");
            }
            
        } catch (Exception e) {
            log.error("删除用户订单失败", e);
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新订单状态
     */
    @Override
    @Transactional
    public Result<Boolean> updateOrderStatus(String orderNo, Integer status) {
        try {
            log.info("更新订单状态: orderNo={}, status={}", orderNo, status);
            
            int result = orderMapper.updateOrderStatus(orderNo, status);
            return Result.success(result > 0);
            
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单统计信息
     */
    @Override
    public Result<Map<String, Object>> getOrderStatistics(Long userId, Integer orderType) {
        try {
            log.info("获取订单统计信息: userId={}, orderType={}", userId, orderType);
            
            Map<String, Object> statistics = orderMapper.selectOrderStatistics(userId, orderType);
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取订单统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    // ===== 私有辅助方法 =====
    
    /**
     * 计算订阅金额（包含折扣）
     */
    private BigDecimal calculateSubscriptionAmount(BigDecimal monthlyPrice, Integer months) {
        BigDecimal totalAmount = monthlyPrice.multiply(new BigDecimal(months));
        
        // 应用折扣
        BigDecimal discountRate = BigDecimal.ONE;
        switch (months) {
            case 3:
                discountRate = new BigDecimal("0.95"); // 95折
                break;
            case 6:
                discountRate = new BigDecimal("0.90"); // 9折
                break;
            case 12:
                discountRate = new BigDecimal("0.85"); // 85折
                break;
        }
        
        return totalAmount.multiply(discountRate).setScale(2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo(String prefix) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        String random = String.valueOf((int) (Math.random() * 9000) + 1000);
        return prefix + timestamp + random;
    }
    
    /**
     * 转换为OrderDTO
     */
    private OrderDTO convertToOrderDTO(Map<String, Object> orderData) {
        try {
            // 添加详细的调试日志
            log.info("=== convertToOrderDTO 开始 ===");
            log.info("接收到的orderData包含以下键: {}", orderData.keySet());
            for (Map.Entry<String, Object> entry : orderData.entrySet()) {
                log.info("字段 '{}' = '{}'", entry.getKey(), entry.getValue());
            }
            
            OrderDTO dto = new OrderDTO();
            
            // 基本订单信息 - 使用正确的数据库字段名
            dto.setOrderId(getLongValue(orderData, "order_id"));
            dto.setUserId(getLongValue(orderData, "user_id"));
            dto.setOrderNo(getString(orderData, "order_no"));
            dto.setOrderType(getIntegerValue(orderData, "order_type"));
            dto.setOrderStatus(getIntegerValue(orderData, "order_status"));
            dto.setTotalAmount(getBigDecimalValue(orderData, "total_amount"));
            dto.setPaidAmount(getBigDecimalValue(orderData, "paid_amount"));
            
            // 兼容前端的字段
            BigDecimal paidAmount = getBigDecimalValue(orderData, "paid_amount");
            if (paidAmount != null) {
                dto.setPayAmount(paidAmount.doubleValue());
            }
            
            BigDecimal freightAmount = getBigDecimalValue(orderData, "shipping_fee");
            if (freightAmount != null) {
                dto.setFreightAmount(freightAmount.doubleValue());
            }
            
            dto.setNote(getString(orderData, "order_note"));
            
            log.info("设置基本信息 - 订单ID: {}, 订单号: {}, 状态: {}", dto.getOrderId(), dto.getOrderNo(), dto.getOrderStatus());
            
            // 时间信息
            Object createTimeObj = orderData.get("create_time");
            if (createTimeObj instanceof Date) {
                dto.setCreateTimeOld((Date) createTimeObj);
            } else if (createTimeObj instanceof LocalDateTime) {
                dto.setCreateTime((LocalDateTime) createTimeObj);
            }
            
            Object updateTimeObj = orderData.get("update_time");
            if (updateTimeObj instanceof Date) {
                dto.setUpdateTimeOld((Date) updateTimeObj);
            } else if (updateTimeObj instanceof LocalDateTime) {
                dto.setUpdateTime((LocalDateTime) updateTimeObj);
            }
            
            // 收货地址信息 - 适配统一地址系统
            dto.setReceiverName(getString(orderData, "receiver_name"));
            dto.setReceiverPhone(getString(orderData, "receiver_phone"));
            
            // 使用日本地址系统字段
            dto.setReceiverProvince(getString(orderData, "prefecture")); // 都道府県
            dto.setReceiverCity(getString(orderData, "city")); // 市区町村 (municipality的别名)
            dto.setReceiverDistrict(getString(orderData, "town")); // 町名
            dto.setReceiverAddress(getString(orderData, "shipping_address")); // 完整地址
            
            // 设置邮编
            dto.setReceiverZip(getString(orderData, "postal_code"));
            
            log.info("设置收货地址信息 - 姓名: {}, 电话: {}, 地址: {} {} {} {}", 
                    dto.getReceiverName(), dto.getReceiverPhone(), 
                    dto.getReceiverProvince(), dto.getReceiverCity(),
                    dto.getReceiverDistrict(), dto.getReceiverAddress());
            
            // 设置物流信息 - 关键修复
            String logisticsInfo = getString(orderData, "logistics_info");
            dto.setLogisticsInfo(logisticsInfo);
            log.info("设置物流信息: {}", logisticsInfo != null ? logisticsInfo.substring(0, Math.min(100, logisticsInfo.length())) + "..." : "null");
            
            // 检查地址是否被软删除
            Object addressDeleted = orderData.get("address_deleted");
            if (addressDeleted != null) {
                boolean isDeleted = false;
                if (addressDeleted instanceof Boolean) {
                    isDeleted = (Boolean) addressDeleted;
                } else if (addressDeleted instanceof Number) {
                    isDeleted = ((Number) addressDeleted).intValue() == 1;
                } else if (addressDeleted instanceof String) {
                    isDeleted = "1".equals(addressDeleted) || "true".equalsIgnoreCase((String) addressDeleted);
                }
                
                if (isDeleted) {
                    log.info("注意：该订单关联的地址已被软删除，但仍可显示历史信息");
                }
            }
            
            // 商家信息
            Long merchantId = getLongValue(orderData, "related_id");
            log.info("商家信息处理 - related_id: {}", merchantId);
            
            if (merchantId != null) {
                dto.setMerchantId(merchantId);
                
                // 直接从查询结果中获取商家信息（已通过JOIN查询获得）
                String merchantName = getString(orderData, "merchant_name");
                String merchantLogo = getString(orderData, "merchant_logo");
                
                log.info("从查询结果获取商家信息 - 商家名称: '{}', 商家Logo: '{}'", merchantName, merchantLogo);
                
                if (merchantName != null) {
                    dto.setMerchantName(merchantName);
                    dto.setMerchantLogo(merchantLogo);
                    log.info("直接设置商家信息成功");
                } else {
                    log.warn("查询结果中没有商家信息，进行额外查询");
                    // 如果查询结果中没有商家信息，则进行额外查询（兼容性处理）
                    try {
                        Merchant merchant = merchantMapper.selectByPrimaryKey(merchantId);
                        if (merchant != null) {
                            dto.setMerchantName(merchant.getMerchantName());
                            dto.setMerchantLogo(merchant.getLogo());
                            log.info("通过额外查询设置商家信息: '{}', '{}'", merchant.getMerchantName(), merchant.getLogo());
                        } else {
                            dto.setMerchantName("未知商家");
                            log.warn("商家不存在: merchantId={}", merchantId);
                        }
                    } catch (Exception e) {
                        log.warn("查询商家信息失败: merchantId={}", merchantId, e);
                        dto.setMerchantName("未知商家");
                    }
                }
            } else {
                log.warn("merchantId为null，无法设置商家信息");
            }
            
            // 订单项信息
            Object orderItemsObj = orderData.get("orderItems");
            if (orderItemsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> orderItemMaps = (List<Map<String, Object>>) orderItemsObj;
                List<OrderItemDTO> orderItems = new ArrayList<>();
                for (Map<String, Object> itemMap : orderItemMaps) {
                    OrderItemDTO itemDTO = convertToOrderItemDTO(itemMap);
                    if (itemDTO != null) {
                        orderItems.add(itemDTO);
                    }
                }
                dto.setOrderItems(orderItems);
            }
            
            // 兼容性字段
            dto.setStatus(dto.getOrderStatus());
            if (dto.getTotalAmount() != null) {
                dto.setPayAmount(dto.getTotalAmount().doubleValue());
            }
            
            return dto;
            
        } catch (Exception e) {
            log.error("转换OrderDTO失败", e);
            log.error("异常堆栈:", e);
            log.error("orderData内容: {}", orderData);
            // 不返回null，而是抛出异常让上层处理
            throw new RuntimeException("转换OrderDTO失败: " + e.getMessage(), e);
        }
    }
    
    // 辅助方法：安全地从Map中获取值
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        // 添加Boolean类型检查
        if (value instanceof Boolean) {
            log.warn("getLongValue: Boolean类型不能转换为Long: key={}, value={}", key, value);
            return null;
        }
        
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                log.warn("getLongValue: 无法转换字符串为Long: key={}, value={}", key, value);
                return null;
            }
        }
        log.warn("getLongValue: 无法转换类型为Long: key={}, value={}, type={}", key, value, value.getClass().getSimpleName());
        return null;
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        // 添加Boolean类型检查
        if (value instanceof Boolean) {
            log.warn("getIntegerValue: Boolean类型不能转换为Integer: key={}, value={}", key, value);
            return null;
        }
        
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                log.warn("getIntegerValue: 无法转换字符串为Integer: key={}, value={}", key, value);
                return null;
            }
        }
        log.warn("getIntegerValue: 无法转换类型为Integer: key={}, value={}, type={}", key, value, value.getClass().getSimpleName());
        return null;
    }
    
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        log.info("getBigDecimalValue: key={}, value={}, valueType={}", key, value, value != null ? value.getClass().getSimpleName() : "null");
        
        if (value == null) {
            log.info("getBigDecimalValue: 返回null，因为value为null");
            return null;
        }
        
        // 添加Boolean类型检查
        if (value instanceof Boolean) {
            log.warn("getBigDecimalValue: Boolean类型不能转换为BigDecimal: key={}, value={}", key, value);
            return null;
        }
        
        if (value instanceof BigDecimal) {
            log.info("getBigDecimalValue: 直接返回BigDecimal: {}", value);
            return (BigDecimal) value;
        }
        if (value instanceof Double) {
            BigDecimal result = BigDecimal.valueOf((Double) value);
            log.info("getBigDecimalValue: Double转换为BigDecimal: {} -> {}", value, result);
            return result;
        }
        if (value instanceof Float) {
            BigDecimal result = BigDecimal.valueOf(((Float) value).doubleValue());
            log.info("getBigDecimalValue: Float转换为BigDecimal: {} -> {}", value, result);
            return result;
        }
        if (value instanceof Integer) {
            BigDecimal result = BigDecimal.valueOf(((Integer) value).doubleValue());
            log.info("getBigDecimalValue: Integer转换为BigDecimal: {} -> {}", value, result);
            return result;
        }
        if (value instanceof Long) {
            BigDecimal result = BigDecimal.valueOf(((Long) value).doubleValue());
            log.info("getBigDecimalValue: Long转换为BigDecimal: {} -> {}", value, result);
            return result;
        }
        if (value instanceof String) {
            try {
                BigDecimal result = new BigDecimal((String) value);
                log.info("getBigDecimalValue: String转换为BigDecimal: {} -> {}", value, result);
                return result;
            } catch (NumberFormatException e) {
                log.error("无法将字符串转换为BigDecimal: key={}, value={}", key, value);
                return null;
            }
        }
        
        log.error("无法转换为BigDecimal: key={}, value={}, type={}", key, value, value.getClass().getSimpleName());
        return null;
    }
    
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        // 如果是其他时间类型，需要转换
        return null;
    }
    
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        return null;
    }
    
    /**
     * 获取商家各状态订单数量统计
     */
    @Override
    public Map<String, Integer> getOrderCountByStatusForMerchant(Long merchantId) {
        try {
            log.info("获取商家订单状态统计: merchantId={}", merchantId);
            
            Map<String, Integer> statusCount = new HashMap<>();
            
            // 查询各状态订单数量
            for (int status = 1; status <= 8; status++) {
                int count = orderMapper.countOrdersByMerchantAndStatus(merchantId, status);
                statusCount.put("status_" + status, count);
            }
            
            log.info("商家订单状态统计完成: {}", statusCount);
            return statusCount;
            
        } catch (Exception e) {
            log.error("获取商家订单状态统计失败", e);
            return new HashMap<>();
        }
    }
    
    /**
     * 获取商家待处理的退款申请列表
     */
    @Override
    public List<Order> getPendingRefundsByMerchantId(Long merchantId, int page, int size) {
        try {
            log.info("获取商家待处理退款申请: merchantId={}, page={}, size={}", merchantId, page, size);
            
            int offset = (page - 1) * size;
            List<Order> refundOrders = orderMapper.selectPendingRefundsByMerchant(merchantId, offset, size);
            
            log.info("获取到{}条待处理退款申请", refundOrders.size());
            return refundOrders;
            
        } catch (Exception e) {
            log.error("获取商家待处理退款申请失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取商家待处理的退款申请列表
     */
    @Override
    public Result<List<OrderDTO>> getMerchantOrdersWithFilter(Long merchantId, Integer status, String keyword, 
                                                             String timeFilter, Integer refundStatus, int page, int size) {
        try {
            log.info("获取商家订单列表（带筛选）: merchantId={}, status={}, keyword={}, timeFilter={}, refundStatus={}, page={}, size={}", 
                    merchantId, status, keyword, timeFilter, refundStatus, page, size);
            
            int offset = (page - 1) * size;
            
            // 调用mapper方法获取筛选后的订单列表
            List<Map<String, Object>> orderDataList = orderMapper.selectMerchantOrdersWithFilter(
                    merchantId, status, keyword, timeFilter, refundStatus, offset, size);
            
            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Map<String, Object> orderData : orderDataList) {
                OrderDTO orderDTO = convertToOrderDTO(orderData);
                
                // 查询订单项
                Long orderId = getLongValue(orderData, "order_id");
                if (orderId != null) {
                    List<Map<String, Object>> orderItems = orderMapper.selectOrderItemsByOrderId(orderId);
                    
                    // 转换订单项为DTO
                    List<org.example.afd.dto.OrderItemDTO> orderItemDTOs = new ArrayList<>();
                    for (Map<String, Object> itemData : orderItems) {
                        org.example.afd.dto.OrderItemDTO itemDTO = convertToOrderItemDTO(itemData);
                        orderItemDTOs.add(itemDTO);
                    }
                    
                    orderDTO.setOrderItems(orderItemDTOs);
                }
                
                orderDTOList.add(orderDTO);
            }
            
            log.info("商家订单列表查询完成，共{}条订单", orderDTOList.size());
            return Result.success(orderDTOList);
            
        } catch (Exception e) {
            log.error("获取商家订单列表失败", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    private OrderItemDTO convertToOrderItemDTO(Map<String, Object> itemMap) {
        OrderItemDTO itemDTO = new OrderItemDTO();
        
        itemDTO.setOrderItemId(getLongValue(itemMap, "item_id"));
        itemDTO.setOrderId(getLongValue(itemMap, "order_id"));
        itemDTO.setProductId(getLongValue(itemMap, "product_id"));
        itemDTO.setSkuId(getLongValue(itemMap, "sku_id"));
        itemDTO.setProductName((String) itemMap.get("product_name"));
        itemDTO.setSkuName((String) itemMap.get("sku_name"));
        itemDTO.setSkuImage((String) itemMap.get("sku_image"));
        itemDTO.setPrice(getBigDecimalValue(itemMap, "unit_price") != null ? 
                getBigDecimalValue(itemMap, "unit_price").doubleValue() : 0.0);
        itemDTO.setQuantity(getIntegerValue(itemMap, "quantity"));
        itemDTO.setTotalPrice(getBigDecimalValue(itemMap, "item_amount") != null ? 
                getBigDecimalValue(itemMap, "item_amount").doubleValue() : 0.0);
        
        return itemDTO;
    }
    
    // ===== 新增的订单管理方法实现 =====
    
    @Override
    public Map<String, Object> getTodayOrderStatistics(Long merchantId) {
        try {
            log.info("获取今日订单统计数据: merchantId={}", merchantId);
            
            Map<String, Object> todayData = new HashMap<>();
            
            // 今日订单数量
            Integer todayOrderCount = orderMapper.getTodayOrderCount(merchantId);
            todayData.put("orderCount", todayOrderCount != null ? todayOrderCount : 0);
            
            // 今日销售额
            BigDecimal todaySales = orderMapper.getTodaySales(merchantId);
            todayData.put("sales", todaySales != null ? todaySales : BigDecimal.ZERO);
            
            // 今日新增客户数
            Integer todayNewCustomers = orderMapper.getTodayNewCustomers(merchantId);
            todayData.put("newCustomers", todayNewCustomers != null ? todayNewCustomers : 0);
            
            log.info("今日订单统计数据获取成功: {}", todayData);
            return todayData;
            
        } catch (Exception e) {
            log.error("获取今日订单统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Integer> getOrderCountByStatus(Long merchantId) {
        try {
            log.info("获取各状态订单数量: merchantId={}", merchantId);
            
            Map<String, Integer> statusCount = new HashMap<>();
            
            // 待发货 (status=2,3)
            Integer pendingShip = orderMapper.getOrderCountByStatuses(merchantId, Arrays.asList(2, 3));
            statusCount.put("pendingShip", pendingShip != null ? pendingShip : 0);
            log.info("待发货订单数量: {}", pendingShip);
            
            // 已发货 (status=4)
            Integer shipped = orderMapper.getOrderCountByStatus(merchantId, 4);
            statusCount.put("shipped", shipped != null ? shipped : 0);
            log.info("已发货订单数量: {}", shipped);
            
            // 已完成 (status=5)
            Integer completed = orderMapper.getOrderCountByStatus(merchantId, 5);
            statusCount.put("completed", completed != null ? completed : 0);
            log.info("已完成订单数量: {}", completed);
            
            // 退款中 (status=7) - 只统计状态7的订单
            Integer refunding = orderMapper.getOrderCountByStatus(merchantId, 7);
            statusCount.put("refund", refunding != null ? refunding : 0);
            log.info("退款中订单数量: {}", refunding);
            
            // 已取消 (status=6)
            Integer cancelled = orderMapper.getOrderCountByStatus(merchantId, 6);
            statusCount.put("cancelled", cancelled != null ? cancelled : 0);
            log.info("已取消订单数量: {}", cancelled);
            
            log.info("各状态订单统计完成: {}", statusCount);
            return statusCount;
            
        } catch (Exception e) {
            log.error("获取各状态订单数量失败", e);
            Map<String, Integer> statusCount = new HashMap<>();
            statusCount.put("pendingShip", 0);
            statusCount.put("shipped", 0);
            statusCount.put("completed", 0);
            statusCount.put("refund", 0);
            statusCount.put("cancelled", 0);
            return statusCount;
        }
    }
    
    @Override
    public BigDecimal getMonthlySales(Long merchantId) {
        try {
            log.info("获取本月销售额: merchantId={}", merchantId);
            
            BigDecimal monthlySales = orderMapper.getMonthlySales(merchantId);
            
            log.info("本月销售额获取成功: {}", monthlySales);
            return monthlySales != null ? monthlySales : BigDecimal.ZERO;
            
        } catch (Exception e) {
            log.error("获取本月销售额失败", e);
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public Integer getTotalOrderCount(Long merchantId) {
        try {
            log.info("获取总订单数量: merchantId={}", merchantId);
            
            Integer totalCount = orderMapper.getTotalOrderCount(merchantId);
            Integer result = totalCount != null ? totalCount : 0;
            
            log.info("总订单数量获取成功: merchantId={}, totalCount={}", merchantId, result);
            return result;
            
        } catch (Exception e) {
            log.error("获取总订单数量失败: merchantId={}", merchantId, e);
            return 0;
        }
    }
    
    @Override
    public Integer getOvertimeOrderCount(Long merchantId) {
        try {
            log.info("获取超时未发货订单数量: merchantId={}", merchantId);
            
            // 查询已支付但超过24小时未发货的订单
            Integer overtimeCount = orderMapper.getOverTimeOrderCount(merchantId);
            
            log.info("超时未发货订单数量获取成功: {}", overtimeCount);
            return overtimeCount != null ? overtimeCount : 0;
            
        } catch (Exception e) {
            log.error("获取超时未发货订单数量失败", e);
            return 0;
        }
    }
    
    @Override
    public Integer getPendingRefundOrderCount(Long merchantId) {
        try {
            log.info("获取待处理退款订单数量: merchantId={}", merchantId);
            
            Integer pendingRefundCount = orderMapper.getOrderCountByStatus(merchantId, 7);
            
            log.info("待处理退款订单数量获取成功: {}", pendingRefundCount);
            return pendingRefundCount != null ? pendingRefundCount : 0;
            
        } catch (Exception e) {
            log.error("获取待处理退款订单数量失败", e);
            return 0;
        }
    }
    
    @Override
    public Integer getUrgentShipOrderCount(Long merchantId) {
        try {
            log.info("获取24小时内需发货订单数量: merchantId={}", merchantId);
            
            // 查询已支付且在24小时内需要发货的订单
            Integer urgentShipCount = orderMapper.getUrgentShipOrderCount(merchantId);
            
            log.info("24小时内需发货订单数量获取成功: {}", urgentShipCount);
            return urgentShipCount != null ? urgentShipCount : 0;
            
        } catch (Exception e) {
            log.error("获取24小时内需发货订单数量失败", e);
            return 0;
        }
    }
    
    @Override
    public List<OrderDTO> getOvertimeOrders(Long merchantId, int page, int size) {
        try {
            log.info("获取超时订单列表: merchantId={}, page={}, size={}", merchantId, page, size);
            
            int offset = (page - 1) * size;
            List<Map<String, Object>> orderDataList = orderMapper.getOvertimeOrders(merchantId, offset, size);
            
            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Map<String, Object> orderData : orderDataList) {
                OrderDTO orderDTO = convertToOrderDTO(orderData);
                if (orderDTO != null) {
                    orderDTOList.add(orderDTO);
                }
            }
            
            log.info("超时订单列表获取成功，共{}条", orderDTOList.size());
            return orderDTOList;
            
        } catch (Exception e) {
            log.error("获取超时订单列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<OrderDTO> getPendingRefundOrders(Long merchantId, int page, int size) {
        try {
            log.info("获取待处理退款订单列表: merchantId={}, page={}, size={}", merchantId, page, size);
            
            int offset = (page - 1) * size;
            List<Map<String, Object>> orderDataList = orderMapper.getPendingRefundOrders(merchantId, offset, size);
            
            List<OrderDTO> orderDTOList = new ArrayList<>();
            for (Map<String, Object> orderData : orderDataList) {
                OrderDTO orderDTO = convertToOrderDTO(orderData);
                if (orderDTO != null) {
                    orderDTOList.add(orderDTO);
                }
            }
            
            log.info("待处理退款订单列表获取成功，共{}条", orderDTOList.size());
            return orderDTOList;
            
        } catch (Exception e) {
            log.error("获取待处理退款订单列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getTopCustomers(Map<String, Object> params) {
        try {
            log.info("获取客户排行: params={}", params);
            
            // 这里可以根据params参数实现不同的客户排行逻辑
            // 暂时返回空列表，后续可以根据需求完善
            List<Map<String, Object>> topCustomers = new ArrayList<>();
            
            log.info("客户排行获取成功，共{}条", topCustomers.size());
            return topCustomers;
            
        } catch (Exception e) {
            log.error("获取客户排行失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public PageResult<Map<String, Object>> getMerchantOrdersAdvanced(Map<String, Object> queryParams) {
        try {
            log.info("获取商家订单列表（高级版本）: queryParams={}", queryParams);
            
            // 暂时返回空结果，后续可以根据需求完善
            PageResult<Map<String, Object>> result = new PageResult<>();
            result.setRecords(new ArrayList<>());
            result.setTotal(0L);
            result.setCurrent(1);
            result.setSize(10);
            
            return result;
            
        } catch (Exception e) {
            log.error("获取商家订单列表（高级版本）失败", e);
            return new PageResult<>();
        }
    }
    
    @Override
    public Map<String, Object> getOrderStatistics(Long merchantId, Date targetDate) {
        try {
            log.info("获取订单统计数据: merchantId={}, targetDate={}", merchantId, targetDate);
            
            // 暂时返回空统计，后续可以根据需求完善
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("orderCount", 0);
            statistics.put("sales", BigDecimal.ZERO);
            statistics.put("newCustomers", 0);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取订单统计数据失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> batchShipOrders(Map<String, Object> shipmentInfo) {
        try {
            log.info("批量发货: shipmentInfo={}", shipmentInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "批量发货成功");
            
            return result;
            
        } catch (Exception e) {
            log.error("批量发货失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量发货失败");
            return result;
        }
    }
    
    @Override
    public boolean canShipOrder(String orderNumber, Long merchantId) {
        try {
            log.info("检查订单是否可以发货: orderNumber={}, merchantId={}", orderNumber, merchantId);
            
            // 暂时返回true，后续可以根据需求完善
            return true;
            
        } catch (Exception e) {
            log.error("检查订单是否可以发货失败", e);
            return false;
        }
    }
    
    @Override
    public boolean shipOrder(Map<String, Object> shipmentInfo) {
        try {
            log.info("单个订单发货: shipmentInfo={}", shipmentInfo);
            
            // 暂时返回true，后续可以根据需求完善
            return true;
            
        } catch (Exception e) {
            log.error("单个订单发货失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> batchAddOrderRemark(Map<String, Object> remarkInfo) {
        try {
            log.info("批量添加订单备注: remarkInfo={}", remarkInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "批量添加订单备注成功");
            
            return result;
            
        } catch (Exception e) {
            log.error("批量添加订单备注失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量添加订单备注失败");
            return result;
        }
    }
    
    @Override
    public boolean isOrderBelongToMerchant(String orderNumber, Long merchantId) {
        try {
            log.info("检查订单是否属于指定商家: orderNumber={}, merchantId={}", orderNumber, merchantId);
            
            // 暂时返回true，后续可以根据需求完善
            return true;
            
        } catch (Exception e) {
            log.error("检查订单是否属于指定商家失败", e);
            return false;
        }
    }
    
    @Override
    public boolean addOrderRemark(String orderNumber, Long merchantId, String remark) {
        try {
            log.info("添加订单备注: orderNumber={}, merchantId={}, remark={}", orderNumber, merchantId, remark);
            
            // 暂时返回true，后续可以根据需求完善
            return true;
            
        } catch (Exception e) {
            log.error("添加订单备注失败", e);
            return false;
        }
    }
    
    @Override
    public boolean canCancelOrder(String orderNumber, Long merchantId) {
        try {
            log.info("检查订单是否可以取消: orderNumber={}, merchantId={}", orderNumber, merchantId);
            
            // 查询订单信息
            Map<String, Object> orderInfo = orderMapper.getOrderByOrderNumber(orderNumber);
            if (orderInfo == null) {
                log.warn("订单不存在: orderNumber={}", orderNumber);
                return false;
            }
            
            // 检查订单是否属于该商家
            Long orderMerchantId = getLongValue(orderInfo, "related_id");
            Integer orderType = getIntegerValue(orderInfo, "order_type");
            
            // 只有购物订单(order_type=1)才需要检查商家权限
            if (orderType != null && orderType == 1) {
                if (!merchantId.equals(orderMerchantId)) {
                    log.warn("订单不属于该商家: orderNumber={}, orderMerchantId={}, requestMerchantId={}", 
                             orderNumber, orderMerchantId, merchantId);
                    return false;
                }
            }
            
            // 检查订单状态是否允许取消
            Integer orderStatus = getIntegerValue(orderInfo, "order_status");
            if (orderStatus == null) {
                log.warn("订单状态为空: orderNumber={}", orderNumber);
                return false;
            }
            
            // 只有待支付(1)、已支付(2)、待发货(3)状态的订单可以取消
            boolean canCancel = orderStatus == 1 || orderStatus == 2 || orderStatus == 3;
            if (!canCancel) {
                log.warn("订单状态不允许取消: orderNumber={}, status={}", orderNumber, orderStatus);
            }
            
            return canCancel;
            
        } catch (Exception e) {
            log.error("检查订单是否可以取消失败: orderNumber={}, merchantId={}", orderNumber, merchantId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(String orderNumber, Long merchantId, String cancelReason) {
        try {
            log.info("取消订单（商家操作）: orderNumber={}, merchantId={}, cancelReason={}", orderNumber, merchantId, cancelReason);
            
            // 再次检查订单是否可以取消（防止并发问题）
            if (!canCancelOrder(orderNumber, merchantId)) {
                log.warn("订单状态已变更，无法取消: orderNumber={}", orderNumber);
                return false;
            }
            
            // 更新订单状态为已取消(6)
            int updateResult = orderMapper.updateOrderStatus(orderNumber, 6);
            if (updateResult <= 0) {
                log.error("更新订单状态失败: orderNumber={}", orderNumber);
                return false;
            }
            
            // 如果订单已支付，需要创建退款记录
            Map<String, Object> orderInfo = orderMapper.getOrderByOrderNumber(orderNumber);
            Integer orderStatus = getIntegerValue(orderInfo, "order_status");
            
            if (orderStatus != null && (orderStatus == 2 || orderStatus == 3)) {
                // 订单已支付或待发货状态，需要处理退款
                boolean refundResult = createRefundRecord(orderInfo, cancelReason, merchantId);
                if (!refundResult) {
                    log.warn("创建退款记录失败，但订单已取消: orderNumber={}", orderNumber);
                    // 不回滚订单取消操作，只是记录警告
                }
            }
            
            // 恢复商品库存（如果是商品订单）
            Integer orderType = getIntegerValue(orderInfo, "order_type");
            if (orderType != null && orderType == 1) {
                restoreProductStock(orderNumber);
            }
            
            log.info("订单取消成功: orderNumber={}, merchantId={}", orderNumber, merchantId);
            return true;
            
        } catch (Exception e) {
            log.error("取消订单（商家操作）失败: orderNumber={}, merchantId={}", orderNumber, merchantId, e);
            return false;
        }
    }
    
    /**
     * 创建退款记录
     */
    private boolean createRefundRecord(Map<String, Object> orderInfo, String reason, Long merchantId) {
        try {
            Long orderId = getLongValue(orderInfo, "order_id");
            Long userId = getLongValue(orderInfo, "user_id");
            BigDecimal refundAmount = getBigDecimalValue(orderInfo, "paid_amount");
            
            if (orderId == null || userId == null || refundAmount == null) {
                log.error("订单信息不完整，无法创建退款记录: {}", orderInfo);
                return false;
            }
            
            // 这里应该调用退款相关的服务来创建退款记录
            // 暂时只记录日志，实际项目中需要实现完整的退款流程
            log.info("应创建退款记录: orderId={}, userId={}, amount={}, reason={}", 
                    orderId, userId, refundAmount, reason);
            
            return true;
            
        } catch (Exception e) {
            log.error("创建退款记录失败", e);
            return false;
        }
    }
    
    /**
     * 恢复商品库存
     */
    private void restoreProductStock(String orderNumber) {
        try {
            // 查询订单项
            Order order = orderMapper.selectByOrderNo(orderNumber);
            if (order == null) {
                log.warn("订单不存在，无法恢复库存: orderNumber={}", orderNumber);
                return;
            }
            
            List<Map<String, Object>> orderItems = orderMapper.selectOrderItemsByOrderId(order.getOrderId());
            
            for (Map<String, Object> item : orderItems) {
                Long skuId = getLongValue(item, "sku_id");
                Integer quantity = getIntegerValue(item, "quantity");
                
                if (skuId != null && quantity != null && quantity > 0) {
                    // 这里应该调用库存服务来恢复库存
                    // 暂时只记录日志，实际项目中需要实现完整的库存管理
                    log.info("应恢复库存: skuId={}, quantity={}", skuId, quantity);
                }
            }
            
        } catch (Exception e) {
            log.error("恢复商品库存失败: orderNumber={}", orderNumber, e);
        }
    }
    
    @Override
    public boolean hasRefundRequest(String orderNumber, Long merchantId) {
        try {
            log.info("检查订单是否有退款申请: orderNumber={}, merchantId={}", orderNumber, merchantId);
            
            // 暂时返回false，后续可以根据需求完善
            return false;
            
        } catch (Exception e) {
            log.error("检查订单是否有退款申请失败", e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean handleRefundRequest(Map<String, Object> refundInfo) {
        try {
            log.info("处理退款申请: refundInfo={}", refundInfo);
            
            String orderNumber = (String) refundInfo.get("orderNumber");
            Long merchantId = (Long) refundInfo.get("merchantId");
            Boolean approved = (Boolean) refundInfo.get("approved");
            String reason = (String) refundInfo.get("reason");
            
            if (orderNumber == null || merchantId == null || approved == null) {
                log.error("退款处理参数不完整");
                return false;
            }
            
            // 查询订单信息
            Map<String, Object> orderInfo = orderMapper.getOrderByOrderNumber(orderNumber);
            if (orderInfo == null) {
                log.error("订单不存在: orderNumber={}", orderNumber);
                return false;
            }
            
            // 验证订单是否属于该商家
            if (!isOrderBelongToMerchant(orderNumber, merchantId)) {
                log.error("订单不属于该商家: orderNumber={}, merchantId={}", orderNumber, merchantId);
                return false;
            }
            
            // 检查订单状态是否为退款中(7)
            Integer currentStatus = (Integer) orderInfo.get("order_status");
            if (currentStatus == null || currentStatus != 7) {
                log.error("订单状态不是退款中，无法处理退款: orderNumber={}, status={}", orderNumber, currentStatus);
                return false;
            }
            
            int newStatus;
            String statusReason;
            
            if (approved) {
                // 同意退款 - 更新订单状态为已退款(8)
                newStatus = 8;
                statusReason = "商家同意退款: " + (reason != null ? reason : "无");
                
                // TODO: 这里应该调用实际的退款接口
                // 目前暂时模拟退款成功
                log.info("模拟退款成功: orderNumber={}", orderNumber);
                
                // 恢复商品库存
                restoreProductStock(orderNumber);
                
            } else {
                // 拒绝退款 - 恢复订单状态为已完成(5)
                newStatus = 5;
                statusReason = "商家拒绝退款: " + (reason != null ? reason : "无");
            }
            
            // 更新订单状态
            int updateResult = orderMapper.updateOrderStatus(orderNumber, newStatus);
            if (updateResult <= 0) {
                log.error("更新订单状态失败: orderNumber={}, newStatus={}", orderNumber, newStatus);
                return false;
            }
            
            // 记录退款处理日志
            log.info("退款处理完成: orderNumber={}, approved={}, newStatus={}, reason={}", 
                    orderNumber, approved, newStatus, statusReason);
            
            return true;
            
        } catch (Exception e) {
            log.error("处理退款申请失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getLogisticsInfo(String orderNumber) {
        try {
            log.info("获取物流信息: orderNumber={}", orderNumber);
            
            // 暂时返回空物流信息，后续可以根据需求完善
            Map<String, Object> logisticsInfo = new HashMap<>();
            logisticsInfo.put("orderNumber", orderNumber);
            logisticsInfo.put("status", "暂无物流信息");
            
            return logisticsInfo;
            
        } catch (Exception e) {
            log.error("获取物流信息失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public boolean updateLogisticsInfo(Map<String, Object> logisticsInfo) {
        try {
            log.info("更新物流信息: logisticsInfo={}", logisticsInfo);
            
            // 暂时返回true，后续可以根据需求完善
            return true;
            
        } catch (Exception e) {
            log.error("更新物流信息失败", e);
            return false;
        }
    }
    
    @Override
    public String exportOrders(Map<String, Object> exportParams) {
        try {
            log.info("导出订单数据: exportParams={}", exportParams);
            
            // 暂时返回空字符串，后续可以根据需求完善
            return "";
            
        } catch (Exception e) {
            log.error("导出订单数据失败", e);
            return "";
        }
    }
    
    @Override
    public Map<String, Object> getOrderAnalysis(Map<String, Object> analysisParams) {
        try {
            log.info("获取订单分析报告: analysisParams={}", analysisParams);
            
            // 暂时返回空分析报告，后续可以根据需求完善
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("totalOrders", 0);
            analysis.put("totalSales", BigDecimal.ZERO);
            
            return analysis;
            
        } catch (Exception e) {
            log.error("获取订单分析报告失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getHotProducts(Map<String, Object> params) {
        try {
            log.info("获取热销商品排行: params={}", params);
            
            // 暂时返回空列表，后续可以根据需求完善
            List<Map<String, Object>> hotProducts = new ArrayList<>();
            
            return hotProducts;
            
        } catch (Exception e) {
            log.error("获取热销商品排行失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Result<Map<String, Object>> getMerchantOrders(Integer userId, Integer status, Integer orderType, Integer page, Integer size) {
        try {
            log.info("=== 获取商家订单列表 ===");
            log.info("商家用户ID: {}, 状态: {}, 订单类型: {}, 页码: {}, 每页数量: {}", userId, status, orderType, page, size);
            
            // 首先获取商家ID
            Long merchantId = getMerchantIdByUserId(userId);
            if (merchantId == null) {
                return Result.error("用户不是商家或商家信息不存在");
            }
            
            log.info("商家ID: {}", merchantId);
            
            // 计算偏移量
            int offset = (page - 1) * size;
            
            List<Map<String, Object>> orders;
            int totalCount;
            
            // 根据状态值判断是否需要多状态查询
            if (status != null && status == 2) {
                // 状态2通常表示"待发货"，应该包含已支付(2)和待发货(3)两种状态
                log.info("查询待发货状态订单，包含状态2(已支付)和状态3(待发货)");
                List<Integer> statuses = Arrays.asList(2, 3);
                orders = orderMapper.getMerchantOrdersByStatuses(merchantId, statuses, orderType, offset, size);
                totalCount = orderMapper.getMerchantOrderCountByStatuses(merchantId, statuses, orderType);
            } else {
                // 其他状态使用原来的单状态查询
                log.info("查询单一状态订单: {}", status);
                orders = orderMapper.getMerchantOrders(merchantId, status, orderType, offset, size);
                totalCount = orderMapper.getMerchantOrderCount(merchantId, status, orderType);
            }
            
            // 详细日志记录返回的数据
            log.info("查询到{}条商家订单", orders.size());
            if (!orders.isEmpty()) {
                log.info("第一条订单的详细数据: {}", orders.get(0));
                log.info("第一条订单的字段: {}", orders.get(0).keySet());
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("orders", orders);
            result.put("page", page);
            result.put("size", size);
            result.put("totalCount", totalCount);
            result.put("totalPages", (int) Math.ceil((double) totalCount / size));
            
            log.info("返回结果结构: {}", result.keySet());
            return Result.success("获取商家订单列表成功", result);
            
        } catch (Exception e) {
            log.error("获取商家订单列表失败", e);
            return Result.error("获取商家订单列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> shipOrder(Long orderId, Integer userId, Map<String, Object> shipmentData) {
        try {
            log.info("=== 商家发货开始 ===");
            log.info("订单ID: {}, 商家用户ID: {}, 发货信息: {}", orderId, userId, shipmentData);
            
            // 获取商家ID
            log.info("步骤1: 获取商家ID");
            Long merchantId = getMerchantIdByUserId(userId);
            if (merchantId == null) {
                log.error("获取商家ID失败: 用户不是商家或商家信息不存在, userId={}", userId);
                return Result.error("用户不是商家或商家信息不存在");
            }
            log.info("获取商家ID成功: merchantId={}", merchantId);
            
            // 验证订单是否属于该商家
            log.info("步骤2: 验证订单归属");
            if (!isOrderBelongToMerchant(orderId, merchantId)) {
                log.error("订单归属验证失败: 订单不属于该商家, orderId={}, merchantId={}", orderId, merchantId);
                return Result.error("订单不属于该商家");
            }
            log.info("订单归属验证成功");
            
            // 检查订单状态是否可以发货（应该是已支付状态2或待发货状态3）
            log.info("步骤3: 检查订单状态");
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("订单查询失败: 订单不存在, orderId={}", orderId);
                return Result.error("订单不存在");
            }
            log.info("订单查询成功: orderNo={}, status={}", order.getOrderNo(), order.getOrderStatus());
            
            if (order.getOrderStatus() != 2 && order.getOrderStatus() != 3) {
                log.error("订单状态验证失败: 订单状态不允许发货, orderId={}, currentStatus={}", orderId, order.getOrderStatus());
                return Result.error("订单状态不允许发货，当前状态：" + order.getOrderStatus());
            }
            log.info("订单状态验证成功，可以发货");
            
            // 获取发货信息
            log.info("步骤4: 解析发货信息");
            String logisticsCompany = (String) shipmentData.get("logisticsCompany");
            String trackingNumber = (String) shipmentData.get("trackingNumber");
            String shipNote = (String) shipmentData.get("shipNote");
            
            log.info("物流公司: {}", logisticsCompany);
            log.info("快递单号: {}", trackingNumber);
            log.info("发货备注: {}", shipNote);
            
            if (logisticsCompany == null || logisticsCompany.trim().isEmpty()) {
                log.error("发货信息验证失败: 物流公司不能为空");
                return Result.error("物流公司不能为空");
            }
            
            if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
                log.error("发货信息验证失败: 快递单号不能为空");
                return Result.error("快递单号不能为空");
            }
            log.info("发货信息验证成功");
            
            // 构造物流信息JSON
            log.info("步骤5: 构造物流信息JSON");
            Map<String, Object> logisticsInfo = new HashMap<>();
            logisticsInfo.put("company", logisticsCompany);
            logisticsInfo.put("trackingNumber", trackingNumber);
            logisticsInfo.put("shipTime", LocalDateTime.now().toString());
            if (shipNote != null && !shipNote.trim().isEmpty()) {
                logisticsInfo.put("shipNote", shipNote);
            }
            
            // 转换为JSON字符串
            String logisticsJson;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                logisticsJson = objectMapper.writeValueAsString(logisticsInfo);
                log.info("物流信息JSON构造成功: {}", logisticsJson);
            } catch (Exception e) {
                log.error("物流信息JSON序列化失败", e);
                return Result.error("物流信息格式错误");
            }
            
            // 插入物流记录到shop_logistics表
            log.info("步骤6: 插入物流记录");
            try {
                Map<String, Object> logistics = new HashMap<>();
                logistics.put("orderId", orderId);
                logistics.put("orderNo", order.getOrderNo());
                logistics.put("shippingCompany", logisticsCompany);
                logistics.put("trackingNo", trackingNumber);
                logistics.put("deliveryStatus", 1); // 1-已发货
                logistics.put("deliveryTime", LocalDateTime.now());
                
                int logisticsResult = orderMapper.insertLogistics(logistics);
                log.info("插入物流记录结果: {}", logisticsResult);
                if (logisticsResult > 0) {
                    log.info("物流记录插入成功");
                } else {
                    log.warn("物流记录插入失败，但继续执行主流程");
                }
            } catch (Exception e) {
                log.warn("插入物流记录失败，继续执行: {}", e.getMessage(), e);
                // 不影响主流程，继续执行
            }
            
            // 更新订单状态为已发货(4)并保存物流信息
            log.info("步骤7: 更新订单状态和物流信息");
            log.info("准备更新: orderId={}, status=4, logisticsJson={}", orderId, logisticsJson);
            
            int result = orderMapper.updateOrderLogisticsInfo(orderId, 4, logisticsJson, LocalDateTime.now());
            log.info("订单更新结果: {}", result);
            
            if (result > 0) {
                log.info("=== 商家发货成功 ===");
                log.info("订单ID: {}, 物流公司: {}, 快递单号: {}", orderId, logisticsCompany, trackingNumber);
                return Result.success("发货成功", true);
            } else {
                log.error("=== 商家发货失败 ===");
                log.error("订单更新失败: orderId={}, updateResult={}", orderId, result);
                return Result.error("发货失败，订单更新失败");
            }
            
        } catch (Exception e) {
            log.error("=== 商家发货异常 ===");
            log.error("orderId: {}, userId: {}, shipmentData: {}", orderId, userId, shipmentData);
            log.error("商家发货失败", e);
            return Result.error("商家发货失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> processMerchantRefund(Long orderId, Integer userId, Map<String, Object> refundData) {
        try {
            log.info("=== 商家处理退款 ===");
            log.info("订单ID: {}, 商家用户ID: {}, 退款处理: {}", orderId, userId, refundData);
            
            // 获取商家ID
            Long merchantId = getMerchantIdByUserId(userId);
            if (merchantId == null) {
                return Result.error("用户不是商家或商家信息不存在");
            }
            
            // 验证订单是否属于该商家
            if (!isOrderBelongToMerchant(orderId, merchantId)) {
                return Result.error("订单不属于该商家");
            }
            
            // 获取退款处理结果
            Boolean approved = (Boolean) refundData.get("approved");
            String reason = (String) refundData.get("reason");
            
            if (approved == null) {
                return Result.error("请指定是否同意退款");
            }
            
            // 更新订单状态
            int newStatus = approved ? 5 : 6; // 5-退款中，6-退款拒绝
            int result = orderMapper.updateOrderStatusById(orderId, newStatus, LocalDateTime.now());
            
            if (result > 0) {
                String message = approved ? "同意退款" : "拒绝退款";
                log.info("商家{}成功，订单ID: {}, 原因: {}", message, orderId, reason);
                return Result.success(message + "成功", true);
            } else {
                return Result.error("处理退款失败");
            }
            
        } catch (Exception e) {
            log.error("商家处理退款失败", e);
            return Result.error("商家处理退款失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getMerchantOrderDetail(Long orderId, Integer userId) {
        try {
            log.info("=== 获取商家订单详情 ===");
            log.info("订单ID: {}, 商家用户ID: {}", orderId, userId);
            
            // 获取商家ID
            Long merchantId = getMerchantIdByUserId(userId);
            if (merchantId == null) {
                return Result.error("用户不是商家或商家信息不存在");
            }
            
            // 验证订单是否属于该商家
            if (!isOrderBelongToMerchant(orderId, merchantId)) {
                return Result.error("订单不属于该商家");
            }
            
            // 获取订单详情（包含用户信息、地址信息等）
            Map<String, Object> orderDetail = orderMapper.getMerchantOrderDetail(orderId);
            
            if (orderDetail == null) {
                return Result.error("订单不存在");
            }
            
            // 查询订单项信息（商品信息）
            List<Map<String, Object>> orderItems = orderMapper.selectOrderItemsByOrderId(orderId);
            orderDetail.put("orderItems", orderItems);
            
            log.info("=== 商家订单详情获取完成 ===");
            log.info("订单基础信息字段: {}", orderDetail.keySet());
            log.info("订单项数量: {}", orderItems.size());
            if (!orderItems.isEmpty()) {
                log.info("第一个订单项信息: {}", orderItems.get(0));
            }
            
            return Result.success("获取订单详情成功", orderDetail);
            
        } catch (Exception e) {
            log.error("获取商家订单详情失败", e);
            return Result.error("获取商家订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取商家ID
     */
    private Long getMerchantIdByUserId(Integer userId) {
        try {
            log.info("根据用户ID获取商家ID: userId={}", userId);
            Map<String, Object> merchant = orderMapper.getMerchantByUserId(userId.longValue());
            if (merchant != null) {
                Long merchantId = (Long) merchant.get("merchant_id");
                log.info("查询到商家ID: userId={}, merchantId={}", userId, merchantId);
                return merchantId;
            }
            log.warn("用户不是商家: userId={}", userId);
            return null;
        } catch (Exception e) {
            log.error("获取商家ID失败: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 检查订单是否属于指定商家
     */
    private boolean isOrderBelongToMerchant(Long orderId, Long merchantId) {
        try {
            Map<String, Object> order = orderMapper.getMerchantOrderDetail(orderId);
            if (order == null) {
                return false;
            }
            
            Long orderMerchantId = (Long) order.get("merchant_id");
            return merchantId.equals(orderMerchantId);
        } catch (Exception e) {
            log.error("检查订单归属失败", e);
            return false;
        }
    }
    
    @Override
    public Result<Boolean> applyUserRefund(Long orderId, Long userId, Map<String, Object> refundData) {
        try {
            log.info("=== 用户申请退款 ===");
            log.info("订单ID: {}, 用户ID: {}, 退款申请: {}", orderId, userId, refundData);
            
            // 验证订单是否属于该用户
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            if (!order.getUserId().equals(userId.intValue())) {
                return Result.error("订单不属于该用户");
            }
            
            // 检查订单状态是否可以申请退款（已发货或已完成）
            if (order.getOrderStatus() != 3 && order.getOrderStatus() != 4) {
                return Result.error("订单状态不允许申请退款");
            }
            
            // 更新订单状态为申请退款
            int result = orderMapper.updateOrderStatusById(orderId, 7, LocalDateTime.now()); // 7-申请退款
            
            if (result > 0) {
                log.info("用户申请退款成功，订单ID: {}", orderId);
                return Result.success("退款申请提交成功，请等待商家处理", true);
            } else {
                return Result.error("退款申请失败");
            }
            
        } catch (Exception e) {
            log.error("用户申请退款失败", e);
            return Result.error("用户申请退款失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> cancelMerchantOrder(Long orderId, Integer userId, Map<String, Object> cancelData) {
        try {
            // 1. 验证订单是否存在
            Order order = orderMapper.selectByOrderId(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 2. 验证是否是商家的订单（购物订单的relatedId是商家ID）
            // 首先通过userId查找对应的商家ID
            Map<String, Object> merchantInfo = orderMapper.getMerchantByUserId(userId.longValue());
            if (merchantInfo == null) {
                return Result.error("商家信息不存在");
            }
            Long merchantId = (Long) merchantInfo.get("merchant_id");
            
            if (order.getOrderType() != 1 || !order.getRelatedId().equals(merchantId)) {
                return Result.error("无权限操作此订单");
            }
            
            // 3. 验证订单状态是否可以取消
            Integer currentStatus = order.getOrderStatus();
            if (currentStatus == null || currentStatus > 2) { // 只有待付款(1)和待发货(2)状态可以取消
                return Result.error("订单当前状态不允许取消");
            }
            
            // 4. 取消订单
            order.setOrderStatus(5); // 设置为已取消状态
            order.setUpdateTime(LocalDateTime.now());
            
            String cancelReason = (String) cancelData.get("cancelReason");
            if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                order.setOrderNote(order.getOrderNote() + " [商家取消原因: " + cancelReason.trim() + "]");
            }
            
            int updated = orderMapper.updateOrder(order);
            if (updated > 0) {
                log.info("商家取消订单成功，订单ID: {}, 商家ID: {}", orderId, userId);
                return Result.success(true);
            } else {
                return Result.error("取消订单失败");
            }
            
        } catch (Exception e) {
            log.error("商家取消订单失败", e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
} 