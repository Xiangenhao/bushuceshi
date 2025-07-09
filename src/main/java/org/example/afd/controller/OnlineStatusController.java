package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.model.Result;
import org.example.afd.service.ChatService;
import org.example.afd.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户在线状态控制器
 * 提供用户在线状态相关的API接口
 * 
 * @author AFD Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/online-status")
public class OnlineStatusController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 检查单个用户是否在线
     * @param userId 要查询的用户ID
     * @param request HTTP请求（用于获取当前用户信息）
     * @return 用户在线状态
     */
    @GetMapping("/check/{userId}")
    public Result<Boolean> checkUserOnlineStatus(@PathVariable Integer userId, HttpServletRequest request) {
        try {
            log.info("检查用户在线状态: userId={}", userId);
            
            // 验证用户身份
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            Boolean isOnline = chatService.isUserOnline(userId);
            log.info("用户{}在线状态: {}", userId, isOnline);
            
            return Result.success(isOnline);
            
        } catch (Exception e) {
            log.error("检查用户在线状态失败: userId={}", userId, e);
            return Result.error("检查在线状态失败");
        }
    }

    /**
     * 批量检查用户在线状态
     * @param userIds 用户ID列表
     * @param request HTTP请求
     * @return 用户在线状态Map
     */
    @PostMapping("/batch-check")
    public Result<Map<Integer, Boolean>> batchCheckOnlineStatus(@RequestBody List<Integer> userIds, 
                                                               HttpServletRequest request) {
        try {
            log.info("批量检查用户在线状态: userIds={}", userIds);
            
            // 验证用户身份
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            if (userIds == null || userIds.isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            if (userIds.size() > 100) {
                return Result.error("一次最多查询100个用户");
            }
            
            Map<Integer, Boolean> onlineStatusMap = chatService.batchCheckOnlineStatus(userIds);
            log.info("批量查询在线状态完成: 查询{}个用户", userIds.size());
            
            return Result.success(onlineStatusMap);
            
        } catch (Exception e) {
            log.error("批量检查用户在线状态失败: userIds={}", userIds, e);
            return Result.error("批量检查在线状态失败");
        }
    }

    /**
     * 更新当前用户活跃时间
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/heartbeat")
    public Result<String> updateActiveTime(HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            log.debug("更新用户活跃时间: userId={}", currentUserId);
            chatService.updateUserActiveTime(currentUserId);
            
            return Result.success("活跃时间更新成功");
            
        } catch (Exception e) {
            log.error("更新用户活跃时间失败", e);
            return Result.error("更新活跃时间失败");
        }
    }

    /**
     * 手动设置用户离线
     * @param request HTTP请求
     * @return 操作结果
     */
    @PostMapping("/offline")
    public Result<String> setOffline(HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            log.info("手动设置用户离线: userId={}", currentUserId);
            chatService.setUserOffline(currentUserId);
            
            return Result.success("已设置为离线状态");
            
        } catch (Exception e) {
            log.error("设置用户离线失败: userId={}", e);
            return Result.error("设置离线状态失败");
        }
    }

    /**
     * 获取当前用户的在线状态
     * @param request HTTP请求
     * @return 当前用户在线状态
     */
    @GetMapping("/my-status")
    public Result<Boolean> getMyOnlineStatus(HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Integer currentUserId = jwtUtil.getUserIdFromRequest(request);
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            Boolean isOnline = chatService.isUserOnline(currentUserId);
            log.debug("获取当前用户在线状态: userId={}, isOnline={}", currentUserId, isOnline);
            
            return Result.success(isOnline);
            
        } catch (Exception e) {
            log.error("获取当前用户在线状态失败", e);
            return Result.error("获取在线状态失败");
        }
    }
} 