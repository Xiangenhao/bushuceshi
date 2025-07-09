package org.example.afd.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.example.afd.mapper.PostMapper;
import org.example.afd.mapper.CommentMapper;
import org.example.afd.mapper.ReplyMapper;
import org.example.afd.mapper.UserMapper;
import org.example.afd.model.Result;
import org.example.afd.dto.CommentDTO;
import org.example.afd.dto.PostDTO;
import org.example.afd.dto.ReplyDTO;
import org.example.afd.dto.SubscriptionPlanDTO;
import org.example.afd.dto.TagDTO;
import org.example.afd.pojo.Comment;
import org.example.afd.pojo.CommentLike;
import org.example.afd.pojo.Post;
import org.example.afd.pojo.Reply;
import org.example.afd.pojo.ReplyLike;
import org.example.afd.pojo.User;
import org.example.afd.service.PostService;
import org.example.afd.utils.DateUtils;
import org.example.afd.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内容服务实现类
 * 
 * 该服务类实现了PostService接口中定义的所有方法
 * 主要分为以下几类功能：
 * 1. 动态内容相关 - 发布、查询、互动（点赞、收藏）
 * 2. 评论相关 - 评论的增删查、点赞
 * 3. 回复相关 - 回复的增删查、点赞
 * 4. 订阅计划相关 - 创建、查询、更新、删除、订阅操作
 * 5. 标签相关 - 热门标签、标签搜索等
 */
@Service
@Slf4j
public class PostServiceImpl implements PostService {

    @Autowired 
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderService orderService;
    
    // =====================================================
    // ============== 一、动态内容相关方法 =================
    // =====================================================

    /**
     * 创建动态
     * 
     * 处理流程：
     * 1. 创建Post对象并设置基本信息
     * 2. 插入动态记录到数据库
     * 3. 处理媒体文件（如图片、视频）
     * 4. 处理标签信息
     * 5. 返回创建后的动态详情
     *
     * @param postDTO 动态数据传输对象，包含动态的内容、媒体等信息
     * @return 创建结果，包含完整的动态信息
     */
    @Override
    @Transactional
    public Result<PostDTO> createPost(PostDTO postDTO) {
        try {
            log.info("创建动态: userId={}, content={}", postDTO.getUserId(), postDTO.getContent());
            
            // 创建Post对象
            Post post = new Post();
            post.setUserId(postDTO.getUserId().intValue());
            post.setContent(postDTO.getContent());
            post.setVisibilityType(postDTO.getVisibilityType());
            post.setLocation(postDTO.getLocation());
            post.setStatus(1); // 设置状态为正常
            
            // 插入动态记录到post_content表
            postMapper.insertPost(post);
            
            // 获取插入后的动态ID
            Long postId = post.getPostId();
            if (postId == null) {
                return Result.error("发布动态失败");
            }
            
            // 处理媒体文件
            if (postDTO.getMediaUrls() != null && !postDTO.getMediaUrls().isEmpty()) {
                // 根据媒体类型处理
                for (int i = 0; i < postDTO.getMediaUrls().size(); i++) {
                    String mediaUrl = postDTO.getMediaUrls().get(i);
                    
                    // 插入媒体记录到post_media表
                    postMapper.insertPostMedia(postId, mediaUrl, i, postDTO.getMediaType());
                }
            }
            
            // 处理标签
            if (postDTO.getTags() != null && !postDTO.getTags().isEmpty()) {
                log.info("处理动态标签，数量: {}", postDTO.getTags().size());
                
                for (TagDTO tag : postDTO.getTags()) {
                    // 获取标签名
                    String tagName = tag.getTagName();
                    Long tagId = tag.getTagId();
                    
                    log.info("处理标签: id={}, name={}", tagId, tagName);
                    
                    // 如果标签ID为空但有标签名，先查找是否存在
                    if (tagId == null && tagName != null && !tagName.trim().isEmpty()) {
                        tagId = postMapper.getTagIdByName(tagName);
                        
                        // 如果标签不存在则创建
                        if (tagId == null) {
                            log.info("标签不存在，创建新标签: {}", tagName);
                            postMapper.createTag(tagName);
                            tagId = postMapper.getTagIdByName(tagName);
                        }
                    }
                    
                    // 创建关联
                    if (tagId != null) {
                        log.info("创建动态-标签关联: postId={}, tagId={}", postId, tagId);
                        postMapper.createPostTagRelation(postId, tagId);
                        // 更新标签使用次数
                        postMapper.updateTagUseCount(tagId);
                    } else {
                        log.warn("无法处理标签: {}, 缺少有效的标签ID", tagName);
                    }
                }
            }
            
            // 处理可见性 - 对于指定订阅计划的情况
            if (postDTO.getVisibilityType() == 2 && postDTO.getPlanId() != null) {
                log.info("处理订阅计划可见性: postId={}, planId={}", postId, postDTO.getPlanId());
                // 插入可见性关联记录到post_content_visibility表
                boolean success = postMapper.insertPostVisibility(postId, postDTO.getPlanId());
                if (success) {
                    log.info("成功创建内容可见性关联: postId={}, planId={}", postId, postDTO.getPlanId());
                } else {
                    log.error("创建内容可见性关联失败: postId={}, planId={}", postId, postDTO.getPlanId());
                }
            }
            
            // 获取刚插入的动态详情并返回
            Post post2 = postMapper.getPostById(postId);
            PostDTO newPost = new PostDTO();
            
            // 手动转换Post为PostDTO
            if (post2 != null) {
                newPost.setPostId(post2.getPostId());
                newPost.setUserId(post2.getUserId().longValue());
                newPost.setContent(post2.getContent());
                newPost.setVisibilityType(post2.getVisibilityType());
                newPost.setLikeCount(post2.getLikeCount());
                newPost.setCommentCount(post2.getCommentCount());
                newPost.setCollectCount(post2.getCollectCount());
                newPost.setViewCount(post2.getViewCount());
                newPost.setCreateTime(post2.getCreateTime());
                newPost.setUsername(post2.getUsername());
                newPost.setUserAvatarUrl(post2.getUserAvatarUrl());
                
                // 获取动态标签
                List<TagDTO> postTags = postMapper.getPostTagsDTO(postId.intValue());
                if (postTags != null) {
                    newPost.setTags(postTags);
                    log.info("获取到动态标签: {}", postTags.size());
                } else {
                    newPost.setTags(new ArrayList<>());
                    log.info("动态没有标签");
                }
                
                // 获取动态媒体
                List<String> mediaUrls = postMapper.getPostMediaUrls(postId);
                newPost.setMediaUrls(mediaUrls);
                
                // 如果是指定订阅可见，设置订阅计划ID
                if (post2.getVisibilityType() == 2) {
                    Long planId = postMapper.getPostVisibilityPlanId(postId);
                    newPost.setPlanId(planId);
                    log.info("设置返回数据的订阅计划ID: {}", planId);
                }
            }
            
            return Result.success(newPost);
        } catch (Exception e) {
            log.error("发布动态失败", e);
            return Result.error("发布动态失败: " + e.getMessage());
        }
    }

