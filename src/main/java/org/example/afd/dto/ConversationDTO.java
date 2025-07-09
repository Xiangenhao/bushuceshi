package org.example.afd.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 会话DTO
 * 用于API接口的数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 对方用户ID
     */
    private Integer otherUserId;

    /**
     * 对方用户名
     */
    private String otherUsername;

    /**
     * 对方用户头像
     */
    private String otherUserAvatar;

    /**
     * 对方在线状态
     */
    private Boolean isOnline;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastMessageTime;

    /**
     * 最后一条消息类型，1-文本，2-图片，3-视频，4-表情
     */
    private Integer lastMessageType;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 会话创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 会话更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 时间戳（毫秒）- 确保序列化为数字
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long timestamp;

    /**
     * 检查是否有未读消息
     */
    public boolean hasUnreadMessages() {
        return unreadCount != null && unreadCount > 0;
    }

    /**
     * 获取最后一条消息类型描述
     */
    public String getLastMessageTypeDescription() {
        if (lastMessageType == null) {
            return "消息";
        }
        switch (lastMessageType) {
            case 1:
                return "文本消息";
            case 2:
                return "[图片]";
            case 3:
                return "[视频]";
            case 4:
                return "[表情]";
            case 5:
                return "[系统消息]";
            default:
                return "消息";
        }
    }

    /**
     * 获取显示的最后一条消息内容（处理特殊消息类型）
     */
    public String getDisplayLastMessage() {
        if (lastMessageType == null || lastMessageType == 1) {
            // 文本消息直接显示内容
            return lastMessageContent != null ? lastMessageContent : "";
        } else {
            // 其他类型显示类型描述
            return getLastMessageTypeDescription();
        }
    }

    /**
     * 获取未读消息数显示文本
     */
    public String getUnreadCountDisplay() {
        if (unreadCount == null || unreadCount <= 0) {
            return "";
        } else if (unreadCount > 99) {
            return "99+";
        } else {
            return String.valueOf(unreadCount);
        }
    }
} 