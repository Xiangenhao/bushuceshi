package org.example.afd.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.ChatMessageDTO;
import org.example.afd.dto.ConversationDTO;
import org.example.afd.entity.UserChatMessage;
import org.example.afd.entity.UserChatConversation;
import org.example.afd.mapper.ChatMapper;
import org.example.afd.mapper.UserOnlineStatusMapper;
import org.example.afd.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.ZoneId;

/**
 * 聊天服务实现类
 * 实现用户私信聊天的所有业务逻辑
 * 
 * @author AFD Team
 * @version 1.0
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserOnlineStatusMapper userOnlineStatusMapper;

    /**
     * 发送消息
     * 包括创建或更新会话、插入消息、更新未读数等操作
     */
    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Integer senderId, Integer receiverId, String content, Integer messageType, String mediaUrl) {
        try {
            log.info("开始发送消息: 发送者={}, 接收者={}, 类型={}", senderId, receiverId, messageType);
            
            // 1. 创建或获取会话ID
            Long conversationId = createOrGetConversation(senderId, receiverId);
            if (conversationId == null) {
                log.error("创建或获取会话失败: 发送者={}, 接收者={}", senderId, receiverId);
                return null;
            }
            
            // 2. 创建消息对象
            UserChatMessage message = new UserChatMessage();
            message.setConversationId(conversationId);
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setMessageType(messageType);
            message.setContent(content);
            message.setMediaUrl(mediaUrl);
            message.setMessageStatus(1); // 1-已发送
            message.setIsRecalled(0); // 0-未撤回
            message.setCreateTime(LocalDateTime.now());
            
            // 为不同类型的消息设置缩略图URL
            if (messageType == 3 && mediaUrl != null) { // 视频消息
                // 视频消息需要生成缩略图，这里暂时设置为媒体URL
                message.setThumbnailUrl(mediaUrl);
            }
            
            // 3. 插入消息到数据库
            int insertResult = chatMapper.insertMessage(message);
            if (insertResult <= 0) {
                log.error("插入消息失败: 发送者={}, 接收者={}", senderId, receiverId);
                return null;
            }
            
            log.info("消息插入成功: messageId={}", message.getMessageId());
            
            // 4. 更新会话的最后一条消息信息
            String messageContent = getMessageContentSummary(content, messageType);
            updateConversationLastMessage(conversationId, message.getMessageId(), 
                    messageContent, messageType);
            
            // 5. 增加接收者的未读消息数
            incrementUnreadCount(conversationId, receiverId);
            
            // 6. 构造返回的DTO对象
            ChatMessageDTO messageDTO = new ChatMessageDTO();
            messageDTO.setMessageId(message.getMessageId());
            messageDTO.setConversationId(message.getConversationId());
            messageDTO.setSenderId(senderId);
            messageDTO.setReceiverId(receiverId);
            messageDTO.setMessageType(messageType);
            messageDTO.setContent(content);
            messageDTO.setMediaUrl(mediaUrl);
            messageDTO.setThumbnailUrl(message.getThumbnailUrl());
            messageDTO.setMessageStatus(message.getMessageStatus());
            messageDTO.setIsRecalled(message.getIsRecalled());
            messageDTO.setCreateTime(message.getCreateTime());
            
            // 获取发送者信息
            try {
                var senderInfo = chatMapper.selectUserBasicInfo(senderId);
                if (senderInfo != null) {
                    messageDTO.setSenderUsername((String) senderInfo.get("username"));
                    messageDTO.setSenderAvatar((String) senderInfo.get("avatar"));
                }
            } catch (Exception e) {
                log.warn("获取发送者信息失败: senderId={}", senderId, e);
            }
            
            log.info("消息发送成功: messageId={}", message.getMessageId());
            return messageDTO;
            
        } catch (Exception e) {
            log.error("发送消息异常: 发送者={}, 接收者={}", senderId, receiverId, e);
            return null;
        }
    }

    /**
     * 获取两个用户之间的聊天消息列表
     */
    @Override
    public List<ChatMessageDTO> getMessages(Integer currentUserId, Integer targetUserId, Long lastMessageId, Integer pageSize) {
        try {
            log.info("获取聊天消息: 当前用户={}, 目标用户={}, 最后消息ID={}, 页面大小={}", 
                    currentUserId, targetUserId, lastMessageId, pageSize);
            
            if (pageSize == null || pageSize <= 0) {
                pageSize = 20; // 默认每页20条
            }
            if (pageSize > 100) {
                pageSize = 100; // 最多100条
            }
            
            List<ChatMessageDTO> messages = chatMapper.selectMessagesBetweenUsers(currentUserId, targetUserId, lastMessageId, pageSize);
            
            // 由于查询结果是按时间倒序的，需要反转列表以按时间正序排列
            if (messages != null && !messages.isEmpty()) {
                Collections.reverse(messages);
                log.info("获取到{}条消息", messages.size());
            } else {
                log.info("未获取到消息");
                return Collections.emptyList();
            }
            
            return messages;
            
        } catch (Exception e) {
            log.error("获取聊天消息异常: 当前用户={}, 目标用户={}", currentUserId, targetUserId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取会话列表
     */
    @Override
    public List<ConversationDTO> getConversations(Integer currentUserId, Integer page, Integer size) {
        try {
            log.debug("获取会话列表: 用户={}, 页码={}, 大小={}", currentUserId, page, size);
            
            // 参数校验
            if (page == null || page < 0) {
                page = 0;
            }
            if (size == null || size <= 0) {
                size = 20;
            }
            if (size > 100) {
                size = 100;
            }
            
            int offset = page * size;
            
            // 调用完整的会话查询方法（包含用户在线状态）
            List<ConversationDTO> conversations = chatMapper.selectUserConversations(currentUserId, offset, size);
            
            log.debug("查询到{}个会话", conversations.size());
            
            // 为每个会话补充在线状态信息和时间戳
            for (ConversationDTO conversation : conversations) {
                // 如果在线状态为null，查询用户在线状态
                if (conversation.getIsOnline() == null) {
                    Boolean isOnline = userOnlineStatusMapper.isUserOnline(conversation.getOtherUserId());
                    conversation.setIsOnline(isOnline != null ? isOnline : false);
                }
                
                // 设置时间戳，用于前端排序
                if (conversation.getLastMessageTime() != null) {
                    conversation.setTimestamp(
                        conversation.getLastMessageTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    );
                } else {
                    conversation.setTimestamp(System.currentTimeMillis());
                }
            }
            
            return conversations;
            
        } catch (Exception e) {
            log.error("获取会话列表异常: 用户={}", currentUserId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 标记消息为已读
     */
    @Override
    @Transactional
    public boolean markMessagesAsRead(Integer currentUserId, Integer targetUserId) {
        try {
            log.info("标记消息为已读: 当前用户={}, 目标用户={}", currentUserId, targetUserId);
            
            // 1. 更新消息状态为已读
            LocalDateTime readTime = LocalDateTime.now();
            int updateCount = chatMapper.markMessagesAsRead(currentUserId, targetUserId, readTime);
            
            // 2. 获取会话并清空未读数
            UserChatConversation conversation = chatMapper.selectConversationBetweenUsers(currentUserId, targetUserId);
            if (conversation != null) {
                chatMapper.clearUnreadCount(conversation.getConversationId(), currentUserId);
            }
            
            log.info("标记已读完成: 更新了{}条消息", updateCount);
            return true;
            
        } catch (Exception e) {
            log.error("标记消息已读异常: 当前用户={}, 目标用户={}", currentUserId, targetUserId, e);
            return false;
        }
    }

    /**
     * 删除会话
     */
    @Override
    @Transactional
    public boolean deleteConversation(Integer userId, Integer targetUserId) {
        try {
            log.info("删除会话: 用户={}, 目标用户={}", userId, targetUserId);
            
            int deleteCount = chatMapper.deleteConversation(userId, targetUserId);
            boolean success = deleteCount > 0;
            
            log.info("删除会话{}: 影响行数={}", success ? "成功" : "失败", deleteCount);
            return success;
            
        } catch (Exception e) {
            log.error("删除会话异常: 用户={}, 目标用户={}", userId, targetUserId, e);
            return false;
        }
    }

    /**
     * 撤回消息
     */
    @Override
    @Transactional
    public boolean recallMessage(Long messageId, Integer senderId) {
        try {
            log.info("撤回消息: 消息ID={}, 发送者={}", messageId, senderId);
            
            // 检查消息是否存在且属于发送者
            UserChatMessage message = chatMapper.selectMessageById(messageId);
            if (message == null) {
                log.warn("消息不存在: messageId={}", messageId);
                return false;
            }
            
            if (!message.getSenderId().equals(senderId)) {
                log.warn("用户无权撤回此消息: messageId={}, senderId={}, actualSenderId={}", 
                        messageId, senderId, message.getSenderId());
                return false;
            }
            
            // 检查消息是否已超过撤回时间限制（比如2分钟内）
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createTime = message.getCreateTime();
            if (createTime.plusMinutes(2).isBefore(now)) {
                log.warn("消息撤回时间已过: messageId={}, createTime={}", messageId, createTime);
                return false;
            }
            
            // 执行撤回
            int recallCount = chatMapper.recallMessage(messageId, senderId);
            boolean success = recallCount > 0;
            
            log.info("撤回消息{}: messageId={}", success ? "成功" : "失败", messageId);
            return success;
            
        } catch (Exception e) {
            log.error("撤回消息异常: messageId={}, senderId={}", messageId, senderId, e);
            return false;
        }
    }

    /**
     * 获取用户未读消息总数
     */
    @Override
    public Integer getUnreadCount(Integer userId) {
        try {
            log.debug("获取未读消息总数: 用户={}", userId);
            
            int unreadCount = chatMapper.selectUnreadCount(userId);
            
            log.debug("用户{}的未读消息数: {}", userId, unreadCount);
            return unreadCount;
            
        } catch (Exception e) {
            log.error("获取未读消息总数异常: 用户={}", userId, e);
            return 0;
        }
    }

    /**
     * 搜索聊天消息
     */
    @Override
    public List<ChatMessageDTO> searchMessages(Integer currentUserId, Integer targetUserId, String keyword, Integer page, Integer size) {
        try {
            log.info("搜索聊天消息: 当前用户={}, 目标用户={}, 关键词={}, 页码={}, 大小={}", 
                    currentUserId, targetUserId, keyword, page, size);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("搜索关键词为空");
                return Collections.emptyList();
            }
            
            if (page == null || page < 0) {
                page = 0;
            }
            if (size == null || size <= 0) {
                size = 20;
            }
            if (size > 100) {
                size = 100;
            }
            
            int offset = page * size;
            List<ChatMessageDTO> messages = chatMapper.searchMessages(currentUserId, targetUserId, keyword.trim(), offset, size);
            
            if (messages == null) {
                messages = Collections.emptyList();
            }
            
            log.info("搜索到{}条消息", messages.size());
            return messages;
            
        } catch (Exception e) {
            log.error("搜索聊天消息异常: 当前用户={}, 关键词={}", currentUserId, keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 创建或获取会话
     */
    @Override
    @Transactional
    public Long createOrGetConversation(Integer user1Id, Integer user2Id) {
        try {
            log.debug("创建或获取会话: 用户1={}, 用户2={}", user1Id, user2Id);
            
            // 查找现有会话
            UserChatConversation conversation = chatMapper.selectConversationBetweenUsers(user1Id, user2Id);
            
            if (conversation != null) {
                log.debug("找到现有会话: conversationId={}", conversation.getConversationId());
                return conversation.getConversationId();
            }
            
            // 创建新会话
            conversation = new UserChatConversation();
            conversation.setUser1Id(Math.min(user1Id, user2Id)); // 保证user1Id < user2Id，便于查询
            conversation.setUser2Id(Math.max(user1Id, user2Id));
            conversation.setUser1UnreadCount(0);
            conversation.setUser2UnreadCount(0);
            conversation.setUser1Deleted(0);
            conversation.setUser2Deleted(0);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            
            int insertResult = chatMapper.insertOrUpdateConversation(conversation);
            if (insertResult > 0) {
                log.info("创建新会话成功: conversationId={}", conversation.getConversationId());
                return conversation.getConversationId();
            } else {
                log.error("创建会话失败: 用户1={}, 用户2={}", user1Id, user2Id);
                return null;
            }
            
        } catch (Exception e) {
            log.error("创建或获取会话异常: 用户1={}, 用户2={}", user1Id, user2Id, e);
            return null;
        }
    }

    /**
     * 更新会话的最后一条消息信息
     */
    @Override
    @Transactional
    public void updateConversationLastMessage(Long conversationId, Long messageId, String content, Integer messageType) {
        try {
            log.debug("更新会话最后消息: conversationId={}, messageId={}", conversationId, messageId);
            
            LocalDateTime updateTime = LocalDateTime.now();
            LocalDateTime messageTime = LocalDateTime.now(); // 使用当前时间作为消息时间
            int updateResult = chatMapper.updateConversationLastMessage(conversationId, messageId, content, messageType, messageTime, updateTime);
            
            if (updateResult > 0) {
                log.debug("更新会话最后消息成功");
            } else {
                log.warn("更新会话最后消息失败: conversationId={}", conversationId);
            }
            
        } catch (Exception e) {
            log.error("更新会话最后消息异常: conversationId={}, messageId={}", conversationId, messageId, e);
        }
    }

    /**
     * 增加用户的未读消息数
     */
    @Override
    @Transactional
    public void incrementUnreadCount(Long conversationId, Integer userId) {
        try {
            log.debug("增加未读消息数: conversationId={}, userId={}", conversationId, userId);
            
            int updateResult = chatMapper.incrementUnreadCount(conversationId, userId);
            
            if (updateResult > 0) {
                log.debug("增加未读消息数成功");
            } else {
                log.warn("增加未读消息数失败: conversationId={}, userId={}", conversationId, userId);
            }
            
        } catch (Exception e) {
            log.error("增加未读消息数异常: conversationId={}, userId={}", conversationId, userId, e);
        }
    }

    /**
     * 获取消息内容摘要，用于会话列表显示
     */
    private String getMessageContentSummary(String content, Integer messageType) {
        if (messageType == null) {
            return "未知消息";
        }
        
        switch (messageType) {
            case 1: // 文本消息
                if (content == null || content.trim().isEmpty()) {
                    return "文本消息";
                }
                // 限制长度，避免过长
                return content.length() > 50 ? content.substring(0, 50) + "..." : content;
            case 2: // 图片消息
                return "[图片]";
            case 3: // 视频消息
                return "[视频]";
            case 4: // 表情消息
                return "[表情]";
            case 5: // 系统消息
                return content != null ? content : "[系统消息]";
            default:
                return "未知消息";
        }
    }
    
    /**
     * 发送消息（使用实体对象）
     */
    @Override
    @Transactional
    public Long sendMessage(UserChatMessage message) {
        try {
            log.info("发送消息: 发送者={}, 接收者={}", message.getSenderId(), message.getReceiverId());
            
            // 1. 创建或获取会话ID
            if (message.getConversationId() == null) {
                Long conversationId = createOrGetConversation(message.getSenderId(), message.getReceiverId());
                message.setConversationId(conversationId);
            }
            
            // 2. 设置默认值
            if (message.getMessageStatus() == null) {
                message.setMessageStatus(1); // 1-已发送
            }
            if (message.getIsRecalled() == null) {
                message.setIsRecalled(0); // 0-未撤回
            }
            if (message.getCreateTime() == null) {
                message.setCreateTime(LocalDateTime.now());
            }
            
            // 3. 插入消息到数据库
            int insertResult = chatMapper.insertMessage(message);
            if (insertResult <= 0) {
                log.error("插入消息失败");
                return null;
            }
            
            // 4. 更新会话的最后一条消息信息
            String messageContent = getMessageContentSummary(message.getContent(), message.getMessageType());
            updateConversationLastMessage(message.getConversationId(), message.getMessageId(), 
                    messageContent, message.getMessageType());
            
            // 5. 增加接收者的未读消息数
            incrementUnreadCount(message.getConversationId(), message.getReceiverId());
            
            log.info("消息发送成功: messageId={}", message.getMessageId());
            return message.getMessageId();
            
        } catch (Exception e) {
            log.error("发送消息异常", e);
            return null;
        }
    }
    
    /**
     * 更新用户在线状态
     */
    @Override
    @Transactional
    public void updateUserOnlineStatus(Integer userId, boolean isOnline) {
        try {
            log.info("更新用户在线状态: userId={}, isOnline={}", userId, isOnline);
            
            // 更新用户在线状态
            LocalDateTime statusTime = LocalDateTime.now();
            int status = isOnline ? 1 : 0;
            int updateResult = userOnlineStatusMapper.updateUserOnlineStatus(userId, status, statusTime, "WebSocket", statusTime);
            
            if (updateResult > 0) {
                log.info("用户在线状态更新成功: userId={}, isOnline={}", userId, isOnline);
            } else {
                log.warn("用户在线状态更新失败: userId={}, isOnline={}", userId, isOnline);
            }
            
        } catch (Exception e) {
            log.error("更新用户在线状态异常: userId={}, isOnline={}", userId, isOnline, e);
        }
    }

    /**
     * 设置用户在线
     */
    @Override
    @Transactional
    public void setUserOnline(Integer userId, String deviceInfo, Integer clientType, 
                              String sessionToken, String ipAddress, String location) {
        try {
            log.info("设置用户在线: userId={}, deviceInfo={}", userId, deviceInfo);
            
            int updateResult = userOnlineStatusMapper.setUserOnline(userId, deviceInfo, clientType, 
                                                                   sessionToken, ipAddress, location);
            
            if (updateResult > 0) {
                log.info("设置用户在线成功: userId={}", userId);
            } else {
                log.warn("设置用户在线失败: userId={}", userId);
            }
            
        } catch (Exception e) {
            log.error("设置用户在线异常: userId={}", userId, e);
        }
    }

    /**
     * 设置用户离线
     */
    @Override
    @Transactional
    public void setUserOffline(Integer userId) {
        try {
            log.info("设置用户离线: userId={}", userId);
            
            int updateResult = userOnlineStatusMapper.setUserOffline(userId);
            
            if (updateResult > 0) {
                log.info("设置用户离线成功: userId={}", userId);
            } else {
                log.warn("设置用户离线失败: userId={}", userId);
            }
            
        } catch (Exception e) {
            log.error("设置用户离线异常: userId={}", userId, e);
        }
    }

    /**
     * 更新用户活跃时间
     */
    @Override
    @Transactional
    public void updateUserActiveTime(Integer userId) {
        try {
            log.debug("更新用户活跃时间: userId={}", userId);
            
            int updateResult = userOnlineStatusMapper.updateActiveTime(userId);
            
            if (updateResult > 0) {
                log.debug("更新用户活跃时间成功: userId={}", userId);
            } else {
                log.debug("更新用户活跃时间失败: userId={}", userId);
            }
            
        } catch (Exception e) {
            log.error("更新用户活跃时间异常: userId={}", userId, e);
        }
    }

    /**
     * 检查用户是否在线
     */
    @Override
    public Boolean isUserOnline(Integer userId) {
        try {
            log.debug("检查用户在线状态: userId={}", userId);
            
            Boolean isOnline = userOnlineStatusMapper.isUserOnline(userId);
            log.debug("用户{}在线状态: {}", userId, isOnline);
            
            return isOnline != null ? isOnline : false;
            
        } catch (Exception e) {
            log.error("检查用户在线状态异常: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 批量检查用户在线状态
     */
    @Override
    public Map<Integer, Boolean> batchCheckOnlineStatus(List<Integer> userIds) {
        try {
            log.debug("批量检查用户在线状态: userIds={}", userIds);
            
            if (userIds == null || userIds.isEmpty()) {
                return new HashMap<>();
            }
            
            List<Map<String, Object>> results = userOnlineStatusMapper.batchGetOnlineStatus(userIds);
            Map<Integer, Boolean> onlineStatusMap = new HashMap<>();
            
            for (Map<String, Object> result : results) {
                Integer userId = (Integer) result.get("user_id");
                Boolean isOnline = ((Number) result.get("is_online")).intValue() == 1;
                onlineStatusMap.put(userId, isOnline);
            }
            
            // 对于未查询到的用户，设置为离线
            for (Integer userId : userIds) {
                if (!onlineStatusMap.containsKey(userId)) {
                    onlineStatusMap.put(userId, false);
                }
            }
            
            log.debug("批量查询在线状态结果: {}", onlineStatusMap);
            return onlineStatusMap;
            
        } catch (Exception e) {
            log.error("批量检查用户在线状态异常: userIds={}", userIds, e);
            Map<Integer, Boolean> errorMap = new HashMap<>();
            for (Integer userId : userIds) {
                errorMap.put(userId, false);
            }
            return errorMap;
        }
    }

    /**
     * 调试用户会话数据
     */
    @Override
    public List<Map<String, Object>> selectUserConversationsDebug(Integer userId) {
        try {
            log.info("调试查询用户会话: userId={}", userId);
            List<Map<String, Object>> results = chatMapper.selectUserConversationsDebug(userId);
            log.info("调试查询结果: 找到{}条记录", results.size());
            for (Map<String, Object> row : results) {
                log.info("调试数据: {}", row);
            }
            return results;
        } catch (Exception e) {
            log.error("调试查询失败: userId={}", userId, e);
            throw new RuntimeException("调试查询失败", e);
        }
    }
} 