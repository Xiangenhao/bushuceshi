package org.example.afd.service;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.mapper.UserOnlineStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 在线状态清理服务
 * 定时清理过期的用户在线状态
 * 
 * @author AFD Team
 * @version 1.0
 */
@Slf4j
@Service
public class OnlineStatusCleanupService {

    @Autowired
    private UserOnlineStatusMapper userOnlineStatusMapper;

    /**
     * 定时清理过期的在线状态
     * 每5分钟执行一次，将超过30分钟未活跃的用户设为离线
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300,000毫秒
    public void cleanupExpiredOnlineStatus() {
        try {
            log.debug("开始执行在线状态清理任务");
            
            int cleanedCount = userOnlineStatusMapper.cleanupExpiredOnlineStatus();
            
            if (cleanedCount > 0) {
                log.info("在线状态清理任务完成: 清理了{}个过期的在线状态", cleanedCount);
            } else {
                log.debug("在线状态清理任务完成: 没有需要清理的过期状态");
            }
            
        } catch (Exception e) {
            log.error("在线状态清理任务执行异常", e);
        }
    }

    /**
     * 获取在线用户统计信息 - 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3,600,000毫秒
    public void logOnlineStatistics() {
        try {
            var statistics = userOnlineStatusMapper.getOnlineStatistics();

            Number totalUsersNum = (Number) statistics.get("total_users");
            Number onlineUsersNum = (Number) statistics.get("online_users");
            Number offlineUsersNum = (Number) statistics.get("offline_users");

            int totalUsers = (totalUsersNum != null) ? totalUsersNum.intValue() : 0;
            int onlineUsers = (onlineUsersNum != null) ? onlineUsersNum.intValue() : 0;
            int offlineUsers = (offlineUsersNum != null) ? offlineUsersNum.intValue() : 0;

            log.info("用户在线统计 - 总用户数: {}, 在线用户数: {}, 离线用户数: {}",
                    totalUsers, onlineUsers, offlineUsers);

        } catch (Exception e) {
            log.error("获取在线用户统计信息异常", e);
        }
    }
} 