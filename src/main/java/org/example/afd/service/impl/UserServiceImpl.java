package org.example.afd.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.UserDTO;
import org.example.afd.mapper.UserMapper;
import org.example.afd.model.AuthResponse;
import org.example.afd.model.LoginRequest;
import org.example.afd.model.RefreshTokenRequest;
import org.example.afd.model.RegisterRequest;
import org.example.afd.pojo.LoginHistory;
import org.example.afd.pojo.User;
import org.example.afd.model.UserToken;
import org.example.afd.service.UserService;
import org.example.afd.utils.JwtUtils;
import org.example.afd.utils.PasswordUtils;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private org.example.afd.mapper.MerchantMapper merchantMapper;

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpirationInSeconds;

    // 用于生成简单的token，实际应用中应该使用更安全的JWT
    private static final String TOKEN_SECRET = "your_secret_key";
    
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    
    // 手机号正则表达式（简化版，仅支持中国大陆11位手机号）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public User register(RegisterRequest registerRequest) {
        log.info("开始用户注册流程: 用户名={}", registerRequest.getUsername());

        // 验证短信/邮件验证码
        String verificationCode = registerRequest.getVerificationCode();
        if (verificationCode == null || verificationCode.isEmpty()) {
            log.warn("注册失败: 验证码为空");
            throw new RuntimeException("请输入验证码");
        }
        
        // TODO: 实际验证短信/邮件验证码
        // 这里暂时跳过验证，实际应该调用验证码服务进行验证
        
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            log.warn("注册失败: 用户名已存在, 用户名={}", registerRequest.getUsername());
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已被注册
        if (registerRequest.getPhoneNumber() != null && !registerRequest.getPhoneNumber().isEmpty()) {
            log.debug("检查手机号是否已被注册: {}", registerRequest.getPhoneNumber());
            existingUser = userMapper.selectByPhoneNumber(registerRequest.getPhoneNumber());
            if (existingUser != null) {
                log.warn("注册失败: 手机号已被注册, 手机号={}", registerRequest.getPhoneNumber());
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 检查邮箱是否已被注册
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            log.debug("检查邮箱是否已被注册: {}", registerRequest.getEmail());
            existingUser = userMapper.selectByEmail(registerRequest.getEmail());
            if (existingUser != null) {
                log.warn("注册失败: 邮箱已被注册, 邮箱={}", registerRequest.getEmail());
                throw new RuntimeException("邮箱已被注册");
            }
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setEmail(registerRequest.getEmail());

        // 密码加密
        String salt = passwordUtils.generateSalt();
        String encryptedPassword = passwordUtils.encryptPassword(registerRequest.getPassword(), salt);
        user.setPassword(encryptedPassword);
        user.setSalt(salt);

        // 设置用户信息
        // 优先使用请求中的角色，如果没有设置则默认为USER
        String role = (registerRequest.getRole() != null && !registerRequest.getRole().isEmpty()) 
                     ? registerRequest.getRole() 
                     : "USER";
        user.setRole(role);
        user.setGender(0);
        user.setStatus(0);
        user.setDeleted(0);
        user.setRegistrationTime(LocalDateTime.now());
        
        log.debug("设置用户角色: {}", role);

        // 保存用户
        log.debug("开始保存用户信息到数据库");
        userMapper.insert(user);

        // 记录注册日志
        log.info("新用户注册成功: 用户名={}, ID={}, 邮箱={}, 手机号={}",
                user.getUsername(), user.getUserId(), user.getEmail(), user.getPhoneNumber());

        return user;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        User user = null;
        String account = loginRequest.getAccount();
        String loginIp = loginRequest.getLoginIp();
        String deviceInfo = loginRequest.getDeviceInfo();

        log.info("用户登录请求: 账号={}, IP={}, 设备信息={}", account, loginIp, deviceInfo);

        // 创建登录历史记录
        LoginHistory loginHistory = createLoginHistory(loginIp, deviceInfo);

        try {
            log.debug("开始用户登录认证流程");

            // 1. 用户认证阶段
            user = authenticateUser(account, loginRequest.getPassword(), loginHistory);

            // 2. 更新用户登录信息
            updateUserLoginInfo(user, loginIp);

            // 3. 生成令牌并保存
            TokenInfo tokenInfo = generateAndSaveTokens(user, loginIp, deviceInfo, loginRequest.getRememberMe());

            // 4. 记录登录成功
            recordSuccessfulLogin(loginHistory, user, loginIp);

            // 5. 返回认证响应
            return AuthResponse.of(
                    tokenInfo.getAccessToken(),
                    tokenInfo.getRefreshToken(),
                    tokenInfo.getExpirationTime(),
                    user);
        } catch (RuntimeException e) {
            log.warn("登录失败(业务异常): 账号={}, 原因={}", account, e.getMessage());
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            // 记录系统错误
            log.error("登录失败(系统异常): 账号={}", account, e);
            handleLoginError(e, user, loginHistory);
            throw new RuntimeException("登录失败，系统错误");
        }
    }




//    @Override
//    public Integer getUserPoints(Long userId) {
//        if (userId == null) {
//            return 0;
//        }
//
//        Integer points = userMapper.selectUserPoints(userId);
//        return points != null ? points : 0;
//    }

    @Override
    @Transactional
    public Map<String, Object> addPoints(Long userId, Integer points, String description) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || points == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        
        // 检查用户是否存在
        Map<String, Object> user = userMapper.selectUserById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 更新积分
        int rows = userMapper.updatePoints(userId, points);
        
        if (rows > 0) {
            // 记录积分变动
            Map<String, Object> pointsRecord = new HashMap<>();
            pointsRecord.put("user_id", userId);
            pointsRecord.put("points", points);
            pointsRecord.put("description", description);
            pointsRecord.put("create_time", new Date());
            userMapper.insertPointsRecord(pointsRecord);
            
            // 获取当前积分，更新用户等级
            Integer currentPoints = userMapper.selectUserPoints(userId);
            if (currentPoints != null) {
                // 根据积分计算等级（简单实现）
                int level = calculateLevel(currentPoints);
                userMapper.updateUserLevel(userId, level);
            }
            
            result.put("success", true);
            result.put("message", "积分增加成功");
        } else {
            result.put("success", false);
            result.put("message", "积分增加失败");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> checkUsername(String username) {
        Map<String, Object> result = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            result.put("available", false);
            return result;
        }
        
        Map<String, Object> user = userMapper.selectUserByUsername(username);
        boolean available = (user == null);
        
        result.put("success", true);
        result.put("available", available);
        result.put("message", available ? "用户名可用" : "用户名已存在");
        
        return result;
    }

    @Override
    public Map<String, Object> checkPhone(String phone) {
        Map<String, Object> result = new HashMap<>();
        
        if (phone == null || phone.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "手机号不能为空");
            result.put("available", false);
            return result;
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            result.put("success", false);
            result.put("message", "手机号格式不正确");
            result.put("available", false);
            return result;
        }
        
        Map<String, Object> user = userMapper.selectUserByPhone(phone);
        boolean available = (user == null);
        
        result.put("success", true);
        result.put("available", available);
        result.put("message", available ? "手机号可用" : "手机号已被注册");
        
        return result;
    }

    @Override
    public Map<String, Object> checkEmail(String email) {
        Map<String, Object> result = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "邮箱不能为空");
            result.put("available", false);
            return result;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            result.put("success", false);
            result.put("message", "邮箱格式不正确");
            result.put("available", false);
            return result;
        }
        
        Map<String, Object> user = userMapper.selectUserByEmail(email);
        boolean available = (user == null);
        
        result.put("success", true);
        result.put("available", available);
        result.put("message", available ? "邮箱可用" : "邮箱已被注册");
        
        return result;
    }
    
    /**
     * 将数据库结果转换为UserDTO
     * @param user 数据库查询结果
     * @return UserDTO对象
     */
    private UserDTO convertToUserDTO(Map<String, Object> user) {
        UserDTO userDTO = new UserDTO();
        
        userDTO.setUserId(((Number) user.get("user_id")).longValue());
        userDTO.setUsername((String) user.get("username"));
        userDTO.setNickname((String) user.get("nickname"));
        userDTO.setAvatar((String) user.get("avatar"));
        userDTO.setPhone((String) user.get("phone"));
        userDTO.setEmail((String) user.get("email"));
        userDTO.setGender((Integer) user.get("gender"));
        userDTO.setBirthday((Date) user.get("birthday"));
        userDTO.setRegisterTime((Date) user.get("register_time"));
        userDTO.setLastLoginTime((Date) user.get("last_login_time"));
        userDTO.setStatus((Integer) user.get("status"));
        userDTO.setPoints((Integer) user.get("points"));
        userDTO.setLevel((Integer) user.get("level"));
        
        return userDTO;
    }
    
    /**
     * 根据积分计算用户等级
     * @param points 用户积分
     * @return 用户等级
     */
    private int calculateLevel(int points) {
        if (points < 100) {
            return 1;
        } else if (points < 500) {
            return 2;
        } else if (points < 1000) {
            return 3;
        } else if (points < 2000) {
            return 4;
        } else if (points < 5000) {
            return 5;
        } else {
            return 6;
        }
    }
    
    /**
     * 生成简单的token
     * @param userId 用户ID
     * @param username 用户名
     * @return token字符串
     */
    private String generateToken(Long userId, String username) {
        // 简单实现，实际应使用JWT
        String data = userId + ":" + username + ":" + System.currentTimeMillis() + ":" + TOKEN_SECRET;
        return DigestUtils.md5DigestAsHex(data.getBytes());
    }


    /**
     * 创建登录历史记录
     */
    private LoginHistory createLoginHistory(String loginIp, String deviceInfo) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setLoginTime(LocalDateTime.now());
        loginHistory.setLoginIp(loginIp);
        loginHistory.setLoginDevice(deviceInfo);
        return loginHistory;
    }

    /**
     * 验证用户身份
     */
    private User authenticateUser(String account, String password, LoginHistory loginHistory) {
        // 根据账号类型查询用户
        User user = findUserByAccount(account);
        
        // 用户不存在
        if (user == null) {
            // 不记录登录历史，直接抛出异常
            log.warn("登录失败: 用户不存在, 账号={}", account);
            throw new RuntimeException("用户名或密码错误");
        }
        log.debug("用户存在: 用户ID={}, 用户名={}", user.getUserId(), user.getUsername());
        
        // 设置用户ID到登录历史
        loginHistory.setUserId(user.getUserId());
        log.debug("设置用户ID到登录历史: 用户ID={}", user.getUserId());
        
        // 检查账号状态
        checkUserStatus(user, loginHistory);
        log.debug("账号状态正常: 用户ID={}", user.getUserId());
        
        // 验证密码
        verifyPassword(password, user, loginHistory);
        log.debug("密码验证成功: 用户ID={}", user.getUserId());
        
        return user;
    }

    /**
     * 根据账号类型查询用户
     */
    private User findUserByAccount(String account) {
        if (account.contains("@")) {
            // 邮箱登录
            log.debug("尝试通过邮箱登录: {}", account);
            return userMapper.selectByEmail(account);
        } else if (account.matches("^\\d+$")) {
            // 手机号登录
            log.debug("尝试通过手机号登录: {}", account);
            return userMapper.selectByPhoneNumber(account);
        } else {
            // 用户名登录
            log.debug("尝试通过用户名登录: {}", account);
            return userMapper.selectByUsername(account);
        }
    }

    /**
     * 检查用户账号状态
     */
    private void checkUserStatus(User user, LoginHistory loginHistory) {
        // 账号被禁用
        if (user.getStatus() != null && user.getStatus() == 1) {
            log.warn("登录失败: 账号已被禁用, 用户ID={}, 用户名={}", user.getUserId(), user.getUsername());
            loginHistory.setLoginStatus(0);
            loginHistory.setLoginMessage("账号已被禁用");
            userMapper.saveLoginHistory(loginHistory);
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
    }

    /**
     * 验证用户密码
     */
    private void verifyPassword(String password, User user, LoginHistory loginHistory) {
        if (!passwordUtils.matches(password, user.getPassword(), user.getSalt())) {
            log.warn("登录失败: 密码错误, 用户ID={}, 用户名={}", user.getUserId(), user.getUsername());
            loginHistory.setLoginStatus(0);
            loginHistory.setLoginMessage("密码错误");
            userMapper.saveLoginHistory(loginHistory);
            throw new RuntimeException("用户名或密码错误");
        }

    }

    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(User user, String loginIp) {
        log.debug("更新用户登录信息: 用户ID={}, IP={}", user.getUserId(), loginIp);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userMapper.updateLoginInfo(user.getUserId(), user.getLastLoginTime(), user.getLastLoginIp());
    }

    /**
     * 生成和保存令牌
     */
    private TokenInfo generateAndSaveTokens(User user, String loginIp, String deviceInfo, Boolean rememberMe) {
        log.debug("开始生成用户令牌: 用户ID={}, 记住我={}", user.getUserId(), rememberMe);
        
        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        
        // 计算过期时间
        Long expirationTime = refreshExpirationInSeconds;
        if (rememberMe != null && rememberMe) {
            expirationTime = expirationTime * 2; // 记住我，延长过期时间
        }
        
        // 保存刷新令牌
        saveRefreshToken(user, refreshToken, loginIp, deviceInfo, expirationTime);
        
        log.debug("令牌生成完成: 用户ID={}, 过期时间={}秒", user.getUserId(), expirationTime);
        return new TokenInfo(accessToken, refreshToken, expirationTime);
    }

    /**
     * 保存刷新令牌
     */
    private void saveRefreshToken(User user, String refreshToken, String loginIp, String deviceInfo, Long expirationTime) {
        log.debug("保存刷新令牌: 用户ID={}, IP={}", user.getUserId(), loginIp);
        UserToken userToken = new UserToken();
        userToken.setUserId(user.getUserId());
        userToken.setRefreshToken(refreshToken);
        userToken.setClientIp(loginIp);
        userToken.setUserAgent(deviceInfo);
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setExpiresAt(LocalDateTime.now().plusSeconds(expirationTime));
        userToken.setRevoked(0);
        
        userMapper.saveUserToken(userToken);
        log.debug("刷新令牌保存成功: 用户ID={}, 过期时间={}", user.getUserId(), userToken.getExpiresAt());
    }

    /**
     * 记录登录成功
     */
    private void recordSuccessfulLogin(LoginHistory loginHistory, User user, String loginIp) {
        loginHistory.setLoginStatus(1);
        loginHistory.setLoginMessage("登录成功");
        userMapper.saveLoginHistory(loginHistory);
        
        log.info("用户登录成功: {}, ID: {}, IP: {}", user.getUsername(), user.getUserId(), loginIp);
    }

    /**
     * 处理登录错误
     */
    private void handleLoginError(Exception e, User user, LoginHistory loginHistory) {
        log.error("登录过程发生系统错误", e);
        
        // 只有当用户不为null时才记录登录历史
        if (user != null) {
            log.debug("记录登录失败历史: 用户ID={}, 错误={}", user.getUserId(), e.getMessage());
            loginHistory.setUserId(user.getUserId());
            loginHistory.setLoginStatus(0);
            loginHistory.setLoginMessage("系统错误: " + e.getMessage());
            userMapper.saveLoginHistory(loginHistory);
        }
    }

    /**
     * 令牌信息类
     */
    private static class TokenInfo {
        private final String accessToken;
        private final String refreshToken;
        private final Long expirationTime;
        
        public TokenInfo(String accessToken, String refreshToken, Long expirationTime) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expirationTime = expirationTime;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public Long getExpirationTime() {
            return expirationTime;
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String clientIp = request.getClientIp();
        
        log.info("开始刷新令牌: IP={}, 刷新令牌长度={}", 
                 clientIp, 
                 refreshToken != null ? refreshToken.length() : 0);
        
        // 验证刷新令牌
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("刷新令牌失败: 刷新令牌为空, IP={}", clientIp);
            throw new RuntimeException("刷新令牌不能为空");
        }
        
        // 从数据库查询刷新令牌
        log.debug("查询刷新令牌: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())) + "...");
        UserToken userToken = userMapper.findByRefreshToken(refreshToken);
        if (userToken == null) {
            log.warn("刷新令牌失败: 数据库中未找到该令牌, IP={}", clientIp);
            throw new RuntimeException("刷新令牌无效或已被撤销");
        }
        
        if (userToken.getRevoked() == 1) {
            log.warn("刷新令牌失败: 令牌已被撤销, 用户ID={}, IP={}", userToken.getUserId(), clientIp);
            throw new RuntimeException("刷新令牌无效或已被撤销");
        }
        
        // 验证令牌是否过期
        if (userToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("刷新令牌失败: 令牌已过期, 用户ID={}, 过期时间={}, 当前时间={}, IP={}", 
                     userToken.getUserId(), 
                     userToken.getExpiresAt(), 
                     LocalDateTime.now(), 
                     clientIp);
            userMapper.revokeRefreshToken(refreshToken);
            throw new RuntimeException("刷新令牌已过期");
        }
        
        // 获取用户信息
        log.debug("获取用户信息: 用户ID={}", userToken.getUserId());
        User user = userMapper.selectById(userToken.getUserId());
        if (user == null) {
            log.warn("刷新令牌失败: 用户不存在, 用户ID={}, IP={}", userToken.getUserId(), clientIp);
            userMapper.revokeRefreshToken(refreshToken);
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        if (user.getDeleted() == 1) {
            log.warn("刷新令牌失败: 用户已被删除, 用户ID={}, IP={}", userToken.getUserId(), clientIp);
            userMapper.revokeRefreshToken(refreshToken);
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        if (user.getStatus() == 1) {
            log.warn("刷新令牌失败: 用户已被禁用, 用户ID={}, IP={}", userToken.getUserId(), clientIp);
            userMapper.revokeRefreshToken(refreshToken);
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        log.debug("用户信息验证通过: {}, ID: {}, 状态: {}", 
                  user.getUsername(), user.getUserId(), user.getStatus());
        
        // 生成新的访问令牌和刷新令牌
        String newAccessToken = jwtUtils.generateToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);
        
        log.debug("生成新令牌完成: 访问令牌长度={}, 刷新令牌长度={}", 
                  newAccessToken.length(), newRefreshToken.length());
        
        // 撤销旧的刷新令牌
        log.debug("撤销旧的刷新令牌");
        userMapper.revokeRefreshToken(refreshToken);
        
        // 保存新的刷新令牌
        UserToken newUserToken = new UserToken();
        newUserToken.setUserId(user.getUserId());
        newUserToken.setRefreshToken(newRefreshToken);
        newUserToken.setClientIp(request.getClientIp());
        newUserToken.setUserAgent(request.getDeviceInfo());
        newUserToken.setCreatedAt(LocalDateTime.now());
        newUserToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationInSeconds));
        newUserToken.setRevoked(0);
        
        log.debug("保存新的刷新令牌到数据库");
        userMapper.saveUserToken(newUserToken);
        
        log.info("用户刷新令牌成功: {}, ID: {}, 新令牌过期时间: {}", 
                 user.getUsername(), user.getUserId(), newUserToken.getExpiresAt());
        
        // 构造认证响应
        return AuthResponse.of(newAccessToken, newRefreshToken, refreshExpirationInSeconds, user);
    }

    @Override
    public void logout(String refreshToken) {
        log.info("用户登出请求: 刷新令牌={}", refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : "null");
        if (refreshToken != null && !refreshToken.isEmpty()) {
            userMapper.revokeRefreshToken(refreshToken);
            log.info("用户登出成功，已撤销刷新令牌");
        } else {
            log.warn("用户登出请求但刷新令牌为空");
        }
    }

    /**
     * 根据用户ID获取用户信息。
     * 该方法会从用户表中查询用户信息，包含统计字段。
     * 若查询成功，会清除用户的敏感信息（密码和盐值），并确保统计字段不为null。
     * 若查询失败，会记录错误日志并抛出异常。
     * 
     * @param userId 用户的ID
     * @return 包含用户信息的User对象，如果未找到则返回null
     */
    @Override
    public User getUserById(int userId) {
        long startTime = System.currentTimeMillis();
        log.info("获取用户信息开始: 用户ID={}", userId);
        
        try {
            // 直接从用户表中获取用户信息（包含统计字段）
            User user = userMapper.selectById(userId);
            
            if (user != null) {
                log.debug("成功获取用户基本信息: 用户ID={}, 用户名={}, 关注数={}, 粉丝数={}, 订阅数={}", 
                         userId, user.getUsername(), user.getFollowCount(), user.getFansCount(), user.getPlanCount());
                
                // 清除敏感信息
                user.setPassword(null);
                user.setSalt(null);
                
                // 如果统计数据字段为null，设置为0
                if (user.getFollowCount() == null) {
                    log.debug("用户关注数为null，设置为0: 用户ID={}", userId);
                    user.setFollowCount(0);
                }
                
                if (user.getFansCount() == null) {
                    log.debug("用户粉丝数为null，设置为0: 用户ID={}", userId);
                    user.setFansCount(0);
                }
                
                if (user.getPlanCount() == null) {
                    log.debug("用户订阅数为null，设置为0: 用户ID={}", userId);
                    user.setPlanCount(0);
                }

                // 获取当前登录用户ID
                Integer currentUserId = UserIdHolder.getUserId();
                if (currentUserId != null && currentUserId > 0) {
                    // 查询当前用户是否关注了目标用户
                    boolean isFollowing = userMapper.isUserFollowing(currentUserId, userId);
                    user.setIsFollowed(isFollowing);
                    log.debug("设置关注状态: 当前用户ID={}, 目标用户ID={}, 是否关注={}", currentUserId, userId, isFollowing);
                } else {
                    // 未登录或无法获取当前用户ID，设置为未关注
                    user.setIsFollowed(false);
                    log.debug("当前用户未登录或无法获取用户ID，设置为未关注: 目标用户ID={}", userId);
                }
                
            } else {
                log.warn("未找到用户: 用户ID={}", userId);
            }
            
            long endTime = System.currentTimeMillis();
            log.info("获取用户信息完成: 用户ID={}, 耗时={}ms", userId, (endTime - startTime));
            
            return user;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("获取用户信息异常: 用户ID={}, 耗时={}ms, 错误信息={}", userId, (endTime - startTime), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void updateAvatar(int userId, String avatar) {
        log.info("开始更新用户头像: 用户ID={}", userId);
        userMapper.updateAvatar(userId, avatar);
        log.info("用户头像更新成功: 用户ID={}", userId);
    }

    @Override
    public void updateGender(int userId, int gender) {
        log.info("开始更新用户性别: 用户ID={}, 性别={}", userId, gender);
        userMapper.updateGender(userId, gender);
        log.info("用户性别更新成功: 用户ID={}, 性别={}", userId, gender);
    }

    @Override
    public void updateBirthday(int userId, String birthday) {
        log.info("开始更新用户生日: 用户ID={}, 生日={}", userId, birthday);
        userMapper.updateBirthday(userId, birthday);
        log.info("用户生日更新成功: 用户ID={}", userId);
    }

    @Override
    public void updateUsername(int userId, String username) {
        try {
            log.info("开始更新用户名: 用户ID={}, 新用户名={}", userId, username);
            
            // 检查用户是否存在
            log.debug("检查用户是否存在: 用户ID={}", userId);
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("更新用户名失败: 用户不存在, 用户ID={}", userId);
                throw new RuntimeException("用户不存在");
            }
            
            // 直接更新用户名
            log.debug("开始更新用户名: 用户ID={}, 旧用户名={}, 新用户名={}", userId, user.getUsername(), username);
            userMapper.updateUsername(userId, username);
            log.info("用户名更新成功: 用户ID={}, 旧用户名={}, 新用户名={}", userId, user.getUsername(), username);
        } catch (Exception e) {
            log.error("更新用户名失败: userId={}, 新用户名={}", userId, username, e);
            throw new RuntimeException("更新用户名失败: " + e.getMessage());
        }
    }

    @Override
    public void updateSignature(int userId, String signature) {
        log.info("开始更新用户个性签名: 用户ID={}", userId);
        userMapper.updateSignature(userId, signature);
        log.info("用户个性签名更新成功: 用户ID={}", userId);
    }

    @Override
    public void updateBackgroundImage(int userId, String backgroundImage) {
        log.info("开始更新用户背景图: 用户ID={}", userId);
        userMapper.updateBackgroundImage(userId, backgroundImage);
        log.info("用户背景图更新成功: 用户ID={}", userId);
    }

    @Override
    public void updatePassword(int userId, String oldPassword, String newPassword) {
        log.info("开始更新用户密码: 用户ID={}", userId);
        // 验证旧密码并更新新密码
        boolean success = changePassword(userId, oldPassword, newPassword);
        if (!success) {
            log.warn("密码更新失败: 旧密码验证失败, 用户ID={}", userId);
            throw new RuntimeException("旧密码验证失败");
        }
        log.info("用户密码更新成功: 用户ID={}", userId);
    }

    @Override
    public void updateUser(User user) {
        log.info("开始更新用户信息: 用户ID={}, 用户名={}", user.getUserId(), user.getUsername());
        try {
            userMapper.update(user);
            log.info("用户信息更新成功: 用户ID={}, 用户名={}", user.getUserId(), user.getUsername());
        } catch (Exception e) {
            log.error("用户信息更新失败: 用户ID={}, 用户名={}", user.getUserId(), user.getUsername(), e);
            throw e;
        }
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        log.info("开始修改用户密码: 用户ID={}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("修改密码失败: 用户不存在, 用户ID={}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        log.debug("验证旧密码: 用户ID={}", userId);
        if (!passwordUtils.matches(oldPassword, user.getPassword(), user.getSalt())) {
            log.warn("修改密码失败: 旧密码验证失败, 用户ID={}", userId);
            return false;
        }
        
        // 生成新的盐值和密码
        log.debug("生成新的密码和盐值: 用户ID={}", userId);
        String newSalt = passwordUtils.generateSalt();
        String encryptedPassword = passwordUtils.encryptPassword(newPassword, newSalt);
        
        // 更新密码和盐值
        log.debug("更新密码和盐值: 用户ID={}", userId);
        userMapper.updatePassword(userId, encryptedPassword, newSalt);
        
        // 撤销用户所有刷新令牌，强制重新登录
        log.debug("撤销用户所有刷新令牌: 用户ID={}", userId);
        userMapper.revokeAllUserTokens(userId);
        
        log.info("用户密码修改成功: 用户ID={}, 用户名={}", userId, user.getUsername());
        
        return true;
    }

    /**
     * 关注用户
     */
    @Override
    @Transactional
    public boolean followUser(Integer userId, Integer targetId) {
        log.info("用户关注: userId={}, targetId={}", userId, targetId);
        
        try {
            // 检查是否已经关注
            boolean isFollowing = userMapper.isUserFollowingRecord(userId, targetId);
            
            if (isFollowing) {
                // 已关注，更新状态为有效
                int rows = userMapper.updateFollowRelationStatus(userId, targetId, 1);
                return rows > 0;
            } else {
                // 未关注，添加关注关系
                int rows = userMapper.addFollowRelation(userId, targetId);
                return rows > 0;
            }
        } catch (Exception e) {
            log.error("关注用户失败: userId={}, targetId={}", userId, targetId, e);
            return false;
        }
    }

    /**
     * 取消关注用户
     */
    @Override
    @Transactional
    public boolean unfollowUser(Integer userId, Integer targetId) {
        log.info("用户取消关注: userId={}, targetId={}", userId, targetId);
        
        try {
            // 更新关注状态为无效
            int rows = userMapper.updateFollowRelationStatus(userId, targetId, 0);
            return rows > 0;
        } catch (Exception e) {
            log.error("取消关注用户失败: userId={}, targetId={}", userId, targetId, e);
            return false;
        }
    }

    /**
     * 获取用户的粉丝列表
     */
    @Override
    public List<User> getUserFollowers(Integer userId, Integer page, Integer size) {
        log.info("获取用户粉丝列表: userId={}, page={}, size={}", userId, page, size);
        
        try {
            int offset = (page - 1) * size;
            List<User> followers = userMapper.getUserFollowers(userId, offset, size);
            
            // 清除敏感信息
            followers.forEach(user -> {
                user.setPassword(null);
                user.setSalt(null);
            });
            
            return followers;
        } catch (Exception e) {
            log.error("获取用户粉丝列表失败: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 获取用户的关注列表
     */
    @Override
    public List<User> getUserFollowing(Integer userId, Integer page, Integer size) {
        log.info("获取用户关注列表: userId={}, page={}, size={}", userId, page, size);
        
        try {
            int offset = (page - 1) * size;
            List<User> following = userMapper.getUserFollowing(userId, offset, size);
            
            // 清除敏感信息
            following.forEach(user -> {
                user.setPassword(null);
                user.setSalt(null);
            });
            
            return following;
        } catch (Exception e) {
            log.error("获取用户关注列表失败: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public void updatePasswordWithVerification(Integer userId, String newPassword, String verificationType, String account, String verificationCode) {
        log.info("开始通过验证码更新用户密码: userId={}, verificationType={}", userId, verificationType);

        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new RuntimeException("用户不存在");
        }

        // 验证账号信息
        if ("phone".equals(verificationType)) {
            if (user.getPhoneNumber() == null || !user.getPhoneNumber().equals(account)) {
                log.warn("手机号验证失败: userId={}, requestAccount={}, userAccount={}", userId, account, user.getPhoneNumber());
                throw new RuntimeException("手机号验证失败");
            }
        } else if ("email".equals(verificationType)) {
            if (user.getEmail() == null || !user.getEmail().equals(account)) {
                log.warn("邮箱验证失败: userId={}, requestAccount={}, userAccount={}", userId, account, user.getEmail());
                throw new RuntimeException("邮箱验证失败");
            }
        } else {
            log.warn("不支持的验证方式: {}", verificationType);
            throw new RuntimeException("不支持的验证方式");
        }

        // TODO: 验证验证码
        // 这里应该验证验证码是否正确且未过期
        // 暂时跳过验证码验证
        log.debug("验证码验证（暂时跳过）: verificationCode={}", verificationCode);

        // 生成新的盐值和加密密码
        String newSalt = passwordUtils.generateSalt();
        String encryptedPassword = passwordUtils.encryptPassword(newPassword, newSalt);

        // 更新密码
        userMapper.updatePassword(userId, encryptedPassword, newSalt);

        // 撤销用户所有刷新令牌，强制重新登录
        log.debug("撤销用户所有刷新令牌: 用户ID={}", userId);
        userMapper.revokeAllUserTokens(userId);

        log.info("用户通过验证码更新密码成功: userId={}, verificationType={}", userId, verificationType);
    }
    
    @Override
    @Transactional
    public void bindPhoneNumber(Integer userId, String phoneNumber) {
        log.info("开始绑定手机号: userId={}, phoneNumber={}", userId, phoneNumber);
        
        // 验证手机号格式
        if (phoneNumber == null || !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            log.warn("手机号格式无效: phoneNumber={}", phoneNumber);
            throw new RuntimeException("手机号格式无效");
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 检查手机号是否已被其他用户使用
        User existingUser = userMapper.selectByPhoneNumber(phoneNumber);
        if (existingUser != null && !existingUser.getUserId().equals(userId)) {
            log.warn("手机号已被其他用户使用: phoneNumber={}, existingUserId={}", phoneNumber, existingUser.getUserId());
            throw new RuntimeException("手机号已被使用");
        }
        
        // 更新用户手机号
        try {
            user.setPhoneNumber(phoneNumber);
            userMapper.update(user);
            log.info("手机号绑定成功: userId={}, phoneNumber={}", userId, phoneNumber);
        } catch (Exception e) {
            log.error("手机号绑定失败: userId={}, phoneNumber={}", userId, phoneNumber, e);
            throw new RuntimeException("手机号绑定失败");
        }
    }
    
    @Override
    @Transactional
    public void unbindPhoneNumber(Integer userId) {
        log.info("开始解绑手机号: userId={}", userId);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否已绑定手机号
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            log.warn("用户未绑定手机号: userId={}", userId);
            throw new RuntimeException("用户未绑定手机号");
        }
        
        // 解绑手机号
        try {
            user.setPhoneNumber(null);
            userMapper.update(user);
            log.info("手机号解绑成功: userId={}", userId);
        } catch (Exception e) {
            log.error("手机号解绑失败: userId={}", userId, e);
            throw new RuntimeException("手机号解绑失败");
        }
    }
    
    @Override
    @Transactional
    public void bindEmail(Integer userId, String email) {
        log.info("开始绑定邮箱: userId={}, email={}", userId, email);
        
        // 验证邮箱格式
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            log.warn("邮箱格式无效: email={}", email);
            throw new RuntimeException("邮箱格式无效");
        }
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 检查邮箱是否已被其他用户使用
        User existingUser = userMapper.selectByEmail(email);
        if (existingUser != null && !existingUser.getUserId().equals(userId)) {
            log.warn("邮箱已被其他用户使用: email={}, existingUserId={}", email, existingUser.getUserId());
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 更新用户邮箱
        try {
            user.setEmail(email);
            userMapper.update(user);
            log.info("邮箱绑定成功: userId={}, email={}", userId, email);
        } catch (Exception e) {
            log.error("邮箱绑定失败: userId={}, email={}", userId, email, e);
            throw new RuntimeException("邮箱绑定失败");
        }
    }
    
    @Override
    @Transactional
    public void unbindEmail(Integer userId) {
        log.info("开始解绑邮箱: userId={}", userId);
        
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 检查是否已绑定邮箱
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            log.warn("用户未绑定邮箱: userId={}", userId);
            throw new RuntimeException("用户未绑定邮箱");
        }
        
        // 解绑邮箱
        try {
            user.setEmail(null);
            userMapper.update(user);
            log.info("邮箱解绑成功: userId={}", userId);
        } catch (Exception e) {
            log.error("邮箱解绑失败: userId={}", userId, e);
            throw new RuntimeException("邮箱解绑失败");
        }
    }

    @Override
    public void updateRegion(int userId, String region) {
        log.info("开始更新用户地区: 用户ID={}, 地区={}", userId, region);
        userMapper.updateRegion(userId, region);
        log.info("用户地区更新成功: 用户ID={}, 地区={}", userId, region);
    }
    
    @Override
    public void updateIntroduction(int userId, String introduction) {
        log.info("开始更新用户个人介绍: 用户ID={}", userId);
        userMapper.updateIntroduction(userId, introduction);
        log.info("用户个人介绍更新成功: 用户ID={}", userId);
    }

    @Override
    public Long getMerchantUserId(Long merchantId) {
        log.info("开始获取商家对应的用户ID: merchantId={}", merchantId);
        
        if (merchantId == null) {
            log.warn("商家ID为空");
            return null;
        }
        
        try {
            // 查询商家信息
            org.example.afd.model.Merchant merchant = merchantMapper.selectByPrimaryKey(merchantId);
            
            if (merchant == null) {
                log.warn("未找到商家信息: merchantId={}", merchantId);
                return null;
            }
            
            Long userId = merchant.getUserId();
            log.info("获取商家用户ID成功: merchantId={}, userId={}", merchantId, userId);
            return userId;
            
        } catch (Exception e) {
            log.error("获取商家用户ID失败: merchantId={}", merchantId, e);
            return null;
        }
    }
}
