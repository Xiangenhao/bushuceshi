package org.example.afd.service;

import org.example.afd.dto.ReviewDTO;

import java.util.Map;

/**
 * 商品评价服务接口
 */
public interface ReviewService {

    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页数量
     * @return 评价列表分页数据
     */
    Map<String, Object> getProductReviews(Long productId, int page, int size);
    
    /**
     * 提交商品评价
     * @param userId 用户ID
     * @param orderItemId 订单项ID
     * @param reviewDTO 评价信息
     * @return 评价结果
     */
    Map<String, Object> submitReview(Long userId, Long orderItemId, ReviewDTO reviewDTO);
    
    /**
     * 获取用户的评价列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 用户评价列表
     */
    Map<String, Object> getUserReviews(Long userId, int page, int size);
    
    /**
     * 获取评论详情
     * @param reviewId 评论ID
     * @return 评论详情
     */
    Map<String, Object> getReviewDetail(Long reviewId);
    
    /**
     * 添加评论
     * @param review 评论信息
     * @return 操作结果
     */
    Map<String, Object> addReview(Map<String, Object> review);
    
    /**
     * 更新评论有用数
     * @param reviewId 评论ID
     * @param useful 是否有用
     * @return 操作结果
     */
    Map<String, Object> updateReviewUseful(Long reviewId, boolean useful);
    
    /**
     * 管理员回复评论
     * @param reviewId 评论ID
     * @param reply 回复内容
     * @return 操作结果
     */
    Map<String, Object> replyReview(Long reviewId, String reply);
} 