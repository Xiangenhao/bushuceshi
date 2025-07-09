package org.example.afd.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论数据传输对象
 */
@Data
public class CommentDTO {
    private Long commentId;      // 评论ID
    private Long postId;         // 动态ID
    private Long userId;         // 用户ID
    private Long parentId;       // 父评论ID
    private Long rootId;         // 根评论ID
    private String username;     // 用户名
    private String avatar;       // 用户头像
    private String content;      // 评论内容
    private Integer likeCount;   // 点赞数量
    private LocalDateTime createTime; // 原始创建时间
    private String formattedTime;   // 创建时间(格式化后的)
    private Integer replyCount;  // 回复数量
    private Boolean isLiked;     // 当前用户是否点赞
    private List<ReplyDTO> replies; // 回复列表
    private Integer status;      // 评论状态，0-正常，1-已删除，2-审核中
} 