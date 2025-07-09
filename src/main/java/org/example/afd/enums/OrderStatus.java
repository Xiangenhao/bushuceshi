package org.example.afd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举 - 统一订单状态管理
 * 
 * 解决生产环境订单状态混乱问题
 * 提供状态转换验证和业务逻辑
 * 
 * @author 系统架构师
 * @version 2.0 - 生产就绪版本
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    
    /**
     * 已删除 - 软删除状态
     */
    DELETED(0, "已删除", "订单已被删除"),
    
    /**
     * 待支付 - 订单创建后的初始状态
     */
    PENDING_PAYMENT(1, "待支付", "等待买家付款"),
    
    /**
     * 已支付 - 支付成功，等待发货
     */
    PAID(2, "已支付", "买家已付款，等待商家发货"),
    
    /**
     * 待发货 - 商家确认订单，准备发货
     */
    PENDING_SHIPMENT(3, "待发货", "商家正在准备发货"),
    
    /**
     * 已发货 - 商家已发货，等待买家确认收货
     */
    SHIPPED(4, "已发货", "商家已发货，等待买家确认收货"),
    
    /**
     * 已完成 - 交易成功完成
     */
    COMPLETED(5, "已完成", "交易成功完成"),
    
    /**
     * 已取消 - 订单被取消
     */
    CANCELLED(6, "已取消", "订单已取消"),
    
    /**
     * 退款中 - 退款申请处理中
     */
    REFUNDING(7, "退款中", "退款申请处理中"),
    
    /**
     * 已退款 - 退款已完成
     */
    REFUNDED(8, "已退款", "退款已完成");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态名称
     */
    private final String name;
    
    /**
     * 状态描述
     */
    private final String description;
    
    /**
     * 根据状态码获取枚举
     */
    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("订单状态码不能为空");
        }
        
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态: " + code);
    }
    
    /**
     * 检查状态转换是否合法
     * 
     * @param targetStatus 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        // 相同状态不需要转换
        if (this == targetStatus) {
            return true;
        }
        
        switch (this) {
            case PENDING_PAYMENT:
                // 待支付 -> 已支付、已取消
                return targetStatus == PAID || targetStatus == CANCELLED;
                
            case PAID:
                // 已支付 -> 待发货、退款中
                return targetStatus == PENDING_SHIPMENT || targetStatus == REFUNDING;
                
            case PENDING_SHIPMENT:
                // 待发货 -> 已发货、退款中
                return targetStatus == SHIPPED || targetStatus == REFUNDING;
                
            case SHIPPED:
                // 已发货 -> 已完成、退款中
                return targetStatus == COMPLETED || targetStatus == REFUNDING;
                
            case COMPLETED:
                // 已完成 -> 退款中（售后退款）
                return targetStatus == REFUNDING;
                
            case REFUNDING:
                // 退款中 -> 已退款、已完成（退款失败恢复）
                return targetStatus == REFUNDED || targetStatus == COMPLETED;
                
            case CANCELLED:
            case REFUNDED:
            case DELETED:
                // 终态，不允许转换
                return false;
                
            default:
                return false;
        }
    }
    
    /**
     * 获取状态的下一个可能状态列表
     */
    public OrderStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING_PAYMENT:
                return new OrderStatus[]{PAID, CANCELLED};
            case PAID:
                return new OrderStatus[]{PENDING_SHIPMENT, REFUNDING};
            case PENDING_SHIPMENT:
                return new OrderStatus[]{SHIPPED, REFUNDING};
            case SHIPPED:
                return new OrderStatus[]{COMPLETED, REFUNDING};
            case COMPLETED:
                return new OrderStatus[]{REFUNDING};
            case REFUNDING:
                return new OrderStatus[]{REFUNDED, COMPLETED};
            default:
                return new OrderStatus[]{};
        }
    }
    
    /**
     * 是否为终态（不可再转换的状态）
     */
    public boolean isFinalStatus() {
        return this == CANCELLED || this == REFUNDED || this == DELETED;
    }
    
    /**
     * 是否为可支付状态
     */
    public boolean isPayable() {
        return this == PENDING_PAYMENT;
    }
    
    /**
     * 是否为可发货状态
     */
    public boolean isShippable() {
        return this == PAID || this == PENDING_SHIPMENT;
    }
    
    /**
     * 是否为可取消状态
     */
    public boolean isCancellable() {
        return this == PENDING_PAYMENT;
    }
    
    /**
     * 是否为可确认收货状态
     */
    public boolean isConfirmable() {
        return this == SHIPPED;
    }
    
    /**
     * 是否为可申请退款状态
     */
    public boolean isRefundable() {
        return this == PAID || this == PENDING_SHIPMENT || 
               this == SHIPPED || this == COMPLETED;
    }
    
    /**
     * 是否为可删除状态（软删除）
     */
    public boolean isDeletable() {
        return this == COMPLETED || this == CANCELLED || this == REFUNDED;
    }
    
    @Override
    public String toString() {
        return String.format("OrderStatus{code=%d, name='%s'}", code, name);
    }
} 