package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.UserOnlineStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户在线状态数据访问层
 * 专门处理用户在线状态相关的数据库操作
 * 
 * @author AFD Team
 * @version 1.0
 */
@Mapper
public interface UserOnlineStatusMapper {

    /**
     * 插入或更新用户在线状态
     * @param userOnlineStatus 用户在线状态实体
     * @return 影响的行数
     */
    @Insert("INSERT INTO user_online_status (user_id, status, last_active_time, device_info, " +
            "client_type, session_token, ip_address, location, update_time) " +
            "VALUES (#{userId}, #{status}, #{lastActiveTime}, #{deviceInfo}, " +
            "#{clientType}, #{sessionToken}, #{ipAddress}, #{location}, #{updateTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "status = VALUES(status), " +
            "last_active_time = VALUES(last_active_time), " +
            "device_info = VALUES(device_info), " +
            "client_type = VALUES(client_type), " +
            "session_token = VALUES(session_token), " +
            "ip_address = VALUES(ip_address), " +
            "location = VALUES(location), " +
            "update_time = VALUES(update_time)")
    int insertOrUpdateUserOnlineStatus(UserOnlineStatus userOnlineStatus);

    /**
     * 根据用户ID查询在线状态
     * @param userId 用户ID
     * @return 用户在线状态实体
     */
    @Select("SELECT * FROM user_online_status WHERE user_id = #{userId}")
    UserOnlineStatus selectByUserId(@Param("userId") Integer userId);

    /**
     * 获取用户是否在线（简化版本，仅返回boolean）
     * @param userId 用户ID
     * @return 是否在线
     */
    @Select("SELECT CASE " +
            "WHEN status = 1 AND last_active_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN 1 " +
            "ELSE 0 END as is_online " +
            "FROM user_online_status WHERE user_id = #{userId}")
    Boolean isUserOnline(@Param("userId") Integer userId);

    /**
     * 更新用户在线状态（简化版本）
     * @param userId 用户ID
     * @param isOnline 是否在线
     * @param lastActiveTime 最后活跃时间
     * @param deviceInfo 设备信息
     * @param updateTime 更新时间
     * @return 影响的行数
     */
    @Insert("INSERT INTO user_online_status (user_id, status, last_active_time, device_info, update_time) " +
            "VALUES (#{userId}, #{status}, #{lastActiveTime}, #{deviceInfo}, #{updateTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "status = VALUES(status), " +
            "last_active_time = VALUES(last_active_time), " +
            "device_info = VALUES(device_info), " +
            "update_time = VALUES(update_time)")
    int updateUserOnlineStatus(@Param("userId") Integer userId,
                              @Param("status") Integer status,
                              @Param("lastActiveTime") LocalDateTime lastActiveTime,
                              @Param("deviceInfo") String deviceInfo,
                              @Param("updateTime") LocalDateTime updateTime);

    /**
     * 设置用户在线
     * @param userId 用户ID
     * @param deviceInfo 设备信息
     * @param clientType 客户端类型
     * @param sessionToken 会话token
     * @param ipAddress IP地址
     * @param location 位置信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO user_online_status (user_id, status, last_active_time, device_info, " +
            "client_type, session_token, ip_address, location, update_time) " +
            "VALUES (#{userId}, 1, NOW(), #{deviceInfo}, #{clientType}, #{sessionToken}, " +
            "#{ipAddress}, #{location}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "status = 1, " +
            "last_active_time = NOW(), " +
            "device_info = VALUES(device_info), " +
            "client_type = VALUES(client_type), " +
            "session_token = VALUES(session_token), " +
            "ip_address = VALUES(ip_address), " +
            "location = VALUES(location), " +
            "update_time = NOW()")
    int setUserOnline(@Param("userId") Integer userId,
                     @Param("deviceInfo") String deviceInfo,
                     @Param("clientType") Integer clientType,
                     @Param("sessionToken") String sessionToken,
                     @Param("ipAddress") String ipAddress,
                     @Param("location") String location);

    /**
     * 设置用户离线
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Update("UPDATE user_online_status SET status = 0, update_time = NOW() WHERE user_id = #{userId}")
    int setUserOffline(@Param("userId") Integer userId);

    /**
     * 更新用户活跃时间
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Update("UPDATE user_online_status SET last_active_time = NOW(), update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateActiveTime(@Param("userId") Integer userId);

    /**
     * 批量查询用户在线状态
     * @param userIds 用户ID列表
     * @return 用户在线状态Map（用户ID -> 是否在线）
     */
    @Select("<script>" +
            "SELECT user_id, " +
            "CASE WHEN status = 1 AND last_active_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN 1 ELSE 0 END as is_online " +
            "FROM user_online_status WHERE user_id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    List<Map<String, Object>> batchGetOnlineStatus(@Param("userIds") List<Integer> userIds);

    /**
     * 获取所有在线用户
     * @return 在线用户列表
     */
    @Select("SELECT * FROM user_online_status " +
            "WHERE status = 1 AND last_active_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE)")
    List<UserOnlineStatus> getAllOnlineUsers();

    /**
     * 清理过期的在线状态（超过30分钟未活跃的用户设为离线）
     * @return 影响的行数
     */
    @Update("UPDATE user_online_status SET status = 0, update_time = NOW() " +
            "WHERE status = 1 AND last_active_time < DATE_SUB(NOW(), INTERVAL 30 MINUTE)")
    int cleanupExpiredOnlineStatus();

    /**
     * 根据用户ID删除在线状态记录
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM user_online_status WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);

    /**
     * 获取在线用户统计信息
     * @return 在线用户统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_users, " +
            "COUNT(CASE WHEN status = 1 AND last_active_time > DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN 1 END) as online_users, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as offline_users " +
            "FROM user_online_status")
    Map<String, Object> getOnlineStatistics();

    /**
     * 更新会话token
     * @param userId 用户ID
     * @param sessionToken 新的会话token
     * @return 影响的行数
     */
    @Update("UPDATE user_online_status SET session_token = #{sessionToken}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int updateSessionToken(@Param("userId") Integer userId, @Param("sessionToken") String sessionToken);
} 