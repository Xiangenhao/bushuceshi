package org.example.afd.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存锁定项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLockItem {
    
    /**
     * SKU ID
     */
    private Long skuId;
    
    /**
     * 锁定数量
     */
    private Integer quantity;
    
    /**
     * 商品名称（用于日志记录）
     */
    private String productName;
    
    /**
     * SKU规格（用于日志记录）
     */
    private String skuSpec;
} 