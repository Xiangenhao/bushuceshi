package org.example.afd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT工具类 - 用于从HttpServletRequest中获取用户信息
 * 这是对JwtUtils的补充，专门处理HTTP请求中的用户信息提取
 * 
 * @author AFD Team
 * @version 1.0
 */
@Slf4j
@Component
public class JwtUtil {
    
    /**
     * 从HttpServletRequest中获取当前用户ID（实例方法）
     * 
     * @param request HTTP请求对象
     * @return 用户ID，如果未找到返回null
     */
    public Integer getUserIdFromRequest(HttpServletRequest request) {
        return getCurrentUserId(request);
    }
    
    /**
     * 从HttpServletRequest中获取当前用户ID
     * 
     * @param request HTTP请求对象
     * @return 用户ID，如果未找到返回null
     */
    public static Integer getCurrentUserId(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest为空，无法获取用户ID");
            return null;
        }
        
        // 方法1：直接从请求属性中获取（由JwtAuthInterceptor设置）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Integer) {
            log.debug("从请求属性获取到用户ID: {}", userIdAttr);
            return (Integer) userIdAttr;
        }
        
        // 方法2：从请求头中获取用户ID
        String userIdHeader = request.getHeader("X-User-ID");
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            try {
                Integer userId = Integer.parseInt(userIdHeader.trim());
                log.debug("从X-User-ID请求头获取到用户ID: {}", userId);
                return userId;
            } catch (NumberFormatException e) {
                log.warn("X-User-ID请求头中的用户ID格式不正确: {}", userIdHeader);
            }
        }
        
        log.warn("无法从请求中获取用户ID，请求URI: {}", request.getRequestURI());
        return null;
    }
    
    /**
     * 从HttpServletRequest中获取当前用户名
     * 
     * @param request HTTP请求对象
     * @return 用户名，如果未找到返回null
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        // 从请求属性中获取（由JwtAuthInterceptor设置）
        Object usernameAttr = request.getAttribute("username");
        if (usernameAttr instanceof String) {
            return (String) usernameAttr;
        }
        
        log.debug("无法从请求中获取用户名，请求URI: {}", request.getRequestURI());
        return null;
    }
    
    /**
     * 从HttpServletRequest中获取当前用户角色
     * 
     * @param request HTTP请求对象
     * @return 用户角色，如果未找到返回null
     */
    public static String getCurrentUserRole(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        // 从请求属性中获取（由JwtAuthInterceptor设置）
        Object roleAttr = request.getAttribute("role");
        if (roleAttr instanceof String) {
            return (String) roleAttr;
        }
        
        log.debug("无法从请求中获取用户角色，请求URI: {}", request.getRequestURI());
        return null;
    }
    
    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param request HTTP请求对象
     * @param requiredRole 所需角色
     * @return 是否具有指定角色
     */
    public static boolean hasRole(HttpServletRequest request, String requiredRole) {
        String currentRole = getCurrentUserRole(request);
        return requiredRole != null && requiredRole.equals(currentRole);
    }
    
    /**
     * 检查当前用户是否为管理员
     * 
     * @param request HTTP请求对象
     * @return 是否为管理员
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, "ADMIN");
    }
} 