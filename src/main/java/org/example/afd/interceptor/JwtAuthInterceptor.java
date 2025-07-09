package org.example.afd.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.utils.JwtUtils;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求信息
        log.debug("JWT认证开始处理请求: {}", request.getRequestURI());
        
        // 排除OPTIONS预检请求，这些请求不需要JWT认证
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("跳过OPTIONS预检请求: {}", request.getRequestURI());
            return true;
        }
        
        // 从请求头中获取Authorization
        String authHeader = request.getHeader("Authorization");
        log.debug("Authorization头: {}", authHeader);

        // 检查是否提供了Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("请求缺少有效的Authorization头: {}，请求方法: {}，请求头信息: {}", 
                    request.getRequestURI(), 
                    request.getMethod(),
                    request.getHeaderNames() != null ? 
                    java.util.Collections.list(request.getHeaderNames()).toString() : "无请求头");
            
            // 输出所有请求头用于调试
            if (request.getHeaderNames() != null) {
                java.util.Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    log.debug("请求头 {}: {}", headerName, headerValue);
                }
            }
            
            handleAuthError(response, "未提供有效的认证令牌", HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 提取token
        String token = authHeader.substring(7);
        log.debug("提取的令牌长度: {}, 前20字符: {}", 
                token.length(), 
                token.length() > 20 ? token.substring(0, 20) + "..." : token);

        try {
            // 验证token
            if (!jwtUtils.validateToken(token)) {
                log.warn("令牌无效或已过期: {}，令牌验证失败详情将由JwtUtils输出", request.getRequestURI());
                
                // 添加令牌过期检查的详细日志
                try {
                    boolean isExpired = jwtUtils.isTokenExpired(token);
                    log.warn("令牌过期状态: {}", isExpired ? "已过期" : "未过期但无效");
                    
                    if (!isExpired) {
                        // 如果未过期但验证失败，可能是格式或签名问题
                        log.warn("令牌未过期但验证失败，可能是格式或签名问题");
                    }
                } catch (Exception e) {
                    log.warn("检查令牌过期状态时出错", e);
                }
                
                handleAuthError(response, "认证令牌已过期或无效", HttpStatus.UNAUTHORIZED.value());
                return false;
            }

            // 优先从请求头获取用户ID
            Integer userId = null;
            String userIdHeader = request.getHeader("X-User-ID");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    userId = Integer.parseInt(userIdHeader);
                    log.debug("从请求头获取到用户ID: {}", userId);
                } catch (NumberFormatException e) {
                    log.warn("请求头中的用户ID格式不正确: {}", userIdHeader);
                }
            }
            
            // 如果请求头中没有有效的用户ID，则从token中获取
            if (userId == null) {
                userId = jwtUtils.getUserIdFromToken(token);
                log.debug("从JWT令牌获取到用户ID: {}", userId);
            }
            
            // 从token获取用户名和角色
            String username = jwtUtils.getUsernameFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);

            if (userId == null || username == null) {
                log.warn("令牌中缺少必要信息: {}，用户ID: {}, 用户名: {}", 
                        request.getRequestURI(), userId, username);
                handleAuthError(response, "认证令牌中缺少必要信息", HttpStatus.UNAUTHORIZED.value());
                return false;
            }

            // 设置到request属性中
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            
            // 直接调用UserIdHolder设置用户ID
            try {
                UserIdHolder.setUserId(userId);
                log.debug("已通过JwtAuthInterceptor直接设置UserIdHolder: {}", userId);
            } catch (Exception e) {
                log.error("直接设置UserIdHolder失败", e);
            }

            // 记录访问日志
            log.info("用户 {} (ID: {}, 角色: {}) 访问 {}", username, userId, role, request.getRequestURI());

            return true;
        } catch (Exception e) {
            log.error("认证拦截器异常，请求URI: {}，异常详情: ", request.getRequestURI(), e);
            handleAuthError(response, "认证处理过程发生错误", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return false;
        }
    }

    /**
     * 处理认证错误
     */
    private void handleAuthError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = String.format("{\"code\": %d, \"message\": \"%s\", \"data\": null}", 0, message);
        
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
        writer.close();
    }
} 