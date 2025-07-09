package org.example.afd.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.afd.model.*;
import org.example.afd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Result<Object>> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("用户注册请求: {}", request);
            // 设置客户端信息
            request.setClientIp(getClientIp(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            
            // 调用服务注册
            userService.register(request);
            return ResponseEntity.ok(Result.success("注册成功"));
        } catch (Exception e) {
            log.error("注册失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("用户登录请求: {}", request);
            // 设置客户端信息
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            request.setClientIp(clientIp);
            request.setUserAgent(userAgent);
            request.setLoginIp(clientIp);
            request.setDeviceInfo(userAgent);
            
            // 调用服务登录
            AuthResponse authResponse = userService.login(request);
            System.out.println("--------------------准备返回登录成功");
            return ResponseEntity.ok(Result.success("登录成功", authResponse));
        } catch (Exception e) {
            log.error("登录失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 刷新认证令牌
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Result<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("刷新令牌请求: {}", request);
            // 设置客户端信息
            request.setClientIp(getClientIp(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            
            // 调用服务刷新令牌
            AuthResponse authResponse = userService.refreshToken(request);
            return ResponseEntity.ok(Result.success("令牌刷新成功", authResponse));
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Object>> logout(@RequestBody RefreshTokenRequest request) {
        try {
            log.info("用户登出请求: {}", request);
            // 调用服务登出
            userService.logout(request.getRefreshToken());
            return ResponseEntity.ok(Result.success("登出成功"));
        } catch (Exception e) {
            log.error("登出失败", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 