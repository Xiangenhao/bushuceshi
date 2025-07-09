package org.example.afd.pojo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 回复点赞实体类
 */
@Data
public class ReplyLike {
    private Long id;           // 主键ID
    private Long replyId;      // 回复ID
    private Integer userId;    // 用户ID
    private LocalDateTime createTime; // 创建时间
    private Integer status;    // 状态 0-已取消 1-已点赞
} 