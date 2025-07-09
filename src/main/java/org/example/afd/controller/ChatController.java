package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.annotation.JwtAuth;
import org.example.afd.dto.ChatMessageDTO;
import org.example.afd.dto.ConversationDTO;
import org.example.afd.model.Result;
import org.example.afd.service.ChatService;
import org.example.afd.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 聊天控制器
 * 提供聊天相关的REST API
 * 注意：实时消息发送已移至WebSocket处理
 * 
 * @author AFD Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取聊天消息列表（分页）
     * GET /api/chat/messages?targetUserId=xxx&lastMessageId=xxx&pageSize=20
     */
    @JwtAuth
    @GetMapping("/messages")
    public Result<List<ChatMessageDTO>> getMessages(
            @RequestParam Integer targetUserId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        try {
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            log.info("获取聊天消息: 当前用户={}, 目标用户={}, 最后消息ID={}, 页面大小={}", 
                    currentUserId, targetUserId, lastMessageId, pageSize);
            
            List<ChatMessageDTO> messages = chatService.getMessages(currentUserId, targetUserId, lastMessageId, pageSize);
            
            log.info("成功获取{}条消息", messages.size());
            return Result.success(messages);
            
        } catch (Exception e) {
            log.error("获取聊天消息失败", e);
            return Result.error("获取聊天消息失败");
        }
    }

    /**
     * 获取会话列表
     * GET /api/chat/conversations?page=0&size=20
     */
    @JwtAuth
    @GetMapping("/conversations")
    public Result<List<ConversationDTO>> getConversations(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        try {
            Integer userId = jwtUtil.getUserIdFromRequest(request);
            log.info("获取会话列表: 用户={}, 页码={}, 大小={}", userId, page, size);
            
            List<ConversationDTO> conversations = chatService.getConversations(userId, page, size);
            
            log.info("成功获取{}个会话", conversations.size());
            return Result.success(conversations);
            
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            return Result.error("获取会话列表失败");
        }
    }

    /**
     * 标记消息为已读
     * PUT /api/chat/messages/read?targetUserId=xxx
     */
    @JwtAuth
    @PutMapping("/messages/read")
    public Result<Void> markMessagesAsRead(
            @RequestParam Integer targetUserId,
            HttpServletRequest request) {
        try {
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            log.info("标记消息为已读: 当前用户={}, 目标用户={}", currentUserId, targetUserId);
            
            boolean success = chatService.markMessagesAsRead(currentUserId, targetUserId);
            
            if (success) {
                log.info("消息标记为已读成功");
                return Result.success("操作成功");
            } else {
                log.warn("消息标记为已读失败");
                return Result.error("标记消息失败");
            }
            
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            return Result.error("标记消息失败");
        }
    }

    /**
     * 删除会话
     * DELETE /api/chat/conversations/{targetUserId}
     */
    @JwtAuth
    @DeleteMapping("/conversations/{targetUserId}")
    public Result<Void> deleteConversation(
            @PathVariable Integer targetUserId,
            HttpServletRequest request) {
        try {
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            log.info("删除会话: 当前用户={}, 目标用户={}", currentUserId, targetUserId);
            
            boolean success = chatService.deleteConversation(currentUserId, targetUserId);
            
            if (success) {
                log.info("删除会话成功");
                return Result.success("删除成功");
            } else {
                log.warn("删除会话失败");
                return Result.error("删除会话失败");
            }
            
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return Result.error("删除会话失败");
        }
    }

    /**
     * 撤回消息
     * PUT /api/chat/messages/{messageId}/recall
     */
    @JwtAuth
    @PutMapping("/messages/{messageId}/recall")
    public Result<Void> recallMessage(
            @PathVariable Long messageId,
            HttpServletRequest request) {
        try {
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            log.info("撤回消息: 消息ID={}, 用户={}", messageId, currentUserId);
            
            boolean success = chatService.recallMessage(messageId, currentUserId);
            
            if (success) {
                log.info("撤回消息成功");
                return Result.success("撤回成功");
            } else {
                log.warn("撤回消息失败");
                return Result.error("撤回消息失败");
            }
            
        } catch (Exception e) {
            log.error("撤回消息失败", e);
            return Result.error("撤回消息失败");
        }
    }

    /**
     * 获取未读消息总数
     * GET /api/chat/unread-count
     */
    @JwtAuth
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        try {
            Integer userId = jwtUtil.getUserIdFromRequest(request);
            log.info("获取未读消息总数: 用户={}", userId);
            
            Integer unreadCount = chatService.getUnreadCount(userId);
            
            log.info("未读消息总数: {}", unreadCount);
            return Result.success(unreadCount);
            
        } catch (Exception e) {
            log.error("获取未读消息总数失败", e);
            return Result.error("获取未读消息总数失败");
        }
    }

    /**
     * 搜索聊天记录
     * GET /api/chat/search?targetUserId=xxx&keyword=xxx&page=0&size=20
     */
    @JwtAuth
    @GetMapping("/search")
    public Result<List<ChatMessageDTO>> searchMessages(
            @RequestParam(required = false) Integer targetUserId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        try {
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            log.info("搜索聊天记录: 当前用户={}, 目标用户={}, 关键词={}, 页码={}, 大小={}", 
                    currentUserId, targetUserId, keyword, page, size);
            
            List<ChatMessageDTO> messages = chatService.searchMessages(currentUserId, targetUserId, keyword, page, size);
            
            log.info("搜索到{}条消息", messages.size());
            return Result.success(messages);
            
        } catch (Exception e) {
            log.error("搜索聊天记录失败", e);
            return Result.error("搜索聊天记录失败");
        }
    }

    /**
     * 调试API - 查看原始会话数据
     */
    @GetMapping("/debug/conversations")
    public Result<Map<String, Object>> debugConversations(HttpServletRequest request) {
        try {
            // 获取用户ID
            Integer userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户认证失败");
            }
            
            log.info("调试查询 - 用户ID: {}", userId);
            
            // 查询原始数据
            Map<String, Object> debugData = new HashMap<>();
            List<Map<String, Object>> rawData = chatService.selectUserConversationsDebug(userId);
            debugData.put("用户ID", userId);
            debugData.put("原始会话数据", rawData);
            
            return Result.success("调试数据获取成功", debugData);
            
        } catch (Exception e) {
            log.error("获取调试数据失败", e);
            return Result.error("获取调试数据失败: " + e.getMessage());
        }
    }
} 