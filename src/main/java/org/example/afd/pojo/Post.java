package org.example.afd.pojo;

import java.util.Date;

/**
 * 动态实体类，对应afd.post_content表
 */
public class Post {
    private Long postId;           // 内容ID
    private Integer userId;        // 发布用户ID
    private String content;        // 内容文字
    private Integer visibilityType; // 可见性类型，0-公开，1-订阅可见，2-指定订阅可见
    private String location;       // 发布位置
    private Integer likeCount;     // 点赞数
    private Integer commentCount;  // 评论数
    private Integer collectCount;  // 收藏数
    private Integer forwardCount;  // 转发数
    private Integer viewCount;     // 查看次数
    private Integer mediaType;     // 媒体类型，0-无媒体，1-图片，2-视频
    private Boolean isTop;         // 是否置顶，0-否，1-是
    private Date createTime;       // 发布时间
    private Date updateTime;       // 更新时间
    private Integer status;        // 状态，1-正常，0-删除，2-审核中
    
    // 非数据库字段，用于显示
    private String username;       // 用户名
    private String userAvatarUrl;  // 用户头像URL
    
    public Post() {
        // 初始化统计数据
        this.likeCount = 0;
        this.commentCount = 0;
        this.collectCount = 0;
        this.forwardCount = 0;
        this.viewCount = 0;
        this.isTop = false;
        this.status = 1;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    public Long getPostId() {
        return postId;
    }
    
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getVisibilityType() {
        return visibilityType;
    }
    
    public void setVisibilityType(Integer visibilityType) {
        this.visibilityType = visibilityType;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
    
    public Integer getCollectCount() {
        return collectCount;
    }
    
    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }
    
    public Integer getForwardCount() {
        return forwardCount;
    }
    
    public void setForwardCount(Integer forwardCount) {
        this.forwardCount = forwardCount;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(Integer mediaType) {
        this.mediaType = mediaType;
    }
    
    public Boolean getIsTop() {
        return isTop;
    }
    
    public void setIsTop(Boolean isTop) {
        this.isTop = isTop;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }
    
    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
}
