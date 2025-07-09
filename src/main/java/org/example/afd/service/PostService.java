package org.example.afd.service;

import java.util.List;
import java.util.Map;

import org.example.afd.dto.*;
import org.example.afd.model.Result;

public interface PostService {

    // ===== 动态内容相关接口 =====
    
    /**
     * 创建动态
     * @param postDTO 动态数据传输对象
     * @return 创建结果
     */
    Result<PostDTO> createPost(PostDTO postDTO);
    
    /**
     * 获取动态详情
     * @param postId 动态ID
     * @param userId 当前用户ID，用于判断是否点赞、收藏等
     * @return 动态详情
     */
    Result<PostDTO> getPostDetail(Long postId, Long userId);
    
    /**
     * 获取用户发布的动态列表
     * @param authorId 作者ID
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    Result<List<PostDTO>> getUserPosts(Long authorId, Long userId, Integer page, Integer size);
    
    /**
     * 获取关注的用户发布的动态列表
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    Result<List<PostDTO>> getFollowingPosts(Long userId, Integer page, Integer size);
    
    /**
     * 获取推荐动态列表
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    Result<List<PostDTO>> getRecommendPosts(Long userId, Integer page, Integer size);
    
    /**
     * 点赞/取消点赞动态
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 操作结果，true表示当前状态为已点赞，false表示当前状态为未点赞
     */
    Result<Boolean> toggleLike(Long postId, Long userId);
    
    /**
     * 收藏/取消收藏动态
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 操作结果，true表示当前状态为已收藏，false表示当前状态为未收藏
     */
    Result<Boolean> toggleCollect(Long postId, Long userId);
    
    /**
     * 获取用户收藏的动态列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    Result<List<PostDTO>> getCollectedPosts(Long userId, Integer page, Integer size);
    
    // ===== 订阅计划相关接口 =====
    
    /**
     * 创建订阅计划
     * @param plan 订阅计划
     * @return 创建结果
     */
    Result<SubscriptionPlanDTO> createSubscriptionPlan(SubscriptionPlanDTO plan);
    
    /**
     * 获取创作者的所有订阅计划
     * @param creatorId 创作者ID
     * @param userId 当前用户ID，用于判断是否已订阅
     * @return 订阅计划列表
     */
    Result<List<SubscriptionPlanDTO>> getCreatorSubscriptionPlans(Long creatorId, Long userId);
    
    /**
     * 获取当前用户的所有订阅计划
     * @param userId 用户ID
     * @return 订阅计划列表
     */
    Result<List<SubscriptionPlanDTO>> getMySubscriptionPlans(Long userId);
    
    /**
     * 获取订阅计划详情
     * @param planId 计划ID
     * @param userId 当前用户ID
     * @return 订阅计划详情
     */
    Result<SubscriptionPlanDTO> getSubscriptionPlanDetail(Long planId, Long userId);
    
    /**
     * 更新订阅计划
     * @param planId 计划ID
     * @param plan 更新内容
     * @param userId 当前用户ID
     * @return 更新结果
     */
    Result<SubscriptionPlanDTO> updateSubscriptionPlan(Long planId, SubscriptionPlanDTO plan, Long userId);
    
    /**
     * 删除订阅计划
     * @param planId 计划ID
     * @param userId 当前用户ID
     * @return 删除结果
     */
    Result<Boolean> deleteSubscriptionPlan(Long planId, Long userId);
    
    /**
     * 订阅计划（创建订单）
     * @param planId 计划ID
     * @param userId 用户ID
     * @param months 订阅月数
     * @return 订单信息
     */
    Result<Map<String, Object>> createSubscriptionOrder(Long planId, Long userId, Integer months);
    
    /**
     * 获取订阅内容列表
     * @param planId 计划ID
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 内容列表
     */
    Result<List<PostDTO>> getSubscriptionContents(Long planId, Long userId, Integer page, Integer size);
    
    /**
     * 检查用户是否已订阅某个计划
     * @param planId 计划ID
     * @param userId 用户ID
     * @return 是否已订阅
     */
    Result<Boolean> checkUserSubscription(Long planId, Long userId);
    
    /**
     * 完成订阅支付
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param paymentData 支付数据
     * @return 支付结果
     */
    Result<Boolean> completeSubscriptionPayment(String orderNo, Long userId, Map<String, Object> paymentData);
    
    // ===== 评论相关接口 =====
    
    /**
     * 获取帖子评论列表
     * @param postId 帖子ID
     * @param userId 当前用户ID，用于查询点赞状态
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    Result<List<CommentDTO>> getComments(Long postId, Long userId, Integer page, Integer size);
    
    /**
     * 添加评论
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param content 评论内容
     * @return 操作结果
     */
    Result<CommentDTO> addComment(Long postId, Long userId, String content);
    
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> likeComment(Long commentId, Long userId);
    
    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> unlikeComment(Long commentId, Long userId);
    
    /**
     * 添加回复
     * 支持回复评论和回复回复，根据数据库设计：
     * 1. 直接回复评论：parent_id=评论ID，root_id=评论ID
     * 2. 回复评论的回复：parent_id=被回复的回复ID，root_id=顶级评论ID
     * @param commentId 顶级评论ID（用于API调用）
     * @param userId 用户ID
     * @param content 回复内容
     * @param replyToUserId 被回复的用户ID
     * @param parentId 父评论ID（数据库中的parent_id）
     * @param rootId 根评论ID（数据库中的root_id）
     * @return 操作结果
     */
    Result<ReplyDTO> addReply(Long commentId, Long userId, String content, Long replyToUserId, Long parentId, Long rootId);
    
    /**
     * 获取评论的回复列表
     * @param commentId 评论ID
     * @param userId 当前用户ID，用于查询点赞状态  
     * @param page 页码
     * @param size 每页大小
     * @return 回复列表
     */
    Result<List<ReplyDTO>> getReplies(Long commentId, Long userId, Integer page, Integer size);
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> deleteComment(Long commentId, Long userId);
    
    /**
     * 删除回复
     * @param replyId 回复ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> deleteReply(Long replyId, Long userId);
    
    /**
     * 点赞回复
     * @param replyId 回复ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> likeReply(Long replyId, Long userId);
    
    // ===== 标签相关接口 =====
    
    /**
     * 获取热门标签
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    Result<List<TagDTO>> getHotTags(Integer limit);
    
    /**
     * 搜索标签
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    Result<List<TagDTO>> searchTags(String keyword);
    
    /**
     * 获取所有标签
     * @param limit 返回数量限制
     * @return 标签列表
     */
    Result<List<TagDTO>> getAllTags(Integer limit);
    
    /**
     * 删除动态
     * @param postId 动态ID
     * @param userId 当前用户ID
     * @return 删除结果
     */
    Result<Void> deletePost(Long postId, Long userId);
}
