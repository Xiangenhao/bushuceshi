package org.example.afd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String account;   // 账号(用户名/手机号/邮箱)
    private String password;  // 密码
    private String clientIp;   // 登录IP
    private String userAgent; // 设备信息
    private Boolean rememberMe;  // 记住我（延长token有效期）
    private String loginIp;   // 登录IP（与clientIp保持一致）
    private String deviceInfo; // 设备信息（与userAgent保持一致）
    
    /**
     * 获取用户名（兼容性方法，返回account字段值）
     */
    public String getUsername() {
        return account;
    }
} 