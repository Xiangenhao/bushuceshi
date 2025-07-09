package org.example.afd.service;

import org.example.afd.dto.ChatMessageDTO;
import org.example.afd.dto.ConversationDTO;
import org.example.afd.entity.UserChatMessage;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 * 定义聊天功能相关的业务逻辑方法
 */
public interface ChatService {

    /**
     * 发送消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @param messageType 消息类型
     * @param mediaUrl 媒体文件URL（可选）
     * @return 消息DTO
     */
    ChatMessageDTO sendMessage(Integer senderId, Integer receiverId, String content, Integer messageType, String mediaUrl);

    /**
     * 发送消息（使用实体对象）
     * @param message 消息实体
     * @return 消息ID
     */
    Long sendMessage(UserChatMessage message);

    /**
     * 更新用户在线状态
     * @param userId 用户ID
     * @param isOnline 是否在线
     */
    void updateUserOnlineStatus(Integer userId, boolean isOnline);

    /**
     * 设置用户在线
     * @param userId 用户ID
     * @param deviceInfo 设备信息
     * @param clientType 客户端类型
     * @param sessionToken 会话token
     * @param ipAddress IP地址
     * @param location 位置信息
     */
    void setUserOnline(Integer userId, String deviceInfo, Integer clientType, 
                       String sessionToken, String ipAddress, String location);

    /**
     * 设置用户离线
     * @param userId 用户ID
     */
    void setUserOffline(Integer userId);

    /**
     * 更新用户活跃时间
     * @param userId 用户ID
     */
    void updateUserActiveTime(Integer userId);

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    Boolean isUserOnline(Integer userId);

    /**
     * 批量检查用户在线状态
     * @param userIds 用户ID列表
     * @return 用户在线状态Map
     */
    Map<Integer, Boolean> batchCheckOnlineStatus(List<Integer> userIds);

    /**
     * 获取聊天消息列表
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @param lastMessageId 最后一条消息ID（用于分页）
     * @param pageSize 页面大小
     * @return 消息列表
     */
    List<ChatMessageDTO> getMessages(Integer currentUserId, Integer targetUserId, Long lastMessageId, Integer pageSize);

    /**
     * 获取会话列表
     * @param currentUserId 当前用户ID
     * @param page 页码
     * @param size 页面大小
     * @return 会话列表
     */
    List<ConversationDTO> getConversations(Integer currentUserId, Integer page, Integer size);

    /**
     * 标记消息为已读
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    boolean markMessagesAsRead(Integer currentUserId, Integer targetUserId);

    /**
     * 删除会话
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Integer currentUserId, Integer targetUserId);

    /**
     * 撤回消息
     * @param messageId 消息ID
     * @param currentUserId 当前用户ID
     * @return 是否成功
     */
    boolean recallMessage(Long messageId, Integer currentUserId);

    /**
     * 获取未读消息总数
     * @param currentUserId 当前用户ID
     * @return 未读消息总数
     */
    Integer getUnreadCount(Integer currentUserId);

    /**
     * 搜索聊天记录
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 页面大小
     * @return 消息列表
     */
    List<ChatMessageDTO> searchMessages(Integer currentUserId, Integer targetUserId, String keyword, Integer page, Integer size);

    /**
     * 创建或获取会话
     * @param user1Id 用户1 ID
     * @param user2Id 用户2 ID
     * @return 会话ID
     */
    Long createOrGetConversation(Integer user1Id, Integer user2Id);

    /**
     * 更新会话最后一条消息信息
     * @param conversationId 会话ID
     * @param messageId 消息ID
     * @param content 消息内容
     * @param messageType 消息类型
     */
    void updateConversationLastMessage(Long conversationId, Long messageId, String content, Integer messageType);

    /**
     * 增加未读消息数
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    void incrementUnreadCount(Long conversationId, Integer userId);

    /**
     * 调试用户会话数据
     */
    List<Map<String, Object>> selectUserConversationsDebug(Integer userId);
} 