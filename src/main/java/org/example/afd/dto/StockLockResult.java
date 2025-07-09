package org.example.afd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 库存锁定结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLockResult {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 成功锁定的项目
     */
    private List<StockLockItem> successItems;
    
    /**
     * 失败的项目
     */
    private List<StockLockItem> failedItems;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建成功结果
     */
    public static StockLockResult success(List<StockLockItem> successItems) {
        return StockLockResult.builder()
            .success(true)
            .successItems(successItems)
            .build();
    }
    
    /**
     * 创建失败结果
     */
    public static StockLockResult failure(List<StockLockItem> failedItems) {
        return StockLockResult.builder()
            .success(false)
            .failedItems(failedItems)
            .errorMessage("部分商品库存不足")
            .build();
    }
    
    /**
     * 创建失败结果（带错误信息）
     */
    public static StockLockResult failure(String errorMessage) {
        return StockLockResult.builder()
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }
} 