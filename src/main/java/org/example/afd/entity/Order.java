package org.example.afd.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一订单实体类
 * 对应数据库表：orders
 */
public class Order {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 订单类型：1-购物订单，2-订阅订单
     */
    private Integer orderType;
    
    /**
     * 相关ID：购物订单为商家ID，订阅订单为计划ID
     */
    private Long relatedId;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 运费
     */
    private BigDecimal shippingFee;
    
    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 优惠券金额
     */
    private BigDecimal couponAmount;
    
    /**
     * 订单状态：1-待付款，2-待发货，3-待收货，4-已完成，5-已取消，6-退款中，7-已退款
     */
    private Integer orderStatus;
    
    /**
     * 订单备注
     */
    private String orderNote;
    
    /**
     * 收货地址ID
     */
    private Long addressId;
    
    /**
     * 物流信息
     */
    private String logisticsInfo;
    
    /**
     * 订阅月数（仅订阅订单）
     */
    private Integer subscriptionMonths;
    
    /**
     * 订阅开始时间（仅订阅订单）
     */
    private LocalDateTime subscriptionStartTime;
    
    /**
     * 订阅结束时间（仅订阅订单）
     */
    private LocalDateTime subscriptionEndTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    // Constructors
    public Order() {}

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(BigDecimal couponAmount) {
        this.couponAmount = couponAmount;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getLogisticsInfo() {
        return logisticsInfo;
    }

    public void setLogisticsInfo(String logisticsInfo) {
        this.logisticsInfo = logisticsInfo;
    }

    public Integer getSubscriptionMonths() {
        return subscriptionMonths;
    }

    public void setSubscriptionMonths(Integer subscriptionMonths) {
        this.subscriptionMonths = subscriptionMonths;
    }

    public LocalDateTime getSubscriptionStartTime() {
        return subscriptionStartTime;
    }

    public void setSubscriptionStartTime(LocalDateTime subscriptionStartTime) {
        this.subscriptionStartTime = subscriptionStartTime;
    }

    public LocalDateTime getSubscriptionEndTime() {
        return subscriptionEndTime;
    }

    public void setSubscriptionEndTime(LocalDateTime subscriptionEndTime) {
        this.subscriptionEndTime = subscriptionEndTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", orderType=" + orderType +
                ", relatedId=" + relatedId +
                ", totalAmount=" + totalAmount +
                ", paidAmount=" + paidAmount +
                ", shippingFee=" + shippingFee +
                ", discountAmount=" + discountAmount +
                ", couponAmount=" + couponAmount +
                ", orderStatus=" + orderStatus +
                ", orderNote='" + orderNote + '\'' +
                ", addressId=" + addressId +
                ", logisticsInfo='" + logisticsInfo + '\'' +
                ", subscriptionMonths=" + subscriptionMonths +
                ", subscriptionStartTime=" + subscriptionStartTime +
                ", subscriptionEndTime=" + subscriptionEndTime +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", expireTime=" + expireTime +
                '}';
    }
} 