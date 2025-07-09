package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.UserChatMessage;
import org.example.afd.entity.UserChatConversation;
import org.example.afd.dto.ChatMessageDTO;
import org.example.afd.dto.ConversationDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天数据访问层
 * 负责聊天消息和会话的数据库操作
 */
@Mapper
public interface ChatMapper {

    // ==================== 消息相关操作 ====================
    
    /**
     * 插入聊天消息
     */
    @Insert("INSERT INTO user_chat_message (conversation_id, sender_id, receiver_id, message_type, content, " +
            "media_url, thumbnail_url, message_status, is_recalled, extra_data, create_time) " +
            "VALUES (#{conversationId}, #{senderId}, #{receiverId}, #{messageType}, #{content}, " +
            "#{mediaUrl}, #{thumbnailUrl}, #{messageStatus}, #{isRecalled}, #{extraData}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insertMessage(UserChatMessage message);

    /**
     * 根据消息ID查询消息
     */
    @Select("SELECT * FROM user_chat_message WHERE message_id = #{messageId}")
    UserChatMessage selectMessageById(Long messageId);

    /**
     * 获取两个用户之间的聊天消息列表（分页）
     */
    @Select("SELECT m.*, " +
            "us.username as sender_username, us.avatar as sender_avatar, " +
            "ur.username as receiver_username, ur.avatar as receiver_avatar " +
            "FROM user_chat_message m " +
            "LEFT JOIN users us ON m.sender_id = us.user_id " +
            "LEFT JOIN users ur ON m.receiver_id = ur.user_id " +
            "WHERE ((m.sender_id = #{currentUserId} AND m.receiver_id = #{targetUserId}) " +
            "   OR (m.sender_id = #{targetUserId} AND m.receiver_id = #{currentUserId})) " +
            "AND m.is_recalled = 0 " +
            "AND (#{lastMessageId} IS NULL OR m.message_id < #{lastMessageId}) " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{pageSize}")
    List<ChatMessageDTO> selectMessagesBetweenUsers(@Param("currentUserId") Integer currentUserId, 
                                                   @Param("targetUserId") Integer targetUserId,
                                                   @Param("lastMessageId") Long lastMessageId,
                                                   @Param("pageSize") Integer pageSize);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE user_chat_message SET message_status = 3, read_time = #{readTime} " +
            "WHERE receiver_id = #{currentUserId} AND sender_id = #{targetUserId} AND message_status < 3")
    int markMessagesAsRead(@Param("currentUserId") Integer currentUserId, 
                          @Param("targetUserId") Integer targetUserId,
                          @Param("readTime") LocalDateTime readTime);

    /**
     * 撤回消息
     */
    @Update("UPDATE user_chat_message SET is_recalled = 1 WHERE message_id = #{messageId} AND sender_id = #{senderId}")
    int recallMessage(@Param("messageId") Long messageId, @Param("senderId") Integer senderId);

    /**
     * 获取用户未读消息总数
     */
    @Select("SELECT COUNT(*) FROM user_chat_message " +
            "WHERE receiver_id = #{userId} AND message_status < 3 AND is_recalled = 0")
    int selectUnreadCount(@Param("userId") Integer userId);

    /**
     * 搜索聊天消息
     */
    @Select("SELECT m.*, " +
            "us.username as sender_username, us.avatar as sender_avatar, " +
            "ur.username as receiver_username, ur.avatar as receiver_avatar " +
            "FROM user_chat_message m " +
            "LEFT JOIN users us ON m.sender_id = us.user_id " +
            "LEFT JOIN users ur ON m.receiver_id = ur.user_id " +
            "WHERE ((m.sender_id = #{currentUserId} OR m.receiver_id = #{currentUserId}) " +
            "   AND (#{targetUserId} IS NULL OR m.sender_id = #{targetUserId} OR m.receiver_id = #{targetUserId})) " +
            "AND m.content LIKE CONCAT('%', #{keyword}, '%') " +
            "AND m.is_recalled = 0 AND m.message_type = 1 " +
            "ORDER BY m.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<ChatMessageDTO> searchMessages(@Param("currentUserId") Integer currentUserId,
                                       @Param("targetUserId") Integer targetUserId,
                                       @Param("keyword") String keyword,
                                       @Param("offset") Integer offset,
                                       @Param("size") Integer size);

    // ==================== 会话相关操作 ====================

    /**
     * 插入或更新会话
     */
    @Insert("INSERT INTO user_chat_conversation (user1_id, user2_id, last_message_id, last_message_content, " +
            "last_message_time, last_message_type, user1_unread_count, user2_unread_count, create_time, update_time) " +
            "VALUES (#{user1Id}, #{user2Id}, #{lastMessageId}, #{lastMessageContent}, " +
            "#{lastMessageTime}, #{lastMessageType}, #{user1UnreadCount}, #{user2UnreadCount}, #{createTime}, #{updateTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "last_message_id = VALUES(last_message_id), " +
            "last_message_content = VALUES(last_message_content), " +
            "last_message_time = VALUES(last_message_time), " +
            "last_message_type = VALUES(last_message_type), " +
            "update_time = VALUES(update_time)")
    @Options(useGeneratedKeys = true, keyProperty = "conversationId")
    int insertOrUpdateConversation(UserChatConversation conversation);

    /**
     * 查找两个用户之间的会话
     */
    @Select("SELECT * FROM user_chat_conversation " +
            "WHERE (user1_id = #{user1Id} AND user2_id = #{user2Id}) " +
            "   OR (user1_id = #{user2Id} AND user2_id = #{user1Id})")
    UserChatConversation selectConversationBetweenUsers(@Param("user1Id") Integer user1Id, 
                                                       @Param("user2Id") Integer user2Id);

    /**
     * 获取用户的会话列表
     */
    @Select("SELECT c.*, " +
            "CASE WHEN c.user1_id = #{userId} THEN u2.user_id ELSE u1.user_id END as other_user_id, " +
            "CASE WHEN c.user1_id = #{userId} THEN u2.username ELSE u1.username END as other_username, " +
            "CASE WHEN c.user1_id = #{userId} THEN u2.avatar ELSE u1.avatar END as other_user_avatar, " +
            "CASE WHEN c.user1_id = #{userId} THEN c.user1_unread_count ELSE c.user2_unread_count END as unread_count, " +
            "uo.is_online " +
            "FROM user_chat_conversation c " +
            "LEFT JOIN users u1 ON c.user1_id = u1.user_id " +
            "LEFT JOIN users u2 ON c.user2_id = u2.user_id " +
            "LEFT JOIN user_online_status uo ON " +
            "   (CASE WHEN c.user1_id = #{userId} THEN u2.user_id ELSE u1.user_id END) = uo.user_id " +
            "WHERE (c.user1_id = #{userId} OR c.user2_id = #{userId}) " +
            "AND NOT ((c.user1_id = #{userId} AND COALESCE(c.user1_deleted, 0) = 1) " +
            "     OR (c.user2_id = #{userId} AND COALESCE(c.user2_deleted, 0) = 1)) " +
            "ORDER BY c.update_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<ConversationDTO> selectUserConversations(@Param("userId") Integer userId,
                                                 @Param("offset") Integer offset,
                                                 @Param("size") Integer size);

    /**
     * 简化的会话查询 - 用于调试
     */
    @Select("SELECT c.conversation_id, c.user1_id, c.user2_id, c.last_message_content, " +
            "c.user1_unread_count, c.user2_unread_count, c.user1_deleted, c.user2_deleted " +
            "FROM user_chat_conversation c " +
            "WHERE (c.user1_id = #{userId} OR c.user2_id = #{userId})")
    List<Map<String, Object>> selectUserConversationsDebug(@Param("userId") Integer userId);

    /**
     * 更新会话的最后一条消息信息
     */
    @Update("UPDATE user_chat_conversation SET " +
            "last_message_id = #{messageId}, " +
            "last_message_content = #{content}, " +
            "last_message_time = #{messageTime}, " +
            "last_message_type = #{messageType}, " +
            "update_time = #{updateTime} " +
            "WHERE conversation_id = #{conversationId}")
    int updateConversationLastMessage(@Param("conversationId") Long conversationId,
                                    @Param("messageId") Long messageId,
                                    @Param("content") String content,
                                    @Param("messageType") Integer messageType,
                                    @Param("messageTime") LocalDateTime messageTime,
                                    @Param("updateTime") LocalDateTime updateTime);

    /**
     * 增加用户的未读消息数
     */
    @Update("UPDATE user_chat_conversation SET " +
            "user1_unread_count = CASE WHEN user1_id = #{userId} THEN user1_unread_count + 1 ELSE user1_unread_count END, " +
            "user2_unread_count = CASE WHEN user2_id = #{userId} THEN user2_unread_count + 1 ELSE user2_unread_count END " +
            "WHERE conversation_id = #{conversationId}")
    int incrementUnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Integer userId);

