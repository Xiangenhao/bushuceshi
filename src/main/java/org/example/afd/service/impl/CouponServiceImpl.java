package org.example.afd.service.impl;

import org.example.afd.dto.CouponDTO;
import org.example.afd.dto.UserCouponDTO;
import org.example.afd.mapper.CouponMapper;
import org.example.afd.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 */
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public List<CouponDTO> getAvailableCoupons(Long userId, Long categoryId, Long productId, Double minAmount) {
        // 获取所有有效的优惠券
        List<Map<String, Object>> allCoupons = new ArrayList<>();
        
        // 获取商品类别相关的优惠券
        if (categoryId != null) {
            allCoupons.addAll(couponMapper.selectCouponsByCategory(categoryId));
        }
        
        // 获取商品相关的优惠券
        if (productId != null) {
            allCoupons.addAll(couponMapper.selectCouponsByProduct(productId));
        }
        
        // 获取通用优惠券
        allCoupons.addAll(couponMapper.selectGeneralCoupons());
        
        // 去重
        Set<Long> couponIds = new HashSet<>();
        List<Map<String, Object>> uniqueCoupons = new ArrayList<>();
        
        for (Map<String, Object> coupon : allCoupons) {
            Long couponId = ((Number) coupon.get("coupon_id")).longValue();
            if (!couponIds.contains(couponId)) {
                couponIds.add(couponId);
                uniqueCoupons.add(coupon);
            }
        }
        
        // 筛选满足最小金额条件的优惠券
        List<Map<String, Object>> filteredCoupons = uniqueCoupons.stream()
                .filter(coupon -> {
                    Double minOrderAmount = (Double) coupon.get("min_amount");
                    return minOrderAmount == null || minOrderAmount <= minAmount;
                })
                .collect(Collectors.toList());
        
        // 转换为DTO
        return filteredCoupons.stream()
                .map(this::convertToCouponDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserCouponDTO> getUserCoupons(Long userId, Integer status) {
        // 获取用户的优惠券
        List<Map<String, Object>> userCoupons = couponMapper.selectUserCoupons(userId, status);
        
        // 转换为DTO
        return userCoupons.stream()
                .map(this::convertToUserCouponDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> receiveCoupon(Long userId, Long couponId) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查优惠券是否存在
        Map<String, Object> coupon = couponMapper.selectCouponById(couponId);
        if (coupon == null) {
            result.put("success", false);
            result.put("message", "优惠券不存在");
            return result;
        }
        
        // 检查优惠券状态
        Integer status = (Integer) coupon.get("status");
        if (status == null || status != 1) {
            result.put("success", false);
            result.put("message", "优惠券不可领取");
            return result;
        }
        
        // 检查优惠券是否过期
        Date now = new Date();
        Date startTime = (Date) coupon.get("start_time");
        Date endTime = (Date) coupon.get("end_time");
        
        if (startTime != null && now.before(startTime)) {
            result.put("success", false);
            result.put("message", "优惠券尚未开始");
            return result;
        }
        
        if (endTime != null && now.after(endTime)) {
            result.put("success", false);
            result.put("message", "优惠券已过期");
            return result;
        }
        
        // 检查是否已领取
        int count = couponMapper.countUserCoupon(userId, couponId);
        if (count > 0) {
            result.put("success", false);
            result.put("message", "已领取该优惠券");
            return result;
        }
        
        // 检查剩余数量
        Integer totalCount = (Integer) coupon.get("total_count");
        Integer receiveCount = (Integer) coupon.get("receive_count");
        
        if (totalCount != null && totalCount > 0 && receiveCount != null && receiveCount >= totalCount) {
            result.put("success", false);
            result.put("message", "优惠券已领完");
            return result;
        }
        
        // 领取优惠券
        Map<String, Object> userCoupon = new HashMap<>();
        userCoupon.put("user_id", userId);
        userCoupon.put("coupon_id", couponId);
        userCoupon.put("status", 0); // 未使用
        userCoupon.put("receive_time", now);
        
        int rows = couponMapper.insertUserCoupon(userCoupon);
        if (rows > 0) {
            // 更新优惠券领取数量
            couponMapper.updateCouponReceiveCount(couponId);
            
            result.put("success", true);
            result.put("message", "优惠券领取成功");
        } else {
            result.put("success", false);
            result.put("message", "优惠券领取失败");
        }
        
        return result;
    }

    @Override
    public CouponDTO getCoupon(Long couponId) {
        Map<String, Object> coupon = couponMapper.selectCouponById(couponId);
        return coupon != null ? convertToCouponDTO(coupon) : null;
    }

    @Override
    @Transactional
    public Map<String, Object> useCoupon(Long userId, Long userCouponId, Long orderId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取用户优惠券
        List<Map<String, Object>> userCoupons = couponMapper.selectUserCoupons(userId, 0); // 未使用状态
        
        // 查找指定的优惠券
        Map<String, Object> targetCoupon = null;
        for (Map<String, Object> userCoupon : userCoupons) {
            Long id = ((Number) userCoupon.get("user_coupon_id")).longValue();
            if (id.equals(userCouponId)) {
                targetCoupon = userCoupon;
                break;
            }
        }
        
        if (targetCoupon == null) {
            result.put("success", false);
            result.put("message", "优惠券不存在或已使用");
            return result;
        }
        
        // 使用优惠券
        int rows = couponMapper.useCoupon(userCouponId, userId, orderId);
        if (rows > 0) {
            // 更新优惠券使用数量
            Long couponId = ((Number) targetCoupon.get("coupon_id")).longValue();
            couponMapper.updateCouponUsedCount(couponId, 1);
            
            result.put("success", true);
            result.put("message", "优惠券使用成功");
        } else {
            result.put("success", false);
            result.put("message", "优惠券使用失败");
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> returnCoupon(Long userId, Long userCouponId, Long orderId) {
        Map<String, Object> result = new HashMap<>();
        
        // 返还优惠券
        int rows = couponMapper.returnCoupon(userCouponId, userId, orderId);
        if (rows > 0) {
            // 获取优惠券ID
            List<Map<String, Object>> userCoupons = couponMapper.selectUserCoupons(userId, 1); // 已使用状态
            Long couponId = null;
            
            for (Map<String, Object> userCoupon : userCoupons) {
                Long id = ((Number) userCoupon.get("user_coupon_id")).longValue();
                if (id.equals(userCouponId)) {
                    couponId = ((Number) userCoupon.get("coupon_id")).longValue();
                    break;
                }
            }
            
            if (couponId != null) {
                // 更新优惠券使用数量
                couponMapper.updateCouponUsedCount(couponId, -1);
            }
            
            result.put("success", true);
            result.put("message", "优惠券返还成功");
        } else {
            result.put("success", false);
            result.put("message", "优惠券返还失败");
        }
        
        return result;
    }

    @Override
    public int getAvailableCouponCount(Long userId) {
        return couponMapper.countAvailableUserCoupons(userId);
    }
    
    /**
     * 将数据库查询结果转换为优惠券DTO
     * @param coupon 优惠券数据
     * @return 优惠券DTO
     */
    private CouponDTO convertToCouponDTO(Map<String, Object> coupon) {
        CouponDTO couponDTO = new CouponDTO();
        
        couponDTO.setCouponId(((Number) coupon.get("coupon_id")).longValue());
        couponDTO.setCouponName((String) coupon.get("coupon_name"));
        couponDTO.setCouponCode((String) coupon.get("coupon_code"));
        couponDTO.setType((Integer) coupon.get("type"));
        couponDTO.setAmount((Double) coupon.get("amount"));
        couponDTO.setMinAmount((Double) coupon.get("min_amount"));
        
        Object categoryId = coupon.get("category_id");
        if (categoryId != null) {
            couponDTO.setCategoryId(((Number) categoryId).longValue());
        }
        
        Object productId = coupon.get("product_id");
        if (productId != null) {
            couponDTO.setProductId(((Number) productId).longValue());
        }
        
        couponDTO.setStartTime((Date) coupon.get("start_time"));
        couponDTO.setEndTime((Date) coupon.get("end_time"));
        couponDTO.setStatus((Integer) coupon.get("status"));
        couponDTO.setTotalCount((Integer) coupon.get("total_count"));
        couponDTO.setUsedCount((Integer) coupon.get("used_count"));
        couponDTO.setReceiveCount((Integer) coupon.get("receive_count"));
        
        return couponDTO;
    }
    
    /**
     * 将数据库查询结果转换为用户优惠券DTO
     * @param userCoupon 用户优惠券数据
     * @return 用户优惠券DTO
     */
    private UserCouponDTO convertToUserCouponDTO(Map<String, Object> userCoupon) {
        UserCouponDTO userCouponDTO = new UserCouponDTO();
        
        userCouponDTO.setUserCouponId(((Number) userCoupon.get("user_coupon_id")).longValue());
        userCouponDTO.setUserId(((Number) userCoupon.get("user_id")).longValue());
        userCouponDTO.setCouponId(((Number) userCoupon.get("coupon_id")).longValue());
        userCouponDTO.setStatus((Integer) userCoupon.get("status"));
        userCouponDTO.setUseTime((Date) userCoupon.get("use_time"));
        userCouponDTO.setReceiveTime((Date) userCoupon.get("receive_time"));
        
        Object orderId = userCoupon.get("order_id");
        if (orderId != null) {
            userCouponDTO.setOrderId(((Number) orderId).longValue());
        }
        
        // 设置关联的优惠券信息
        Map<String, Object> couponInfo = (Map<String, Object>) userCoupon.get("coupon_info");
        if (couponInfo != null) {
            userCouponDTO.setCoupon(convertToCouponDTO(couponInfo));
        } else {
            // 如果没有关联信息，可以单独查询
            Long couponId = userCouponDTO.getCouponId();
            if (couponId != null) {
                Map<String, Object> coupon = couponMapper.selectCouponById(couponId);
                if (coupon != null) {
                    userCouponDTO.setCoupon(convertToCouponDTO(coupon));
                }
            }
        }
        
        return userCouponDTO;
    }
} 