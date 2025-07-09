package org.example.afd.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户私信会话实体类
 * 对应数据库表：user_chat_conversation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_chat_conversation")
public class UserChatConversation {

    /**
     * 会话ID，主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    /**
     * 用户1的ID
     */
    @Column(name = "user1_id", nullable = false)
    private Integer user1Id;

    /**
     * 用户2的ID
     */
    @Column(name = "user2_id", nullable = false)
    private Integer user2Id;

    /**
     * 最后一条消息ID
     */
    @Column(name = "last_message_id")
    private Long lastMessageId;

    /**
     * 最后一条消息内容
     */
    @Column(name = "last_message_content", columnDefinition = "TEXT")
    private String lastMessageContent;

    /**
     * 最后一条消息时间
     */
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 最后一条消息类型
     */
    @Column(name = "last_message_type")
    private Integer lastMessageType;

    /**
     * 用户1的未读消息数
     */
    @Column(name = "user1_unread_count")
    private Integer user1UnreadCount = 0;

    /**
     * 用户2的未读消息数
     */
    @Column(name = "user2_unread_count")
    private Integer user2UnreadCount = 0;

    /**
     * 会话创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 会话更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 用户1是否删除了会话，0-未删除，1-已删除
     */
    @Column(name = "user1_deleted")
    private Integer user1Deleted = 0;

    /**
     * 用户2是否删除了会话，0-未删除，1-已删除
     */
    @Column(name = "user2_deleted")
    private Integer user2Deleted = 0;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.updateTime == null) {
            this.updateTime = LocalDateTime.now();
        }
        if (this.user1UnreadCount == null) {
            this.user1UnreadCount = 0;
        }
        if (this.user2UnreadCount == null) {
            this.user2UnreadCount = 0;
        }
        if (this.user1Deleted == null) {
            this.user1Deleted = 0;
        }
        if (this.user2Deleted == null) {
            this.user2Deleted = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 获取指定用户的未读消息数
     */
    public Integer getUnreadCountForUser(Integer userId) {
        if (userId.equals(user1Id)) {
            return user1UnreadCount;
        } else if (userId.equals(user2Id)) {
            return user2UnreadCount;
        }
        return 0;
    }

    /**
     * 设置指定用户的未读消息数
     */
    public void setUnreadCountForUser(Integer userId, Integer count) {
        if (userId.equals(user1Id)) {
            this.user1UnreadCount = count;
        } else if (userId.equals(user2Id)) {
            this.user2UnreadCount = count;
        }
    }

    /**
     * 增加指定用户的未读消息数
     */
    public void incrementUnreadCountForUser(Integer userId) {
        if (userId.equals(user1Id)) {
            this.user1UnreadCount = (this.user1UnreadCount == null ? 0 : this.user1UnreadCount) + 1;
        } else if (userId.equals(user2Id)) {
            this.user2UnreadCount = (this.user2UnreadCount == null ? 0 : this.user2UnreadCount) + 1;
        }
    }

    /**
     * 清空指定用户的未读消息数
     */
    public void clearUnreadCountForUser(Integer userId) {
        setUnreadCountForUser(userId, 0);
    }
} 