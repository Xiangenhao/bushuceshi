package org.example.afd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 */
@Slf4j
public class SecurityUtils {
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID，如果未登录返回null
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("获取当前用户ID失败: 用户未认证");
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            try {
                // 假设用户名是用户ID
                return Integer.parseInt(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                log.warn("获取当前用户ID失败: 用户名不是数字", e);
                return null;
            }
        } else if (principal instanceof String) {
            try {
                // 假设用户名是用户ID
                return Integer.parseInt((String) principal);
            } catch (NumberFormatException e) {
                log.warn("获取当前用户ID失败: 用户名不是数字", e);
                return null;
            }
        }
        
        log.warn("获取当前用户ID失败: 未知的Principal类型: {}", principal.getClass());
        return null;
    }
    
    /**
     * 检查当前用户是否具有指定权限
     * @param permission 权限标识
     * @return 是否具有权限
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }
} 