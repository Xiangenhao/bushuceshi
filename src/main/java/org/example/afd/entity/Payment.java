package org.example.afd.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一支付实体类
 * 对应数据库表：payments
 */
public class Payment {
    
    /**
     * 支付ID
     */
    private Long paymentId;
    
    /**
     * 支付流水号
     */
    private String paymentNo;
    
    /**
     * 关联订单ID
     */
    private Long orderId;
    
    /**
     * 用户ID
     */
    private Integer userId;
    
    /**
     * 支付渠道ID (对应数据库channel_id字段)
     */
    private Integer channelId;
    
    /**
     * 支付方式：1-微信，2-支付宝，3-银行卡，4-余额
     * @deprecated 使用channelId替代
     */
    @Deprecated
    private Integer paymentMethod;
    
    /**
     * 支付渠道
     * @deprecated 使用channelId替代
     */
    @Deprecated
    private String paymentChannel;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付状态：1-待支付，2-支付成功，3-支付失败，4-已退款
     */
    private Integer paymentStatus;
    
    /**
     * 第三方订单号
     */
    private String thirdPartyOrderNo;
    
    /**
     * 第三方交易ID
     */
    private String thirdPartyTransactionId;
    
    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;
    
    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // Constructors
    public Payment() {}

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    @Deprecated
    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    @Deprecated
    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.channelId = paymentMethod; // 向后兼容
    }

    @Deprecated
    public String getPaymentChannel() {
        return paymentChannel;
    }

    @Deprecated
    public void setPaymentChannel(String paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getThirdPartyOrderNo() {
        return thirdPartyOrderNo;
    }

    public void setThirdPartyOrderNo(String thirdPartyOrderNo) {
        this.thirdPartyOrderNo = thirdPartyOrderNo;
    }

    public String getThirdPartyTransactionId() {
        return thirdPartyTransactionId;
    }

    public void setThirdPartyTransactionId(String thirdPartyTransactionId) {
        this.thirdPartyTransactionId = thirdPartyTransactionId;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public LocalDateTime getCallbackTime() {
        return callbackTime;
    }

    public void setCallbackTime(LocalDateTime callbackTime) {
        this.callbackTime = callbackTime;
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

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", paymentNo='" + paymentNo + '\'' +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", paymentMethod=" + paymentMethod +
                ", paymentAmount=" + paymentAmount +
                ", paymentStatus=" + paymentStatus +
                ", paymentTime=" + paymentTime +
                '}';
    }
} 