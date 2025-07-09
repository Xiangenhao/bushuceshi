package org.example.afd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户令牌实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToken {
    private Integer id;
    private Integer userId;
    private String refreshToken;
    private String clientIp;
    private String userAgent;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Integer revoked;
} 