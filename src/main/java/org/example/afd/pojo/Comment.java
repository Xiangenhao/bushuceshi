package org.example.afd.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
public class Comment {
    private Long commentId;      // 评论ID
    private Long postId;         // 动态ID
    private Integer userId;      // 用户ID
    private Long parentId;       // 父评论ID
    private Long rootId;         // 根评论ID
    private String content;      // 评论内容
    private Integer likeCount;   // 点赞数
    private Integer replyCount;  // 回复数量
    private Integer status;      // 状态 0-正常 1-已删除 2-审核中
    private LocalDateTime createTime; // 创建时间
} 