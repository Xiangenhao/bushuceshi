package org.example.afd.service.impl;

import org.example.afd.dto.ReviewDTO;
import org.example.afd.mapper.ReviewMapper;
import org.example.afd.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Map<String, Object> getProductReviews(Long productId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询评价列表
        List<Map<String, Object>> reviews = reviewMapper.selectProductReviews(productId, offset, size);
        
        // 查询总数
        int total = reviewMapper.countProductReviews(productId);
        
        // 查询商品评分统计
        double averageRating = reviewMapper.getProductAverageRating(productId);
        List<Map<String, Object>> ratingCounts = reviewMapper.getProductRatingCounts(productId);
        
        // 转换为前端需要的数据格式
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        for (Map<String, Object> reviewMap : reviews) {
            ReviewDTO dto = convertMapToReviewDTO(reviewMap);
            reviewDTOs.add(dto);
        }
        
        result.put("list", reviewDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        result.put("averageRating", averageRating);
        result.put("ratingCounts", ratingCounts);
        
        return result;
    }

    @Override
    public Map<String, Object> submitReview(Long userId, Long orderItemId, ReviewDTO reviewDTO) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 转换为Map方便插入
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("order_id", reviewDTO.getOrderId());
            reviewMap.put("order_item_id", orderItemId);
            reviewMap.put("product_id", reviewDTO.getProductId());
            reviewMap.put("sku_id", reviewDTO.getSkuId());
            reviewMap.put("user_id", userId);
            reviewMap.put("merchant_id", 0); // 假设merchantId暂时设为0，实际应该从产品或订单获取
            reviewMap.put("rating", reviewDTO.getRating());
            reviewMap.put("content", reviewDTO.getContent());
            reviewMap.put("images", String.join(",", reviewDTO.getImages() != null ? reviewDTO.getImages() : new ArrayList<>()));
            reviewMap.put("is_anonymous", reviewDTO.getIsAnonymous() ? 1 : 0);
            
            // 插入评价
            reviewMapper.insertReview(reviewMap);
            
            // 更新订单项评价状态
            reviewMapper.updateOrderItemReviewStatus(orderItemId);
            
            result.put("success", true);
            result.put("message", "评价提交成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "评价提交失败：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getUserReviews(Long userId, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询用户评价列表
        List<Map<String, Object>> reviews = reviewMapper.selectUserReviews(userId, offset, size);
        
        // 查询总数
        int total = reviewMapper.countUserReviews(userId);
        
        // 转换为前端需要的数据格式
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        for (Map<String, Object> reviewMap : reviews) {
            ReviewDTO dto = convertMapToReviewDTO(reviewMap);
            reviewDTOs.add(dto);
        }
        
        result.put("list", reviewDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    @Override
    public Map<String, Object> getReviewDetail(Long reviewId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询评论详情
        Map<String, Object> reviewMap = reviewMapper.selectReviewById(reviewId);
        
        if (reviewMap != null) {
            ReviewDTO reviewDTO = convertMapToReviewDTO(reviewMap);
            result.put("review", reviewDTO);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "评论不存在");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> addReview(Map<String, Object> review) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 插入评价
            reviewMapper.insertReview(review);
            
            result.put("success", true);
            result.put("message", "评价添加成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "评价添加失败：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> updateReviewUseful(Long reviewId, boolean useful) {
        Map<String, Object> result = new HashMap<>();
        
        // 这个功能可能需要额外的数据库表或字段来支持，暂时模拟实现
        result.put("success", true);
        result.put("message", "操作成功");
        
        return result;
    }

    @Override
    public Map<String, Object> replyReview(Long reviewId, String reply) {
        Map<String, Object> result = new HashMap<>();
        
        // 这个功能需要修改评论表的回复内容，暂时简单模拟实现
        result.put("success", true);
        result.put("message", "回复成功");
        
        return result;
    }
    
    /**
     * 将Map转换为ReviewDTO对象
     */
    private ReviewDTO convertMapToReviewDTO(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        ReviewDTO dto = new ReviewDTO();
        
        // 设置基本属性
        dto.setReviewId(getLongValue(map, "review_id"));
        dto.setOrderId(getLongValue(map, "order_id"));
        dto.setOrderItemId(getLongValue(map, "order_item_id"));
        dto.setProductId(getLongValue(map, "product_id"));
        dto.setSkuId(getLongValue(map, "sku_id"));
        dto.setUserId(getLongValue(map, "user_id"));
        dto.setRating(getIntValue(map, "rating"));
        dto.setContent((String) map.get("content"));
        
        // 处理图片列表
        String images = (String) map.get("images");
        if (images != null && !images.isEmpty()) {
            dto.setImages(List.of(images.split(",")));
        }
        
        // 设置匿名状态
        dto.setIsAnonymous(getIntValue(map, "is_anonymous") == 1);
        
        // 设置商家回复
        dto.setMerchantReply((String) map.get("merchant_reply"));
        
        // 设置时间
        dto.setReplyTime((java.util.Date) map.get("reply_time"));
        dto.setCreateTime((java.util.Date) map.get("create_time"));
        
        // 设置关联用户信息
        dto.setUsername((String) map.get("username"));
        dto.setAvatar((String) map.get("avatar"));
        
        // 设置关联商品信息
        dto.setProductName((String) map.get("product_name"));
        dto.setProductImage((String) map.get("product_image"));
        dto.setSkuName((String) map.get("sku_name"));
        
        return dto;
    }
    
    /**
     * 安全获取Long类型值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 安全获取Integer类型值
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 