package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.dto.NotificationDTO;
import org.example.afd.pojo.Notification;

import java.util.List;

/**
 * 通知数据访问层
 */
@Mapper
public interface NotificationMapper {

    /**
     * 插入一条新的通知
     *
     * @param notification 通知对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO notifications (receiver_id, trigger_user_id, type, target_id, secondary_target_id, content, is_read, create_time) " +
            "VALUES (#{receiverId}, #{triggerUserId}, #{type}, #{targetId}, #{secondaryTargetId}, #{content}, #{isRead}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertNotification(Notification notification);

    /**
     * 根据接收者ID获取通知列表（分页）
     *
     * @param receiverId 接收者用户ID
     * @param offset     偏移量
     * @param limit      每页数量
     * @return 通知DTO列表
     */
    @Select("SELECT " +
            "   n.id, n.type, n.target_id, n.secondary_target_id, n.content, n.is_read, n.create_time, " +
            "   u.user_id as trigger_user_id, u.username as trigger_username, u.avatar as trigger_avatar " +
            "FROM " +
            "   notifications n " +
            "JOIN " +
            "   users u ON n.trigger_user_id = u.user_id " +
            "WHERE " +
            "   n.receiver_id = #{receiverId} " +
            "ORDER BY " +
            "   n.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
            @Result(property = "id", column = "id", id = true),
            @Result(property = "type", column = "type"),
            @Result(property = "targetId", column = "target_id"),
            @Result(property = "secondaryTargetId", column = "secondary_target_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "isRead", column = "is_read"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "triggerUser.userId", column = "trigger_user_id"),
            @Result(property = "triggerUser.username", column = "trigger_username"),
            @Result(property = "triggerUser.avatar", column = "trigger_avatar")
    })
    List<NotificationDTO> getNotificationsByReceiverId(@Param("receiverId") Long receiverId,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    /**
     * 获取指定用户的未读通知数量
     *
     * @param receiverId 接收者用户ID
     * @return 未读通知数
     */
    @Select("SELECT count(*) FROM notifications WHERE receiver_id = #{receiverId} AND is_read = 0")
    int getUnreadNotificationCount(@Param("receiverId") Long receiverId);

    /**
     * 将指定用户的所有未读通知标记为已读
     *
     * @param receiverId 接收者用户ID
     * @return 影响的行数
     */
    @Update("UPDATE notifications SET is_read = 1 WHERE receiver_id = #{receiverId} AND is_read = 0")
    int markAllAsRead(@Param("receiverId") Long receiverId);

    /**
     * 根据ID删除通知
     *
     * @param notificationId 通知ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM notifications WHERE id = #{notificationId}")
    int deleteNotificationById(@Param("notificationId") Long notificationId);
} 