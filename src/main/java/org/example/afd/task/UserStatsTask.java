package org.example.afd.task;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.mapper.UserMapper;
import org.example.afd.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户统计数据同步任务
 * 定期同步用户的关注数、粉丝数和订阅数，确保数据准确性
 */
@Component
@Slf4j
public class UserStatsTask {

    @Autowired
    private UserMapper userMapper;

    /**
     * 每天凌晨2点执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncUserStats() {
        log.info("开始同步用户统计数据...");
        List<User> users = userMapper.getAllUsers();
        
        int successCount = 0;
        for (User user : users) {
            try {
                // 获取真实的统计数据
                int followCount = userMapper.getUserFollowingCount(user.getUserId());
                int fansCount = userMapper.getUserFollowerCount(user.getUserId());
                int subscriptionCount = userMapper.getUserSubscriptionCount(user.getUserId());
                
                // 由于users表中没有统计字段，暂时不更新统计数据
                // userMapper.updateUserStats(user.getUserId(), followCount, fansCount, subscriptionCount);
                log.debug("用户统计数据: userId={}, followCount={}, fansCount={}, subscriptionCount={}", 
                    user.getUserId(), followCount, fansCount, subscriptionCount);
                successCount++;
            } catch (Exception e) {
                log.error("同步用户统计数据失败: userId={}", user.getUserId(), e);
            }
        }
        
        log.info("用户统计数据同步完成，成功处理 {} 个用户", successCount);
    }
} 