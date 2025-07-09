package org.example.afd.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 动态数据传输对象
 */
@Data
public class PostDTO {
    private Long postId;
    private Long userId;
    private String username;
    private String userAvatarUrl;
    private String avatar;
    private String content;
    private Integer visibilityType; // 0-公开，1-订阅可见，2-指定订阅可见
    private Long planId; // 当visibilityType=2时，指定的订阅计划ID
    private List<String> mediaUrls;
    private Integer mediaType; // 0-无媒体，1-图片，2-视频
    private List<TagDTO> tags;
    private Integer likeCount;
    private Integer commentCount;
    private Integer forwardCount;
    private Integer collectCount;
    private Date createTime;
    private Boolean isLiked; // 当前用户是否点赞
    private Boolean isCollected; // 当前用户是否收藏
    
    // 扩展字段，仅后端使用
    private Integer viewCount;
    private String location;
    private Boolean isFollowing;     // 当前用户是否关注作者
    private Integer followerCount;   // 作者粉丝数量
    private Integer postCount;       // 作者视频发布数量
    
    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
        this.avatar = userAvatarUrl;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
        this.userAvatarUrl = avatar;
    }
}
