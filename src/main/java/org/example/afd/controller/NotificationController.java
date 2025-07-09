package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.NotificationDTO;
import org.example.afd.model.Result;
import org.example.afd.service.NotificationService;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 * 提供获取通知列表、未读数、标记已读等功能
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取当前用户的通知列表（分页）
     *
     * @param page 页码，默认为1
     * @param size 每页数量，默认为20
     * @return 通知列表
     */
    @GetMapping
    public Result<List<NotificationDTO>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Integer userIdInt = UserIdHolder.getUserId();
        if (userIdInt == null) {
            return Result.error("用户未登录");
        }
        Long userId = userIdInt.longValue();
        log.info("获取用户 {} 的通知列表, page={}, size={}", userId, page, size);
        return notificationService.getNotifications(userId, page, size);
    }

    /**
     * 获取当前用户的未读通知数量
     *
     * @return 未读通知数
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadNotificationCount() {
        Integer userIdInt = UserIdHolder.getUserId();
        if (userIdInt == null) {
            return Result.error("用户未登录");
        }
        Long userId = userIdInt.longValue();
        log.info("获取用户 {} 的未读通知数", userId);
        return notificationService.getUnreadNotificationCount(userId);
    }

    /**
     * 将当前用户的所有未读通知标记为已读
     *
     * @return 操作结果
     */
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Integer userIdInt = UserIdHolder.getUserId();
        if (userIdInt == null) {
            return Result.error("用户未登录");
        }
        Long userId = userIdInt.longValue();
        log.info("标记用户 {} 的所有通知为已读", userId);
        return notificationService.markAllNotificationsAsRead(userId);
    }
} 