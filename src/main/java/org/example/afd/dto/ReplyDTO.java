package org.example.afd.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回复数据传输对象
 * 注意：replyToUserId和replyToUsername字段需要在业务层手动设置，
 * 因为数据库中的post_comment表没有对应的字段
 */
@Data
public class ReplyDTO {
    private Long replyId;        // 回复ID
    private Long commentId;      // 评论ID
    private Long parentId;       // 父回复ID
    private Long rootId;         // 根回复ID
    private Long userId;         // 用户ID
    private String username;     // 用户名
    private String avatar;       // 用户头像
    private String content;      // 回复内容
    private LocalDateTime createTime; // 原始创建时间
    private String formattedTime;   // 创建时间(格式化后的)
    private Long replyToUserId;  // 被回复用户ID (业务层手动设置)
    private String replyToUsername; // 被回复用户名 (业务层手动设置)
    private Integer likeCount;   // 点赞数
    private Boolean isLiked;     // 当前用户是否点赞
    private Integer status;      // 状态 0-正常 1-已删除
} 