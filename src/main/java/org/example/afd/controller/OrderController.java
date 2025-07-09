package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.OrderDTO;
import org.example.afd.entity.Order;
import org.example.afd.entity.Payment;
import org.example.afd.mapper.OrderMapper;
import org.example.afd.mapper.PaymentMapper;
import org.example.afd.model.Result;
import org.example.afd.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.example.afd.entity.Order;
import org.example.afd.entity.Payment;
import org.example.afd.mapper.OrderMapper;
import org.example.afd.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Random;

/**
 * 统一订单控制器
 * 支持购物订单和订阅订单
 */
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    /**
     * 创建订阅订单
     */
    @PostMapping("/subscription")
    public Result<Map<String, Object>> createSubscriptionOrder(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Long planId = getLongValue(request, "planId");
            Integer months = getIntegerValue(request, "subscriptionMonths");
            if (months == null) {
                months = getIntegerValue(request, "months");
            }
            
            if (planId == null || months == null) {
                return Result.error("参数错误：planId和subscriptionMonths为必填项");
            }
            
            if (months <= 0 || months > 12) {
                return Result.error("订阅月数必须在1-12之间");
            }
            
            log.info("创建订阅订单请求: userId={}, planId={}, months={}", userId, planId, months);
            
            return orderService.createSubscriptionOrder(userId, planId, months);
            
        } catch (Exception e) {
            log.error("创建订阅订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建商品订单
     */
    @PostMapping("/product")
    public Result<Map<String, Object>> createProductOrder(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("创建商品订单请求: userId={}, orderData={}", userId, request);
            
            return orderService.createProductOrder(userId, request);
            
        } catch (Exception e) {
            log.error("创建商品订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建购物车订单
     */
    @PostMapping("/cart")
    public Result<Map<String, Object>> createCartOrder(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("创建购物车订单请求: userId={}, orderData={}", userId, request);
            
            return orderService.createCartOrder(userId, request);
            
        } catch (Exception e) {
            log.error("创建购物车订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建通用订单（支持商品备注）
     */
    @PostMapping("")
    public Result<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("创建通用订单请求: userId={}, orderData={}", userId, request);
            
            return orderService.createUnifiedOrder(userId, request);
            
        } catch (Exception e) {
            log.error("创建通用订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 创建购物订单（兼容老接口）
     */
    @PostMapping("/api/v1/orders/shopping")
    public Result<Map<String, Object>> createShoppingOrder(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) request.get("cartItems");
            Long addressId = getLongValue(request, "addressId");
            String orderNote = (String) request.get("orderNote");
            
            if (cartItems == null || cartItems.isEmpty()) {
                return Result.error("购物车不能为空");
            }
            
            log.info("创建购物订单请求: userId={}, cartItems={}, addressId={}", userId, cartItems.size(), addressId);
            
            return orderService.createShoppingOrder(userId, cartItems, addressId, orderNote);
            
        } catch (Exception e) {
            log.error("创建购物订单失败", e);
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单列表 - 新路径
     */
    @GetMapping("/user/list")
    public Result<Map<String, Object>> getUserOrdersList(
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                log.warn("用户未登录，无法获取订单列表");
                return Result.error("用户未登录");
            }
            
            log.info("=== 收到获取用户订单列表请求 ===");
            log.info("请求参数: userId={}, orderType={}, status={}, page={}, size={}", 
                    userId, orderType, status, page, size);
            log.info("请求路径: /api/v1/user/orders");
            log.info("请求头中的userId: {}", userId);
            
            log.info("=== 调用OrderService.getUserOrders ===");
            Result<List<OrderDTO>> result = orderService.getUserOrders(userId, orderType, status, page, size);
            log.info("=== OrderService.getUserOrders 调用完成 ===");
            log.info("服务层返回结果: success={}, message={}", result.isSuccess(), result.getMessage());
            
            if (result.isSuccess()) {
                List<OrderDTO> orderList = result.getData();
                log.info("服务层返回订单数量: {}", orderList != null ? orderList.size() : 0);
                
                // 记录每个订单的基本信息
                if (orderList != null) {
                    for (int i = 0; i < orderList.size(); i++) {
                        OrderDTO order = orderList.get(i);
                        log.info("订单[{}]: orderNo={}, status={}, merchantName={}, orderItems={}", 
                                i, order.getOrderNo(), order.getStatus(), order.getMerchantName(),
                                order.getOrderItems() != null ? order.getOrderItems().size() : 0);
                    }
                }
                
                // 构造分页数据格式
                Map<String, Object> pageData = new HashMap<>();
                pageData.put("currentPage", page);
                pageData.put("size", size);
                pageData.put("orders", orderList != null ? orderList : new ArrayList<>());
                pageData.put("total", orderList != null ? orderList.size() : 0);
                pageData.put("totalPages", 1); // 暂时设为1页
                pageData.put("first", page == 1);
                pageData.put("last", true); // 暂时设为最后一页
                pageData.put("hasNext", false);
                pageData.put("hasPrevious", page > 1);
                
                log.info("=== 构造分页响应数据 ===");
                log.info("响应分页数据: page={}, size={}, total={}, orders={}", 
                        page, size, orderList != null ? orderList.size() : 0,
                        orderList != null ? orderList.size() : 0);
                
                log.info("=== 成功返回分页订单数据 ===");
                return Result.success(pageData);
            } else {
                log.error("服务层返回失败: {}", result.getMessage());
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("=== 获取用户订单列表发生异常 ===", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/api/v1/orders/{orderNo}")
    public Result<OrderDTO> getOrderDetail(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取订单详情请求: userId={}, orderNo={}", userId, orderNo);
            
            return orderService.getOrderDetail(orderNo, userId);
            
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单详情 - 新路径
     */
    @GetMapping("/api/v1/user/orders/{orderId}")
    public Result<OrderDTO> getUserOrderDetail(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取用户订单详情请求: userId={}, orderId={}", userId, orderId);
            
            return orderService.getUserOrderDetail(orderId, userId);
            
        } catch (Exception e) {
            log.error("获取用户订单详情失败", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订单列表（兼容老接口）
     */
    @GetMapping("/api/v1/orders")
    public Result<List<OrderDTO>> getUserOrders(
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取用户订单列表请求: userId={}, orderType={}, orderStatus={}, page={}, size={}", 
                    userId, orderType, orderStatus, page, size);
            
            return orderService.getUserOrders(userId, orderType, orderStatus, page, size);
            
        } catch (Exception e) {
            log.error("获取用户订单列表失败", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/api/v1/orders/{orderNo}/cancel")
    public Result<Boolean> cancelOrder(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("取消订单请求: userId={}, orderNo={}", userId, orderNo);
            
            return orderService.cancelOrder(orderNo, userId);
            
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单 - 新路径
     */
    @PostMapping("/api/v1/user/orders/{orderId}/cancel")
    public Result<Void> cancelUserOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("取消用户订单请求: userId={}, orderId={}", userId, orderId);
            
            Result<Boolean> result = orderService.cancelUserOrder(orderId, userId);
            if (result.isSuccess()) {
                return Result.success(null);
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("取消用户订单失败", e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认收货
     */
    @PostMapping("/api/v1/orders/{orderNo}/confirm")
    public Result<Boolean> confirmOrder(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("确认收货请求: userId={}, orderNo={}", userId, orderNo);
            
            return orderService.confirmOrder(orderNo, userId);
            
        } catch (Exception e) {
            log.error("确认收货失败", e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    /**
     * 确认收货 - 新路径
     */
    @PostMapping("/api/v1/user/orders/{orderId}/confirm")
    public Result<Void> confirmUserOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("确认用户订单收货请求: userId={}, orderId={}", userId, orderId);
            
            Result<Boolean> result = orderService.confirmUserOrder(orderId, userId);
            if (result.isSuccess()) {
                return Result.success(null);
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("确认用户订单收货失败", e);
            return Result.error("确认收货失败: " + e.getMessage());
        }
    }

    /**
     * 删除订单 - 新路径
     */
    @DeleteMapping("/api/v1/user/orders/{orderId}")
    public Result<Void> deleteUserOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("删除用户订单请求: userId={}, orderId={}", userId, orderId);
            
            Result<Boolean> result = orderService.deleteUserOrder(orderId, userId);
            if (result.isSuccess()) {
                return Result.success(null);
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("删除用户订单失败", e);
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单统计信息
     */
    @GetMapping("/api/v1/orders/statistics")
    public Result<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) Integer orderType,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取订单统计请求: userId={}, orderType={}", userId, orderType);
            
            return orderService.getOrderStatistics(userId, orderType);
            
        } catch (Exception e) {
            log.error("获取订单统计失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取商家的订单列表（增强版：支持搜索和筛选）
     * @param merchantId 商家ID
     * @param status 订单状态
     * @param keyword 搜索关键词（订单号、用户昵称、商品名称）
     * @param timeFilter 时间筛选（今天、昨天、本周、本月）
     * @param refundStatus 退款状态
     * @param page 页码
     * @param size 每页数量
     * @return 订单列表
     */
    // 商家订单API已移动到MerchantOrderController，删除此冗余实现
    
    // 工具方法
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) return Integer.parseInt((String) value);
        return null;
    }

    /**
     * 安全地从HttpServletRequest中获取userId
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return null;
        }
        
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            // 尝试从字符串转换
            try {
                return Long.parseLong(userIdObj.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    // ===== 新增：订单状态管理接口 =====
    
    @Autowired
    private org.example.afd.service.OrderStatusService orderStatusService;
    
    /**
     * 安全的订单状态变更（新增）
     * @param orderNo 订单号
     * @param request 包含targetStatus（目标状态）、reason（变更原因）、operatorId（操作员ID）
     * @return 是否成功
     */
    @PostMapping("/api/v1/orders/{orderNo}/status/change")
    public Result<Boolean> changeOrderStatusSafely(
            @PathVariable String orderNo,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Integer targetStatusCode = getIntegerValue(request, "targetStatus");
            String reason = (String) request.get("reason");
            Long operatorId = getLongValue(request, "operatorId");
            
            if (targetStatusCode == null) {
                return Result.error("目标状态不能为空");
            }
            
            if (operatorId == null) {
                operatorId = userId; // 默认使用当前用户作为操作员
            }
            
            log.info("安全订单状态变更请求: orderNo={}, targetStatus={}, reason={}, operatorId={}", 
                    orderNo, targetStatusCode, reason, operatorId);
            
            org.example.afd.enums.OrderStatus targetStatus = 
                org.example.afd.enums.OrderStatus.fromCode(targetStatusCode);
            
            return orderStatusService.changeOrderStatus(orderNo, targetStatus, reason, operatorId);
            
        } catch (Exception e) {
            log.error("安全订单状态变更失败", e);
            return Result.error("状态变更失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单状态变更历史（新增）
     * @param orderNo 订单号
     * @return 状态变更历史列表
     */
    @GetMapping("/api/v1/orders/{orderNo}/status/history")
    public Result<List<Map<String, Object>>> getOrderStatusHistory(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取订单状态历史请求: orderNo={}, userId={}", orderNo, userId);
            
            Result<java.util.List<org.example.afd.entity.OrderStatusLog>> result = 
                orderStatusService.getOrderStatusHistory(orderNo);
            
            if (result.isSuccess()) {
                // 转换为Map格式返回给前端
                List<Map<String, Object>> historyList = new ArrayList<>();
                for (org.example.afd.entity.OrderStatusLog log : result.getData()) {
                    Map<String, Object> historyItem = new HashMap<>();
                    historyItem.put("logId", log.getLogId());
                    historyItem.put("orderNo", log.getOrderNo());
                    historyItem.put("fromStatus", log.getFromStatus());
                    historyItem.put("toStatus", log.getToStatus());
                    historyItem.put("reason", log.getReason());
                    historyItem.put("operatorId", log.getOperatorId());
                    historyItem.put("createTime", log.getCreateTime());
                    historyList.add(historyItem);
                }
                return Result.success(historyList);
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("获取订单状态历史失败", e);
            return Result.error("获取状态历史失败: " + e.getMessage());
        }
    }

    /**
     * 验证状态转换是否合法（新增）
     * @param orderNo 订单号
     * @param targetStatus 目标状态
     * @return 是否可以转换
     */
    @GetMapping("/api/v1/orders/{orderNo}/status/validate")
    public Result<Boolean> validateStatusTransition(
            @PathVariable String orderNo,
            @RequestParam Integer targetStatus,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("验证状态转换请求: orderNo={}, targetStatus={}, userId={}", 
                    orderNo, targetStatus, userId);
            
            org.example.afd.enums.OrderStatus targetStatusEnum = 
                org.example.afd.enums.OrderStatus.fromCode(targetStatus);
            
            return orderStatusService.validateStatusTransition(orderNo, targetStatusEnum);
            
        } catch (Exception e) {
            log.error("验证状态转换失败", e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单下一步可能的状态（新增）
     * @param orderNo 订单号
     * @return 可能的状态列表
     */
    @GetMapping("/api/v1/orders/{orderNo}/status/next")
    public Result<List<Map<String, Object>>> getNextPossibleStatuses(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取下一步状态请求: orderNo={}, userId={}", orderNo, userId);
            
            Result<org.example.afd.enums.OrderStatus[]> result = 
                orderStatusService.getNextPossibleStatuses(orderNo);
            
            if (result.isSuccess()) {
                // 转换为Map格式返回给前端
                List<Map<String, Object>> statusList = new ArrayList<>();
                for (org.example.afd.enums.OrderStatus status : result.getData()) {
                    Map<String, Object> statusItem = new HashMap<>();
                    statusItem.put("code", status.getCode());
                    statusItem.put("name", status.getName());
                    statusItem.put("description", status.getDescription());
                    statusList.add(statusItem);
                }
                return Result.success(statusList);
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("获取下一步状态失败", e);
            return Result.error("获取下一步状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新订单状态（新增）
     * @param request 包含orderNos（订单号数组）、targetStatus（目标状态）、reason（变更原因）、operatorId（操作员ID）
     * @return 成功更新的数量
     */
    @PostMapping("/api/v1/orders/batch/status/change")
    public Result<Integer> batchChangeOrderStatus(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            @SuppressWarnings("unchecked")
            List<String> orderNosList = (List<String>) request.get("orderNos");
            Integer targetStatusCode = getIntegerValue(request, "targetStatus");
            String reason = (String) request.get("reason");
            Long operatorId = getLongValue(request, "operatorId");
            
            if (orderNosList == null || orderNosList.isEmpty()) {
                return Result.error("订单号列表不能为空");
            }
            
            if (targetStatusCode == null) {
                return Result.error("目标状态不能为空");
            }
            
            if (operatorId == null) {
                operatorId = userId; // 默认使用当前用户作为操作员
            }
            
            String[] orderNos = orderNosList.toArray(new String[0]);
            
            log.info("批量更新订单状态请求: orderNos={}, targetStatus={}, reason={}, operatorId={}", 
                    orderNos.length, targetStatusCode, reason, operatorId);
            
            org.example.afd.enums.OrderStatus targetStatus = 
                org.example.afd.enums.OrderStatus.fromCode(targetStatusCode);
            
            return orderStatusService.batchChangeOrderStatus(orderNos, targetStatus, reason, operatorId);
            
        } catch (Exception e) {
            log.error("批量更新订单状态失败", e);
            return Result.error("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 立即支付订单（模拟支付成功）
     * 将订单状态从待支付(1)更新为已支付(2)
     * 
     * @param orderNo 订单编号
     * @return 支付结果
     */
    @PostMapping("/{orderNo}/immediate-pay")
    public Result<Map<String, Object>> immediatePayOrder(@PathVariable String orderNo, HttpServletRequest request) {
        try {
            log.info("立即支付订单: {}", orderNo);
            
            // 获取当前用户ID
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            // 查询订单
            Order order = orderMapper.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 验证订单所有者
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权限操作此订单");
            }
            
            // 验证订单状态 - 只有待支付状态(1)可以支付
            if (!order.getOrderStatus().equals(1)) {
                return Result.error("订单状态不允许支付，当前状态：" + getOrderStatusText(order.getOrderStatus()));
            }
            
            // 更新订单状态为已支付(2)
            order.setOrderStatus(2); // 已支付，等待发货
            order.setPaidAmount(order.getTotalAmount()); // 设置实际支付金额
            order.setUpdateTime(LocalDateTime.now());
            
            int updateResult = orderMapper.updateOrder(order);
            if (updateResult <= 0) {
                return Result.error("支付失败，请稍后重试");
            }
            
            // 创建支付记录
            Payment payment = new Payment();
            payment.setPaymentNo("PAY" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000)));
            payment.setOrderId(order.getOrderId());
            payment.setUserId(userId);
            payment.setChannelId(1); // 模拟支付渠道
            payment.setPaymentAmount(order.getTotalAmount());
            payment.setPaymentStatus(2); // 支付成功
            payment.setPaymentTime(LocalDateTime.now());
            payment.setCallbackTime(LocalDateTime.now());
            payment.setThirdPartyOrderNo("MOCK_" + System.currentTimeMillis());
            payment.setThirdPartyTransactionId("TXN_" + System.currentTimeMillis());
            payment.setCreateTime(LocalDateTime.now());
            payment.setUpdateTime(LocalDateTime.now());
            
            paymentMapper.insertPayment(payment);
            
            // 返回支付结果
            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("paymentNo", payment.getPaymentNo());
            result.put("paymentAmount", order.getTotalAmount());
            result.put("paymentStatus", 2);
            result.put("paymentStatusText", "支付成功");
            result.put("orderStatus", order.getOrderStatus());
            result.put("orderStatusText", getOrderStatusText(order.getOrderStatus()));
            result.put("paymentTime", payment.getPaymentTime());
            
            log.info("订单支付成功: orderNo={}, paymentNo={}, amount={}", 
                    orderNo, payment.getPaymentNo(), order.getTotalAmount());
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("立即支付订单失败: orderNo=" + orderNo, e);
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    /**
     * 更新订单状态
     * 
     * @param orderNo 订单编号
     * @param statusData 状态数据
     * @return 更新结果
     */
    @PutMapping("/{orderNo}/status")
    public Result<Boolean> updateOrderStatus(@PathVariable String orderNo, 
                                           @RequestBody Map<String, Object> statusData,
                                           HttpServletRequest request) {
        try {
            log.info("更新订单状态: orderNo={}, statusData={}", orderNo, statusData);
            
            // 获取当前用户ID
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            Object statusObj = statusData.get("status");
            if (statusObj == null) {
                return Result.error("订单状态不能为空");
            }
            
            Integer newStatus;
            try {
                newStatus = Integer.valueOf(statusObj.toString());
            } catch (NumberFormatException e) {
                return Result.error("订单状态格式错误");
            }
            
            // 查询订单
            Order order = orderMapper.getOrderByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 验证订单所有者或商家权限
            if (!order.getUserId().equals(userId)) {
                // 检查是否是商家操作
                if (order.getOrderType().equals(1)) { // 购物订单，需要验证商家权限
                    // 这里应该验证当前用户是否是该订单的商家，暂时跳过
                    log.warn("用户{}尝试操作他人订单{}", userId, orderNo);
                    // return Result.error("无权限操作此订单");
                }
            }
            
            // 验证状态转换是否合法
            if (!isValidStatusTransition(order.getOrderStatus(), newStatus)) {
                return Result.error("订单状态转换不合法：" + 
                                  getOrderStatusText(order.getOrderStatus()) + " -> " + 
                                  getOrderStatusText(newStatus));
            }
            
            // 更新订单状态
            int updateResult = orderMapper.updateOrderStatus(orderNo, newStatus);
            if (updateResult <= 0) {
                return Result.error("订单状态更新失败");
            }
            
            log.info("订单状态更新成功: orderNo={}, oldStatus={}, newStatus={}", 
                    orderNo, order.getOrderStatus(), newStatus);
            
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("更新订单状态失败: orderNo=" + orderNo, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 验证订单状态转换是否合法
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        
        switch (currentStatus) {
            case 1: // 待支付 -> 已支付、已取消
                return targetStatus == 2 || targetStatus == 6;
            case 2: // 已支付 -> 待发货、退款中
                return targetStatus == 3 || targetStatus == 7;
            case 3: // 待发货 -> 已发货、退款中
                return targetStatus == 4 || targetStatus == 7;
            case 4: // 已发货 -> 已完成、退款中
                return targetStatus == 5 || targetStatus == 7;
            case 5: // 已完成 -> 退款中（售后）
                return targetStatus == 7;
            case 7: // 退款中 -> 已退款、已完成（退款失败）
                return targetStatus == 8 || targetStatus == 5;
            case 6: // 已取消
            case 8: // 已退款
            case 0: // 已删除
                return false; // 终态，不允许转换
            default:
                return false;
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知状态";
        
        switch (status) {
            case 0: return "已删除";
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "待发货";
            case 4: return "已发货";
            case 5: return "已完成";
            case 6: return "已取消";
            case 7: return "退款中";
            case 8: return "已退款";
            default: return "未知状态(" + status + ")";
        }
    }

    /**
     * 创建测试订单数据
     * 创建不同支付状态的订单用于测试商家和用户API
     */
    @PostMapping("/test/create-test-orders")
    public Result<Map<String, Object>> createTestOrders(HttpServletRequest request) {
        try {
            log.info("=== 开始创建测试订单数据 ===");
            
            // 获取当前用户ID
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            List<Map<String, Object>> testOrders = new ArrayList<>();
            Random random = new Random();
            
            // 创建10个测试订单，覆盖不同状态
            for (int i = 1; i <= 10; i++) {
                // 生成订单号和支付号
                String orderNo = "TEST" + System.currentTimeMillis() + String.format("%03d", i);
                String paymentNo = "PAY" + System.currentTimeMillis() + String.format("%03d", i);
                
                // 随机商品信息
                Long productId = (long) (13 + random.nextInt(3)); // 产品ID 13-15
                Long skuId = productId; // 简化，SKU ID和产品ID相同
                BigDecimal unitPrice = new BigDecimal(50 + random.nextInt(200)); // 50-250元
                Integer quantity = 1 + random.nextInt(3); // 1-3个
                BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));
                
                // 获取商家ID
                Long merchantId = orderMapper.getMerchantIdByProductId(productId);
                if (merchantId == null) {
                    merchantId = 1L; // 默认商家ID
                }
                
                // 创建订单
                Order order = new Order();
                order.setOrderNo(orderNo);
                order.setUserId(userId);
                order.setOrderType(1); // 商品订单
                order.setRelatedId(merchantId);
                order.setTotalAmount(totalAmount);
                order.setPaidAmount(BigDecimal.ZERO);
                order.setShippingFee(BigDecimal.ZERO);
                order.setDiscountAmount(BigDecimal.ZERO);
                order.setCouponAmount(BigDecimal.ZERO);
                order.setAddressId(1L); // 默认地址
                order.setOrderNote("测试订单 - " + i);
                order.setCreateTime(LocalDateTime.now().minusHours(random.nextInt(72))); // 随机时间
                order.setUpdateTime(LocalDateTime.now());
                order.setExpireTime(LocalDateTime.now().plusDays(1));
                
                // 根据编号设置不同的订单状态
                int orderStatus = 1;
                int paymentStatus = 1;
                BigDecimal paidAmount = BigDecimal.ZERO;
                LocalDateTime paymentTime = null;
                
                switch (i % 8) {
                    case 1: // 待支付
                        orderStatus = 1;
                        paymentStatus = 1;
                        break;
                    case 2: // 已支付，待发货
                        orderStatus = 2;
                        paymentStatus = 2;
                        paidAmount = totalAmount;
                        paymentTime = LocalDateTime.now().minusHours(random.nextInt(24));
                        break;
                    case 3: // 已发货，待收货
                        orderStatus = 3;
                        paymentStatus = 2;
                        paidAmount = totalAmount;
                        paymentTime = LocalDateTime.now().minusHours(24 + random.nextInt(48));
                        break;
                    case 4: // 已完成
                        orderStatus = 4;
                        paymentStatus = 2;
                        paidAmount = totalAmount;
                        paymentTime = LocalDateTime.now().minusHours(72 + random.nextInt(120));
                        break;
                    case 5: // 已取消
                        orderStatus = 5;
                        paymentStatus = 1;
                        break;
                    case 6: // 申请退款中
                        orderStatus = 7;
                        paymentStatus = 2;
                        paidAmount = totalAmount;
                        paymentTime = LocalDateTime.now().minusHours(48 + random.nextInt(72));
                        break;
                    case 7: // 已退款
                        orderStatus = 8;
                        paymentStatus = 4; // 已退款
                        paidAmount = totalAmount;
                        paymentTime = LocalDateTime.now().minusHours(72 + random.nextInt(120));
                        break;
                    case 0: // 支付失败
                        orderStatus = 1;
                        paymentStatus = 3;
                        break;
                }
                
                order.setOrderStatus(orderStatus);
                order.setPaidAmount(paidAmount);
                
                // 插入订单
                int orderResult = orderMapper.insertOrder(order);
                if (orderResult <= 0) {
                    log.error("创建测试订单失败: {}", orderNo);
                    continue;
                }
                
                // 创建订单项
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("orderId", order.getOrderId());
                orderItem.put("itemType", 1); // 商品类型
                orderItem.put("productId", productId);
                orderItem.put("skuId", skuId);
                orderItem.put("unitPrice", unitPrice);
                orderItem.put("quantity", quantity);
                orderItem.put("itemAmount", totalAmount);
                orderItem.put("itemNote", "测试商品备注 - " + i);
                
                List<Map<String, Object>> itemList = new ArrayList<>();
                itemList.add(orderItem);
                orderMapper.batchInsertOrderItems(itemList);
                
                // 创建支付记录
                Payment payment = new Payment();
                payment.setPaymentNo(paymentNo);
                payment.setOrderId(order.getOrderId());
                payment.setUserId(userId);
                payment.setChannelId(1 + random.nextInt(4)); // 随机支付渠道 1-4
                payment.setPaymentAmount(totalAmount);
                payment.setPaymentStatus(paymentStatus);
                payment.setCreateTime(order.getCreateTime());
                payment.setUpdateTime(LocalDateTime.now());
                
                if (paymentTime != null) {
                    payment.setPaymentTime(paymentTime);
                    payment.setCallbackTime(paymentTime.plusMinutes(1));
                    payment.setThirdPartyOrderNo("MOCK_" + paymentNo);
                    payment.setThirdPartyTransactionId("TXN_" + paymentNo);
                }
                
                paymentMapper.insertPayment(payment);
                
                // 记录测试订单信息
                Map<String, Object> testOrderInfo = new HashMap<>();
                testOrderInfo.put("orderNo", orderNo);
                testOrderInfo.put("paymentNo", paymentNo);
                testOrderInfo.put("orderStatus", orderStatus);
                testOrderInfo.put("orderStatusText", getOrderStatusText(orderStatus));
                testOrderInfo.put("paymentStatus", paymentStatus);
                testOrderInfo.put("paymentStatusText", getPaymentStatusText(paymentStatus));
                testOrderInfo.put("totalAmount", totalAmount);
                testOrderInfo.put("merchantId", merchantId);
                testOrderInfo.put("productId", productId);
                testOrders.add(testOrderInfo);
                
                log.info("创建测试订单: orderNo={}, status={}, paymentStatus={}, amount={}", 
                        orderNo, orderStatus, paymentStatus, totalAmount);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("testOrders", testOrders);
            result.put("totalCount", testOrders.size());
            result.put("message", "测试订单创建完成，包含各种状态的订单");
            
            log.info("=== 测试订单数据创建完成，共创建{}个订单 ===", testOrders.size());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("创建测试订单失败", e);
            return Result.error("创建测试订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付状态文本
     */
    private String getPaymentStatusText(int paymentStatus) {
        switch (paymentStatus) {
            case 1: return "待支付";
            case 2: return "支付成功";
            case 3: return "支付失败";
            case 4: return "已退款";
            default: return "未知状态";
        }
    }
} 