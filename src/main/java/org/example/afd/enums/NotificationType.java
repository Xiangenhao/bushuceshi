package org.example.afd.enums;

/**
 * 通知类型枚举
 * 定义了系统中所有用户间互动的通知类型
 */
public enum NotificationType {
    
    /**
     * 点赞了你的动态
     */
    LIKE_POST("赞了你的动态"),

    /**
     * 评论了你的动态
     */
    COMMENT_POST("评论了你的动态"),

    /**
     * 回复了你的评论
     */
    REPLY_COMMENT("回复了你的评论"),
    
    /**
     * 点赞了你的评论
     */
    LIKE_COMMENT("赞了你的评论"),

    /**
     * 回复了你的回复
     */
    REPLY_REPLY("回复了你的评论"),
    
    /**
     * 关注了你
     */
    FOLLOW_USER("关注了你"),

    /**
     * 订阅了你的计划
     */
    SUBSCRIBE_PLAN("订阅了你的计划");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 