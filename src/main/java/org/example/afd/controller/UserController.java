package org.example.afd.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.UserRegisterRequest;
import org.example.afd.pojo.User;
import org.example.afd.mapper.UserMapper;
import org.example.afd.model.Result;
import org.example.afd.service.UserService;
import org.example.afd.utils.CaptchaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CaptchaUtils captchaUtils;

    // ==================== 注册相关接口 ====================
    
    /**
     * 生成图像验证码
     */
    @GetMapping("/captcha")
    public ResponseEntity<Result<Map<String, String>>> generateCaptcha() {
        try {
            log.info("=== 生成图像验证码 ===");
            Map<String, String> captcha = captchaUtils.generateCaptcha();
            log.info("验证码生成成功: captchaId={}", captcha.get("captchaId"));
            return ResponseEntity.ok(Result.success(captcha));
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return ResponseEntity.ok(Result.error("验证码生成失败"));
        }
    }
    
    /**
     * 发送短信/邮件验证码
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<Result<Map<String, Object>>> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String account = request.get("account");
            String registerType = request.get("registerType");
            String captcha = request.get("captcha");
            String captchaId = request.get("captchaId");
            
            log.info("=== 发送验证码 ===");
            log.info("请求参数: account={}, registerType={}, captchaId={}", account, registerType, captchaId);
            
            // 验证图像验证码
            if (!captchaUtils.verifyCaptcha(captchaId, captcha)) {
                log.warn("图像验证码验证失败: captchaId={}, inputCode={}", captchaId, captcha);
                return ResponseEntity.ok(Result.error("图像验证码错误"));
            }
            
            // TODO: 实际发送短信/邮件验证码
            // 这里暂时模拟发送成功
            log.info("验证码发送成功（模拟）: account={}", account);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "验证码已发送");
            result.put("expiryTime", 300); // 5分钟有效期
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return ResponseEntity.ok(Result.error("发送验证码失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户注册（新版）
     */
    @PostMapping("/register")
    public ResponseEntity<Result<Map<String, Object>>> userRegister(@RequestBody UserRegisterRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("=== 用户注册 ===");
            log.info("请求参数: {}", request);
            
            // 图形验证码已在第一步（发送验证码）时验证，此处不再重复验证
            
            // 构造原有的RegisterRequest
            org.example.afd.model.RegisterRequest registerRequest = new org.example.afd.model.RegisterRequest();
            registerRequest.setUsername(request.getUsername());
            registerRequest.setPassword(request.getPassword());
            
            if ("phone".equals(request.getRegisterType())) {
                registerRequest.setPhoneNumber(request.getAccount());
            } else if ("email".equals(request.getRegisterType())) {
                registerRequest.setEmail(request.getAccount());
            }
            
            registerRequest.setVerificationCode(request.getVerificationCode());
            registerRequest.setClientIp(getClientIp(httpRequest));
            registerRequest.setUserAgent(httpRequest.getHeader("User-Agent"));
            registerRequest.setRole("USER");
            
            // 调用注册服务
            User user = userService.register(registerRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "注册成功");
            result.put("userId", user.getUserId());
            result.put("account", request.getAccount());
            
            log.info("用户注册成功: userId={}, account={}", user.getUserId(), request.getAccount());
            return ResponseEntity.ok(Result.success(result));
            
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return ResponseEntity.ok(Result.error("注册失败: " + e.getMessage()));
        }
    }

    /**
     * 检查手机号是否已注册
     */
    @GetMapping("/check-phone")
    public ResponseEntity<Result<Map<String, Object>>> checkPhoneRegistration(@RequestParam String phone) {
        try {
            log.info("=== 检查手机号是否已注册 ===");
            log.info("请求参数: phone={}", phone);
            
            Map<String, Object> result = userService.checkPhone(phone);
            log.info("检查手机号结果: {}", result);
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("检查手机号失败", e);
            return ResponseEntity.ok(Result.error("检查手机号失败: " + e.getMessage()));
        }
    }
    
    /**
     * 检查邮箱是否已注册
     */
    @GetMapping("/check-email")
    public ResponseEntity<Result<Map<String, Object>>> checkEmailRegistration(@RequestParam String email) {
        try {
            log.info("=== 检查邮箱是否已注册 ===");
            log.info("请求参数: email={}", email);
            
            Map<String, Object> result = userService.checkEmail(email);
            log.info("检查邮箱结果: {}", result);
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            log.error("检查邮箱失败", e);
            return ResponseEntity.ok(Result.error("检查邮箱失败: " + e.getMessage()));
        }
    }

    // ==================== 原有接口 ====================

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Result<User>> getCurrentUser(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            // 清除敏感信息
            user.setPassword(null);
            user.setSalt(null);

            return ResponseEntity.ok(Result.success(user));
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取指定用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Result<User>> getUserById(@PathVariable Integer userId) {
        long startTime = System.currentTimeMillis();
        log.info("接收获取用户信息请求: userId={}", userId);

        try {
            if (userId == null || userId <= 0) {
                log.warn("获取用户信息参数无效: userId={}", userId);
                return ResponseEntity.ok(Result.error("用户ID无效"));
            }

            // 调用服务获取用户信息
            User user = userService.getUserById(userId);

            if (user == null) {
                log.warn("用户不存在: userId={}", userId);
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            // 确保统计字段不为null
            if (user.getFollowCount() == null) user.setFollowCount(0);
            if (user.getFansCount() == null) user.setFansCount(0);
            if (user.getPlanCount() == null) user.setPlanCount(0);

            // 记录返回数据大小，辅助排查性能问题
            long endTime = System.currentTimeMillis();
            log.info("返回用户信息成功: userId={}, username={}, 字段数量={}, 响应耗时={}ms",
                    userId, user.getUsername(), countNonNullFields(user), (endTime - startTime));

            return ResponseEntity.ok(Result.success(user));
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("获取用户信息失败: userId={}, 错误信息={}, 耗时={}ms",
                    userId, e.getMessage(), (endTime - startTime), e);
            return ResponseEntity.ok(Result.error("获取用户信息失败: " + e.getMessage()));
        }
    }

    /**
     * 计算对象中非空字段的数量，用于日志记录
     */
    private int countNonNullFields(Object obj) {
        if (obj == null) return 0;

        int count = 0;
        try {
            // 获取所有字段，包括父类字段
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(obj) != null) {
                    count++;
                }
            }
        } catch (Exception e) {
            log.warn("计算非空字段失败", e);
        }

        return count;
    }

    /**
     * 更新用户头像
     */
    @PutMapping("/avatar")
    public ResponseEntity<Result<String>> updateAvatar(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String avatar = requestBody.get("avatar");
            if (avatar == null || avatar.isEmpty()) {
                return ResponseEntity.ok(Result.error("头像URL不能为空"));
            }

            userService.updateAvatar(userId, avatar);

            return ResponseEntity.ok(Result.success("头像更新成功", avatar));
        } catch (Exception e) {
            log.error("更新头像失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新用户性别
     */
    @PutMapping("/gender")
    public ResponseEntity<Result<Integer>> updateGender(
            @RequestBody Integer gender,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            if (gender == null || (gender != 0 && gender != 1 && gender != 2)) {
                return ResponseEntity.ok(Result.error("性别参数无效，请提供有效的性别值(0-未知，1-男，2-女)"));
            }

            userService.updateGender(userId, gender);

            return ResponseEntity.ok(Result.success("性别更新成功", gender));
        } catch (Exception e) {
            log.error("更新性别失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新用户生日
     */
    @PutMapping("/birthday")
    public ResponseEntity<Result<String>> updateBirthday(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String birthday = requestBody.get("birthday");
            if (birthday == null || birthday.isEmpty()) {
                return ResponseEntity.ok(Result.error("生日不能为空"));
            }

            userService.updateBirthday(userId, birthday);

            return ResponseEntity.ok(Result.success("生日更新成功", birthday));
        } catch (Exception e) {
            log.error("更新生日失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新用户名
     */
    @PutMapping("/username")
    public ResponseEntity<Result<String>> updateUsername(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String username = requestBody.get("username");
            if (username == null || username.isEmpty()) {
                return ResponseEntity.ok(Result.error("用户名不能为空"));
            }

            userService.updateUsername(userId, username);

            return ResponseEntity.ok(Result.success("用户名更新成功", username));
        } catch (Exception e) {
            log.error("更新用户名失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新个性签名
     */
    @PutMapping("/signature")
    public ResponseEntity<Result<String>> updateSignature(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String signature = requestBody.get("signature");
            if (signature == null) {
                signature = ""; // 允许清空签名
            }

            userService.updateSignature(userId, signature);

            return ResponseEntity.ok(Result.success("个性签名更新成功", signature));
        } catch (Exception e) {
            log.error("更新个性签名失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新用户背景图片
     */
    @PutMapping("/background")
    public ResponseEntity<Result<String>> updateBackgroundImage(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String backgroundImage = requestBody.get("backgroundImage");
            if (backgroundImage == null || backgroundImage.isEmpty()) {
                return ResponseEntity.ok(Result.error("背景图URL不能为空"));
            }

            // 确保UserService中有对应的方法
            userService.updateBackgroundImage(userId, backgroundImage);

            return ResponseEntity.ok(Result.success("背景图更新成功", backgroundImage));
        } catch (Exception e) {
            log.error("更新背景图失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 更新用户地区
     */
    @PutMapping("/region")
    public ResponseEntity<Result<String>> updateRegion(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String region = requestBody.get("region");
            if (region == null) {
                region = ""; // 允许清空地区
            }

            userService.updateRegion(userId, region);

            return ResponseEntity.ok(Result.success("地区更新成功", region));
        } catch (Exception e) {
            log.error("更新地区失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
    
    /**
     * 更新用户个人介绍
     */
    @PutMapping("/introduction")
    public ResponseEntity<Result<String>> updateIntroduction(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String introduction = requestBody.get("introduction");
            if (introduction == null) {
                introduction = ""; // 允许清空介绍
            }

            userService.updateIntroduction(userId, introduction);

            return ResponseEntity.ok(Result.success("个人介绍更新成功", introduction));
        } catch (Exception e) {
            log.error("更新个人介绍失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    public ResponseEntity<Result<Object>> updatePassword(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String oldPassword = requestBody.get("oldPassword");
            String newPassword = requestBody.get("newPassword");

            if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.ok(Result.error("旧密码和新密码不能为空"));
            }

            // 确保UserService中有对应的方法
            userService.updatePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(Result.success("密码更新成功"));
        } catch (Exception e) {
            log.error("更新密码失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 通过验证码修改密码
     */
    @PutMapping("/password/verify")
    public ResponseEntity<Result<Object>> updatePasswordWithVerification(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String newPassword = requestBody.get("newPassword");
            String verificationCode = requestBody.get("verificationCode");
            String verificationType = requestBody.get("verificationType"); // "phone" 或 "email"
            String account = requestBody.get("account"); // 手机号或邮箱

            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.ok(Result.error("新密码不能为空"));
            }

            if (verificationCode == null || verificationCode.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证码不能为空"));
            }

            if (verificationType == null || verificationType.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证方式不能为空"));
            }

            if (account == null || account.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证账号不能为空"));
            }

            // TODO: 验证验证码的有效性
            // 这里应该检查验证码是否正确且未过期
            // 暂时跳过验证码验证

            // 调用服务层更新密码（使用验证码方式）
            userService.updatePasswordWithVerification(userId, newPassword, verificationType, account, verificationCode);

            log.info("用户通过验证码修改密码成功: userId={}, verificationType={}", userId, verificationType);
            return ResponseEntity.ok(Result.success("密码修改成功"));
        } catch (Exception e) {
            log.error("通过验证码修改密码失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 关注用户
     */
    @PostMapping("/{targetId}/follow")
    @Transactional
    public ResponseEntity<Result<Boolean>> followUser(
            @PathVariable Integer targetId,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            // 检查目标用户是否存在
            User targetUser = userService.getUserById(targetId);
            if (targetUser == null) {
                return ResponseEntity.ok(Result.error("目标用户不存在"));
            }

            // 不能关注自己
            if (userId.equals(targetId)) {
                return ResponseEntity.ok(Result.error("不能关注自己"));
            }

            // 检查是否已经关注
            boolean isFollowing = userMapper.isUserFollowing(userId, targetId);
            if (isFollowing) {
                return ResponseEntity.ok(Result.success("已经关注过该用户", true));
            }

            // 添加关注关系（假设在UserService中实现）
            boolean success = userService.followUser(userId, targetId);
            if (!success) {
                return ResponseEntity.ok(Result.error("关注失败"));
            }

            // 不再更新关注数和粉丝数，因为users表中没有这些统计字段
            log.info("用户关注成功: userId={}, targetId={}", userId, targetId);

            return ResponseEntity.ok(Result.success("关注成功", true));
        } catch (Exception e) {
            log.error("关注用户失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 取消关注用户
     */
    @DeleteMapping("/{targetId}/unfollow")
    @Transactional
    public ResponseEntity<Result<Boolean>> unfollowUser(
            @PathVariable Integer targetId,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            // 检查目标用户是否存在
            User targetUser = userService.getUserById(targetId);
            if (targetUser == null) {
                return ResponseEntity.ok(Result.error("目标用户不存在"));
            }

            // 检查是否已经关注
            boolean isFollowing = userMapper.isUserFollowing(userId, targetId);
            if (!isFollowing) {
                return ResponseEntity.ok(Result.success("未关注该用户", false));
            }

            // 取消关注关系（假设在UserService中实现）
            boolean success = userService.unfollowUser(userId, targetId);
            if (!success) {
                return ResponseEntity.ok(Result.error("取消关注失败"));
            }

            // 不再更新关注数和粉丝数，因为users表中没有这些统计字段
            log.info("用户取消关注成功: userId={}, targetId={}", userId, targetId);

            return ResponseEntity.ok(Result.success("取消关注成功", false));
        } catch (Exception e) {
            log.error("取消关注用户失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的粉丝列表
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Result<List<User>>> getUserFollowers(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            // 检查用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            // 获取粉丝列表（假设在UserService中实现）
            List<User> followers = userService.getUserFollowers(userId, page, size);

            return ResponseEntity.ok(Result.success(followers));
        } catch (Exception e) {
            log.error("获取用户粉丝列表失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的关注列表
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<Result<List<User>>> getUserFollowing(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            // 检查用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            // 获取关注列表（假设在UserService中实现）
            List<User> following = userService.getUserFollowing(userId, page, size);

            return ResponseEntity.ok(Result.success(following));
        } catch (Exception e) {
            log.error("获取用户关注列表失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 绑定手机号
     */
    @PutMapping("/phone")
    public ResponseEntity<Result<String>> bindPhoneNumber(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String phoneNumber = requestBody.get("phoneNumber");
            String verificationCode = requestBody.get("verificationCode");

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return ResponseEntity.ok(Result.error("手机号不能为空"));
            }

            if (verificationCode == null || verificationCode.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证码不能为空"));
            }

            // TODO: 验证验证码的有效性
            // 这里应该检查验证码是否正确且未过期
            // 暂时跳过验证码验证

            // 调用服务层绑定手机号
            userService.bindPhoneNumber(userId, phoneNumber);

            log.info("用户绑定手机号成功: userId={}, phoneNumber={}", userId, phoneNumber);

            return ResponseEntity.ok(Result.success("绑定手机号成功", phoneNumber));
        } catch (Exception e) {
            log.error("绑定手机号失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 解绑手机号
     */
    @DeleteMapping("/phone")
    public ResponseEntity<Result<Object>> unbindPhoneNumber(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String verificationCode = requestBody.get("verificationCode");

            if (verificationCode == null || verificationCode.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证码不能为空"));
            }

            // TODO: 验证验证码的有效性
            // 这里应该检查验证码是否正确且未过期
            // 暂时跳过验证码验证

            // 调用服务层解绑手机号
            userService.unbindPhoneNumber(userId);

            log.info("用户解绑手机号成功: userId={}", userId);

            return ResponseEntity.ok(Result.success("解绑手机号成功"));
        } catch (Exception e) {
            log.error("解绑手机号失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 绑定邮箱
     */
    @PutMapping("/email")
    public ResponseEntity<Result<String>> bindEmail(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String email = requestBody.get("email");
            String verificationCode = requestBody.get("verificationCode");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.ok(Result.error("邮箱不能为空"));
            }

            if (verificationCode == null || verificationCode.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证码不能为空"));
            }

            // TODO: 验证验证码的有效性
            // 这里应该检查验证码是否正确且未过期
            // 暂时跳过验证码验证

            // 调用服务层绑定邮箱
            userService.bindEmail(userId, email);

            log.info("用户绑定邮箱成功: userId={}, email={}", userId, email);

            return ResponseEntity.ok(Result.success("绑定邮箱成功", email));
        } catch (Exception e) {
            log.error("绑定邮箱失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 解绑邮箱
     */
    @DeleteMapping("/email")
    public ResponseEntity<Result<Object>> unbindEmail(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            // 从JWT令牌中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Result.error("未授权"));
            }

            String verificationCode = requestBody.get("verificationCode");

            if (verificationCode == null || verificationCode.isEmpty()) {
                return ResponseEntity.ok(Result.error("验证码不能为空"));
            }

            // TODO: 验证验证码的有效性
            // 这里应该检查验证码是否正确且未过期
            // 暂时跳过验证码验证

            // 调用服务层解绑邮箱
            userService.unbindEmail(userId);

            log.info("用户解绑邮箱成功: userId={}", userId);

            return ResponseEntity.ok(Result.success("解绑邮箱成功"));
        } catch (Exception e) {
            log.error("解绑邮箱失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }
}


