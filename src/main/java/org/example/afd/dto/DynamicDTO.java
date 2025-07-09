package org.example.afd.dto;

import lombok.Data;
import org.example.afd.pojo.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态数据传输对象
 */
@Data
public class DynamicDTO {
    private Long dynamicId;       // 动态ID
    private Integer userId;       // 发布用户ID
    private User user;            // 发布用户信息
    private String content;       // 动态文字内容
    private String location;      // 位置信息
    private Boolean isForward;    // 是否为转发 0-原创 1-转发
    private Long originalId;      // 原动态ID（如果是转发）
    private DynamicDTO originalDynamic; // 原动态信息（如果是转发）
    private Integer forwardCount; // 转发数
    private Integer likeCount;    // 点赞数
    private Integer commentCount; // 评论数
    private Integer viewCount;    // 浏览数
    private Boolean isLiked;      // 当前用户是否点赞
    private List<String> imageUrls; // 图片URL列表
    private LocalDateTime createdAt; // 创建时间
    private String timeAgo;       // 多久前发布（1分钟前、2小时前等）
}
