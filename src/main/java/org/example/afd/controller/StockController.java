package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.StockLockItem;
import org.example.afd.dto.StockLockResult;
import org.example.afd.entity.StockOperationLog;
import org.example.afd.model.Result;
import org.example.afd.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 库存管理控制器
 * 提供库存锁定、释放、查询等功能
 */
@RestController
@RequestMapping("/api/v1/stock")
@Slf4j
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    /**
     * 锁定库存（创建订单时调用）
     * @param request 包含lockItems（库存锁定项列表）和orderNo（订单号）
     * @return 锁定结果
     */
    @PostMapping("/lock")
    public Result<StockLockResult> lockStock(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lockItemsData = (List<Map<String, Object>>) request.get("lockItems");
            String orderNo = (String) request.get("orderNo");
            
            if (lockItemsData == null || lockItemsData.isEmpty()) {
                return Result.error("库存锁定项列表不能为空");
            }
            
            if (orderNo == null || orderNo.trim().isEmpty()) {
                return Result.error("订单号不能为空");
            }
            
            // 转换为StockLockItem对象
            List<StockLockItem> lockItems = lockItemsData.stream()
                .map(data -> StockLockItem.builder()
                    .skuId(getLongValue(data, "skuId"))
                    .quantity(getIntegerValue(data, "quantity"))
                    .productName((String) data.get("productName"))
                    .build())
                .toList();
            
            log.info("库存锁定请求: orderNo={}, items={}, userId={}", 
                    orderNo, lockItems.size(), userId);
            
            StockLockResult result = stockService.lockStock(lockItems, orderNo);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("库存锁定失败", e);
            return Result.error("库存锁定失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认扣减库存（支付成功时调用）
     * @param orderNo 订单号
     * @return 是否成功
     */
    @PostMapping("/confirm/{orderNo}")
    public Result<Boolean> confirmStockDeduction(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("确认扣减库存请求: orderNo={}, userId={}", orderNo, userId);
            
            stockService.confirmStockDeduction(orderNo);
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("确认扣减库存失败", e);
            return Result.error("确认扣减库存失败: " + e.getMessage());
        }
    }
    
    /**
     * 释放库存锁定（订单取消时调用）
     * @param orderNo 订单号
     * @return 是否成功
     */
    @PostMapping("/release/{orderNo}")
    public Result<Boolean> releaseStockLock(
            @PathVariable String orderNo,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("释放库存锁定请求: orderNo={}, userId={}", orderNo, userId);
            
            stockService.releaseStockLock(orderNo);
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("释放库存锁定失败", e);
            return Result.error("释放库存锁定失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查库存可用性
     * @param request 包含checkItems（检查项列表）
     * @return 检查结果
     */
    @PostMapping("/check")
    public Result<Map<String, Object>> checkStockAvailability(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> checkItemsData = (List<Map<String, Object>>) request.get("checkItems");
            
            if (checkItemsData == null || checkItemsData.isEmpty()) {
                return Result.error("检查项列表不能为空");
            }
            
            // 转换为StockLockItem对象
            List<StockLockItem> checkItems = checkItemsData.stream()
                .map(data -> StockLockItem.builder()
                    .skuId(getLongValue(data, "skuId"))
                    .quantity(getIntegerValue(data, "quantity"))
                    .build())
                .toList();
            
            log.info("库存可用性检查请求: items={}, userId={}", checkItems.size(), userId);
            
            Map<String, Object> result = stockService.checkStockAvailability(checkItems);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("库存可用性检查失败", e);
            return Result.error("库存检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取库存操作日志
     * @param orderNo 订单号（可选）
     * @param skuId SKU ID（可选）
     * @param operationType 操作类型（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 操作日志列表
     */
    @GetMapping("/logs")
    public Result<List<StockOperationLog>> getStockOperationLogs(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long skuId,
            @RequestParam(required = false) Integer operationType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取库存操作日志请求: orderNo={}, skuId={}, operationType={}, page={}, size={}, userId={}", 
                    orderNo, skuId, operationType, page, size, userId);
            
            List<StockOperationLog> logs = stockService.getStockOperationLogs(
                orderNo, skuId, operationType, page, size);
            return Result.success(logs);
            
        } catch (Exception e) {
            log.error("获取库存操作日志失败", e);
            return Result.error("获取操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取SKU库存信息
     * @param skuId SKU ID
     * @return 库存信息
     */
    @GetMapping("/info/{skuId}")
    public Result<Map<String, Object>> getSkuStockInfo(
            @PathVariable Long skuId,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("获取SKU库存信息请求: skuId={}, userId={}", skuId, userId);
            
            Map<String, Object> stockInfo = stockService.getSkuStockInfo(skuId);
            return Result.success(stockInfo);
            
        } catch (Exception e) {
            log.error("获取SKU库存信息失败", e);
            return Result.error("获取库存信息失败: " + e.getMessage());
        }
    }
    
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
} 