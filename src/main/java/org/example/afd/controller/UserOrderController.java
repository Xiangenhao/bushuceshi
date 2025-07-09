package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.OrderDTO;
import org.example.afd.model.Result;
import org.example.afd.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户订单控制器
 * 处理用户端的订单相关操作
 */
@RestController
@RequestMapping("/api/v1/user/orders")
@Slf4j
public class UserOrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("")
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
            
            Result<List<OrderDTO>> result = orderService.getUserOrders(userId, orderType, status, page, size);
            log.info("=== OrderService.getUserOrders 调用完成 ===");
            log.info("服务层返回结果: success={}, message={}", result.isSuccess(), result.getMessage());
            
            if (result.isSuccess()) {
                List<OrderDTO> orderList = result.getData();
                log.info("服务层返回订单数量: {}", orderList != null ? orderList.size() : 0);
                
                // 构造返回数据
                Map<String, Object> responseData = new java.util.HashMap<>();
                responseData.put("orders", orderList);
                responseData.put("totalCount", orderList != null ? orderList.size() : 0);
                responseData.put("page", page);
                responseData.put("size", size);
                
                return Result.success("获取订单列表成功", responseData);
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("获取用户订单列表失败", e);
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户订单详情
     */
    @GetMapping("/{orderId}")
    public Result<OrderDTO> getUserOrderDetail(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("=== 收到获取用户订单详情请求 ===");
            log.info("请求路径: GET /api/v1/user/orders/{}", orderId);
            log.info("请求参数: orderId={}", orderId);
            
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                log.warn("用户未登录，拒绝获取订单详情");
                return Result.error("用户未登录");
            }
            
            log.info("用户认证通过: userId={}", userId);
            log.info("调用 OrderService.getUserOrderDetail");
            
            Result<OrderDTO> result = orderService.getUserOrderDetail(orderId, userId);
            
            log.info("=== OrderService.getUserOrderDetail 调用完成 ===");
            log.info("服务层返回结果: success={}, message={}", result.isSuccess(), result.getMessage());
            
            if (result.isSuccess()) {
                OrderDTO orderDTO = result.getData();
                if (orderDTO != null) {
                    log.info("返回订单详情: orderNo={}, status={}, totalAmount={}", 
                            orderDTO.getOrderNo(), orderDTO.getStatus(), orderDTO.getTotalAmount());
                    log.info("商家信息: merchantName={}, merchantLogo={}", 
                            orderDTO.getMerchantName(), orderDTO.getMerchantLogo());
                    log.info("订单项数量: {}", orderDTO.getOrderItems() != null ? orderDTO.getOrderItems().size() : 0);
                    log.info("收货地址: {} {}", orderDTO.getReceiverName(), orderDTO.getReceiverPhone());
                } else {
                    log.warn("服务层返回的订单数据为null");
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("=== 获取用户订单详情异常 ===", e);
            log.error("异常类型: {}", e.getClass().getSimpleName());
            log.error("异常消息: {}", e.getMessage());
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消用户订单
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelUserOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户取消订单: userId={}, orderId={}", userId, orderId);
            
            Result<Boolean> result = orderService.cancelUserOrder(orderId, userId);
            if (result.isSuccess()) {
                return Result.success("订单取消成功");
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("取消用户订单失败", e);
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户确认收货
     */
    @PostMapping("/{orderId}/confirm")
    public Result<Boolean> confirmOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            
            log.info("=== 用户确认收货 ===");
            log.info("用户ID: {}, 订单ID: {}", userIdLong, orderId);
            
            return orderService.confirmUserOrder(orderId, userIdLong);
            
        } catch (Exception e) {
            log.error("用户确认收货失败", e);
            return Result.error("用户确认收货失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户订单
     */
    @DeleteMapping("/{orderId}")
    public Result<Void> deleteUserOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("用户删除订单: userId={}, orderId={}", userId, orderId);
            
            Result<Boolean> result = orderService.deleteUserOrder(orderId, userId);
            if (result.isSuccess()) {
                return Result.success("订单删除成功");
            } else {
                return Result.error(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("删除用户订单失败", e);
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户申请退款
     */
    @PostMapping("/{orderId}/refund/apply")
    public Result<Boolean> applyRefund(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> refundData,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            
            log.info("=== 用户申请退款 ===");
            log.info("用户ID: {}, 订单ID: {}, 退款申请: {}", userIdLong, orderId, refundData);
            
            return orderService.applyUserRefund(orderId, userIdLong, refundData);
            
        } catch (Exception e) {
            log.error("用户申请退款失败", e);
            return Result.error("用户申请退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null) {
                return Long.valueOf(userIdHeader);
            }
            
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                if (userIdAttr instanceof Long) {
                    return (Long) userIdAttr;
                } else if (userIdAttr instanceof Integer) {
                    return ((Integer) userIdAttr).longValue();
                } else if (userIdAttr instanceof String) {
                    return Long.valueOf((String) userIdAttr);
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }
} 