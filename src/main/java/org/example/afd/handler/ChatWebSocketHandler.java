package org.example.afd.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.ChatMessageDTO;
import org.example.afd.entity.UserChatMessage;
import org.example.afd.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 聊天WebSocket处理器
 * 处理实时聊天消息的发送和接收
 * 
 * @author AFD Team
 * @version 1.0
 */
@Slf4j
@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 存储用户ID和WebSocket会话的映射
    private final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // 存储会话ID和用户ID的映射
    private final Map<String, Integer> sessionUsers = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            Integer userId = (Integer) session.getAttributes().get("userId");
            String username = (String) session.getAttributes().get("username");
            
            if (userId == null) {
                log.warn("用户ID为空，关闭连接");
                session.close();
                return;
            }
            
            // 存储会话
            userSessions.put(userId, session);
            sessionUsers.put(session.getId(), userId);
            
            log.info("用户{}({})建立WebSocket连接: sessionId={}", username, userId, session.getId());
            
            // 更新用户在线状态 - 使用新的方法
            chatService.setUserOnline(userId, "WebSocket", 3, session.getId(), null, null);
            
            // 发送连接成功消息
            ChatMessageDTO connectMsg = new ChatMessageDTO();
            connectMsg.setMessageType(5); // 系统消息
            connectMsg.setContent("连接成功");
            connectMsg.setCreateTime(LocalDateTime.now());
            
            sendMessage(session, connectMsg);
            
        } catch (Exception e) {
            log.error("建立WebSocket连接异常", e);
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            Integer senderId = sessionUsers.get(session.getId());
            if (senderId == null) {
                log.warn("未找到发送者ID，忽略消息");
                return;
            }
            
            String payload = message.getPayload().toString();
            log.info("收到来自用户{}的消息: {}", senderId, payload);
            
            // 解析消息
            ChatMessageDTO messageDTO = objectMapper.readValue(payload, ChatMessageDTO.class);
            messageDTO.setSenderId(senderId);
            messageDTO.setCreateTime(LocalDateTime.now());
            
            // 验证消息内容
            if (messageDTO.getReceiverId() == null || messageDTO.getContent() == null || messageDTO.getContent().trim().isEmpty()) {
                log.warn("消息内容不完整，忽略消息");
                return;
            }
            
            // 保存消息到数据库
            UserChatMessage chatMessage = new UserChatMessage();
            chatMessage.setSenderId(messageDTO.getSenderId());
            chatMessage.setReceiverId(messageDTO.getReceiverId());
            chatMessage.setContent(messageDTO.getContent());
            chatMessage.setMessageType(messageDTO.getMessageType() != null ? messageDTO.getMessageType() : 1); // 默认文本消息
            chatMessage.setCreateTime(messageDTO.getCreateTime());
            // 注意：UserChatMessage没有isRead属性，使用messageStatus替代
            chatMessage.setMessageStatus(1); // 1-已发送
            
            // 保存消息
            Long messageId = chatService.sendMessage(chatMessage);
            messageDTO.setMessageId(messageId);
            
            // 转发消息给接收者
            WebSocketSession receiverSession = userSessions.get(messageDTO.getReceiverId());
            if (receiverSession != null && receiverSession.isOpen()) {
                sendMessage(receiverSession, messageDTO);
                log.info("消息已转发给用户{}", messageDTO.getReceiverId());
            } else {
                log.info("用户{}不在线，消息已保存到数据库", messageDTO.getReceiverId());
            }
            
            // 给发送者发送确认消息
            ChatMessageDTO ackMsg = new ChatMessageDTO();
            ackMsg.setMessageType(5); // 系统消息
            ackMsg.setContent("消息发送成功");
            ackMsg.setMessageId(messageId);
            ackMsg.setCreateTime(LocalDateTime.now());
            
            sendMessage(session, ackMsg);
            
        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
            
            // 发送错误消息给客户端
            ChatMessageDTO errorMsg = new ChatMessageDTO();
            errorMsg.setMessageType(5); // 系统消息
            errorMsg.setContent("消息发送失败");
            errorMsg.setCreateTime(LocalDateTime.now());
            
            sendMessage(session, errorMsg);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Integer userId = sessionUsers.get(session.getId());
        log.error("WebSocket传输异常: userId={}, sessionId={}", userId, session.getId(), exception);
        
        // 清理会话
        if (userId != null) {
            userSessions.remove(userId);
            chatService.setUserOffline(userId);
        }
        sessionUsers.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Integer userId = sessionUsers.get(session.getId());
        
        if (userId != null) {
            log.info("用户{}断开WebSocket连接: sessionId={}, 状态={}", userId, session.getId(), closeStatus);
            
            // 清理会话
            userSessions.remove(userId);
            sessionUsers.remove(session.getId());
            
            // 更新用户离线状态
            chatService.setUserOffline(userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 发送消息到指定会话
     */
    private void sendMessage(WebSocketSession session, ChatMessageDTO message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("消息已发送: {}", jsonMessage);
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Integer userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
    
    /**
     * 向指定用户发送消息
     */
    public void sendMessageToUser(Integer userId, ChatMessageDTO message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }
} 