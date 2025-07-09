package org.example.afd.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回复实体类（使用post_comment表存储）
 */
@Data
public class Reply {
    private Long replyId;        // 回复ID (对应post_comment表的comment_id)
    private Long postId;         // 动态ID (对应post_comment表的post_id)
    private Integer userId;      // 回复用户ID
    private Long parentId;       // 父评论ID (对应post_comment表的parent_id)
    private Long rootId;         // 根评论ID (对应post_comment表的root_id)
    private String content;      // 回复内容
    private Integer likeCount;   // 点赞数
    private Integer replyCount;  // 回复数
    private Integer status;      // 状态 1-正常 0-已删除
    private LocalDateTime createTime; // 创建时间
} 