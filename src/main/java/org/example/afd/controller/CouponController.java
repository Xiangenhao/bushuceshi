package org.example.afd.controller;

import org.example.afd.dto.CouponDTO;
import org.example.afd.dto.UserCouponDTO;
import org.example.afd.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@RestController
@RequestMapping("/api/v1/coupons")
@Slf4j
public class CouponController {

    @Autowired
    private CouponService couponService;
    
    /**
     * 获取可用的优惠券列表
     * @param categoryId 商品类别ID（可选）
     * @param productId 商品ID（可选）
     * @param minAmount 最小订单金额（可选，默认为0）
     * @return 可用优惠券列表
     */
    @GetMapping("/available")
    public List<CouponDTO> getAvailableCoupons(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false, defaultValue = "0") Double minAmount) {
        // TODO: 从当前登录用户获取userId
        Long userId = 1L; // 这里暂时硬编码
        return couponService.getAvailableCoupons(userId, categoryId, productId, minAmount);
    }
    
    /**
     * 获取用户的优惠券列表
     * @param status 优惠券状态（0-未使用，1-已使用，2-已过期，null-全部）
     * @return 用户优惠券列表
     */
    @GetMapping("/my")
    public List<UserCouponDTO> getUserCoupons(
            @RequestParam(required = false) Integer status) {
        // TODO: 从当前登录用户获取userId
        Long userId = 1L; // 这里暂时硬编码
        return couponService.getUserCoupons(userId, status);
    }
    
    /**
     * 领取优惠券
     * @param couponId 优惠券ID
     * @return 操作结果
     */
    @PostMapping("/receive/{couponId}")
    public Map<String, Object> receiveCoupon(@PathVariable Long couponId) {
        // TODO: 从当前登录用户获取userId
        Long userId = 1L; // 这里暂时硬编码
        return couponService.receiveCoupon(userId, couponId);
    }
    
    /**
     * 根据优惠券ID获取优惠券信息
     * @param couponId 优惠券ID
     * @return 优惠券信息
     */
    @GetMapping("/{couponId}")
    public CouponDTO getCoupon(@PathVariable Long couponId) {
        return couponService.getCoupon(couponId);
    }
    
    /**
     * 获取用户可用优惠券数量
     * @return 可用优惠券数量
     */
    @GetMapping("/my/count")
    public Map<String, Object> getAvailableCouponCount() {
        // TODO: 从当前登录用户获取userId
        Long userId = 1L; // 这里暂时硬编码
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", couponService.getAvailableCouponCount(userId));
        return result;
    }
    
    /**
     * 使用优惠券（订单支付时调用）
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/use")
    public Map<String, Object> useCoupon(
            @RequestParam Long userCouponId,
            @RequestParam Long orderId) {
        // TODO: 从当前登录用户获取userId
        Long userId = 1L; // 这里暂时硬编码
        return couponService.useCoupon(userId, userCouponId, orderId);
    }
    
    /**
     * 返还优惠券（订单取消时调用）
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     * @return 操作结果
     */
    @PostMapping("/return")
    public Map<String, Object> returnCoupon(
            @RequestParam Long userCouponId,
            @RequestParam Long orderId) {
        // TODO: 从当前登录用户获取userId
        Long userId = 1L; // 这里暂时硬编码
        return couponService.returnCoupon(userId, userCouponId, orderId);
    }
} 