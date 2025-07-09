package org.example.afd.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品评价数据访问接口
 */
@Mapper
public interface ReviewMapper {
    
    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @param offset 偏移量
     * @param size 查询数量
     * @return 评价列表
     */
    @Select("SELECT r.*, u.username, u.avatar FROM shop_product_review r " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.product_id = #{productId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectProductReviews(@Param("productId") Long productId, 
                                                 @Param("offset") int offset,
                                                 @Param("size") int size);
    
    /**
     * 统计商品评价总数
     * @param productId 商品ID
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM shop_product_review WHERE product_id = #{productId} AND status = 1")
    int countProductReviews(@Param("productId") Long productId);
    
    /**
     * 根据ID查询评价
     * @param reviewId 评价ID
     * @return 评价详情
     */
    @Select("SELECT r.*, u.username, u.avatar, p.product_name, p.main_image as product_image, " +
            "s.sku_name FROM shop_product_review r " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +
            "LEFT JOIN shop_product p ON r.product_id = p.product_id " +
            "LEFT JOIN shop_product_sku s ON r.sku_id = s.sku_id " +
            "WHERE r.review_id = #{reviewId}")
    Map<String, Object> selectReviewById(@Param("reviewId") Long reviewId);
    
    /**
     * 获取用户的评价列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @param size 查询数量
     * @return 评价列表
     */
    @Select("SELECT r.*, p.product_name, p.main_image as product_image, " +
            "s.sku_name FROM shop_product_review r " +
            "LEFT JOIN shop_product p ON r.product_id = p.product_id " +
            "LEFT JOIN shop_product_sku s ON r.sku_id = s.sku_id " +
            "WHERE r.user_id = #{userId} AND r.status = 1 " +
            "ORDER BY r.create_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectUserReviews(@Param("userId") Long userId, 
                                              @Param("offset") int offset,
                                              @Param("size") int size);
    
    /**
     * 统计用户评价总数
     * @param userId 用户ID
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM shop_product_review WHERE user_id = #{userId} AND status = 1")
    int countUserReviews(@Param("userId") Long userId);
    
    /**
     * 插入商品评价
     * @param review 评价信息
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_product_review(order_id, order_item_id, product_id, sku_id, user_id, merchant_id, " +
            "rating, content, images, is_anonymous, status, create_time, update_time) " +
            "VALUES(#{order_id}, #{order_item_id}, #{product_id}, #{sku_id}, #{user_id}, #{merchant_id}, " +
            "#{rating}, #{content}, #{images}, #{is_anonymous}, 1, NOW(), NOW())")
    int insertReview(Map<String, Object> review);
    
    /**
     * 更新订单项的评价状态
     * @param orderItemId 订单项ID
     * @return 影响行数
     */
    @Select("UPDATE shop_order_item SET review_status = 1 WHERE item_id = #{orderItemId}")
    int updateOrderItemReviewStatus(@Param("orderItemId") Long orderItemId);
    
    /**
     * 获取商品平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    @Select("SELECT COALESCE(AVG(rating), 5) FROM shop_product_review WHERE product_id = #{productId} AND status = 1")
    Double getProductAverageRating(@Param("productId") Long productId);
    
    /**
     * 获取商品各星级评价数量
     * @param productId 商品ID
     * @return 星级评价统计
     */
    @Select("SELECT rating, COUNT(*) as count FROM shop_product_review " +
            "WHERE product_id = #{productId} AND status = 1 " +
            "GROUP BY rating ORDER BY rating DESC")
    List<Map<String, Object>> getProductRatingCounts(@Param("productId") Long productId);
} 