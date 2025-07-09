package org.example.afd.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 商品SKU数据传输对象
 */
public class SkuDTO implements Serializable {
    
    private Long skuId;
    private Long productId;
    private String skuCode;
    private String skuName; // SKU名称
    private Double price;
    private Double promotionPrice;
    private Integer stock;
    private Map<String, String> attributes; // 规格属性，例如 {"颜色": "红色", "尺寸": "XL"}
    private String mainImage;
    private Integer status; // 0-禁用，1-启用
    private String properties; // 规格属性字符串，例如 "颜色:红色;尺寸:XL"
    private Boolean hasPromotion; // 是否有促销
    private String image; // SKU图片
    
    // Getters and Setters
    public Long getSkuId() {
        return skuId;
    }
    
    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getSkuCode() {
        return skuCode;
    }
    
    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }
    
    public String getSkuName() {
        return skuName;
    }
    
    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Double getPromotionPrice() {
        return promotionPrice;
    }
    
    public void setPromotionPrice(Double promotionPrice) {
        this.promotionPrice = promotionPrice;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    public String getMainImage() {
        return mainImage;
    }
    
    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getProperties() {
        return properties;
    }
    
    public void setProperties(String properties) {
        this.properties = properties;
    }
    
    public Boolean getHasPromotion() {
        return hasPromotion;
    }
    
    public void setHasPromotion(Boolean hasPromotion) {
        this.hasPromotion = hasPromotion;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    /**
     * 获取SKU图片，优先返回image字段，其次是mainImage
     * @return SKU图片URL
     */
    public String getSkuImage() {
        if (image != null && !image.isEmpty()) {
            return image;
        }
        return mainImage;
    }
    
    /**
     * 获取显示价格
     * @return 如果有促销价则返回促销价，否则返回原价
     */
    public Double getDisplayPrice() {
        if (promotionPrice != null && promotionPrice > 0) {
            return promotionPrice;
        }
        return price;
    }
} 