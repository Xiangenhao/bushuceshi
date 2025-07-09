package org.example.afd.service;

import org.example.afd.dto.CouponDTO;
import org.example.afd.dto.UserCouponDTO;

import java.util.List;
import java.util.Map;

/**
 * 优惠券服务接口
 */
public interface CouponService {
    
    /**
     * 获取可用的优惠券列表
     * @param userId 用户ID
     * @param categoryId 商品类别ID
     * @param productId 商品ID
     * @param minAmount 最小订单金额
     * @return 可用优惠券列表
     */
    List<CouponDTO> getAvailableCoupons(Long userId, Long categoryId, Long productId, Double minAmount);
    
    /**
     * 获取用户的优惠券列表
     * @param userId 用户ID
     * @param status 优惠券状态（0-未使用，1-已使用，2-已过期，null-全部）
     * @return 用户优惠券列表
     */
    List<UserCouponDTO> getUserCoupons(Long userId, Integer status);
    
    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 操作结果
     */
    Map<String, Object> receiveCoupon(Long userId, Long couponId);
    
    /**
     * 根据优惠券ID获取优惠券信息
     * @param couponId 优惠券ID
     * @return 优惠券信息
     */
    CouponDTO getCoupon(Long couponId);
    
    /**
     * 用户使用优惠券
     * @param userId 用户ID
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    Map<String, Object> useCoupon(Long userId, Long userCouponId, Long orderId);
    
    /**
     * 返还优惠券（订单取消时）
     * @param userId 用户ID
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    Map<String, Object> returnCoupon(Long userId, Long userCouponId, Long orderId);
    
    /**
     * 获取用户可以使用的优惠券数量
     * @param userId 用户ID
     * @return 可用优惠券数量
     */
    int getAvailableCouponCount(Long userId);
} 