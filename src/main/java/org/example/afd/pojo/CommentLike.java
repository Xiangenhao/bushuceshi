package org.example.afd.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论点赞实体类
 */
@Data
public class CommentLike {
    private Long id;           // 主键ID
    private Long commentId;    // 评论ID
    private Integer userId;    // 用户ID
    private LocalDateTime createTime; // 创建时间
    private Integer status;    // 状态 0-已取消 1-已点赞
} 