package org.example.afd.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 优惠券数据传输对象
 */
public class CouponDTO implements Serializable {
    
    private Long couponId;
    private String couponName;
    private String couponCode;
    private Integer type; // 优惠券类型：0-满减券，1-折扣券，2-无门槛券
    private Double amount; // 金额或折扣率
    private Double minAmount; // 最低消费金额
    private Long categoryId; // 适用分类ID，如果为null则全场通用
    private Long productId; // 适用商品ID，如果为null则不限制商品
    private Date startTime;
    private Date endTime;
    private Integer status; // 0-未启用，1-已启用，2-已过期
    private Integer totalCount; // 发行总量
    private Integer usedCount; // 已使用数量
    private Integer receiveCount; // 已领取数量
    
    public Long getCouponId() {
        return couponId;
    }
    
    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
    
    public String getCouponName() {
        return couponName;
    }
    
    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Double getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public Integer getUsedCount() {
        return usedCount;
    }
    
    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }
    
    public Integer getReceiveCount() {
        return receiveCount;
    }
    
    public void setReceiveCount(Integer receiveCount) {
        this.receiveCount = receiveCount;
    }
    
    /**
     * 获取优惠券状态描述
     * @return 状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "";
        
        switch (status) {
            case 0: return "未启用";
            case 1: return "已启用";
            case 2: return "已过期";
            default: return "未知状态";
        }
    }
    
    /**
     * 获取优惠券类型描述
     * @return 类型描述
     */
    public String getTypeDesc() {
        if (type == null) return "";
        
        switch (type) {
            case 0: return "满减券";
            case 1: return "折扣券";
            case 2: return "无门槛券";
            default: return "未知类型";
        }
    }
    
    /**
     * 获取优惠券金额描述
     * @return 金额描述
     */
    public String getAmountDesc() {
        if (amount == null) return "";
        
        if (type != null) {
            switch (type) {
                case 0: case 2: return amount + "元";
                case 1: return amount * 10 + "折";
                default: return amount + "";
            }
        }
        
        return amount + "";
    }
} 