package org.example.afd.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态实体类
 */
@Data
public class Dynamic {
    private Long dynamicId;       // 动态ID
    private Integer userId;       // 发布用户ID
    private String content;       // 动态文字内容
    private String location;      // 位置信息
    private Boolean isForward;    // 是否为转发 0-原创 1-转发
    private Long originalId;      // 原动态ID（如果是转发）
    private Integer forwardCount; // 转发数
    private Integer likeCount;    // 点赞数
    private Integer commentCount; // 评论数
    private Integer viewCount;    // 浏览数
    private Integer status;       // 状态 0-正常 1-已删除
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
