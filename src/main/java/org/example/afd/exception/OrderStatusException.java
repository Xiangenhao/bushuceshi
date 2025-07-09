package org.example.afd.exception;

import org.example.afd.enums.OrderStatus;

/**
 * 订单状态异常类
 * 当订单状态转换失败时抛出此异常
 */
public class OrderStatusException extends RuntimeException {
    private final String orderNo;
    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;
    
    public OrderStatusException(String orderNo, OrderStatus current, OrderStatus target) {
        super(String.format("订单状态转换异常: orderNo=%s, %s -> %s", 
                           orderNo, current.getName(), target.getName()));
        this.orderNo = orderNo;
        this.currentStatus = current;
        this.targetStatus = target;
    }
    
    public OrderStatusException(String message) {
        super(message);
        this.orderNo = null;
        this.currentStatus = null;
        this.targetStatus = null;
    }
    
    public OrderStatusException(String message, Throwable cause) {
        super(message, cause);
        this.orderNo = null;
        this.currentStatus = null;
        this.targetStatus = null;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
} 