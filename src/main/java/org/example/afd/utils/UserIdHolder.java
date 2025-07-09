package org.example.afd.utils;

/**
 * 用户ID持有者，用于获取当前登录用户的ID
 */
public class UserIdHolder {
    
    private static final ThreadLocal<Integer> userIdThreadLocal = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     */
    public static void setUserId(Integer userId) {
        userIdThreadLocal.set(userId);
    }
    
    /**
     * 获取当前用户ID
     */
    public static Integer getUserId() {
        return userIdThreadLocal.get();
    }
    
    /**
     * 清除当前用户ID
     */
    public static void clear() {
        userIdThreadLocal.remove();
    }
} 