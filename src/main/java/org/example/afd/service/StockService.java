package org.example.afd.service;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.StockLockItem;
import org.example.afd.dto.StockLockResult;
import org.example.afd.entity.StockOperationLog;
import org.example.afd.exception.StockException;
import org.example.afd.mapper.ProductMapper;
import org.example.afd.mapper.StockOperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理服务
 * 
 * 核心功能：
 * 1. 分布式锁保证并发安全（Redis可用时）
 * 2. 数据库锁降级方案（Redis不可用时）
 * 3. 乐观锁防止库存超卖
 * 4. 库存锁定机制
 * 5. 完整的操作日志记录
 * 6. 支持库存回滚
 */
@Service
@Slf4j
public class StockService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private StockOperationLogMapper stockLogMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String STOCK_LOCK_PREFIX = "stock_lock:";
    private static final int LOCK_TIMEOUT = 30; // 30秒超时
    
    /**
     * 锁定库存（创建订单时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public StockLockResult lockStock(List<StockLockItem> lockItems, String orderNo) {
        log.info("开始锁定库存，订单号: {}, 商品数量: {}, Redis可用: {}", 
            orderNo, lockItems.size(), redisTemplate != null);
        
        List<StockLockItem> successItems = new ArrayList<>();
        List<StockLockItem> failedItems = new ArrayList<>();
        
        for (StockLockItem item : lockItems) {
            boolean lockAcquired = false;
            String lockKey = STOCK_LOCK_PREFIX + item.getSkuId();
            
            try {
                // 尝试获取锁（Redis可用时使用分布式锁，否则使用数据库锁）
                if (redisTemplate != null) {
                    // Redis分布式锁
                    Boolean lockResult = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, orderNo, Duration.ofSeconds(LOCK_TIMEOUT));
                    lockAcquired = lockResult != null && lockResult;
                } else {
                    // 降级到数据库锁（通过SELECT FOR UPDATE实现）
                    lockAcquired = true; // 数据库锁在查询时获取
                    log.debug("Redis不可用，使用数据库锁: skuId={}", item.getSkuId());
                }
                
                if (!lockAcquired) {
                    log.warn("获取库存锁失败: skuId={}", item.getSkuId());
                    failedItems.add(item);
                    continue;
                }
                
                // 查询当前库存（使用悲观锁）
                Map<String, Object> currentSku = productMapper.selectSkuForUpdate(item.getSkuId());
                if (currentSku == null) {
                    log.error("SKU不存在: {}", item.getSkuId());
                    failedItems.add(item);
                    continue;
                }
                
                Integer stock = getIntegerValue(currentSku.get("stock"));
                Integer lockStock = getIntegerValue(currentSku.get("lock_stock"));
                Integer version = getIntegerValue(currentSku.get("version"));
                
                // 检查可用库存
                int availableStock = stock - lockStock;
                if (availableStock < item.getQuantity()) {
                    log.warn("库存不足: skuId={}, 需要={}, 可用={}", 
                        item.getSkuId(), item.getQuantity(), availableStock);
                    failedItems.add(item);
                    continue;
                }
                
                // 乐观锁更新库存
                int updateResult = productMapper.lockStockWithVersion(
                    item.getSkuId(), 
                    item.getQuantity(), 
                    version
                );
                
                if (updateResult > 0) {
                    // 记录库存操作日志
                    stockLogMapper.insertLog(StockOperationLog.builder()
                        .skuId(item.getSkuId())
                        .operationType(1) // 锁定
                        .quantity(item.getQuantity())
                        .beforeStock(stock)
                        .afterStock(stock)
                        .beforeLockStock(lockStock)
                        .afterLockStock(lockStock + item.getQuantity())
                        .orderNo(orderNo)
                        .build());
                    
                    successItems.add(item);
                    log.info("库存锁定成功: skuId={}, quantity={}", 
                        item.getSkuId(), item.getQuantity());
                } else {
                    log.warn("库存锁定失败-版本冲突: skuId={}", item.getSkuId());
                    failedItems.add(item);
                }
                
            } finally {
                // 释放Redis锁（如果使用了Redis锁）
                if (redisTemplate != null && lockAcquired) {
                    try {
                        redisTemplate.delete(lockKey);
                    } catch (Exception e) {
                        log.warn("释放Redis锁失败: lockKey={}, error={}", lockKey, e.getMessage());
                    }
                }
            }
        }
        
        // 如果有失败的，回滚所有成功的锁定
        if (!failedItems.isEmpty()) {
            log.warn("部分库存锁定失败，回滚所有锁定: 成功={}, 失败={}", 
                successItems.size(), failedItems.size());
            rollbackStockLock(successItems, orderNo);
            return StockLockResult.failure(failedItems);
        }
        
        return StockLockResult.success(successItems);
    }
    
    /**
     * 确认扣减库存（支付成功时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmStockDeduction(String orderNo) {
        log.info("确认扣减库存，订单号: {}", orderNo);
        
        List<StockOperationLog> lockLogs = stockLogMapper.selectByOrderNoAndType(orderNo, 1);
        
        for (StockOperationLog lockLog : lockLogs) {
            int result = productMapper.confirmStockDeduction(
                lockLog.getSkuId(), 
                lockLog.getQuantity()
            );
            
            if (result > 0) {
                // 记录扣减日志
                stockLogMapper.insertLog(StockOperationLog.builder()
                    .skuId(lockLog.getSkuId())
                    .operationType(2) // 扣减
                    .quantity(lockLog.getQuantity())
                    .orderNo(orderNo)
                    .build());
                
                log.info("库存扣减成功: skuId={}, quantity={}", 
                    lockLog.getSkuId(), lockLog.getQuantity());
            } else {
                log.error("库存扣减失败: skuId={}, quantity={}", 
                    lockLog.getSkuId(), lockLog.getQuantity());
                throw new StockException("库存扣减失败: " + lockLog.getSkuId());
            }
        }
    }
    
    /**
     * 释放库存锁定（订单取消时调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseStockLock(String orderNo) {
        log.info("释放库存锁定，订单号: {}", orderNo);
        
        List<StockOperationLog> lockLogs = stockLogMapper.selectByOrderNoAndType(orderNo, 1);
        
        for (StockOperationLog lockLog : lockLogs) {
            int result = productMapper.releaseStockLock(lockLog.getSkuId(), lockLog.getQuantity());
            
            if (result > 0) {
                // 记录释放日志
                stockLogMapper.insertLog(StockOperationLog.builder()
                    .skuId(lockLog.getSkuId())
                    .operationType(3) // 释放
                    .quantity(lockLog.getQuantity())
                    .orderNo(orderNo)
                    .build());
                
                log.info("库存锁定释放成功: skuId={}, quantity={}", 
                    lockLog.getSkuId(), lockLog.getQuantity());
            } else {
                log.error("库存锁定释放失败: skuId={}, quantity={}", 
                    lockLog.getSkuId(), lockLog.getQuantity());
            }
        }
    }
    
    /**
     * 回滚库存锁定（内部使用）
     */
    private void rollbackStockLock(List<StockLockItem> items, String orderNo) {
        for (StockLockItem item : items) {
            try {
                productMapper.releaseStockLock(item.getSkuId(), item.getQuantity());
                
                // 记录回滚日志
                stockLogMapper.insertLog(StockOperationLog.builder()
                    .skuId(item.getSkuId())
                    .operationType(4) // 回滚
                    .quantity(item.getQuantity())
                    .orderNo(orderNo)
                    .build());
                
                log.info("库存锁定回滚成功: skuId={}, quantity={}", 
                    item.getSkuId(), item.getQuantity());
            } catch (Exception e) {
                log.error("库存锁定回滚失败: skuId={}, quantity={}", 
                    item.getSkuId(), item.getQuantity(), e);
            }
        }
    }
    
    /**
     * 检查库存是否充足
     */
    public boolean checkStockAvailable(Long skuId, Integer quantity) {
        Map<String, Object> sku = productMapper.selectSkuById(skuId);
        if (sku == null) {
            return false;
        }
        
        Integer stock = getIntegerValue(sku.get("stock"));
        Integer lockStock = getIntegerValue(sku.get("lock_stock"));
        int availableStock = stock - lockStock;
        
        return availableStock >= quantity;
    }
    
    /**
     * 获取SKU可用库存
     */
    public int getAvailableStock(Long skuId) {
        Map<String, Object> sku = productMapper.selectSkuById(skuId);
        if (sku == null) {
            return 0;
        }
        
        Integer stock = getIntegerValue(sku.get("stock"));
        Integer lockStock = getIntegerValue(sku.get("lock_stock"));
        return stock - lockStock;
    }
    
    /**
     * 获取库存操作历史
     */
    public List<StockOperationLog> getStockOperationHistory(String orderNo) {
        return stockLogMapper.selectByOrderNo(orderNo);
    }
    
    /**
     * 获取SKU库存操作历史
     */
    public List<StockOperationLog> getSkuOperationHistory(Long skuId, Integer limit) {
        return stockLogMapper.selectRecentBySkuId(skuId, limit);
    }
    
    /**
     * 安全地将Object转换为Integer
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法转换为Integer: {}", value);
            return 0;
        }
    }

    /**
     * 检查库存可用性（批量）
     */
    public Map<String, Object> checkStockAvailability(List<StockLockItem> checkItems) {
        log.info("检查库存可用性: items={}", checkItems.size());
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> availableItems = new ArrayList<>();
        List<Map<String, Object>> unavailableItems = new ArrayList<>();
        boolean allAvailable = true;
        
        for (StockLockItem item : checkItems) {
            try {
                Map<String, Object> sku = productMapper.selectSkuById(item.getSkuId());
                if (sku == null) {
                    Map<String, Object> unavailableItem = new HashMap<>();
                    unavailableItem.put("skuId", item.getSkuId());
                    unavailableItem.put("requestQuantity", item.getQuantity());
                    unavailableItem.put("reason", "商品不存在");
                    unavailableItems.add(unavailableItem);
                    allAvailable = false;
                    continue;
                }
                
                Integer stock = getIntegerValue(sku.get("stock"));
                Integer lockStock = getIntegerValue(sku.get("lock_stock"));
                int availableStock = stock - lockStock;
                
                Map<String, Object> stockInfo = new HashMap<>();
                stockInfo.put("skuId", item.getSkuId());
                stockInfo.put("requestQuantity", item.getQuantity());
                stockInfo.put("totalStock", stock);
                stockInfo.put("lockStock", lockStock);
                stockInfo.put("availableStock", availableStock);
                
                if (availableStock >= item.getQuantity()) {
                    stockInfo.put("status", "available");
                    availableItems.add(stockInfo);
                } else {
                    stockInfo.put("status", "insufficient");
                    stockInfo.put("reason", "库存不足");
                    unavailableItems.add(stockInfo);
                    allAvailable = false;
                }
                
            } catch (Exception e) {
                log.error("检查SKU库存失败: skuId={}", item.getSkuId(), e);
                Map<String, Object> errorItem = new HashMap<>();
                errorItem.put("skuId", item.getSkuId());
                errorItem.put("requestQuantity", item.getQuantity());
                errorItem.put("reason", "系统错误: " + e.getMessage());
                unavailableItems.add(errorItem);
                allAvailable = false;
            }
        }
        
        result.put("allAvailable", allAvailable);
        result.put("availableItems", availableItems);
        result.put("unavailableItems", unavailableItems);
        result.put("totalItems", checkItems.size());
        result.put("availableCount", availableItems.size());
        result.put("unavailableCount", unavailableItems.size());
        
        return result;
    }

    /**
     * 获取库存操作日志（分页）
     */
    public List<StockOperationLog> getStockOperationLogs(String orderNo, Long skuId, 
                                                       Integer operationType, Integer page, Integer size) {
        log.info("获取库存操作日志: orderNo={}, skuId={}, operationType={}, page={}, size={}", 
                orderNo, skuId, operationType, page, size);
        
        try {
            // 计算偏移量
            int offset = (page - 1) * size;
            
            return stockLogMapper.selectByConditions(orderNo, skuId, operationType, offset, size);
            
        } catch (Exception e) {
            log.error("获取库存操作日志失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取SKU库存信息
     */
    public Map<String, Object> getSkuStockInfo(Long skuId) {
        log.info("获取SKU库存信息: skuId={}", skuId);
        
        try {
            Map<String, Object> sku = productMapper.selectSkuById(skuId);
            if (sku == null) {
                throw new RuntimeException("SKU不存在: " + skuId);
            }
            
            Integer stock = getIntegerValue(sku.get("stock"));
            Integer lockStock = getIntegerValue(sku.get("lock_stock"));
            
            Map<String, Object> stockInfo = new HashMap<>();
            stockInfo.put("skuId", skuId);
            stockInfo.put("totalStock", stock);
            stockInfo.put("lockStock", lockStock);
            stockInfo.put("availableStock", stock - lockStock);
            stockInfo.put("version", sku.get("version"));
            stockInfo.put("updateTime", sku.get("update_time"));
            
            // 获取最近的操作日志
            List<StockOperationLog> recentLogs = stockLogMapper.selectRecentBySkuId(skuId, 5);
            stockInfo.put("recentOperations", recentLogs);
            
            return stockInfo;
            
        } catch (Exception e) {
            log.error("获取SKU库存信息失败: skuId={}", skuId, e);
            throw new RuntimeException("获取库存信息失败: " + e.getMessage());
        }
    }
} 