    /**
     * 获取动态详情
     * 
     * 处理流程：
     * 1. 根据ID获取动态基本信息
     * 2. 增加动态浏览量
     * 3. 获取用户是否点赞、收藏等状态
     * 4. 获取动态相关的标签、媒体等信息
     * 5. 组装完整的动态信息
     *
     * @param postId 动态ID
     * @param userId 当前用户ID，用于判断是否点赞、收藏等
     * @return 动态详情，包含完整的数据
     */
    @Override
    public Result<PostDTO> getPostDetail(Long postId, Long userId) {
        try {
            // 获取动态基本信息
            Post post = postMapper.getPostById(postId);
            if (post == null) {
                return Result.error("动态不存在");
            }
            
            // 增加浏览量
            postMapper.incrementViewCount(postId);
            
            // 构建返回对象
            PostDTO postDTO = new PostDTO();
            postDTO.setPostId(post.getPostId());
            postDTO.setUserId(post.getUserId().longValue());
            postDTO.setContent(post.getContent());
            postDTO.setVisibilityType(post.getVisibilityType());
            postDTO.setLikeCount(post.getLikeCount());
            postDTO.setCommentCount(post.getCommentCount());
            postDTO.setCollectCount(post.getCollectCount());
            postDTO.setViewCount(post.getViewCount() + 1); // 加上刚增加的1次浏览
            postDTO.setCreateTime(post.getCreateTime());
            postDTO.setUsername(post.getUsername());
            postDTO.setUserAvatarUrl(post.getUserAvatarUrl());
            
            // 获取用户是否点赞、收藏
            if (userId != null) {
                boolean isLiked = postMapper.isPostLiked(postId, userId);
                boolean isCollected = postMapper.isPostCollected(postId, userId);
                postDTO.setIsLiked(isLiked);
                postDTO.setIsCollected(isCollected);
                
                // 查询是否关注了作者
                User author = userMapper.selectById(post.getUserId().intValue());
                if (author != null && !userId.equals(author.getUserId().longValue())) {
                    boolean isFollowing = userMapper.isUserFollowing(userId.intValue(), author.getUserId());
                    postDTO.setIsFollowing(isFollowing);
                }
            }
            
            // 获取动态标签
            List<TagDTO> postTags = postMapper.getPostTagsDTO(postId.intValue());
            if (postTags != null) {
                postDTO.setTags(postTags);
            } else {
                postDTO.setTags(new ArrayList<>());
            }
            
            // 获取动态媒体
            List<String> mediaUrls = postMapper.getPostMediaUrls(postId);
            postDTO.setMediaUrls(mediaUrls);
            
            // 获取作者信息
            if (post.getUserId() != null) {
                User author = userMapper.selectById(post.getUserId().intValue());
                if (author != null) {
                    int followerCount = userMapper.getUserFollowerCount(author.getUserId());
                    int postCount = postMapper.getUserPosts(author.getUserId().longValue(), 0, 1).size();
                    postDTO.setFollowerCount(followerCount);
                    postDTO.setPostCount(postCount);
                }
            }
            
            return Result.success(postDTO);
        } catch (Exception e) {
            log.error("获取动态详情失败", e);
            return Result.error("获取动态详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除动态
     * 
     * 处理流程：
     * 1. 验证用户是否有权限删除该动态
     * 2. 更新动态状态为已删除
     *
     * @param postId 动态ID
     * @param userId 当前用户ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public Result<Void> deletePost(Long postId, Long userId) {
        try {
            log.info("删除动态: postId={}, userId={}", postId, userId);
            
            // 验证动态是否存在
            Post post = postMapper.getPostById(postId);
            if (post == null) {
                log.error("动态不存在: postId={}", postId);
                return Result.error("动态不存在");
            }
            
            log.info("找到动态: postId={}, 作者ID={}, 当前用户ID={}", postId, post.getUserId(), userId);
            
            // 验证用户是否有权限删除（需要类型转换）
            if (!Long.valueOf(post.getUserId()).equals(userId)) {
                log.error("权限验证失败: 动态作者ID={}, 当前用户ID={}", post.getUserId(), userId);
                return Result.error("您没有权限删除此动态");
            }
            
            log.info("权限验证通过，开始删除动态: postId={}", postId);
            
            // 执行删除操作（逻辑删除）
            int rows = postMapper.deletePost(postId, userId);
            if (rows > 0) {
                log.info("删除动态成功: postId={}, 影响行数={}", postId, rows);
                return Result.success("删除成功", null);
            } else {
                log.error("删除动态失败: postId={}, 影响行数={}", postId, rows);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除动态异常: postId={}, userId={}", postId, userId, e);
            return Result.error("删除动态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户发布的动态列表
     *
     * @param authorId 作者ID
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    @Override
    public Result<List<PostDTO>> getUserPosts(Long authorId, Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;
            List<Post> posts = postMapper.getUserPosts(authorId, offset, size);
            List<PostDTO> postDTOs = convertPostsToDTOs(posts, userId);
            log.info("获取到内容，准备返回数据");
            return Result.success(postDTOs);
        } catch (Exception e) {
            log.error("获取用户动态列表失败", e);
            return Result.error("获取用户动态列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取关注用户的动态列表
     *
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    @Override
    public Result<List<PostDTO>> getFollowingPosts(Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;
            List<Post> posts = postMapper.getFollowingPosts(userId, offset, size);
            List<PostDTO> postDTOs = convertPostsToDTOs(posts, userId);
            return Result.success(postDTOs);
        } catch (Exception e) {
            log.error("获取关注用户动态列表失败", e);
            return Result.error("获取关注用户动态列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取推荐动态列表
     *
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    @Override
    public Result<List<PostDTO>> getRecommendPosts(Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;
            List<Post> posts = postMapper.getRecommendPosts(offset, size);
            List<PostDTO> postDTOs = convertPostsToDTOs(posts, userId);
            return Result.success(postDTOs);
        } catch (Exception e) {
            log.error("获取推荐动态列表失败", e);
            return Result.error("获取推荐动态列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 点赞/取消点赞动态
     *
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 操作结果，true表示当前状态为已点赞，false表示当前状态为未点赞
     */
    @Override
    @Transactional
    public Result<Boolean> toggleLike(Long postId, Long userId) {
        try {
            // 验证动态是否存在
            Post post = postMapper.getPostById(postId);
            if (post == null) {
                return Result.error("动态不存在");
            }
            
            // 判断当前状态
            boolean isLiked = postMapper.isPostLiked(postId, userId);
            Date now = new Date();
            
            if (isLiked) {
                // 已点赞，取消点赞
                log.info("取消点赞");
                postMapper.updatePostInteractionStatus(userId, postId, 1, 0); // 1表示点赞类型
                postMapper.updateLikeCount(postId, -1);
                return Result.success("取消点赞成功", false);
            } else {
                // 未点赞，添加点赞
                // 先检查是否有记录
                boolean hasRecord = postMapper.isPostLiked(postId, userId);
                if (hasRecord) {
                    // 有记录但状态为取消，更新状态
                    log.info("有记录但状态为取消，更新状态");
                    postMapper.updatePostInteractionStatus(userId, postId, 1, 1);
                } else {
                    // 无记录，新增
                    log.info("无记录，新增");
                    postMapper.insertPostInteraction(userId, postId, 1, now, 1);
                }
                
                // 更新点赞数
                postMapper.updateLikeCount(postId, 1);
                return Result.success("点赞成功", true);
            }
        } catch (Exception e) {
            log.error("操作点赞失败", e);
            return Result.error("操作点赞失败: " + e.getMessage());
        }
    }
    
    /**
     * 收藏/取消收藏动态
     *
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 操作结果，true表示当前状态为已收藏，false表示当前状态为未收藏
     */
    @Override
    @Transactional
    public Result<Boolean> toggleCollect(Long postId, Long userId) {
        try {
            // 验证动态是否存在
            Post post = postMapper.getPostById(postId);
            if (post == null) {
                return Result.error("动态不存在");
            }
            
            // 判断当前状态
            boolean isCollected = postMapper.isPostCollected(postId, userId);
            Date now = new Date();
            
            if (isCollected) {
                // 已收藏，取消收藏
                postMapper.updatePostInteractionStatus(userId, postId, 2, 0); // 2表示收藏类型
                postMapper.updateCollectCount(postId, -1);
                return Result.success("取消收藏成功", false);
            } else {
                // 未收藏，添加收藏
                // 先检查是否有记录
                boolean hasRecord = postMapper.isPostCollected(postId, userId);
                if (hasRecord) {
                    // 有记录但状态为取消，更新状态
                    postMapper.updatePostInteractionStatus(userId, postId, 2, 1);
                } else {
                    // 无记录，新增
                    postMapper.insertPostInteraction(userId, postId, 2, now, 1);
                }
                
                // 更新收藏数
                postMapper.updateCollectCount(postId, 1);
                return Result.success("收藏成功", true);
            }
        } catch (Exception e) {
            log.error("操作收藏失败", e);
            return Result.error("操作收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户收藏的动态列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 动态列表
     */
    @Override
    public Result<List<PostDTO>> getCollectedPosts(Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;
            List<Post> posts = postMapper.getCollectedPosts(userId, offset, size);
            List<PostDTO> postDTOs = convertPostsToDTOs(posts, userId);
            return Result.success(postDTOs);
        } catch (Exception e) {
            log.error("获取收藏动态列表失败", e);
            return Result.error("获取收藏动态列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Post列表转换为PostDTO列表
     * 
     * @param posts Post实体列表
     * @param userId 当前用户ID
     * @return PostDTO列表
     */
    private List<PostDTO> convertPostsToDTOs(List<Post> posts, Long userId) {
        List<PostDTO> dtoList = new ArrayList<>();
        
        for (Post post : posts) {
            PostDTO dto = new PostDTO();
            dto.setPostId(post.getPostId());
            dto.setUserId(post.getUserId().longValue());
            dto.setContent(post.getContent());
            dto.setVisibilityType(post.getVisibilityType());
            dto.setLikeCount(post.getLikeCount());
            dto.setCommentCount(post.getCommentCount());
            dto.setCollectCount(post.getCollectCount());
            dto.setViewCount(post.getViewCount());
            dto.setCreateTime(post.getCreateTime());
            dto.setUsername(post.getUsername());
            
            // 设置头像 - 会同时设置userAvatarUrl和avatar两个字段
            dto.setUserAvatarUrl(post.getUserAvatarUrl());
            
            if (post.getUserAvatarUrl() != null) {
                log.debug("设置用户头像: avatar={}", post.getUserAvatarUrl());
            } else {
                log.warn("用户头像为空: userId={}, postId={}", post.getUserId(), post.getPostId());
            }
            
            // 获取媒体和标签
            List<String> mediaUrls = postMapper.getPostMediaUrls(post.getPostId());
            List<TagDTO> postTags = postMapper.getPostTagsDTO(post.getPostId().intValue());
            
            // 设置媒体URL
            dto.setMediaUrls(mediaUrls);
            
            // 设置mediaType
            if (mediaUrls != null && !mediaUrls.isEmpty()) {
                // 查询该帖子的媒体类型
                Integer mediaType = postMapper.getPostMediaType(post.getPostId());
                if (mediaType != null) {
                    dto.setMediaType(mediaType);
                    log.debug("设置媒体类型: postId={}, mediaType={}, mediaUrls.size={}", 
                             post.getPostId(), mediaType, mediaUrls.size());
                } else {
                    // 如果没有查到类型但有URL，默认设为图片类型
                    dto.setMediaType(1); // 1-图片
                    log.warn("未查到媒体类型但有URL, 默认设为图片类型: postId={}, mediaUrls.size={}", 
                            post.getPostId(), mediaUrls.size());
                }
            } else {
                // 没有媒体
                dto.setMediaType(0); // 0-无媒体
            }
            
            // 设置标签
            if (postTags != null) {
                dto.setTags(postTags);
            } else {
                dto.setTags(new ArrayList<>());
            }
            
            // 获取交互状态
            if (userId != null) {
                boolean isLiked = postMapper.isPostLiked(post.getPostId(), userId);
                boolean isCollected = postMapper.isPostCollected(post.getPostId(), userId);
                dto.setIsLiked(isLiked);
                dto.setIsCollected(isCollected);
            }
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }
    
    // =====================================================
    // ============== 二、评论相关方法 =====================
    // =====================================================
    
    /**
     * 获取帖子评论列表
     * 
     * 处理流程:
     * 1. 分页查询评论
     * 2. 格式化时间显示
     * 3. 返回评论列表
     *
     * @param postId 帖子ID
     * @param userId 当前用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表，包含点赞状态和格式化时间
     */
    @Override
    public Result<List<CommentDTO>> getComments(Long postId, Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;
            // 获取评论列表，带点赞状态
            Integer userIdInt = (userId != null) ? userId.intValue() : null;
            List<CommentDTO> comments = commentMapper.getCommentsWithLikeStatus(postId, userIdInt, offset, size);
            
            log.info("查询评论列表: postId={}, userId={}, 评论数量={}", postId, userId, comments.size());
            
            // 格式化时间，使用带时区的格式
            for (CommentDTO comment : comments) {
                // 使用带时区的ISO格式
                if (comment.getCreateTime() != null) {
                    // 设置易读的相对时间描述
                    comment.setFormattedTime(DateUtils.getTimeAgo(comment.getCreateTime()));
                }
                
                // 格式化回复时间，同样带时区信息
                if (comment.getReplies() != null) {
                    for (ReplyDTO reply : comment.getReplies()) {
                        if (reply.getCreateTime() != null) {
                            // 设置易读的相对时间描述
                            reply.setFormattedTime(DateUtils.getTimeAgo(reply.getCreateTime()));
                        }
                    }
                }
            }
            
            return Result.success(comments);
        } catch (Exception e) {
            log.error("获取评论失败", e);
            return Result.error("获取评论失败: " + e.getMessage());
        }
    }

    /**
     * 添加评论
     * 
     * 处理流程:
     * 1. 验证用户和动态
     * 2. 创建并保存评论
     * 3. 更新动态评论数
     * 4. 返回新创建的评论信息
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param content 评论内容
     * @return 新创建的评论
     */
    @Override
    @Transactional
    public Result<CommentDTO> addComment(Long postId, Long userId, String content) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                return Result.error("用户不存在，无法发表评论");
            }
            
            // 检查动态是否存在
            Post post = postMapper.getPostById(postId);
            if (post == null) {
                return Result.error("动态不存在");
            }
            
            // 检查动态状态是否正常
            if (post.getStatus() != 1) {
                return Result.error("该动态已被删除或审核中，无法评论");
            }
            
            // 创建评论对象
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(userId.intValue());
            comment.setContent(content);
            comment.setLikeCount(0);
            comment.setReplyCount(0);
            comment.setStatus(0); // 正常状态
            LocalDateTime now = LocalDateTime.now();
            comment.setCreateTime(now);
            
            // 插入评论
            commentMapper.insertComment(comment);
            
            // 更新动态评论数
            postMapper.updateCommentCount(postId, 1);
            
            // 获取刚创建的评论信息
            CommentDTO commentDTO = commentMapper.getCommentWithLikeStatus(comment.getCommentId(), userId.intValue());
            commentDTO.setFormattedTime(DateUtils.getTimeAgo(commentDTO.getCreateTime()));
            
            return Result.success("评论成功", commentDTO);
        } catch (Exception e) {
            log.error("评论失败", e);
            return Result.error("评论失败: " + e.getMessage());
        }
    }

    /**
     * 点赞评论
     * 
     * 处理流程:
     * 1. 验证用户和评论
     * 2. 根据当前状态决定点赞或取消点赞
     * 3. 更新评论点赞数
     * 4. 返回新的点赞状态
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 操作结果，true表示已点赞，false表示未点赞
     */
    @Override
    @Transactional
    public Result<Boolean> likeComment(Long commentId, Long userId) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                return Result.error("用户不存在，无法点赞评论");
            }
            
            // 检查评论是否存在
            CommentDTO comment = commentMapper.getCommentWithLikeStatus(commentId, userId.intValue());
            if (comment == null) {
                return Result.error("评论不存在");
            }
            
            // 检查评论状态
            if (comment.getStatus() != 1) {
                return Result.error("该评论已被删除或审核中，无法点赞");
            }
            
            // 检查评论所属的帖子是否存在和正常
            Post post = postMapper.getPostById(comment.getPostId());
            if (post == null || post.getStatus() != 1) {
                return Result.error("该评论所属的动态不存在或已被删除，无法点赞");
            }
            
            // 判断当前是否已点赞
            boolean isCurrentlyLiked = comment.getIsLiked() != null && comment.getIsLiked();
            
            if (isCurrentlyLiked) {
                // 已经点赞，取消点赞
                CommentLike commentLike = commentMapper.getCommentLike(commentId, userId.intValue());
                commentLike.setStatus(0); // 设置为无效
                commentLike.setCreateTime(LocalDateTime.now());
                commentMapper.updateCommentLike(commentLike);
                
                // 更新评论点赞数
                commentMapper.updateCommentLikeCount(commentId, -1);
                
                return Result.success("取消点赞成功", false);
            } else {
                // 未点赞，添加点赞
                CommentLike commentLike = commentMapper.getCommentLike(commentId, userId.intValue());
                if (commentLike == null) {
                    // 从未点过赞，新增记录
                    commentLike = new CommentLike();
                    commentLike.setCommentId(commentId);
                    commentLike.setUserId(userId.intValue());
                    commentLike.setCreateTime(LocalDateTime.now());
                    commentLike.setStatus(1); // 有效状态
                    commentMapper.insertCommentLike(commentLike);
                } else {
                    // 曾经点过赞但取消了，更新状态
                    commentLike.setStatus(1); // 设置为有效
                    commentLike.setCreateTime(LocalDateTime.now());
                    commentMapper.updateCommentLike(commentLike);
                }
                
                // 更新评论点赞数
                commentMapper.updateCommentLikeCount(commentId, 1);
                
                return Result.success("点赞成功", true);
            }
        } catch (Exception e) {
            log.error("点赞评论失败", e);
            return Result.error("点赞评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> unlikeComment(Long commentId, Long userId) {
        try {
            // 检查评论是否存在
            CommentDTO comment = commentMapper.getCommentWithLikeStatus(commentId, userId.intValue());
            if (comment == null) {
                return Result.error("评论不存在");
            }
            
            // 判断当前是否已点赞
            boolean isCurrentlyLiked = comment.getIsLiked() != null && comment.getIsLiked();
            
            if (isCurrentlyLiked) {
                // 已点赞，执行取消点赞操作
                CommentLike commentLike = commentMapper.getCommentLike(commentId, userId.intValue());
                commentLike.setStatus(0); // 设置为无效
                commentLike.setCreateTime(LocalDateTime.now());
                commentMapper.updateCommentLike(commentLike);
                
                // 更新评论点赞数
                commentMapper.updateCommentLikeCount(commentId, -1);
                
                return Result.success("取消点赞成功", false);
            } else {
                // 未点赞，无需操作
                return Result.success("未点赞状态，无需取消", false);
            }
        } catch (Exception e) {
            log.error("取消点赞评论失败", e);
            return Result.error("取消点赞评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除评论
     * 
     * 处理流程:
     * 1. 验证用户是否有权限删除该评论
     * 2. 级联删除该评论下的所有回复
     * 3. 更新评论状态为已删除
     * 4. 更新帖子评论数（包括评论本身和所有回复）
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> deleteComment(Long commentId, Long userId) {
        try {
            // 检查评论是否存在
            CommentDTO comment = commentMapper.getComment(commentId);
            if (comment == null) {
                return Result.error("评论不存在");
            }
            
            // 验证用户是否有权限删除
            if (!comment.getUserId().equals(userId.intValue())) {
                // 检查用户是否是帖子作者
                Post post = postMapper.getPostById(comment.getPostId());
                if (post == null || !post.getUserId().equals(userId.intValue())) {
                    return Result.error("您没有权限删除此评论");
                }
            }
            
            // 获取该评论下的所有回复数量（用于更新动态评论数）
            int replyCount = comment.getReplyCount();
            log.info("删除评论及其回复: commentId={}, 回复数量={}", commentId, replyCount);
            
            // 先删除该评论下的所有回复
            int deletedReplies = replyMapper.deleteRepliesByCommentId(commentId);
            log.info("已删除回复数量: {}", deletedReplies);
            
            // 更新评论状态为已删除
            int result = commentMapper.updateCommentStatus(commentId, 0); // 0表示删除
            
            if (result > 0) {
                // 更新帖子评论数：减去评论本身(1) + 所有回复数量
                int totalDecrease = 1 + replyCount;
                postMapper.updateCommentCount(comment.getPostId(), -totalDecrease);
                log.info("已更新动态评论数，减少: {}", totalDecrease);
                
                return Result.success("删除评论成功", true);
            } else {
                return Result.error("删除评论失败");
            }
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }
    
    // =====================================================
    // ============== 三、回复相关方法 =====================
    // =====================================================
    
    /**
     * 添加回复
     * 支持回复评论和回复回复，根据数据库设计：
     * 1. 直接回复评论：parent_id=评论ID，root_id=评论ID
     * 2. 回复评论的回复：parent_id=被回复的回复ID，root_id=顶级评论ID
     * 
     * @param commentId 顶级评论ID（用于API调用）
     * @param userId 用户ID
     * @param content 回复内容
     * @param replyToUserId 被回复的用户ID
     * @param parentId 父评论ID（数据库中的parent_id）
     * @param rootId 根评论ID（数据库中的root_id）
     * @return 新创建的回复
     */
    @Override
    @Transactional
    public Result<ReplyDTO> addReply(Long commentId, Long userId, String content, 
                                   Long replyToUserId, Long parentId, Long rootId) {
        try {
            log.info("开始添加回复: commentId={}, userId={}, content={}, replyToUserId={}, parentId={}, rootId={}", 
                    commentId, userId, content, replyToUserId, parentId, rootId);
            
            // 检查用户是否存在
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                log.error("用户不存在: userId={}", userId);
                return Result.error("用户不存在，无法发表回复");
            }
            
            // 检查顶级评论是否存在
            CommentDTO comment = commentMapper.getComment(commentId);
            if (comment == null) {
                log.error("顶级评论不存在: commentId={}", commentId);
                return Result.error("评论不存在");
            }
            
            // 检查评论状态是否正常
            if (comment.getStatus() != 1) { // 1表示正常状态
                log.error("评论状态异常: commentId={}, status={}", commentId, comment.getStatus());
                return Result.error("该评论已被删除或审核中，无法回复");
            }
            
            // 检查评论所属的动态是否存在和正常
            Post post = postMapper.getPostById(comment.getPostId());
            if (post == null || post.getStatus() != 1) {
                log.error("动态不存在或状态异常: postId={}, status={}", 
                         comment.getPostId(), post != null ? post.getStatus() : "null");
                return Result.error("该评论所属的动态不存在或已被删除，无法回复");
            }
            
            // 创建回复对象
            Reply reply = new Reply();
            reply.setUserId(userId.intValue());
            reply.setPostId(comment.getPostId());
            reply.setParentId(parentId); // 使用传入的parentId
            reply.setRootId(rootId); // 使用传入的rootId
            reply.setContent(content);
            reply.setLikeCount(0);
            reply.setReplyCount(0);
            reply.setStatus(1); // 1表示正常状态
            LocalDateTime now = LocalDateTime.now();
            reply.setCreateTime(now);
            
            log.info("创建回复对象: parentId={}, rootId={}, content={}", parentId, rootId, content);
            
            // 插入回复
            replyMapper.insertReply(reply);
            log.info("回复插入成功: replyId={}", reply.getReplyId());
            
            // 更新顶级评论的回复数
            commentMapper.updateCommentReplyCount(commentId, 1);
            log.info("更新顶级评论回复数: commentId={}, 增加1", commentId);
            
            // 如果有根评论且不等于顶级评论，也更新根评论回复数
            if (rootId != null && !rootId.equals(commentId)) {
                commentMapper.updateCommentReplyCount(rootId, 1);
                log.info("更新根评论回复数: rootId={}, 增加1", rootId);
            }
            
            // 更新动态评论数
            postMapper.updateCommentCount(reply.getPostId(), 1);
            log.info("更新动态评论数: postId={}, 增加1", reply.getPostId());
            
            // 构建返回的ReplyDTO
            ReplyDTO replyDTO = new ReplyDTO();
            replyDTO.setReplyId(reply.getReplyId().longValue());
            replyDTO.setUserId(reply.getUserId().longValue());
            replyDTO.setUsername(user.getUsername());
            replyDTO.setAvatar(user.getAvatar());
            replyDTO.setContent(reply.getContent());
            replyDTO.setLikeCount(reply.getLikeCount());
            replyDTO.setRootId(reply.getRootId());
            replyDTO.setCreateTime(reply.getCreateTime());
            replyDTO.setIsLiked(false); // 新创建的回复，当前用户未点赞
            
            // 设置被回复的用户信息 - 根据数据库设计规则
            // 1. parent_id = root_id：直接回复评论，不设置replyTo信息
            // 2. parent_id != root_id：回复回复，设置被回复用户信息
            if (parentId != null && rootId != null) {
                if (parentId.equals(rootId)) {
                    // 直接回复评论的情况，不设置replyToUserId和replyToUsername
                    replyDTO.setReplyToUserId(null);
                    replyDTO.setReplyToUsername(null);
                    log.info("直接回复评论，不设置被回复用户信息: commentId={}", commentId);
                } else {
                    // 回复回复的情况，设置被回复用户信息
                    if (replyToUserId != null) {
                        User replyToUser = userMapper.selectById(replyToUserId.intValue());
                        if (replyToUser != null) {
                            replyDTO.setReplyToUserId(replyToUserId);
                            replyDTO.setReplyToUsername(replyToUser.getUsername());
                            log.info("设置被回复用户信息: replyToUserId={}, replyToUsername={}", 
                                    replyToUserId, replyToUser.getUsername());
                        } else {
                            // 如果找不到被回复的用户，不设置replyTo信息
                            replyDTO.setReplyToUserId(null);
                            replyDTO.setReplyToUsername(null);
                            log.warn("找不到被回复的用户: replyToUserId={}", replyToUserId);
                        }
                    } else {
                        // 通过parentId查询被回复的用户信息
                        try {
                            ReplyDTO parentReply = replyMapper.getReplyById(parentId);
                            if (parentReply != null) {
                                replyDTO.setReplyToUserId(parentReply.getUserId());
                                replyDTO.setReplyToUsername(parentReply.getUsername());
                                log.info("通过parentId查询到被回复用户信息: parentId={}, replyToUsername={}", 
                                        parentId, parentReply.getUsername());
                            } else {
                                replyDTO.setReplyToUserId(null);
                                replyDTO.setReplyToUsername(null);
                                log.warn("找不到父回复: parentId={}", parentId);
                            }
                        } catch (Exception e) {
                            log.error("查询父回复失败: parentId={}, error={}", parentId, e.getMessage());
                            replyDTO.setReplyToUserId(null);
                            replyDTO.setReplyToUsername(null);
                        }
                    }
                }
            } else {
                // 数据异常情况
                replyDTO.setReplyToUserId(null);
                replyDTO.setReplyToUsername(null);
                log.warn("parentId或rootId为空，数据异常: parentId={}, rootId={}", parentId, rootId);
            }
            
            // 格式化时间
            if (replyDTO.getCreateTime() != null) {
                replyDTO.setFormattedTime(DateUtils.getTimeAgo(replyDTO.getCreateTime()));
            }
            
            log.info("回复创建成功: replyId={}, rootId={}", 
                    replyDTO.getReplyId(), replyDTO.getRootId());
            
            return Result.success("回复成功", replyDTO);
        } catch (Exception e) {
            log.error("回复失败: commentId={}, userId={}, error={}", commentId, userId, e.getMessage(), e);
            return Result.error("回复失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取评论的回复列表
     * 
     * 处理流程:
     * 1. 分页查询回复
     * 2. 格式化时间显示
     * 3. 返回回复列表
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID，用于查询点赞状态
     * @param page 页码
     * @param size 每页大小
     * @return 回复列表
     */
    @Override
    public Result<List<ReplyDTO>> getReplies(Long commentId, Long userId, Integer page, Integer size) {
        try {
            // 添加执行开始时间记录
            long startTime = System.currentTimeMillis();
            log.info("开始获取回复列表: commentId={}, userId={}, page={}, size={}, 开始时间={}", commentId, userId, page, size, startTime);
            
            // 检查评论是否存在
            CommentDTO comment = commentMapper.getCommentById(commentId);
            if (comment == null) {
                log.warn("评论不存在: commentId={}", commentId);
                return Result.error("评论不存在");
            }
            
            // 计算偏移量
            int offset = (page - 1) * size;
            log.info("查询参数: commentId={}, userId={}, offset={}, size={}", commentId, userId, offset, size);
            
            // 数据库查询开始时间
            long dbStartTime = System.currentTimeMillis();
            
            // 获取回复列表，包含点赞状态
            Integer userIdInt = (userId != null) ? userId.intValue() : null;
            List<ReplyDTO> replies = replyMapper.getRepliesWithLikeStatus(commentId, userIdInt, offset, size);
            
            long dbTime = System.currentTimeMillis() - dbStartTime;
            log.info("数据库查询完成, 耗时={}ms, 查询到回复数量={}", dbTime, replies != null ? replies.size() : 0);
            
            if (replies == null) {
                replies = new ArrayList<>();
            }
            
            // 数据处理开始时间
            long processingStartTime = System.currentTimeMillis();
            
            // 处理回复数据
            for (int i = 0; i < replies.size(); i++) {
                ReplyDTO reply = replies.get(i);
                
                // 检查回复内容是否为空
                if (reply.getContent() == null || reply.getContent().trim().isEmpty()) {
                    log.warn("回复内容为空, 设置默认值, replyId={}", reply.getReplyId());
                    reply.setContent("[该回复内容为空]");
                }
                
                // 正确设置被回复的用户信息
                // 根据数据库设计规则：
                // 1. parent_id和root_id都为null：对动态的直接评论（不应该出现在回复列表中）
                // 2. parent_id = root_id：对评论的直接回复，不需要显示"回复xxx"
                // 3. parent_id != root_id：对回复的回复，需要显示"回复被回复者的名称"
                
                log.debug("处理回复数据: replyId={}, parentId={}, rootId={}", 
                        reply.getReplyId(), reply.getParentId(), reply.getRootId());
                
                if (reply.getParentId() == null || reply.getRootId() == null) {
                    // 数据异常：回复列表中不应该有parent_id或root_id为空的记录
                    log.warn("回复数据异常，parent_id或root_id为空: replyId={}, parentId={}, rootId={}", 
                            reply.getReplyId(), reply.getParentId(), reply.getRootId());
                    reply.setReplyToUserId(null);
                    reply.setReplyToUsername(null);
                } else if (reply.getParentId().equals(reply.getRootId())) {
                    // 情况2：直接回复评论的情况，parent_id = root_id = 评论ID
                    // 不设置replyToUserId和replyToUsername，前端不显示"回复xxx"
                    reply.setReplyToUserId(null);
                    reply.setReplyToUsername(null);
                    log.debug("直接回复评论: replyId={}, parentId={}, rootId={}", 
                            reply.getReplyId(), reply.getParentId(), reply.getRootId());
                } else {
                    // 情况3：回复回复的情况，parent_id = 被回复的回复ID，root_id = 顶级评论ID
                    // 需要查询被回复的回复的作者信息
                    try {
                        ReplyDTO parentReply = replyMapper.getReplyById(reply.getParentId());
                        if (parentReply != null) {
                            reply.setReplyToUserId(parentReply.getUserId());
                            reply.setReplyToUsername(parentReply.getUsername());
                            log.debug("回复回复: replyId={}, parentId={}, rootId={}, replyToUser={}", 
                                    reply.getReplyId(), reply.getParentId(), reply.getRootId(), 
                                    parentReply.getUsername());
                        } else {
                            // 如果找不到父回复，可能已被删除，不显示回复指示
                            reply.setReplyToUserId(null);
                            reply.setReplyToUsername(null);
                            log.warn("找不到父回复，可能已被删除: replyId={}, parentId={}", 
                                    reply.getReplyId(), reply.getParentId());
                        }
                    } catch (Exception e) {
                        log.error("查询父回复失败: replyId={}, parentId={}, error={}", 
                                reply.getReplyId(), reply.getParentId(), e.getMessage());
                        // 查询失败时，不显示回复指示
                        reply.setReplyToUserId(null);
                        reply.setReplyToUsername(null);
                    }
                }
                
                // 格式化时间
                if (reply.getCreateTime() != null) {
                    // 设置易读的相对时间描述
                    String formattedTime = DateUtils.getTimeAgo(reply.getCreateTime());
                    reply.setFormattedTime(formattedTime);
                }
                
                // 记录点赞状态
                log.debug("回复点赞状态: replyId={}, userId={}, isLiked={}", 
                         reply.getReplyId(), userId, reply.getIsLiked());
            }
            
            log.info("回复数据处理完成, 处理耗时={}ms", System.currentTimeMillis() - processingStartTime);
            
            // 记录总处理时间
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("成功返回回复列表, commentId={}, userId={}, 回复数量={}, 请求数量={}, 总耗时={}ms", 
                    commentId, userId, replies.size(), size, totalTime);
            
            // 如果处理时间过长，记录警告
            if (totalTime > 500) { // 超过500ms视为较慢
                log.warn("获取回复列表耗时较长: commentId={}, userId={}, 总耗时={}ms", commentId, userId, totalTime);
            }
            
            return Result.success(replies);
        } catch (Exception e) {
            log.error("获取回复失败, commentId={}, userId={}, error={}", commentId, userId, e.getMessage(), e);
            return Result.error("获取回复失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除回复
     * 
     * 处理流程:
     * 1. 验证用户是否有权限删除该回复
     * 2. 更新回复状态为已删除
     * 3. 更新评论回复数
     *
     * @param replyId 回复ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> deleteReply(Long replyId, Long userId) {
        try {
            // 检查回复是否存在
            Reply reply = replyMapper.getReply(replyId);
            if (reply == null) {
                return Result.error("回复不存在");
            }
            
            // 验证用户是否有权限删除
            if (!reply.getUserId().equals(userId.intValue())) {
                // 检查用户是否是评论作者
                CommentDTO comment = commentMapper.getComment(reply.getParentId());
                if (comment == null || !comment.getUserId().equals(userId.intValue())) {
                    // 检查用户是否是帖子作者
                    Post post = postMapper.getPostById(comment.getPostId());
                    if (post == null || !post.getUserId().equals(userId.intValue())) {
                        return Result.error("您没有权限删除此回复");
                    }
                }
            }
            
            // 更新回复状态为已删除
            int result = replyMapper.updateReplyStatus(replyId, 0); // 0表示删除
            
            if (result > 0) {
                // 更新评论回复数
                commentMapper.updateCommentReplyCount(reply.getParentId(), -1);
                return Result.success("删除回复成功", true);
            } else {
                return Result.error("删除回复失败");
            }
        } catch (Exception e) {
            log.error("删除回复失败", e);
            return Result.error("删除回复失败: " + e.getMessage());
        }
    }
    
    /**
     * 点赞回复
     * 
     * 处理流程:
     * 1. 验证用户和回复
     * 2. 根据当前状态决定点赞或取消点赞
     * 3. 更新回复点赞数
     * 4. 返回新的点赞状态
     *
     * @param replyId 回复ID
     * @param userId 用户ID
     * @return 操作结果，true表示已点赞，false表示未点赞
     */
    @Override
    @Transactional
    public Result<Boolean> likeReply(Long replyId, Long userId) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId.intValue());
            if (user == null) {
                return Result.error("用户不存在，无法点赞回复");
            }
            
            // 检查回复是否存在
            ReplyDTO reply = replyMapper.getReplyWithLikeStatus(replyId, userId.intValue());
            if (reply == null) {
                return Result.error("回复不存在");
            }
            
            // 专注于isLiked字段调试
            log.info("回复点赞状态查询: replyId={}, userId={}, isLiked={}", 
                     replyId, userId, reply.getIsLiked());
            
            // 检查回复状态
            if (reply.getStatus() == null || reply.getStatus() != 1) {
                return Result.error("该回复已被删除或审核中，无法点赞");
            }
            
            // 判断当前是否已点赞
            boolean isCurrentlyLiked = reply.getIsLiked() != null && reply.getIsLiked();
            
            if (isCurrentlyLiked) {
                // 已经点赞，取消点赞
                ReplyLike replyLike = replyMapper.getReplyLike(replyId, userId.intValue());
                replyLike.setStatus(0); // 设置为无效
                replyLike.setCreateTime(LocalDateTime.now());
                replyMapper.updateReplyLike(replyLike);
                
                // 更新回复点赞数
                replyMapper.updateReplyLikeCount(replyId, -1);
                
                log.info("回复取消点赞成功: replyId={}, userId={}, 结果isLiked=false", replyId, userId);
                return Result.success("取消点赞成功", false);
            } else {
                // 未点赞，添加点赞
                ReplyLike replyLike = replyMapper.getReplyLike(replyId, userId.intValue());
                if (replyLike == null) {
                    // 从未点过赞，新增记录
                    replyLike = new ReplyLike();
                    replyLike.setReplyId(replyId);
                    replyLike.setUserId(userId.intValue());
                    replyLike.setCreateTime(LocalDateTime.now());
                    replyLike.setStatus(1); // 有效状态
                    replyMapper.insertReplyLike(replyLike);
                } else {
                    // 曾经点过赞但取消了，更新状态
                    replyLike.setStatus(1); // 设置为有效
                    replyLike.setCreateTime(LocalDateTime.now());
                    replyMapper.updateReplyLike(replyLike);
                }
                
                // 更新回复点赞数
                replyMapper.updateReplyLikeCount(replyId, 1);
                
                log.info("回复点赞成功: replyId={}, userId={}, 结果isLiked=true", replyId, userId);
                return Result.success("点赞成功", true);
            }
        } catch (Exception e) {
            log.error("点赞回复失败", e);
            return Result.error("点赞回复失败: " + e.getMessage());
        }
    }

    // =====================================================
    // ============== 四、订阅计划相关方法 =================
    // =====================================================

    /**
     * 创建订阅计划
     * 
     * 处理流程：
     * 1. 验证创建者信息
     * 2. 设置计划默认值
     * 3. 插入订阅计划记录
     * 4. 返回创建的计划详情
     *
     * @param plan 订阅计划信息
     * @return 创建结果，包含完整的订阅计划信息
     */
    @Override
    @Transactional
    public Result<SubscriptionPlanDTO> createSubscriptionPlan(SubscriptionPlanDTO plan) {
        try {
            // 验证创建者
            User creator = userMapper.selectById(Math.toIntExact(plan.getCreatorId()));
            if (creator == null) {
                return Result.error("创建者不存在");
            }
            
            // 验证价格
            if (plan.getMonthlyPrice() == null || plan.getMonthlyPrice().compareTo(BigDecimal.ZERO) < 0) {
                return Result.error("月度价格必须大于等于0");
            }
            
            // 设置默认值
            plan.setStatus(1); // 计划状态为正常
            LocalDateTime now = LocalDateTime.now();
            Date createTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            Date updateTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            plan.setCreateTime(createTime);
            plan.setUpdateTime(updateTime);
            
            // 插入订阅计划
            postMapper.insertSubscriptionPlan(plan);
            
            // 如果插入后没有ID，则表示插入失败
            if (plan.getPlanId() == null) {
                return Result.error("创建订阅计划失败");
            }
            
            // 返回创建的计划详情
            SubscriptionPlanDTO createdPlan = postMapper.getSubscriptionPlanById(plan.getPlanId());
            return Result.success(createdPlan);
        } catch (Exception e) {
            log.error("创建订阅计划失败", e);
            return Result.error("创建订阅计划失败: " + e.getMessage());
        }
    }

    /**
     * 获取创作者的订阅计划列表
     * 
     * 处理流程：
     * 1. 验证创作者是否存在
     * 2. 获取创作者的所有订阅计划
     * 3. 检查当前用户是否已订阅各计划
     * 4. 返回计划列表
     *
     * @param creatorId 创作者ID
     * @param userId 当前用户ID，用于判断是否已订阅
     * @return 创作者的订阅计划列表
     */
    @Override
    public Result<List<SubscriptionPlanDTO>> getCreatorSubscriptionPlans(Long creatorId, Long userId) {
        try {
            log.info("获取创作者订阅计划: creatorId={}, userId={}", creatorId, userId);
            
            // 获取订阅计划列表
            List<SubscriptionPlanDTO> plans = postMapper.getCreatorSubscriptionPlans(creatorId);
            
            // 如果没有订阅计划，直接返回空列表
            if (plans == null || plans.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            
            // 获取创建者信息
            User creator = userMapper.selectById(creatorId.intValue());
            if (creator == null) {
                log.error("创作者不存在: creatorId={}", creatorId);
                return Result.error("创作者不存在");
            }
            
            // 丰富订阅计划信息
            for (SubscriptionPlanDTO plan : plans) {
                // 设置创建者信息
                plan.setCreatorName(creator.getUsername());
                plan.setCreatorAvatar(creator.getAvatar());
                
                // 如果post_subscription_plan表中没有subscriber_count字段数据，则从user_subscription表获取
                if (plan.getSubscriberCount() == null) {
                    // 从user_subscription表计算实际订阅人数
                    Integer actualCount = postMapper.countPlanSubscribers(plan.getPlanId());
                    plan.setSubscriberCount(actualCount != null ? actualCount : 0);
                }
                
                // 如果当前用户已登录，检查是否已订阅该计划
                if (userId != null) {
                    Boolean isSubscribed = postMapper.checkUserSubscribed(userId, plan.getPlanId());
                    plan.setIsSubscribed(isSubscribed != null && isSubscribed);
                }
            }
            
            return Result.success(plans);
        } catch (Exception e) {
            log.error("获取创作者订阅计划失败: creatorId={}", creatorId, e);
            return Result.error("获取创作者订阅计划失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户已订阅的计划列表
     * 
     * 处理流程：
     * 1. 获取用户所有有效的订阅记录
     * 2. 按到期时间排序
     * 3. 返回订阅计划列表
     *
     * @param userId 用户ID
     * @return 用户已订阅的计划列表
     */
    @Override
    public Result<List<SubscriptionPlanDTO>> getMySubscriptionPlans(Long userId) {
        try {
            // 验证用户
            User user = userMapper.selectById(Math.toIntExact(userId));
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 获取用户订阅的计划
            List<SubscriptionPlanDTO> plans = postMapper.getUserSubscribedPlans(userId, null);
            
            return Result.success(plans);
        } catch (Exception e) {
            log.error("获取我的订阅计划失败", e);
            return Result.error("获取我的订阅计划失败: " + e.getMessage());
        }
    }

    /**
     * 获取订阅计划详情
     * 
     * 处理流程：
     * 1. 获取计划基本信息
     * 2. 检查用户是否已订阅该计划
     * 3. 返回计划详情
     *
     * @param planId 计划ID
     * @param userId 用户ID，用于判断是否已订阅
     * @return 订阅计划详情
     */
    @Override
    public Result<SubscriptionPlanDTO> getSubscriptionPlanDetail(Long planId, Long userId) {
        try {
            // 获取计划详情
            SubscriptionPlanDTO plan = postMapper.getSubscriptionPlanById(planId);
            if (plan == null) {
                return Result.error("订阅计划不存在");
            }
            
            // 检查用户是否已订阅
            if (userId != null) {
                boolean isSubscribed = postMapper.isUserSubscribed(userId, planId);
                plan.setIsSubscribed(isSubscribed);
            } else {
                plan.setIsSubscribed(false);
            }
            
            return Result.success(plan);
        } catch (Exception e) {
            log.error("获取订阅计划详情失败", e);
            return Result.error("获取订阅计划详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新订阅计划
     * 
     * 处理流程：
     * 1. 验证计划是否存在
     * 2. 验证用户是否有权限更新（是否为创建者）
     * 3. 更新计划信息
     * 4. 返回更新后的计划详情
     *
     * @param planId 计划ID
     * @param plan 更新的计划信息
     * @param userId 当前用户ID
     * @return 更新后的订阅计划
     */
    @Override
    @Transactional
    public Result<SubscriptionPlanDTO> updateSubscriptionPlan(Long planId, SubscriptionPlanDTO plan, Long userId) {
        try {
            // 获取原计划
            SubscriptionPlanDTO existingPlan = postMapper.getSubscriptionPlanById(planId);
            if (existingPlan == null) {
                return Result.error("订阅计划不存在");
            }
            
            // 验证权限
            if (!existingPlan.getCreatorId().equals(userId)) {
                return Result.error("无权限更新此订阅计划");
            }
            
            // 设置更新信息
            plan.setPlanId(planId);
            LocalDateTime now = LocalDateTime.now();
            Date updateTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            plan.setUpdateTime(updateTime);
            
            // 更新计划
            postMapper.updateSubscriptionPlan(plan);
            
            // 返回更新后的计划
            SubscriptionPlanDTO updatedPlan = postMapper.getSubscriptionPlanById(planId);
            return Result.success(updatedPlan);
        } catch (Exception e) {
            log.error("更新订阅计划失败", e);
            return Result.error("更新订阅计划失败: " + e.getMessage());
        }
    }

    /**
     * 删除订阅计划
     * 
     * 处理流程：
     * 1. 验证计划是否存在
     * 2. 验证用户是否有权限删除（是否为创建者）
     * 3. 逻辑删除计划（将状态设为无效）
     * 4. 返回删除结果
     *
     * @param planId 计划ID
     * @param userId 当前用户ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public Result<Boolean> deleteSubscriptionPlan(Long planId, Long userId) {
        try {
            // 获取计划
            SubscriptionPlanDTO existingPlan = postMapper.getSubscriptionPlanById(planId);
            if (existingPlan == null) {
                return Result.error("订阅计划不存在");
            }
            
            // 验证权限
            if (!existingPlan.getCreatorId().equals(userId)) {
                return Result.error("无权限删除此订阅计划");
            }
            
            // 逻辑删除（设置状态为已删除）
            Date updateTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
            int result = postMapper.deleteSubscriptionPlan(planId, userId, updateTime);
            
            return Result.success(result > 0);
        } catch (Exception e) {
            log.error("删除订阅计划失败", e);
            return Result.error("删除订阅计划失败: " + e.getMessage());
        }
    }

    /**
     * 创建订阅订单 - 使用新的统一订单服务
     */
    @Override
    @Transactional
    public Result<Map<String, Object>> createSubscriptionOrder(Long planId, Long userId, Integer months) {
        try {
            log.info("调用统一订单服务创建订阅订单: planId={}, userId={}, months={}", planId, userId, months);
            
            // 调用统一订单服务
            return orderService.createSubscriptionOrder(userId, planId, months);
            
        } catch (Exception e) {
            log.error("创建订阅订单失败", e);
            return Result.error("创建订阅订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订阅内容列表
     * 
     * 处理流程：
     * 1. 验证计划是否存在
     * 2. 验证用户是否已订阅该计划
     * 3. 获取该计划下的内容列表
     * 4. 分页返回结果
     *
     * @param planId 计划ID
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 订阅内容列表
     */
    @Override
    public Result<List<PostDTO>> getSubscriptionContents(Long planId, Long userId, Integer page, Integer size) {
        try {
            // 验证计划
            SubscriptionPlanDTO plan = postMapper.getSubscriptionPlanById(planId);
            if (plan == null) {
                return Result.error("订阅计划不存在");
            }
            
            // 如果不是创作者本人，检查用户是否已订阅
            if (!plan.getCreatorId().equals(userId)) {
                boolean isSubscribed = postMapper.isUserSubscribed(userId, planId);
                if (!isSubscribed) {
                    return Result.error("您尚未订阅此计划，无法查看内容");
                }
            } else {
                log.info("创作者本人查看自己的订阅内容: planId={}, userId={}", planId, userId);
            }
            
            // 计算分页参数
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            int offset = (page - 1) * size;
            
            // 获取该计划下的内容
            List<Post> posts = postMapper.getSubscriptionContents(planId, offset, size);
            
            // 使用统一的转换方法，包含媒体图片、标签和交互状态
            List<PostDTO> postDTOs = convertPostsToDTOs(posts, userId);
            
            log.info("获取订阅内容成功: planId={}, 返回{}条动态", planId, postDTOs.size());
            return Result.success(postDTOs);
        } catch (Exception e) {
            log.error("获取订阅内容失败", e);
            return Result.error("获取订阅内容失败: " + e.getMessage());
        }
    }

    // =====================================================
    // =============== 五、标签相关方法 ====================
    // =====================================================

    /**
     * 获取热门标签
     * 
     * 处理流程：
     * 1. 根据标签使用次数排序获取热门标签
     * 2. 限制返回结果数量
     * 
     * @param limit 返回标签数量上限
     * @return 热门标签列表
     */
    @Override
    public Result<List<TagDTO>> getHotTags(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 20; // 默认返回20个热门标签
            }
            
            List<TagDTO> hotTags = postMapper.getHotTags(limit);
            return Result.success(hotTags);
        } catch (Exception e) {
            log.error("获取热门标签失败", e);
            return Result.error("获取热门标签失败: " + e.getMessage());
        }
    }

    /**
     * 搜索标签
     * 
     * 处理流程：
     * 1. 验证搜索关键词
     * 2. 模糊匹配标签名
     * 3. 返回匹配结果
     * 
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    @Override
    public Result<List<TagDTO>> searchTags(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("搜索关键词不能为空");
            }
            
            List<TagDTO> tags = postMapper.searchTags(keyword);
            return Result.success(tags);
        } catch (Exception e) {
            log.error("搜索标签失败", e);
            return Result.error("搜索标签失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有标签
     * 
     * 处理流程：
     * 1. 限制返回结果数量
     * 2. 返回标签列表
     * 
     * @param limit 返回标签数量上限
     * @return 标签列表
     */
    @Override
    public Result<List<TagDTO>> getAllTags(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 100; // 默认返回100个标签
            }
            
            List<TagDTO> tags = postMapper.getAllTags(limit);
            return Result.success(tags);
        } catch (Exception e) {
            log.error("获取所有标签失败", e);
            return Result.error("获取所有标签失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否已订阅某个计划
     * 
     * 处理流程：
     * 1. 验证计划是否存在
     * 2. 检查用户是否已订阅且订阅有效
     * 3. 返回订阅状态
     *
     * @param planId 计划ID
     * @param userId 用户ID
     * @return 是否已订阅
     */
    @Override
    public Result<Boolean> checkUserSubscription(Long planId, Long userId) {
        try {
            // 验证计划
            SubscriptionPlanDTO plan = postMapper.getSubscriptionPlanById(planId);
            if (plan == null) {
                return Result.error("订阅计划不存在");
            }
            
            // 检查用户是否已订阅
            boolean isSubscribed = postMapper.isUserSubscribed(userId, planId);
            
            log.info("检查用户订阅状态: planId={}, userId={}, isSubscribed={}", planId, userId, isSubscribed);
            
            return Result.success(isSubscribed);
        } catch (Exception e) {
            log.error("检查用户订阅状态失败", e);
            return Result.error("检查订阅状态失败: " + e.getMessage());
        }
    }

    /**
     * 完成订阅支付 - 已迁移到统一支付服务
     * 
     * @deprecated 请使用 PaymentService.confirmPayment() 方法
     */
    @Deprecated
    @Override
    @Transactional
    public Result<Boolean> completeSubscriptionPayment(String orderNo, Long userId, Map<String, Object> paymentData) {
        log.warn("使用了已废弃的支付完成方法，请使用统一支付服务");
        return Result.error("此方法已废弃，请使用统一支付服务");
    }
}

