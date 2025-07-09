package org.example.afd.service;

import org.example.afd.dto.UserDTO;
import org.example.afd.model.AuthResponse;
import org.example.afd.model.LoginRequest;
import org.example.afd.model.RefreshTokenRequest;
import org.example.afd.model.RegisterRequest;
import org.example.afd.pojo.User;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 注册用户
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    User register(RegisterRequest registerRequest);

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 认证结果
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 认证结果
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * 用户登出
     *
     * @param refreshToken 刷新令牌
     */
    void logout(String refreshToken);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(int userId);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像URL
     */
    void updateAvatar(int userId, String avatar);

    /**
     * 更新用户性别
     *
     * @param userId 用户ID
     * @param gender 性别
     */
    void updateGender(int userId, int gender);

    /**
     * 更新用户生日
     *
     * @param userId   用户ID
     * @param birthday 生日
     */
    void updateBirthday(int userId, String birthday);

    /**
     * 更新用户名
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    void updateUsername(int userId, String username);

    /**
     * 更新个性签名
     *
     * @param userId    用户ID
     * @param signature 个性签名
     */
    void updateSignature(int userId, String signature);

    /**
     * 更新用户背景图片
     *
     * @param userId 用户ID
     * @param backgroundImage 背景图片URL
     */
    void updateBackgroundImage(int userId, String backgroundImage);

    /**
     * 更新用户地区
     *
     * @param userId 用户ID
     * @param region 地区
     */
    void updateRegion(int userId, String region);

    /**
     * 更新用户个人介绍
     *
     * @param userId 用户ID
     * @param introduction 个人介绍
     */
    void updateIntroduction(int userId, String introduction);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     */
    void updateUser(User user);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(int userId, String oldPassword, String newPassword);

    /**
     * 更新密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(int userId, String oldPassword, String newPassword);

    /**
     * 通过验证码更新密码
     *
     * @param userId           用户ID
     * @param newPassword      新密码
     * @param verificationType 验证方式（phone/email）
     * @param account          验证账号（手机号或邮箱）
     * @param verificationCode 验证码
     */
    void updatePasswordWithVerification(Integer userId, String newPassword, String verificationType, String account, String verificationCode);

    /**
     * 关注用户
     *
     * @param userId   当前用户ID
     * @param targetId 目标用户ID
     * @return 是否成功
     */
    boolean followUser(Integer userId, Integer targetId);

    /**
     * 取消关注用户
     *
     * @param userId   当前用户ID
     * @param targetId 目标用户ID
     * @return 是否成功
     */
    boolean unfollowUser(Integer userId, Integer targetId);

    /**
     * 获取用户的粉丝列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 粉丝列表
     */
    List<User> getUserFollowers(Integer userId, Integer page, Integer size);

    /**
     * 获取用户的关注列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 关注列表
     */
    List<User> getUserFollowing(Integer userId, Integer page, Integer size);
    
    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param points 积分数量
     * @param description 积分描述
     * @return 增加结果
     */
    Map<String, Object> addPoints(Long userId, Integer points, String description);
    
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 检查结果
     */
    Map<String, Object> checkUsername(String username);
    
    /**
     * 检查手机号是否可用
     * @param phone 手机号
     * @return 检查结果
     */
    Map<String, Object> checkPhone(String phone);
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 检查结果
     */
    Map<String, Object> checkEmail(String email);
    
    /**
     * 绑定手机号
     * @param userId 用户ID
     * @param phoneNumber 手机号
     */
    void bindPhoneNumber(Integer userId, String phoneNumber);
    
    /**
     * 解绑手机号
     * @param userId 用户ID
     */
    void unbindPhoneNumber(Integer userId);
    
    /**
     * 绑定邮箱
     * @param userId 用户ID
     * @param email 邮箱地址
     */
    void bindEmail(Integer userId, String email);
    
    /**
     * 解绑邮箱
     * @param userId 用户ID
     */
    void unbindEmail(Integer userId);

    /**
     * 通过商家ID获取对应的用户ID
     * @param merchantId 商家ID
     * @return 用户ID
     */
    Long getMerchantUserId(Long merchantId);
}
