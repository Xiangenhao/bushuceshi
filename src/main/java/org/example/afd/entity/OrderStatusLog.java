package org.example.afd.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志实体
 * 对应数据库表：order_status_log
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {
    
    /**
     * 日志ID
     */
    private Long logId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 原状态
     */
    private Integer fromStatus;
    
    /**
     * 目标状态
     */
    private Integer toStatus;
    
    /**
     * 变更原因
     */
    private String reason;
    
    /**
     * 操作员ID
     */
    private Long operatorId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 