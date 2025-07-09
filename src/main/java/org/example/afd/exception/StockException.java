package org.example.afd.exception;

/**
 * 库存异常类
 * 当库存操作失败时抛出此异常
 */
public class StockException extends RuntimeException {
    private final String skuId;
    private final Integer requiredQuantity;
    private final Integer availableQuantity;
    
    public StockException(String skuId, Integer required, Integer available) {
        super(String.format("库存不足: SKU=%s, 需要=%d, 可用=%d", skuId, required, available));
        this.skuId = skuId;
        this.requiredQuantity = required;
        this.availableQuantity = available;
    }
    
    public StockException(String message) {
        super(message);
        this.skuId = null;
        this.requiredQuantity = null;
        this.availableQuantity = null;
    }
    
    public StockException(String message, Throwable cause) {
        super(message, cause);
        this.skuId = null;
        this.requiredQuantity = null;
        this.availableQuantity = null;
    }
    
    public String getSkuId() {
        return skuId;
    }
    
    public Integer getRequiredQuantity() {
        return requiredQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
} 