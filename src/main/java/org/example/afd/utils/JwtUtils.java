package org.example.afd.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.afd.pojo.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:wohaiyoumeiyoushixiandemengxiang123456789012345678901234567890abcdefghijklmnopqrstuvwxyz}")
    private String secretKey; // 密钥

    @Value("${jwt.expiration:43200}")
    private Long expiration; // 令牌过期时间(秒)，默认12小时

    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration; // 刷新令牌过期时间(秒)，默认7天

    /**
     * 生成签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从令牌中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return Integer.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            log.warn("从令牌中获取用户ID失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("从令牌中获取用户名失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.warn("从令牌中获取用户角色失败", e);
            return null;
        }
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.warn("从令牌中获取过期时间失败", e);
            return null;
        }
    }
    
    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.warn("检查令牌是否过期失败", e);
            return true;
        }
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            log.debug("开始验证令牌，长度: {}", token != null ? token.length() : 0);
            
            if (token == null || token.trim().isEmpty()) {
                log.warn("令牌验证失败: 令牌为空");
                return false;
            }
            
            // 解析令牌，如果解析成功且未过期则有效
            Claims claims = getClaimsFromToken(token);
            log.debug("令牌解析成功，用户: {}, 用户ID: {}, 过期时间: {}", 
                    claims.getSubject(), 
                    claims.get("userId"), 
                    claims.getExpiration());
            
            boolean isExpired = isTokenExpired(token);
            log.debug("令牌过期检查结果: {}", isExpired ? "已过期" : "有效");
            
            boolean isValid = !isExpired;
            log.debug("令牌验证最终结果: {}", isValid ? "有效" : "无效");
            
            return isValid;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("令牌验证失败: 令牌已过期，过期时间: {}", e.getClaims().getExpiration());
            return false;
        } catch (io.jsonwebtoken.SignatureException e) {
            log.warn("令牌验证失败: 签名无效");
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("令牌验证失败: 令牌格式错误");
            return false;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("令牌验证失败: 不支持的令牌类型");
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("令牌验证失败: 非法参数");
            return false;
        } catch (Exception e) {
            log.warn("令牌验证失败: 未知错误", e);
            return false;
        }
    }

    /**
     * 生成用户令牌
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole() != null ? user.getRole() : "USER");
        return doGenerateToken(claims, user.getUsername());
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("type", "refresh");
        claims.put("random", UUID.randomUUID().toString()); // 添加随机值确保每次生成的令牌不同
        return doGenerateRefreshToken(claims, user.getUsername());
    }

    /**
     * 生成令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    private String doGenerateRefreshToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

//    private void checkUserStatus(User user, LoginHistory loginHistory) {
//        // 账号被禁用
//        if (user.getStatus() != null && user.getStatus() == 1) {
//            loginHistory.setLoginStatus(0);
//            loginHistory.setLoginMessage("账号已被禁用");
//            userMapper.saveLoginHistory(loginHistory);
//            throw new RuntimeException("账号已被禁用，请联系管理员");
//        }
//    }
//
//    private void verifyPassword(String password, User user, LoginHistory loginHistory) {
//        if (!passwordUtils.matches(password, user.getPassword(), user.getSalt())) {
//            loginHistory.setLoginStatus(0);
//            loginHistory.setLoginMessage("密码错误");
//            userMapper.saveLoginHistory(loginHistory);
//            throw new RuntimeException("用户名或密码错误");
//        }
//    }
//
//    private void handleLoginError(Exception e, User user, LoginHistory loginHistory) {
//        log.error("登录过程发生错误", e);
//        if (user != null) {
//            loginHistory.setUserId(user.getUserId());
//            loginHistory.setLoginStatus(0);
//            loginHistory.setLoginMessage("系统错误: " + e.getMessage());
//            userMapper.saveLoginHistory(loginHistory);
//        }
//    }
}

