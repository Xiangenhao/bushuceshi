package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.pojo.LoginHistory;
import org.example.afd.pojo.User;
import org.example.afd.model.UserToken;
import org.springframework.context.annotation.ComponentScan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ComponentScan
@Mapper
public interface UserMapper {
    /**
     * 通过ID查询用户
     */
    @Select("SELECT * FROM users WHERE user_id = #{userId} AND deleted = 0")
    User selectById(int userId);

    /**
     * 通过用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User selectByUsername(String username);

    /**
     * 通过手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone_number = #{phoneNumber} AND deleted = 0")
    User selectByPhoneNumber(String phoneNumber);

    /**
     * 通过邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User selectByEmail(String email);

    /**
     * 插入用户
     */
    @Insert("INSERT INTO users (username, password, salt, phone_number, email, gender, role, " +
            "registration_time, status, deleted) " +
            "VALUES (#{username}, #{password}, #{salt}, #{phoneNumber}, #{email}, " +
            "#{gender}, #{role}, #{registrationTime}, #{status}, #{deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insert(User user);

    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET " +
            "username = #{username}, " +
            "email = #{email}, " +
            "phone_number = #{phoneNumber}, " +
            "gender = #{gender}, " +
            "birthday = #{birthday}, " +
            "signature = #{signature}, " +
            "background_image = #{backgroundImage}, " +
            "region = #{region}, " +
            "role = #{role}, " +
            "status = #{status}, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    void update(User user);

    /**
     * 更新用户密码
     */
    @Update("UPDATE users SET password = #{password}, salt = #{salt}, update_time = NOW() " +
            "WHERE user_id = #{userId}")
    void updatePassword(int userId, String password, String salt);

    /**
     * 更新用户头像
     */
    @Update("UPDATE users SET avatar = #{avatar}, update_time = NOW() WHERE user_id = #{userId}")
    void updateAvatar(int userId, String avatar);

    /**
     * 更新用户背景图
     */
    @Update("UPDATE users SET background_image = #{backgroundImage}, update_time = NOW() WHERE user_id = #{userId}")
    void updateBackgroundImage(int userId, String backgroundImage);

    /**
     * 更新最后登录时间和IP
     */
    @Update("UPDATE users SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp}, " +
            "update_time = NOW() WHERE user_id = #{userId}")
    void updateLoginInfo(int userId, LocalDateTime lastLoginTime, String lastLoginIp);

    /**
     * 更新性别
     */
    @Update("UPDATE users SET gender = #{gender}, update_time = NOW() WHERE user_id = #{userId}")
    void updateGender(int userId, int gender);

    /**
     * 更新生日
     */
    @Update("UPDATE users SET birthday = STR_TO_DATE(#{birthday}, '%Y-%m-%d'), update_time = NOW() " +
            "WHERE user_id = #{userId}")
    void updateBirthday(int userId, String birthday);

    /**
     * 更新用户名
     */
    @Update("UPDATE users SET username = #{username}, update_time = NOW() WHERE user_id = #{userId}")
    void updateUsername(int userId, String username);

    /**
     * 更新个性签名
     */
    @Update("UPDATE users SET signature = #{signature}, update_time = NOW() WHERE user_id = #{userId}")
    void updateSignature(int userId, String signature);

    /**
     * 更新用户地区
     */
    @Update("UPDATE users SET region = #{region}, update_time = NOW() WHERE user_id = #{userId}")
    void updateRegion(int userId, String region);
    
    /**
     * 更新用户个人介绍
     */
    @Update("UPDATE users SET introduction = #{introduction}, update_time = NOW() WHERE user_id = #{userId}")
    void updateIntroduction(int userId, String introduction);

    /**
     * 保存用户令牌
     */
    @Insert("INSERT INTO user_tokens (user_id, refresh_token, client_ip, user_agent, expires_at, created_at, revoked) " +
            "VALUES (#{userId}, #{refreshToken}, #{clientIp}, #{userAgent}, #{expiresAt}, #{createdAt}, #{revoked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void saveUserToken(UserToken userToken);

    /**
     * 根据刷新令牌查询用户令牌
     */
    @Select("SELECT * FROM user_tokens WHERE refresh_token = #{refreshToken} AND revoked = 0")
    UserToken findByRefreshToken(String refreshToken);

    /**
     * 撤销令牌
     */
    @Update("UPDATE user_tokens SET revoked = 1 WHERE refresh_token = #{refreshToken}")
    void revokeRefreshToken(String refreshToken);

    /**
     * 撤销用户所有令牌
     */
    @Update("UPDATE user_tokens SET revoked = 1 WHERE user_id = #{userId}")
    void revokeAllUserTokens(int userId);

    /**
     * 记录登录历史
     */
    @Insert("INSERT INTO user_login_history (user_id, login_time, login_ip, login_device, login_status, login_message) " +
            "VALUES (#{userId}, #{loginTime}, #{loginIp}, #{loginDevice}, #{loginStatus}, #{loginMessage})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void saveLoginHistory(LoginHistory loginHistory);

    /**
     * 获取用户粉丝数量
     */
    @Select("SELECT COUNT(*) FROM user_relation " +
            "WHERE target_id = #{userId} AND relation_type = 1 AND status = 1")
    int getUserFollowerCount(@Param("userId") Integer userId);
    
    /**
     * 获取用户关注数量
     */
    @Select("SELECT COUNT(*) FROM user_relation " +
            "WHERE user_id = #{userId} AND relation_type = 1 AND status = 1")
    int getUserFollowingCount(@Param("userId") Integer userId);
    
    /**
     * 获取用户订阅数量
     */
    @Select("SELECT COUNT(*) FROM post_user_subscription " +
            "WHERE user_id = #{userId} AND status = 1 AND end_time > NOW()")
    int getUserSubscriptionCount(@Param("userId") Integer userId);
    
    /**
     * 查询用户是否关注了目标用户
     */
    @Select("SELECT COUNT(*) > 0 FROM user_relation " +
            "WHERE user_id = #{userId} AND target_id = #{targetId} " +
            "AND relation_type = 1 AND status = 1")
    boolean isUserFollowing(@Param("userId") Integer userId, @Param("targetId") Integer targetId);

    /**
     * 查询用户是否有关注目标用户的记录
     */
    @Select("SELECT COUNT(*) > 0 FROM user_relation " +
            "WHERE user_id = #{userId} AND target_id = #{targetId} " +
            "AND relation_type = 1 AND status = 0")
    boolean isUserFollowingRecord(@Param("userId") Integer userId, @Param("targetId") Integer targetId);

    /**
     * 获取所有用户（仅ID和用户名）
     */
    @Select("SELECT user_id, username FROM users WHERE deleted = 0")
    List<User> getAllUsers();

    /**
     * 添加关注关系
     */
    @Insert("INSERT INTO user_relation (user_id, target_id, relation_type, create_time, status) " +
            "VALUES (#{userId}, #{targetId}, 1, NOW(), 1)")
    int addFollowRelation(@Param("userId") Integer userId, @Param("targetId") Integer targetId);
    
    /**
     * 更新关注关系状态
     */
    @Update("UPDATE user_relation SET status = #{status}, create_time = NOW() " +
            "WHERE user_id = #{userId} AND target_id = #{targetId} AND relation_type = 1")
    int updateFollowRelationStatus(@Param("userId") Integer userId, @Param("targetId") Integer targetId, @Param("status") Integer status);
    
    /**
     * 获取用户的粉丝列表
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_relation ur ON u.user_id = ur.user_id " +
            "WHERE ur.target_id = #{userId} AND ur.relation_type = 1 AND ur.status = 1 " +
            "LIMIT #{offset}, #{size}")
    List<User> getUserFollowers(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("size") Integer size);
    
    /**
     * 获取用户的关注列表
     */
    @Select("SELECT u.* FROM users u " +
            "JOIN user_relation ur ON u.user_id = ur.target_id " +
            "WHERE ur.user_id = #{userId} AND ur.relation_type = 1 AND ur.status = 1 " +
            "LIMIT #{offset}, #{size}")
    List<User> getUserFollowing(@Param("userId") Integer userId, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 通过用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    Map<String, Object> selectUserByUsername(String username);
    
    /**
     * 通过手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE phone_number = #{phone} AND deleted = 0")
    Map<String, Object> selectUserByPhone(String phone);
    
    /**
     * 通过邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    Map<String, Object> selectUserByEmail(String email);
    
    /**
     * 通过ID查询用户
     * @param userId 用户ID
     * @return 用户信息
     */
    Map<String, Object> selectUserById(Long userId);
    
    /**
     * 插入新用户
     * @param user 用户信息
     * @return 影响行数
     */
    int insertUser(Map<String, Object> user);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响行数
     */
    int updateUser(Map<String, Object> user);
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param password 新密码
     * @return 影响行数
     */
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);
    
    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param avatar 头像URL
     * @return 影响行数
     */
    int updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);
    
    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @return 影响行数
     */
    int updateLastLoginTime(Long userId);
    
    /**
     * 更新用户积分
     * @param userId 用户ID
     * @param points 积分变化量（可为正或负）
     * @return 影响行数
     */
    int updatePoints(@Param("userId") Long userId, @Param("points") Integer points);
    
    /**
     * 插入积分记录
     * @param pointsRecord 积分记录
     * @return 影响行数
     */
    int insertPointsRecord(Map<String, Object> pointsRecord);
    
    /**
     * 获取用户积分
     * @param userId 用户ID
     * @return 用户积分
     */
    Integer selectUserPoints(Long userId);
    
    /**
     * 更新用户等级
     * @param userId 用户ID
     * @param level 用户等级
     * @return 影响行数
     */
    int updateUserLevel(@Param("userId") Long userId, @Param("level") Integer level);
}
