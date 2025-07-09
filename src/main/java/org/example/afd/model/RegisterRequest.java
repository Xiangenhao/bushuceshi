package org.example.afd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;      // 用户名
    private String password;      // 密码
    private String phoneNumber;   // 手机号
    private String email;         // 邮箱
    private String verificationCode; // 验证码
    private String userAgent;     // 设备信息
    private String clientIp;      // 注册IP
    private String role;          // 用户角色（USER/ADMIN）
} 