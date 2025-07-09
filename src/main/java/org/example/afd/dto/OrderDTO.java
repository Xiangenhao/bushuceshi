package org.example.afd.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 统一订单数据传输对象
 * 支持购物订单和订阅订单
 */
public class OrderDTO implements Serializable {
    
    private Long orderId;
    private Long userId;
    private String orderNo;
    private Integer orderType; // 1-购物订单，2-订阅订单
    private Integer orderStatus; // 1-待付款，2-待发货，3-待收货，4-已完成，5-已取消，6-退款中，7-已退款
    
    // 金额相关
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private Double payAmount; // 兼容旧版本
    private Double freightAmount; // 兼容旧版本
    
    // 支付相关
    private Integer payStatus; // 0-未支付，1-已支付
    private Integer payType; // 1-支付宝，2-微信，3-银联
    private Date payTime;
    
    // 时间相关
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Date createTimeOld; // 兼容旧版本
    private Date updateTimeOld; // 兼容旧版本
    
    // 收货地址相关（购物订单）
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String receiverZip;
    
    // 订单备注
    private String note;
    private String orderNote;
    
    // 状态相关
    private Integer status; // 兼容旧版本
    private Integer deleteStatus; // 0-未删除，1-已删除
    
    // 订阅相关字段
    private Integer subscriptionMonths; // 订阅月数
    private String planTitle; // 订阅计划标题
    private String planCoverUrl; // 订阅计划封面
    private Long creatorId; // 创作者ID
    private String creatorName; // 创作者名称
    
    // 购物订单相关
    private List<OrderItemDTO> orderItems;
    
    // 商家相关信息
    private Long merchantId;
    private String merchantName;
    private String merchantLogo;
    
    // 物流相关信息
    private String logisticsInfo; // 物流信息JSON字符串
    
    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Integer getOrderType() {
        return orderType;
    }
    
    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
    
    public Integer getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        // 兼容旧版本
        this.setTotalAmountDouble(totalAmount != null ? totalAmount.doubleValue() : null);
    }
    
    public BigDecimal getPaidAmount() {
        return paidAmount;
    }
    
    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
    
    // 兼容旧版本的方法
    public Double getTotalAmountDouble() {
        return totalAmount != null ? totalAmount.doubleValue() : null;
    }
    
    public void setTotalAmountDouble(Double totalAmount) {
        this.totalAmount = totalAmount != null ? new BigDecimal(totalAmount.toString()) : null;
    }
    
    public Double getPayAmount() {
        return payAmount;
    }
    
    public void setPayAmount(Double payAmount) {
        this.payAmount = payAmount;
    }
    
    public Double getFreightAmount() {
        return freightAmount;
    }
    
    public void setFreightAmount(Double freightAmount) {
        this.freightAmount = freightAmount;
    }
    
    public Integer getStatus() {
        return status != null ? status : orderStatus;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
        this.orderStatus = status;
    }
    
    public Integer getPayStatus() {
        return payStatus;
    }
    
    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }
    
    public Integer getPayType() {
        return payType;
    }
    
    public void setPayType(Integer payType) {
        this.payType = payType;
    }
    
    public Date getPayTime() {
        return payTime;
    }
    
    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        // 兼容旧版本
        if (createTime != null) {
            this.createTimeOld = java.sql.Timestamp.valueOf(createTime);
        }
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        // 兼容旧版本
        if (updateTime != null) {
            this.updateTimeOld = java.sql.Timestamp.valueOf(updateTime);
        }
    }
    
    // 兼容旧版本的方法
    public Date getCreateTimeOld() {
        return createTimeOld;
    }
    
    public void setCreateTimeOld(Date createTime) {
        this.createTimeOld = createTime;
    }
    
    public Date getUpdateTimeOld() {
        return updateTimeOld;
    }
    
    public void setUpdateTimeOld(Date updateTime) {
        this.updateTimeOld = updateTime;
    }
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    
    public String getReceiverPhone() {
        return receiverPhone;
    }
    
    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }
    
    public String getReceiverProvince() {
        return receiverProvince;
    }
    
    public void setReceiverProvince(String receiverProvince) {
        this.receiverProvince = receiverProvince;
    }
    
    public String getReceiverCity() {
        return receiverCity;
    }
    
    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }
    
    public String getReceiverDistrict() {
        return receiverDistrict;
    }
    
    public void setReceiverDistrict(String receiverDistrict) {
        this.receiverDistrict = receiverDistrict;
    }
    
    public String getReceiverAddress() {
        return receiverAddress;
    }
    
    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }
    
    public String getReceiverZip() {
        return receiverZip;
    }
    
    public void setReceiverZip(String receiverZip) {
        this.receiverZip = receiverZip;
    }
    
    public String getNote() {
        return note != null ? note : orderNote;
    }
    
    public void setNote(String note) {
        this.note = note;
        this.orderNote = note;
    }
    
    public String getOrderNote() {
        return orderNote;
    }
    
    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }
    
    public Integer getDeleteStatus() {
        return deleteStatus;
    }
    
    public void setDeleteStatus(Integer deleteStatus) {
        this.deleteStatus = deleteStatus;
    }
    
    public Integer getSubscriptionMonths() {
        return subscriptionMonths;
    }
    
    public void setSubscriptionMonths(Integer subscriptionMonths) {
        this.subscriptionMonths = subscriptionMonths;
    }
    
    public String getPlanTitle() {
        return planTitle;
    }
    
    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }
    
    public String getPlanCoverUrl() {
        return planCoverUrl;
    }
    
    public void setPlanCoverUrl(String planCoverUrl) {
        this.planCoverUrl = planCoverUrl;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public String getMerchantLogo() {
        return merchantLogo;
    }
    
    public void setMerchantLogo(String merchantLogo) {
        this.merchantLogo = merchantLogo;
    }
    
    public String getLogisticsInfo() {
        return logisticsInfo;
    }
    
    public void setLogisticsInfo(String logisticsInfo) {
        this.logisticsInfo = logisticsInfo;
    }
    
    /**
     * 获取订单状态描述
     */
    public String getStatusDesc() {
        if (orderStatus == null) return "未知状态";
        
        switch (orderStatus) {
            case 1: return "待付款";
            case 2: return "待发货";
            case 3: return "待收货";
            case 4: return "已完成";
            case 5: return "已取消";
            case 6: return "退款中";
            case 7: return "已退款";
            default: return "未知状态";
        }
    }
    
    /**
     * 获取订单类型描述
     */
    public String getOrderTypeDesc() {
        if (orderType == null) return "未知类型";
        
        switch (orderType) {
            case 1: return "购物订单";
            case 2: return "订阅订单";
            default: return "未知类型";
        }
    }
} 