package org.example.afd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 优惠券数据访问接口
 */
@Mapper
public interface CouponMapper {
    
    /**
     * 获取所有有效的优惠券
     * @return 优惠券列表
     */
    List<Map<String, Object>> selectAllValidCoupons();
    
    /**
     * 根据ID获取优惠券信息
     * @param couponId 优惠券ID
     * @return 优惠券信息
     */
    Map<String, Object> selectCouponById(Long couponId);
    
    /**
     * 获取用户的优惠券列表
     * @param userId 用户ID
     * @param status 优惠券状态（null表示全部）
     * @return 优惠券列表
     */
    List<Map<String, Object>> selectUserCoupons(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 获取可用于特定商品类别的优惠券
     * @param categoryId 商品类别ID
     * @return 优惠券列表
     */
    List<Map<String, Object>> selectCouponsByCategory(Long categoryId);
    
    /**
     * 获取可用于特定商品的优惠券
     * @param productId 商品ID
     * @return 优惠券列表
     */
    List<Map<String, Object>> selectCouponsByProduct(Long productId);
    
    /**
     * 获取通用优惠券（不限商品和类别）
     * @return 优惠券列表
     */
    List<Map<String, Object>> selectGeneralCoupons();
    
    /**
     * 检查用户是否已领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 已领取的数量
     */
    int countUserCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
    
    /**
     * 用户领取优惠券
     * @param userCoupon 用户优惠券信息
     * @return 影响的行数
     */
    int insertUserCoupon(Map<String, Object> userCoupon);
    
    /**
     * 更新优惠券的领取数量
     * @param couponId 优惠券ID
     * @return 影响的行数
     */
    int updateCouponReceiveCount(Long couponId);
    
    /**
     * 使用优惠券
     * @param userCouponId 用户优惠券ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 影响的行数
     */
    int useCoupon(@Param("userCouponId") Long userCouponId, @Param("userId") Long userId, @Param("orderId") Long orderId);
    
    /**
     * 返还优惠券（订单取消时）
     * @param userCouponId 用户优惠券ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 影响的行数
     */
    int returnCoupon(@Param("userCouponId") Long userCouponId, @Param("userId") Long userId, @Param("orderId") Long orderId);
    
    /**
     * 更新优惠券的使用数量
     * @param couponId 优惠券ID
     * @param increment 增量，1表示增加，-1表示减少
     * @return 影响的行数
     */
    int updateCouponUsedCount(@Param("couponId") Long couponId, @Param("increment") int increment);
    
    /**
     * 获取用户可用优惠券数量
     * @param userId 用户ID
     * @return 可用优惠券数量
     */
    int countAvailableUserCoupons(Long userId);
} 