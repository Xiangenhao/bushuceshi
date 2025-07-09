package org.example.afd.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.example.afd.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * 通知数据传输对象 (DTO)
 * 用于在前后端之间传递通知信息
 */
@Data
public class NotificationDTO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 触发通知的用户信息
     */
    private UserInfo triggerUser;

    /**
     * 通知类型
     */
    private NotificationType type;
    
    /**
     * 通知的描述文本，例如 "赞了你的动态"
     */
    private String typeDescription;

    /**
     * 关联内容ID (例如：动态ID, 评论ID)
     */
    private Long targetId;

    /**
     * 关联内容的次要ID (例如：回复的根评论ID或动态ID)
     */
    private Long secondaryTargetId;

    /**
     * 关联内容的快照或摘要 (例如：评论内容)
     */
    private String content;
    
    /**
     * 关联内容（如动态）的封面图或缩略图
     */
    private String targetCoverUrl;

    /**
     * 是否已读
     */
    private boolean isRead;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 格式化后的时间，例如 "5分钟前"
     */
    private String formattedTime;

    /**
     * 触发通知的用户简要信息
     */
    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private String avatar;
    }
} 