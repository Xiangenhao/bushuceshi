package org.example.afd.service;

import org.example.afd.dto.NotificationDTO;
import org.example.afd.model.Result;
import org.example.afd.pojo.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建并保存一条通知
     *
     * @param notification 通知实体
     */
    void createNotification(Notification notification);

    /**
     * 获取用户的通知列表（分页）
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 封装了通知列表的结果对象
     */
    Result<List<NotificationDTO>> getNotifications(Long userId, int page, int size);

    /**
     * 获取用户的未读通知数量
     *
     * @param userId 用户ID
     * @return 封装了未读数量的结果对象
     */
    Result<Integer> getUnreadNotificationCount(Long userId);

    /**
     * 将用户的所有通知标记为已读
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Void> markAllNotificationsAsRead(Long userId);
} 