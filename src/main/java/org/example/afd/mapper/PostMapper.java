package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.example.afd.dto.PostDTO;
import org.example.afd.pojo.Post;
import org.example.afd.dto.SubscriptionPlanDTO;
import org.example.afd.dto.TagDTO;
import org.example.afd.handler.IntegerToBooleanHandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper {

    /**
     * 插入新动态
     */
    @Insert("INSERT INTO afd.post_content (user_id, content, visibility_type, location, " +
            "like_count, comment_count, collect_count, forward_count, view_count, " +
            "is_top, create_time, update_time, status) " +
            "VALUES (#{userId}, #{content}, #{visibilityType}, #{location}, " +
            "#{likeCount}, #{commentCount}, #{collectCount}, #{forwardCount}, #{viewCount}, " +
            "#{isTop}, #{createTime}, #{updateTime}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    void insertPost(Post post);
    
    /**
     * 获取动态详情（基本信息）
     */
    @Select("SELECT p.*, u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "WHERE p.post_id = #{postId} AND p.status = 1")
    @Results({
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "visibilityType", column = "visibility_type"),
        @Result(property = "location", column = "location"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "collectCount", column = "collect_count"),
        @Result(property = "forwardCount", column = "forward_count"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "isTop", column = "is_top"),
        @Result(property = "status", column = "status"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatarUrl", column = "user_avatar_url"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    Post getPostById(Long postId);
    
    /**
     * 获取动态详情（用于返回前端的DTO对象）
     */
    @Select("SELECT p.*, u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "WHERE p.post_id = #{postId} AND p.status = 1")
    @Results({
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatarUrl", column = "user_avatar_url"),
        @Result(property = "content", column = "content"),
        @Result(property = "visibilityType", column = "visibility_type"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "collectCount", column = "collect_count"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "location", column = "location"),
        @Result(property = "createTime", column = "create_time")
    })
    PostDTO getPostDetail(int postId);
    
    /**
     * 获取帖子标签
     */
    @Select("SELECT t.tag_name FROM afd.post_tag t " +
            "JOIN afd.post_content_tag ct ON t.tag_id = ct.tag_id " +
            "WHERE ct.post_id = #{postId}")
    List<String> getPostTags(int postId);
    
    /**
     * 获取用户发布的动态列表
     */
    @Select("SELECT p.*, u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} AND p.status = 1 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "visibilityType", column = "visibility_type"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "collectCount", column = "collect_count"),
        @Result(property = "forwardCount", column = "forward_count"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatarUrl", column = "user_avatar_url"),
        @Result(property = "createTime", column = "create_time")
    })
    List<Post> getUserPosts(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("size") Integer size);
    
    /**
     * 获取关注用户的动态列表
     */
    @Select("SELECT p.*, u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "JOIN afd.user_relation ur ON p.user_id = ur.target_id " +
            "WHERE ur.user_id = #{userId} AND ur.relation_type = 1 AND ur.status = 1 " +
            "AND p.status = 1 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "visibilityType", column = "visibility_type"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "collectCount", column = "collect_count"),
        @Result(property = "forwardCount", column = "forward_count"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatarUrl", column = "user_avatar_url"),
        @Result(property = "createTime", column = "create_time")
    })
    List<Post> getFollowingPosts(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("size") Integer size);
    
    /**
     * 获取推荐动态列表（简单实现，按照点赞数和时间排序）
     */
    @Select("SELECT p.*, u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "WHERE p.status = 1 " +
            "ORDER BY p.like_count DESC, p.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "visibilityType", column = "visibility_type"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "collectCount", column = "collect_count"),
        @Result(property = "forwardCount", column = "forward_count"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatarUrl", column = "user_avatar_url"),
        @Result(property = "createTime", column = "create_time")
    })
    List<Post> getRecommendPosts(@Param("offset") Integer offset, @Param("size") Integer size);
    
    /**
     * 获取用户收藏的动态列表
     */
    @Select("SELECT p.*, u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "JOIN afd.post_interaction pi ON p.post_id = pi.post_id " +
            "WHERE pi.user_id = #{userId} AND pi.interaction_type = 2 AND pi.status = 1 " +
            "AND p.status = 1 " +
            "ORDER BY pi.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    @Results({
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "visibilityType", column = "visibility_type"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "collectCount", column = "collect_count"),
        @Result(property = "forwardCount", column = "forward_count"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "username", column = "username"),
        @Result(property = "userAvatarUrl", column = "user_avatar_url"),
        @Result(property = "createTime", column = "create_time")
    })
    List<Post> getCollectedPosts(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("size") Integer size);
    
    /**
     * 判断用户是否点赞过动态
     */
    @Select("SELECT COUNT(*) FROM afd.post_interaction " +
            "WHERE post_id = #{postId} AND user_id = #{userId} " +
            "AND interaction_type = 1 AND status = 1")
    boolean isPostLiked(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 判断用户是否点赞过动态的记录
     */
    @Select("SELECT COUNT(*) FROM afd.post_interaction " +
            "WHERE post_id = #{postId} AND user_id = #{userId} " +
            "AND interaction_type = 1 AND status = 0")
    boolean isPostLikedRecord(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 判断用户是否收藏过动态
     */
    @Select("SELECT COUNT(*) FROM afd.post_interaction " +
            "WHERE post_id = #{postId} AND user_id = #{userId} " +
            "AND interaction_type = 2 AND status = 1")
    boolean isPostCollected(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 判断用户是否收藏过动态的记录
     */
    @Select("SELECT COUNT(*) FROM afd.post_interaction " +
            "WHERE post_id = #{postId} AND user_id = #{userId} " +
            "AND interaction_type = 2 AND status = 0")
    boolean isPostCollectedRecord(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 增加动态浏览量
     */
    @Update("UPDATE afd.post_content SET view_count = view_count + 1 WHERE post_id = #{postId}")
    void incrementViewCount(@Param("postId") Long postId);
    
    /**
     * 增加动态点赞数
     */
    @Update("UPDATE afd.post_content SET like_count = like_count + #{increment} WHERE post_id = #{postId}")
    void updateLikeCount(@Param("postId") Long postId, @Param("increment") Integer increment);
    
    /**
     * 增加动态收藏数
     */
    @Update("UPDATE afd.post_content SET collect_count = collect_count + #{increment} WHERE post_id = #{postId}")
    void updateCollectCount(@Param("postId") Long postId, @Param("increment") Integer increment);
    
    /**
     * 增加动态评论数
     */
    @Update("UPDATE afd.post_content SET comment_count = comment_count + #{increment} WHERE post_id = #{postId}")
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") Integer increment);
    
    /**
     * 插入用户动态交互（点赞、收藏等）
     */
    @Insert("INSERT INTO afd.post_interaction (user_id, post_id, interaction_type, create_time, status) " +
            "VALUES (#{userId}, #{postId}, #{interactionType}, #{createTime}, #{status})")
    void insertPostInteraction(@Param("userId") Long userId, @Param("postId") Long postId, 
                              @Param("interactionType") Integer interactionType, 
                              @Param("createTime") Date createTime, @Param("status") Integer status);
    
    /**
     * 更新用户动态交互状态
     */
    @Update("UPDATE afd.post_interaction SET status = #{status} " +
            "WHERE user_id = #{userId} AND post_id = #{postId} AND interaction_type = #{interactionType}")
    void updatePostInteractionStatus(@Param("userId") Long userId, @Param("postId") Long postId, 
                                    @Param("interactionType") Integer interactionType, @Param("status") Integer status);
    
    // ===== 订阅计划相关接口 =====
    
    /**
     * 插入订阅计划
     */
    @Insert("INSERT INTO afd.post_subscription_plan (creator_id, title, description, benefits, cover_url, monthly_price, " +
            "subscriber_count, create_time, update_time, status, tag) " +
            "VALUES (#{creatorId}, #{title}, #{description}, #{benefitsJson}, #{coverUrl}, #{monthlyPrice}, " +
            "0, #{createTime}, #{updateTime}, #{status}, #{tag})")
    @Options(useGeneratedKeys = true, keyProperty = "planId")
    void insertSubscriptionPlan(SubscriptionPlanDTO plan);
    
    /**
     * 获取创作者的所有订阅计划
     */
    @Select("SELECT * FROM afd.post_subscription_plan " +
            "WHERE creator_id = #{creatorId} AND status != 0 " +
            "ORDER BY create_time DESC")
    @Results({
        @Result(property = "planId", column = "plan_id"),
        @Result(property = "creatorId", column = "creator_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "benefitsJson", column = "benefits"),
        @Result(property = "coverUrl", column = "cover_url"),
        @Result(property = "monthlyPrice", column = "monthly_price"),
        @Result(property = "subscriberCount", column = "subscriber_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "tag", column = "tag")
    })
    List<SubscriptionPlanDTO> getCreatorSubscriptionPlans(@Param("creatorId") Long creatorId);
    
    /**
     * 获取订阅计划详情
     */
    @Select("SELECT p.*, u.username as creator_name, u.avatar as creator_avatar " +
            "FROM afd.post_subscription_plan p " +
            "LEFT JOIN afd.users u ON p.creator_id = u.user_id " +
            "WHERE p.plan_id = #{planId}")
    @Results({
        @Result(property = "planId", column = "plan_id"),
        @Result(property = "creatorId", column = "creator_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "benefitsJson", column = "benefits"),
        @Result(property = "coverUrl", column = "cover_url"),
        @Result(property = "monthlyPrice", column = "monthly_price"),
        @Result(property = "subscriberCount", column = "subscriber_count"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "tag", column = "tag"),
        @Result(property = "creatorName", column = "creator_name"),
        @Result(property = "creatorAvatar", column = "creator_avatar")
    })
    SubscriptionPlanDTO getSubscriptionPlanById(@Param("planId") Long planId);
    
    /**
     * 更新订阅计划
     */
    @Update("UPDATE afd.post_subscription_plan SET title = #{title}, description = #{description}, " +
            "benefits = #{benefitsJson}, cover_url = #{coverUrl}, monthly_price = #{monthlyPrice}, tag = #{tag}, " +
            "update_time = #{updateTime}, status = #{status} " +
            "WHERE plan_id = #{planId} AND creator_id = #{creatorId}")
    int updateSubscriptionPlan(SubscriptionPlanDTO plan);
    
    /**
     * 删除订阅计划（逻辑删除）
     */
    @Update("UPDATE afd.post_subscription_plan SET status = 0, update_time = #{updateTime} " +
            "WHERE plan_id = #{planId} AND creator_id = #{creatorId}")
    int deleteSubscriptionPlan(@Param("planId") Long planId, @Param("creatorId") Long creatorId, 
                               @Param("updateTime") Date updateTime);
    
    /**
     * 获取用户订阅的计划列表
     */
    @Select("SELECT p.plan_id, p.creator_id, u.username as creator_name, u.avatar as creator_avatar, " +
            "p.title, p.description, p.cover_url, p.monthly_price, p.subscriber_count, " +
            "p.create_time, p.update_time, p.status, " +
            "CASE WHEN s.plan_id IS NOT NULL THEN 1 ELSE 0 END as is_subscribed " +
            "FROM post_subscription_plan p " +
            "LEFT JOIN users u ON p.creator_id = u.user_id " +
            "LEFT JOIN post_user_subscription s ON p.plan_id = s.plan_id AND s.user_id = #{userId} AND s.status = 1 AND s.end_time > NOW() " +
            "WHERE p.status = 1 " +
            "AND (#{keyword} IS NULL OR p.title LIKE CONCAT('%', #{keyword}, '%') OR u.username LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY s.plan_id DESC, p.subscriber_count DESC ")
    @Results({
            @Result(property = "planId", column = "plan_id"),
            @Result(property = "creatorId", column = "creator_id"),
            @Result(property = "creatorName", column = "creator_name"),
            @Result(property = "creatorAvatar", column = "creator_avatar"),
            @Result(property = "coverUrl", column = "cover_url"),
            @Result(property = "monthlyPrice", column = "monthly_price"),
            @Result(property = "subscriberCount", column = "subscriber_count"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isSubscribed", column = "is_subscribed", javaType = Integer.class, jdbcType = JdbcType.INTEGER, typeHandler = IntegerToBooleanHandler.class)
    })
    List<SubscriptionPlanDTO> getUserSubscribedPlans(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * 判断用户是否订阅了某个计划
     */
    @Select("SELECT COUNT(*) > 0 FROM afd.post_user_subscription " +
            "WHERE user_id = #{userId} AND plan_id = #{planId} AND status = 1 AND end_time > NOW()")
    boolean isUserSubscribed(@Param("userId") Long userId, @Param("planId") Long planId);
    
    /**
     * 获取用户订阅到期时间
     */
    @Select("SELECT end_time FROM afd.post_user_subscription " +
            "WHERE user_id = #{userId} AND plan_id = #{planId} AND status = 1 " +
            "ORDER BY end_time DESC LIMIT 1")
    Date getUserSubscriptionExpireTime(@Param("userId") Long userId, @Param("planId") Long planId);
    
    /**
     * 创建用户订阅记录
     */
    @Insert("INSERT INTO afd.post_user_subscription (user_id, plan_id, start_time, end_time, " +
            "subscription_price, auto_renew, status, create_time, update_time) " +
            "VALUES (#{userId}, #{planId}, #{startTime}, #{endTime}, " +
            "#{subscriptionPrice}, #{autoRenew}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "recordId")
    void insertUserSubscription(@Param("userId") Long userId, @Param("planId") Long planId, 
                               @Param("startTime") Date startTime, @Param("endTime") Date endTime,
                               @Param("subscriptionPrice") BigDecimal subscriptionPrice,
                               @Param("autoRenew") Boolean autoRenew, @Param("status") Integer status,
                               @Param("createTime") Date createTime, @Param("updateTime") Date updateTime);
    
    /**
     * 创建订阅支付订单
     */
    @Insert("INSERT INTO afd.post_plan_payment_order (order_no, user_id, plan_id, record_id, amount, " +
            "payment_type, payment_status, subscription_months, create_time, expire_time) " +
            "VALUES (#{orderNo}, #{userId}, #{planId}, #{recordId}, #{amount}, " +
            "#{paymentType}, #{paymentStatus}, #{subscriptionMonths}, #{createTime}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    void insertPaymentOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId, 
                           @Param("planId") Long planId, @Param("recordId") Long recordId,
                           @Param("amount") BigDecimal amount, @Param("paymentType") Integer paymentType,
                           @Param("paymentStatus") Integer paymentStatus, @Param("subscriptionMonths") Integer subscriptionMonths,
                           @Param("createTime") Date createTime, @Param("expireTime") Date expireTime);
    
    /**
     * 获取订阅内容列表
     */
    @Select("SELECT p.post_id, p.user_id, p.content, p.create_time, " +
            "u.username, u.avatar as user_avatar_url " +
            "FROM afd.post_content p " +
            "JOIN afd.post_content_visibility v ON p.post_id = v.post_id " +
            "JOIN afd.users u ON p.user_id = u.user_id " +
            "WHERE v.plan_id = #{planId} AND p.status = 1 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Post> getSubscriptionContents(@Param("planId") Long planId, 
                                      @Param("offset") Integer offset, 
                                      @Param("size") Integer size);
    
    /**
     * 获取订阅订单信息（兼容旧版本）
     */
    @Select("SELECT o.*, p.title as plan_title " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.post_subscription_plan p ON o.related_id = p.plan_id " +
            "WHERE o.order_no = #{orderNo} AND o.order_type = 2")
    Map<String, Object> getSubscriptionOrderByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 辅助方法：返回true
     */
    default boolean isTrue() {
        return true;
    }

    /**
     * 根据标签名获取标签ID
     */
    @Select("SELECT tag_id FROM afd.post_tag WHERE tag_name = #{tagName} LIMIT 1")
    Long getTagIdByName(String tagName);
    
    /**
     * 创建新标签
     */
    @Insert("INSERT INTO afd.post_tag (tag_name, use_count, create_time, status) " +
            "VALUES (#{tagName}, 0, NOW(), 1)")
    boolean createTag(String tagName);
    
    /**
     * 创建动态标签关联
     */
    @Insert("INSERT INTO afd.post_content_tag (post_id, tag_id, create_time) " +
            "VALUES (#{postId}, #{tagId}, NOW())")
    boolean createPostTagRelation(@Param("postId") Long postId, @Param("tagId") Long tagId);
    
    /**
     * 更新标签使用次数
     */
    @Update("UPDATE afd.post_tag SET use_count = use_count + 1 WHERE tag_id = #{tagId}")
    void updateTagUseCount(Long tagId);
    
    /**
     * 插入动态媒体文件
     */
    @Insert("INSERT INTO afd.post_media (post_id, media_type, media_url, sort_order, create_time) " +
            "VALUES (#{postId}, #{mediaType}, #{mediaUrl}, #{sortOrder}, NOW())")
    boolean insertPostMedia(@Param("postId") Long postId, @Param("mediaUrl") String mediaUrl, 
                        @Param("sortOrder") int sortOrder, @Param("mediaType") int mediaType);
    
    /**
     * 删除动态（逻辑删除）
     */
    @Update("UPDATE afd.post_content SET status = 0 WHERE post_id = #{postId} AND user_id = CAST(#{userId} AS SIGNED)")
    int deletePost(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 获取动态媒体URL列表
     */
    @Select("SELECT media_url FROM afd.post_media WHERE post_id = #{postId} ORDER BY sort_order")
    List<String> getPostMediaUrls(@Param("postId") Long postId);
    
    /**
     * 获取热门标签
     */
    @Select("SELECT tag_id, tag_name, use_count as count, 1 as is_hot " +
            "FROM afd.post_tag " +
            "WHERE status = 1 " +
            "ORDER BY use_count DESC " +
            "LIMIT #{limit}")
    @Results({
        @Result(property = "tagId", column = "tag_id"),
        @Result(property = "tagName", column = "tag_name"),
        @Result(property = "count", column = "count"),
        @Result(property = "isHot", column = "is_hot")
    })
    List<TagDTO> getHotTags(Integer limit);
    
    /**
     * 搜索标签
     */
    @Select("SELECT tag_id, tag_name, use_count as count, 0 as is_hot " +
            "FROM afd.post_tag " +
            "WHERE status = 1 AND tag_name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY use_count DESC " +
            "LIMIT 20")
    @Results({
        @Result(property = "tagId", column = "tag_id"),
        @Result(property = "tagName", column = "tag_name"),
        @Result(property = "count", column = "count"),
        @Result(property = "isHot", column = "is_hot")
    })
    List<TagDTO> searchTags(String keyword);
    
    /**
     * 获取全部标签
     */
    @Select("SELECT tag_id, tag_name, use_count as count, 0 as is_hot " +
            "FROM afd.post_tag " +
            "WHERE status = 1 " +
            "ORDER BY use_count DESC, tag_id ASC " +
            "LIMIT #{limit}")
    @Results({
        @Result(property = "tagId", column = "tag_id"),
        @Result(property = "tagName", column = "tag_name"),
        @Result(property = "count", column = "count"),
        @Result(property = "isHot", column = "is_hot")
    })
    List<TagDTO> getAllTags(Integer limit);

    /**
     * 插入内容可见性设置
     */
    @Insert("INSERT INTO afd.post_content_visibility (post_id, plan_id, create_time) " +
            "VALUES (#{postId}, #{planId}, NOW())")
    boolean insertPostVisibility(@Param("postId") Long postId, @Param("planId") Long planId);
    
    /**
     * 获取动态指定的订阅计划ID
     */
    @Select("SELECT plan_id FROM afd.post_content_visibility WHERE post_id = #{postId} LIMIT 1")
    Long getPostVisibilityPlanId(@Param("postId") Long postId);
    
    /**
     * 获取帖子标签DTO列表
     */
    @Select("SELECT t.tag_id, t.tag_name, t.use_count as count, 0 as is_hot FROM afd.post_tag t " +
            "JOIN afd.post_content_tag ct ON t.tag_id = ct.tag_id " +
            "WHERE ct.post_id = #{postId}")
    @Results({
            @Result(property = "tagId", column = "tag_id"),
            @Result(property = "tagName", column = "tag_name"),
            @Result(property = "count", column = "count"),
            @Result(property = "isHot", column = "is_hot")
    })
    List<TagDTO> getPostTagsDTO(int postId);
    
    /**
     * 统计订阅计划的订阅人数
     * @param planId 计划ID
     * @return 订阅人数
     */
    @Select("SELECT COUNT(*) FROM afd.post_user_subscription " +
            "WHERE plan_id = #{planId} AND status = 1 AND end_time > NOW()")
    Integer countPlanSubscribers(Long planId);
    
    /**
     * 检查用户是否已订阅该计划
     * @param userId 用户ID
     * @param planId 计划ID
     * @return 是否已订阅
     */
    @Select("SELECT COUNT(*) > 0 FROM afd.post_user_subscription " +
            "WHERE user_id = #{userId} AND plan_id = #{planId} AND status = 1 AND end_time > NOW()")
    Boolean checkUserSubscribed(@Param("userId") Long userId, @Param("planId") Long planId);

    /**
     * 根据postId获取帖子媒体类型
     */
    @Select("SELECT media_type FROM afd.post_media WHERE post_id = #{postId} LIMIT 1")
    Integer getPostMediaType(Long postId);
    
    /**
     * 更新订单支付状态
     */
    @Update("UPDATE afd.post_plan_payment_order SET payment_status = #{paymentStatus}, " +
            "transaction_id = #{transactionId} " +
            "WHERE order_no = #{orderNo}")
    int updateOrderPaymentStatus(@Param("orderNo") String orderNo, 
                                @Param("paymentStatus") Integer paymentStatus,
                                @Param("transactionId") String transactionId);
    
    /**
     * 获取用户订单列表
     */
    @Select("SELECT order_id, order_no, user_id, plan_id, record_id, amount, payment_type, " +
            "payment_status, transaction_id, subscription_months, create_time, pay_time, expire_time " +
            "FROM afd.post_plan_payment_order " +
            "WHERE user_id = #{userId} " +
            "AND (#{status} IS NULL OR payment_status = #{status}) " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getUserOrders(@Param("userId") Long userId, @Param("offset") Integer offset,
                                           @Param("size") Integer size, @Param("status") Integer status);
}
