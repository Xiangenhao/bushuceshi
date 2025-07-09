package org.example.afd.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 库存操作日志实体
 * 对应数据库表：stock_operation_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOperationLog {
    
    /**
     * 日志ID
     */
    private Long logId;
    
    /**
     * SKU ID
     */
    private Long skuId;
    
    /**
     * 操作类型：1-锁定 2-扣减 3-释放 4-回滚
     */
    private Integer operationType;
    
    /**
     * 操作数量
     */
    private Integer quantity;
    
    /**
     * 操作前库存
     */
    private Integer beforeStock;
    
    /**
     * 操作后库存
     */
    private Integer afterStock;
    
    /**
     * 操作前锁定库存
     */
    private Integer beforeLockStock;
    
    /**
     * 操作后锁定库存
     */
    private Integer afterLockStock;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 操作员ID
     */
    private Long operatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 操作类型枚举
     */
    public enum OperationType {
        LOCK(1, "锁定"),
        DEDUCT(2, "扣减"),
        RELEASE(3, "释放"),
        ROLLBACK(4, "回滚");
        
        private final Integer code;
        private final String name;
        
        OperationType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public Integer getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
    }
} 