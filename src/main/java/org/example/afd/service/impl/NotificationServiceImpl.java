package org.example.afd.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.NotificationDTO;
import org.example.afd.enums.NotificationType;
import org.example.afd.mapper.NotificationMapper;
import org.example.afd.mapper.PostMapper;
import org.example.afd.model.Result;
import org.example.afd.pojo.Notification;
import org.example.afd.service.NotificationService;
import org.example.afd.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private PostMapper postMapper; // 用于获取动态封面等信息

    /**
     * 通用的创建通知方法
     * @param receiverId 接收者ID
     * @param triggerUserId 触发者ID
     * @param type 通知类型
     * @param targetId 目标ID (如动态ID、评论ID)
     * @param secondaryTargetId 次要目标ID (如动态ID)
     * @param content 内容快照
     */
    @Transactional
    public void sendNotification(Long receiverId, Long triggerUserId, NotificationType type, Long targetId, Long secondaryTargetId, String content) {
        // 防止自己给自己发通知
        if (Objects.equals(receiverId, triggerUserId)) {
            log.info("接收者和触发者是同一个人 (userId={})，不发送通知。", receiverId);
            return;
        }

        log.info("准备发送通知: from {} to {}, type={}, targetId={}", triggerUserId, receiverId, type, targetId);
        try {
            Notification notification = new Notification();
            notification.setReceiverId(receiverId);
            notification.setTriggerUserId(triggerUserId);
            notification.setType(type);
            notification.setTargetId(targetId);
            notification.setSecondaryTargetId(secondaryTargetId);
            notification.setRead(false);
            notification.setCreateTime(LocalDateTime.now());

            // 对内容进行截断，防止过长
            if (content != null && content.length() > 100) {
                notification.setContent(content.substring(0, 100) + "...");
            } else {
                notification.setContent(content);
            }

            notificationMapper.insertNotification(notification);
            log.info("通知创建成功: from {} to {}", triggerUserId, receiverId);
        } catch (Exception e) {
            log.error("创建通知失败: from {} to {}, type={}", triggerUserId, receiverId, type, e);
            // 考虑添加重试或告警机制
        }
    }

    @Override
    public void createNotification(Notification notification) {
        if (notification == null || notification.getReceiverId() == null || notification.getTriggerUserId() == null) {
            log.error("尝试创建无效的通知: {}", notification);
            return;
        }
        // 防止自己给自己发通知
        if (Objects.equals(notification.getReceiverId(), notification.getTriggerUserId())) {
            return;
        }
        notification.setRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insertNotification(notification);
    }

    @Override
    public Result<List<NotificationDTO>> getNotifications(Long userId, int page, int size) {
        if (page <= 0) page = 1;
        if (size <= 0) size = 10;
        int offset = (page - 1) * size;

        try {
            List<NotificationDTO> notifications = notificationMapper.getNotificationsByReceiverId(userId, offset, size);
            // 数据后处理
            for (NotificationDTO dto : notifications) {
                // 1. 设置易读的相对时间
                dto.setFormattedTime(DateUtils.getTimeAgo(dto.getCreateTime()));
                // 2. 设置类型描述
                dto.getTypeDescription();
                // 3. 根据类型获取封面图
                if (dto.getType() == NotificationType.LIKE_POST || dto.getType() == NotificationType.COMMENT_POST) {
                    List<String> mediaUrls = postMapper.getPostMediaUrls(dto.getTargetId());
                    if (mediaUrls != null && !mediaUrls.isEmpty()) {
                        dto.setTargetCoverUrl(mediaUrls.get(0));
                    }
                } else if (dto.getType() == NotificationType.REPLY_COMMENT || dto.getType() == NotificationType.LIKE_COMMENT) {
                    if (dto.getSecondaryTargetId() != null) {
                         List<String> mediaUrls = postMapper.getPostMediaUrls(dto.getSecondaryTargetId());
                         if (mediaUrls != null && !mediaUrls.isEmpty()) {
                            dto.setTargetCoverUrl(mediaUrls.get(0));
                        }
                    }
                }
            }
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("获取用户 {} 的通知列表失败", userId, e);
            return Result.error("获取通知列表失败");
        }
    }

    @Override
    public Result<Integer> getUnreadNotificationCount(Long userId) {
        try {
            int count = notificationMapper.getUnreadNotificationCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取用户 {} 的未读通知数失败", userId, e);
            return Result.error("获取未读通知数失败");
        }
    }

    @Override
    @Transactional
    public Result<Void> markAllNotificationsAsRead(Long userId) {
        try {
            notificationMapper.markAllAsRead(userId);
            return Result.success("全部已读操作成功");
        } catch (Exception e) {
            log.error("标记用户 {} 的所有通知为已读失败", userId, e);
            return Result.error("标记已读失败");
        }
    }
} 