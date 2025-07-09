package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.example.afd.mapper.UserMapper;
import org.example.afd.model.Result;
import org.example.afd.dto.CommentDTO;
import org.example.afd.dto.PostDTO;
import org.example.afd.dto.SubscriptionPlanDTO;
import org.example.afd.dto.ReplyDTO;
import org.example.afd.dto.TagDTO;
import org.example.afd.service.PostService;
import org.example.afd.utils.AliyunOSSOperator;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 * 负责处理与帖子相关的HTTP请求，包括发布帖子、获取帖子列表和帖子详情等功能
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;
    
    @Autowired
    private UserMapper userMapper;
    
    // ========== 订阅计划相关接口 ==========
    
    /**
     * 创建订阅计划
     */
    @PostMapping("/subscription/plans")
    public Result<SubscriptionPlanDTO> createSubscriptionPlan(@RequestBody SubscriptionPlanDTO planDTO) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("创建订阅计划: userId={}, title={}", userId, planDTO.getTitle());
        
        // 设置创建者ID
        planDTO.setCreatorId(userId);
        
        // 创建订阅计划
        Result<SubscriptionPlanDTO> result = postService.createSubscriptionPlan(planDTO);
        
        // 如果创建成功，更新用户的订阅数
        if (result.isSuccess() && result.getData() != null) {
            // 将Long转为Integer
            Integer userIdInt = userId.intValue();
            try {
                // 不再更新用户订阅数，因为users表中没有subscription_count字段
                log.info("订阅计划创建成功: userId={}", userId);
            } catch (Exception e) {
                log.error("处理订阅计划后续操作失败: userId={}", userId, e);
                // 这里我们选择不抛出异常，因为订阅计划已经创建成功
            }
        }
        
        return result;
    }
    
    /**
     * 获取创作者的所有订阅计划
     */
    @GetMapping("/subscription/plans/creator/{creatorId}")
    public Result<List<SubscriptionPlanDTO>> getCreatorSubscriptionPlans(@PathVariable Long creatorId) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取创作者订阅计划: creatorId={}, currentUserId={}", creatorId, userId);
        
        return postService.getCreatorSubscriptionPlans(creatorId, userId);
    }
    
    /**
     * 获取当前用户的所有订阅计划
     */
    @GetMapping("/subscription/plans/my")
    public Result<List<SubscriptionPlanDTO>> getMySubscriptionPlans() {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("获取我的订阅计划: userId={}", userId);
        
        return postService.getMySubscriptionPlans(userId);
    }
    
    /**
     * 获取订阅计划详情
     */
    @GetMapping("/subscription/plans/{planId}")
    public Result<SubscriptionPlanDTO> getSubscriptionPlanDetail(@PathVariable Long planId) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取订阅计划详情: planId={}, currentUserId={}", planId, userId);
        
        return postService.getSubscriptionPlanDetail(planId, userId);
    }
    
    /**
     * 更新订阅计划
     */
    @PutMapping("/subscription/plans/{planId}")
    public Result<SubscriptionPlanDTO> updateSubscriptionPlan(
            @PathVariable Long planId,
            @RequestBody SubscriptionPlanDTO planDTO) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("更新订阅计划: planId={}, userId={}", planId, userId);
        
        // 设置计划ID和创建者ID
        planDTO.setPlanId(planId);
        
        return postService.updateSubscriptionPlan(planId, planDTO, userId);
    }
    
    /**
     * 删除订阅计划
     */
    @DeleteMapping("/subscription/plans/{planId}")
    public Result<Boolean> deleteSubscriptionPlan(@PathVariable Long planId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("删除订阅计划: planId={}, userId={}", planId, userId);
        
        return postService.deleteSubscriptionPlan(planId, userId);
    }
    
    /**
     * 订阅计划（创建订单）
     */
    @PostMapping("/subscription/orders")
    public Result<Map<String, Object>> createSubscriptionOrder(@RequestBody Map<String, Object> orderMap) {
        Long userId = UserIdHolder.getUserId().longValue();
        Long planId = Long.parseLong(orderMap.get("planId").toString());
        Integer months = Integer.parseInt(orderMap.get("months").toString());
        
        log.info("创建订阅订单: planId={}, userId={}, months={}", planId, userId, months);
        
        return postService.createSubscriptionOrder(planId, userId, months);
    }
    
    /**
     * 获取订阅内容列表
     */
    @GetMapping("/subscription/contents/{planId}")
    public Result<List<PostDTO>> getSubscriptionContents(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("获取订阅内容列表: planId={}, userId={}, page={}, size={}", planId, userId, page, size);
        
        return postService.getSubscriptionContents(planId, userId, page, size);
    }
    
    /**
     * 检查用户是否已订阅某个计划
     */
    @GetMapping("/subscription/plans/{planId}/check")
    public Result<Boolean> checkUserSubscription(@PathVariable Long planId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("检查用户订阅状态: planId={}, userId={}", planId, userId);
        
        return postService.checkUserSubscription(planId, userId);
    }
    
    /**
     * 完成订阅支付
     */
    @PostMapping("/subscription/orders/{orderNo}/pay")
    public Result<Boolean> completeSubscriptionPayment(
            @PathVariable String orderNo,
            @RequestBody Map<String, Object> paymentData) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("完成订阅支付: orderNo={}, userId={}, paymentData={}", orderNo, userId, paymentData);
        
        return postService.completeSubscriptionPayment(orderNo, userId, paymentData);
    }
    
    // ========== 动态内容相关接口 ==========
    
    /**
     * 发布动态内容
     */
    @PostMapping("/posts")
    public Result<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("发布动态: userId={}, content={}", userId, postDTO.getContent());
        
        // 设置用户ID
        postDTO.setUserId(userId);
        
        return postService.createPost(postDTO);
    }
    
    /**
     * 获取动态详情
     */
    @GetMapping("/posts/{postId}")
    public Result<PostDTO> getPostDetail(@PathVariable Long postId) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取动态详情: postId={}, currentUserId={}", postId, userId);
        
        return postService.getPostDetail(postId, userId);
    }
    
    /**
     * 获取用户发布的动态列表
     */
    @GetMapping("/users/{authorId}/posts")
    public Result<List<PostDTO>> getUserPosts(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取用户动态列表: authorId={}, currentUserId={}, page={}, size={}", authorId, userId, page, size);
        
        return postService.getUserPosts(authorId, userId, page, size);
    }
    
    /**
     * 获取关注用户的动态列表
     */
    @GetMapping("/posts/following")
    public Result<List<PostDTO>> getFollowingPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("获取关注动态列表: userId={}, page={}, size={}", userId, page, size);
        
        return postService.getFollowingPosts(userId, page, size);
    }
    
    /**
     * 获取推荐动态列表
     */
    @GetMapping("/posts/recommend")
    public Result<List<PostDTO>> getRecommendPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取推荐动态列表: currentUserId={}, page={}, size={}", userId, page, size);
        
        return postService.getRecommendPosts(userId, page, size);
    }
    
    /**
     * 动态点赞
     */
    @PostMapping("/posts/{postId}/like")
    public Result<Boolean> likePost(@PathVariable Long postId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("动态点赞: postId={}, userId={}", postId, userId);
        
        return postService.toggleLike(postId, userId);
    }
    
    /**
     * 动态收藏
     */
    @PostMapping("/posts/{postId}/collect")
    public Result<Boolean> collectPost(@PathVariable Long postId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("动态收藏: postId={}, userId={}", postId, userId);
        
        return postService.toggleCollect(postId, userId);
    }
    
    /**
     * 获取用户收藏的动态列表
     */
    @GetMapping("/posts/collected")
    public Result<List<PostDTO>> getCollectedPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("获取收藏动态列表: userId={}, page={}, size={}", userId, page, size);
        
        return postService.getCollectedPosts(userId, page, size);
    }
    
    /**
     * 删除动态
     */
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("删除动态: postId={}, userId={}", postId, userId);
        
        return postService.deletePost(postId, userId);
    }
    
    // ========== 评论相关接口 ==========
    
    /**
     * 获取动态评论列表
     */
    @GetMapping("/posts/{postId}/comments")
    public Result<List<CommentDTO>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取动态评论列表: postId={}, userId={}, page={}, size={}", postId, userId, page, size);
        
        return postService.getComments(postId, userId, page, size);
    }
    
    /**
     * 发表评论
     */
    @PostMapping("/posts/{postId}/comment")
    public Result<CommentDTO> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> commentMap) {
        Long userId = UserIdHolder.getUserId().longValue();
        String content = commentMap.get("content");
        log.info("发表评论: postId={}, userId={}, content={}", postId, userId, content);
        
        return postService.addComment(postId, userId, content);
    }
    
    /**
     * 切换评论点赞状态（原评论点赞接口，现在实现切换逻辑）
     */
    @PostMapping("/comments/{commentId}/like")
    public Result<Boolean> toggleCommentLike(@PathVariable Long commentId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("切换评论点赞状态: commentId={}, userId={}", commentId, userId);
        
        return postService.likeComment(commentId, userId);
    }
    
    /**
     * 取消评论点赞
     */
    @PostMapping("/comments/{commentId}/unlike")
    public Result<Boolean> unlikeComment(@PathVariable Long commentId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("取消评论点赞: commentId={}, userId={}", commentId, userId);
        
        return postService.unlikeComment(commentId, userId);
    }
    
    /**
     * 回复评论
     * 支持回复评论和回复回复，根据数据库设计：
     * 1. 直接回复评论：parent_id=评论ID，root_id=评论ID
     * 2. 回复评论的回复：parent_id=被回复的回复ID，root_id=顶级评论ID
     */
    @PostMapping("/comments/{commentId}/reply")
    public Result<ReplyDTO> addReply(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> replyMap) {
        Long userId = UserIdHolder.getUserId().longValue();
        String content = (String) replyMap.get("content");
        
        // 获取回复相关参数
        Long replyToUserId = null;
        Long parentId = null;
        Long rootId = null;
        
        if (replyMap.get("replyToUserId") != null) {
            replyToUserId = Long.parseLong(replyMap.get("replyToUserId").toString());
        }
        
        if (replyMap.get("parentId") != null) {
            parentId = Long.parseLong(replyMap.get("parentId").toString());
        }
        
        if (replyMap.get("rootId") != null) {
            rootId = Long.parseLong(replyMap.get("rootId").toString());
        }
        
        log.info("回复评论: commentId={}, userId={}, content={}, replyToUserId={}, parentId={}, rootId={}", 
                commentId, userId, content, replyToUserId, parentId, rootId);
        
        return postService.addReply(commentId, userId, content, replyToUserId, parentId, rootId);
    }
    
    /**
     * 获取评论回复列表
     */
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<ReplyDTO>> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserIdHolder.getUserId() != null ? UserIdHolder.getUserId().longValue() : null;
        log.info("获取评论回复列表: commentId={}, userId={}, page={}, size={}", commentId, userId, page, size);
        
        return postService.getReplies(commentId, userId, page, size);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Boolean> deleteComment(@PathVariable Long commentId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("删除评论: commentId={}, userId={}", commentId, userId);
        
        return postService.deleteComment(commentId, userId);
    }
    
    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public Result<Boolean> deleteReply(@PathVariable Long replyId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("删除回复: replyId={}, userId={}", replyId, userId);
        
        return postService.deleteReply(replyId, userId);
    }
    
    /**
     * 回复点赞
     */
    @PostMapping("/replies/{replyId}/like")
    public Result<Boolean> likeReply(@PathVariable Long replyId) {
        Long userId = UserIdHolder.getUserId().longValue();
        log.info("回复点赞: replyId={}, userId={}", replyId, userId);
        
        return postService.likeReply(replyId, userId);
    }
    
    // ========== 标签相关接口 ==========

    
    /**
     * 获取所有标签（分页）
     */
    //可用----------------------------------
    @GetMapping("/tags")
    public Result<List<TagDTO>> getAllTags(
            @RequestParam(defaultValue = "100") Integer limit) {
        log.info("获取所有标签: limit={}", limit);
        
        return postService.getAllTags(limit);
    }
}