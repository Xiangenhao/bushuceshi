package org.example.afd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.afd.pojo.User;

/**
 * 认证响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;     // 访问令牌
    private String refreshToken;    // 刷新令牌
    private String tokenType;       // 令牌类型
    private Long expiresIn;         // 过期时间(秒)
    private User user;              // 用户信息
    
    /**
     * 创建认证响应
     */
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, User user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        
        // 清除敏感信息
        if (user != null) {
            user.setPassword(null);
            user.setSalt(null);
        }
        response.setUser(user);
        
        return response;
    }
} 