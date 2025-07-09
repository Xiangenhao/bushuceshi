package org.example.afd.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 聊天消息DTO
 * 用于API接口的数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Integer senderId;

    /**
     * 发送者用户名
     */
    private String senderUsername;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private Integer receiverId;

    /**
     * 接收者用户名
     */
    private String receiverUsername;

    /**
     * 接收者头像
     */
    private String receiverAvatar;

    /**
     * 消息类型，1-文本，2-图片，3-视频，4-表情，5-系统消息
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 媒体文件URL（图片、视频等）
     */
    private String mediaUrl;

    /**
     * 缩略图URL（视频缩略图）
     */
    private String thumbnailUrl;

    /**
     * 消息状态，0-发送中，1-已发送，2-已送达，3-已读，4-发送失败
     */
    private Integer messageStatus;

    /**
     * 是否已撤回，0-未撤回，1-已撤回
     */
    private Integer isRecalled;

    /**
     * 扩展数据（JSON格式）
     */
    private String extraData;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 阅读时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 时间戳（毫秒）
     */
    private Long timestamp;

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
     * 检查是否为文本消息
     */
    public boolean isTextMessage() {
        return messageType != null && messageType == 1;
    }

    /**
     * 检查是否为图片消息
     */
    public boolean isImageMessage() {
        return messageType != null && messageType == 2;
    }

    /**
     * 检查是否为视频消息
     */
    public boolean isVideoMessage() {
        return messageType != null && messageType == 3;
    }

    /**
     * 检查是否为表情消息
     */
    public boolean isEmojiMessage() {
        return messageType != null && messageType == 4;
    }

    /**
     * 检查是否为系统消息
     */
    public boolean isSystemMessage() {
        return messageType != null && messageType == 5;
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDescription() {
        if (messageType == null) {
            return "未知";
        }
        switch (messageType) {
            case 1:
                return "文本";
            case 2:
                return "图片";
            case 3:
                return "视频";
            case 4:
                return "表情";
            case 5:
                return "系统消息";
            default:
                return "未知";
        }
    }

    /**
     * 获取消息状态描述
     */
    public String getMessageStatusDescription() {
        if (messageStatus == null) {
            return "未知";
        }
        switch (messageStatus) {
            case 0:
                return "发送中";
            case 1:
                return "已发送";
            case 2:
                return "已送达";
            case 3:
                return "已读";
            case 4:
                return "发送失败";
            default:
                return "未知";
        }
    }
} 