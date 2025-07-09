package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.model.Result;
import org.example.afd.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 商家订单管理控制器
 * 处理商家端的订单相关操作
 */
@RestController
@RequestMapping("/api/v1/merchant/orders")
@Slf4j
public class MerchantOrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 获取商家订单列表
     */
    @GetMapping("")
    public Result<Map<String, Object>> getMerchantOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer orderType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            log.info("=== 商家获取订单列表 ===");
            log.info("用户ID: {}, 状态: {}, 订单类型: {}, 页码: {}, 每页数量: {}", 
                    userId, status, orderType, page, size);
            
            // 调用订单服务获取商家订单
            return orderService.getMerchantOrders(userId, status, orderType, page, size);
            
        } catch (Exception e) {
            log.error("获取商家订单列表失败", e);
            return Result.error("获取商家订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 商家处理订单 - 发货
     */
    @PostMapping("/{orderId}/ship")
    public Result<Boolean> shipOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> shipmentData,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            log.info("=== 商家发货 ===");
            log.info("用户ID: {}, 订单ID: {}, 发货信息: {}", userId, orderId, shipmentData);
            
            return orderService.shipOrder(orderId, userId, shipmentData);
            
        } catch (Exception e) {
            log.error("商家发货失败", e);
            return Result.error("商家发货失败: " + e.getMessage());
        }
    }
    
    /**
     * 商家取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public Result<Boolean> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> cancelData,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            log.info("=== 商家取消订单 ===");
            log.info("用户ID: {}, 订单ID: {}, 取消原因: {}", userId, orderId, cancelData);
            
            return orderService.cancelMerchantOrder(orderId, userId, cancelData);
            
        } catch (Exception e) {
            log.error("商家取消订单失败", e);
            return Result.error("商家取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 商家处理退款申请
     */
    @PostMapping("/{orderId}/refund/process")
    public Result<Boolean> processRefund(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> refundData,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            log.info("=== 商家处理退款 ===");
            log.info("用户ID: {}, 订单ID: {}, 退款处理: {}", userId, orderId, refundData);
            
            return orderService.processMerchantRefund(orderId, userId, refundData);
            
        } catch (Exception e) {
            log.error("商家处理退款失败", e);
            return Result.error("商家处理退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public Result<Map<String, Object>> getOrderDetail(
            @PathVariable Long orderId,
            HttpServletRequest request) {
        try {
            Long userIdLong = getUserIdFromRequest(request);
            if (userIdLong == null) {
                return Result.error("用户未登录");
            }
            Integer userId = userIdLong.intValue();
            
            log.info("=== 商家获取订单详情 ===");
            log.info("用户ID: {}, 订单ID: {}", userId, orderId);
            
            return orderService.getMerchantOrderDetail(orderId, userId);
            
        } catch (Exception e) {
            log.error("获取订单详情失败", e);
            return Result.error("获取订单详情失败: " + e.getMessage());
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
            
            // 从JWT令牌中获取用户ID的逻辑
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // 简单解析JWT获取用户ID（实际项目中应该使用JWT库）
                try {
                    // 解码JWT payload部分
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
                        // 简单提取userId字段
                        if (payload.contains("\"userId\":")) {
                            String userIdStr = payload.substring(payload.indexOf("\"userId\":") + 9);
                            userIdStr = userIdStr.substring(0, userIdStr.indexOf(","));
                            return Long.valueOf(userIdStr);
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析JWT失败: {}", e.getMessage());
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }
} 