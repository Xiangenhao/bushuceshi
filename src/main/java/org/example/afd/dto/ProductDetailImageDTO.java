package org.example.afd.dto;

import java.io.Serializable;

/**
 * 商品详情图片DTO（后端）
 */
public class ProductDetailImageDTO implements Serializable {
    
    private Long imageId;
    private Long productId;
    private String imageUrl;
    private Integer sortOrder;
    private Boolean isThumbnail;
    private Integer imageType; // 1=主图, 2=详情图, 3=规格图
    private String description;
    private Integer width;
    private Integer height;
    
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Boolean getIsThumbnail() {
        return isThumbnail;
    }
    
    public void setIsThumbnail(Boolean isThumbnail) {
        this.isThumbnail = isThumbnail;
    }
    
    public Integer getImageType() {
        return imageType;
    }
    
    public void setImageType(Integer imageType) {
        this.imageType = imageType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public void setWidth(Integer width) {
        this.width = width;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
} 