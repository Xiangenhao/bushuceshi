package org.example.afd.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付数据传输对象
 * 
 * 用于前后端支付相关数据传输
 * 
 * @author AFD Team
 * @version 2.0
 */
public class PaymentDTO {
    
    /**
     * 支付ID
     */
    private Long paymentId;
    
    /**
     * 支付流水号
     */
    private String paymentNo;
    
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
     * 支付方式：1-微信，2-支付宝，3-银行卡，4-余额
     */
    private Integer paymentMethod;
    
    /**
     * 支付方式名称
     */
    private String paymentMethodName;
    
    /**
     * 支付渠道
     */
    private String paymentChannel;
    
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    
    /**
     * 支付状态：1-待支付，2-支付中，3-支付成功，4-支付失败，5-已取消，6-已退款
     */
    private Integer paymentStatus;
    
    /**
     * 支付状态名称
     */
    private String paymentStatusName;
    
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
    
    /**
     * 订单相关信息（可选）
     */
    private String orderTitle;
    private String orderDescription;
    private BigDecimal orderAmount;
    
    // 构造函数
    public PaymentDTO() {}
    
    public PaymentDTO(Long paymentId, String paymentNo) {
        this.paymentId = paymentId;
        this.paymentNo = paymentNo;
    }
    
    // Getter 和 Setter 方法
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
    
    public Integer getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentMethodName() {
        return paymentMethodName;
    }
    
    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }
    
    public String getPaymentChannel() {
        return paymentChannel;
    }
    
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
    
    public String getPaymentStatusName() {
        return paymentStatusName;
    }
    
    public void setPaymentStatusName(String paymentStatusName) {
        this.paymentStatusName = paymentStatusName;
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
    
    public String getOrderTitle() {
        return orderTitle;
    }
    
    public void setOrderTitle(String orderTitle) {
        this.orderTitle = orderTitle;
    }
    
    public String getOrderDescription() {
        return orderDescription;
    }
    
    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }
    
    public BigDecimal getOrderAmount() {
        return orderAmount;
    }
    
    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }
    
    @Override
    public String toString() {
        return "PaymentDTO{" +
                "paymentId=" + paymentId +
                ", paymentNo='" + paymentNo + '\'' +
                ", orderId=" + orderId +
                ", orderNo='" + orderNo + '\'' +
                ", userId=" + userId +
                ", orderType=" + orderType +
                ", paymentMethod=" + paymentMethod +
                ", paymentMethodName='" + paymentMethodName + '\'' +
                ", paymentChannel='" + paymentChannel + '\'' +
                ", paymentAmount=" + paymentAmount +
                ", paymentStatus=" + paymentStatus +
                ", paymentStatusName='" + paymentStatusName + '\'' +
                ", paymentTime=" + paymentTime +
                ", createTime=" + createTime +
                '}';
    }
} 