    /**
     * 清空用户的未读消息数
     */
    @Update("UPDATE user_chat_conversation SET " +
            "user1_unread_count = CASE WHEN user1_id = #{userId} THEN 0 ELSE user1_unread_count END, " +
            "user2_unread_count = CASE WHEN user2_id = #{userId} THEN 0 ELSE user2_unread_count END " +
            "WHERE conversation_id = #{conversationId}")
    int clearUnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Integer userId);

    /**
     * 删除会话（标记为已删除）
     */
    @Update("UPDATE user_chat_conversation SET " +
            "user1_deleted = CASE WHEN user1_id = #{userId} THEN 1 ELSE user1_deleted END, " +
            "user2_deleted = CASE WHEN user2_id = #{userId} THEN 1 ELSE user2_deleted END " +
            "WHERE (user1_id = #{userId} OR user2_id = #{userId}) " +
            "AND ((user1_id = #{targetUserId} OR user2_id = #{targetUserId}))")
    int deleteConversation(@Param("userId") Integer userId, @Param("targetUserId") Integer targetUserId);

    // ==================== 用户信息查询 ====================

    /**
     * 根据用户ID查询用户基本信息
     */
    @Select("SELECT user_id, username, avatar FROM users WHERE user_id = #{userId}")
    Map<String, Object> selectUserBasicInfo(@Param("userId") Integer userId);

    /**
     * 简化的会话查询 - 移除user_online_status表JOIN
     */
    @Select("SELECT c.conversation_id, " +
            "CASE WHEN c.user1_id = #{userId} THEN u2.user_id ELSE u1.user_id END as other_user_id, " +
            "CASE WHEN c.user1_id = #{userId} THEN u2.username ELSE u1.username END as other_username, " +
            "CASE WHEN c.user1_id = #{userId} THEN u2.avatar ELSE u1.avatar END as other_user_avatar, " +
            "c.last_message_id, c.last_message_content, c.last_message_time, c.last_message_type, " +
            "CASE WHEN c.user1_id = #{userId} THEN c.user1_unread_count ELSE c.user2_unread_count END as unread_count, " +
            "c.create_time, c.update_time, " +
            "false as is_online " +
            "FROM user_chat_conversation c " +
            "LEFT JOIN users u1 ON c.user1_id = u1.user_id " +
            "LEFT JOIN users u2 ON c.user2_id = u2.user_id " +
            "WHERE (c.user1_id = #{userId} OR c.user2_id = #{userId}) " +
            "AND NOT ((c.user1_id = #{userId} AND c.user1_deleted = 1) OR (c.user2_id = #{userId} AND c.user2_deleted = 1)) " +
            "ORDER BY c.update_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<ConversationDTO> selectUserConversationsSimple(@Param("userId") Integer userId,
                                                        @Param("offset") Integer offset,
                                                        @Param("size") Integer size);
} 