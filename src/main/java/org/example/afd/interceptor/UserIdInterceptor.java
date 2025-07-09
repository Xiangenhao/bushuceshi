package org.example.afd.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.utils.UserIdHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户ID拦截器，从请求属性中获取用户ID并设置到UserIdHolder中
 */
@Slf4j
@Component
public class UserIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 记录请求路径，帮助调试
        log.debug("处理请求: {}", request.getRequestURI());
        
        // 尝试从不同来源获取用户ID
        Integer userId = null;
        
        // 1. 尝试从请求属性中获取
        Object userIdObj = request.getAttribute("userId");
        log.debug("从请求属性获取用户ID: {}", userIdObj);
        
        if (userIdObj != null) {
            try {
                if (userIdObj instanceof Integer) {
                    userId = (Integer) userIdObj;
                } else if (userIdObj instanceof String) {
                    userId = Integer.parseInt(userIdObj.toString());
                }
                log.debug("从请求属性转换用户ID: {}", userId);
            } catch (Exception e) {
                log.error("解析请求属性中的用户ID失败", e);
            }
        }
        
        // 2. 如果请求属性中没有，尝试从请求头获取
        if (userId == null) {
            String userIdHeader = request.getHeader("X-User-ID");
            log.debug("从请求头获取用户ID: {}", userIdHeader);
            
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    userId = Integer.parseInt(userIdHeader);
                    log.debug("从请求头转换用户ID: {}", userId);
                } catch (Exception e) {
                    log.error("解析请求头中的用户ID失败", e);
                }
            }
        }
        
        // 如果获取到有效的用户ID，设置到UserIdHolder中
        if (userId != null && userId > 0) {
            UserIdHolder.setUserId(userId);
            log.info("已设置用户ID到UserIdHolder: {}", userId);
            return true;
        }
        
        // 检查是否是需要用户ID的接口
        String uri = request.getRequestURI();
        boolean isPublishEndpoint = uri.contains("/publish");
        
        if (isPublishEndpoint) {
            log.warn("访问需要用户ID的接口 {} 但未能获取到有效的用户ID", uri);
            // 不返回false，让请求继续到Controller，在Controller中进行权限检查
        } else {
            log.debug("访问接口 {} 无需用户ID或会在业务逻辑中处理", uri);
        }
        
        // 清除UserIdHolder，确保无残留
        UserIdHolder.clear();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除ThreadLocal，防止内存泄漏
        UserIdHolder.clear();
        log.debug("请求处理完成，已清除UserIdHolder");
    }
} 