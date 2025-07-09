package org.example.afd.pojo;

import lombok.Data;
import org.example.afd.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库中的 afd_notifications 表
 */
@Data
public class Notification {

    /**
     * 通知ID，主键
     */
    private Long id;

    /**
     * 接收通知的用户ID
     */
    private Long receiverId;

    /**
     * 触发通知的用户ID
     */
    private Long triggerUserId;

    /**
     * 通知类型
     */
    private NotificationType type;

    /**
     * 关联内容ID (例如：动态ID, 评论ID, 计划ID)
     */
    private Long targetId;

    /**
     * 关联内容的次要ID (例如：当回复评论时，targetId是评论ID，secondaryTargetId可以是动态ID)
     */
    private Long secondaryTargetId;
    
    /**
     * 关联内容的快照或摘要
     */
    private String content;

    /**
     * 是否已读 (0-未读, 1-已读)
     */
    private boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 