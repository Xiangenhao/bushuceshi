package org.example.afd.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户私信消息实体类
 * 对应数据库表：user_chat_message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_chat_message")
public class UserChatMessage {

    /**
     * 消息ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    /**
     * 会话ID
     */
    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id", nullable = false)
    private Integer senderId;

    /**
     * 接收者ID
     */
    @Column(name = "receiver_id", nullable = false)
    private Integer receiverId;

    /**
     * 消息类型，1-文本，2-图片，3-视频，4-表情，5-系统消息
     */
    @Column(name = "message_type", nullable = false)
    private Integer messageType;

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 媒体文件URL（图片、视频等）
     */
    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    /**
     * 缩略图URL（视频缩略图）
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * 消息状态，0-发送中，1-已发送，2-已送达，3-已读，4-发送失败
     */
    @Column(name = "message_status")
    private Integer messageStatus = 0;

    /**
     * 是否已撤回，0-未撤回，1-已撤回
     */
    @Column(name = "is_recalled")
    private Integer isRecalled = 0;

    /**
     * 扩展数据（JSON格式）
     */
    @Column(name = "extra_data", columnDefinition = "JSON")
    private String extraData;

    /**
     * 发送时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    @Column(name = "read_time")
    private LocalDateTime readTime;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.messageStatus == null) {
            this.messageStatus = 0;
        }
        if (this.isRecalled == null) {
            this.isRecalled = 0;
        }
    }

    /**
     * 检查消息是否已读
     */
    public boolean isRead() {
        return messageStatus != null && messageStatus == 3;
    }

    /**
     * 检查消息是否已撤回
     */
    public boolean isRecalled() {
        return isRecalled != null && isRecalled == 1;
    }

    /**
     * 标记消息为已读
     */
    public void markAsRead() {
        this.messageStatus = 3;
        this.readTime = LocalDateTime.now();
    }

    /**
     * 撤回消息
     */
    public void recall() {
        this.isRecalled = 1;
    }
} 