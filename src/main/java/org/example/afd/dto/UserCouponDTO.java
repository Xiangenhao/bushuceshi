package org.example.afd.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户优惠券数据传输对象
 */
public class UserCouponDTO implements Serializable {
    
    private Long userCouponId;
    private Long userId;
    private Long couponId;
    private Integer status; // 0-未使用，1-已使用，2-已过期
    private Date useTime;
    private Date receiveTime;
    private Long orderId; // 使用的订单ID
    private CouponDTO coupon; // 关联的优惠券信息
    
    public Long getUserCouponId() {
        return userCouponId;
    }
    
    public void setUserCouponId(Long userCouponId) {
        this.userCouponId = userCouponId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getCouponId() {
        return couponId;
    }
    
    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Date getUseTime() {
        return useTime;
    }
    
    public void setUseTime(Date useTime) {
        this.useTime = useTime;
    }
    
    public Date getReceiveTime() {
        return receiveTime;
    }
    
    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public CouponDTO getCoupon() {
        return coupon;
    }
    
    public void setCoupon(CouponDTO coupon) {
        this.coupon = coupon;
    }
    
    /**
     * 获取优惠券状态描述
     * @return 状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "";
        
        switch (status) {
            case 0: return "未使用";
            case 1: return "已使用";
            case 2: return "已过期";
            default: return "未知状态";
        }
    }
} 