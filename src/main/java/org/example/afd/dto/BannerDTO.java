package org.example.afd.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 轮播图数据传输对象
 */
public class BannerDTO implements Serializable {
    
    private Long bannerId;
    private String title;
    private String imageUrl;
    private Integer linkType;  // 1-商品，2-分类，3-商家，4-活动，5-外部链接
    private Long targetId;
    private String linkUrl;
    private String position;
    private Integer sortOrder;
    private Long startTime;
    private Long endTime;
    private Integer status;
    private String remark;     // 备注
    private Date createTime;   // 创建时间
    private Date updateTime;   // 更新时间
    
    // Getters and Setters
    public Long getBannerId() {
        return bannerId;
    }
    
    public void setBannerId(Long bannerId) {
        this.bannerId = bannerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getLinkType() {
        return linkType;
    }
    
    public void setLinkType(Integer linkType) {
        this.linkType = linkType;
    }
    
    public Long getTargetId() {
        return targetId;
    }
    
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    
    public Long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
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
} 