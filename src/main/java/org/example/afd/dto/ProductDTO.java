package org.example.afd.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 商品数据传输对象
 */
public class ProductDTO implements Serializable {
    
    private Long productId;
    private Long merchantId;
    private Long categoryId;
    private Long brandId;
    private String productName;
    private String productBrief;
    private String mainImage;
    private String subImages;
    private String description;
    private String detail;          // 商品详情（与数据库字段对应）
    private String productExplain;  // 商家说明字段（与数据库字段product_explain对应）
    private Double price;
    private Integer stock;
    private Integer sales;
    private String unit;
    private Boolean isHot;
    private Boolean isRecommend;
    private Boolean isNew;
    private Integer status;
    private Double promotionPrice;
    private String merchantName;
    private String categoryName;
    private Boolean hasPromotion;
    private Integer productType; // 1-实物商品，2-虚拟商品
    private String keywords; // 商品关键词
    
    // 简化商品规格列表（新增字段，用于简化规格系统）
    private List<SimpleSpecDTO> simpleSpecs;
    
    // 商品图片列表（轮播图）
    private List<ProductImageDTO> productImages;
    
    // 详情图片列表
    private List<ProductDetailImageDTO> detailImages;
    
    // Getters and Setters
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getMerchantId() {
        return merchantId;
    }
    
    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getBrandId() {
        return brandId;
    }
    
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductBrief() {
        return productBrief;
    }
    
    public void setProductBrief(String productBrief) {
        this.productBrief = productBrief;
    }
    
    public String getMainImage() {
        return mainImage;
    }
    
    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }
    
    public String getSubImages() {
        return subImages;
    }
    
    public void setSubImages(String subImages) {
        this.subImages = subImages;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
    
    public String getProductExplain() {
        return productExplain;
    }
    
    public void setProductExplain(String productExplain) {
        this.productExplain = productExplain;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Integer getSales() {
        return sales;
    }
    
    public void setSales(Integer sales) {
        this.sales = sales;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public Boolean getIsHot() {
        return isHot;
    }
    
    public void setIsHot(Boolean isHot) {
        this.isHot = isHot;
    }
    
    public Boolean getIsRecommend() {
        return isRecommend;
    }
    
    public void setIsRecommend(Boolean isRecommend) {
        this.isRecommend = isRecommend;
    }
    
    public Boolean getIsNew() {
        return isNew;
    }
    
    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Double getPromotionPrice() {
        return promotionPrice;
    }
    
    public void setPromotionPrice(Double promotionPrice) {
        this.promotionPrice = promotionPrice;
    }
    
    public String getMerchantName() {
        return merchantName;
    }
    
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Boolean getHasPromotion() {
        return hasPromotion;
    }
    
    public void setHasPromotion(Boolean hasPromotion) {
        this.hasPromotion = hasPromotion;
    }
    
    public Integer getProductType() {
        return productType;
    }
    
    public void setProductType(Integer productType) {
        this.productType = productType;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public List<SimpleSpecDTO> getSimpleSpecs() {
        return simpleSpecs;
    }
    
    public void setSimpleSpecs(List<SimpleSpecDTO> simpleSpecs) {
        this.simpleSpecs = simpleSpecs;
    }
    
    public List<ProductImageDTO> getProductImages() {
        return productImages;
    }
    
    public void setProductImages(List<ProductImageDTO> productImages) {
        this.productImages = productImages;
    }
    
    public List<ProductDetailImageDTO> getDetailImages() {
        return detailImages;
    }
    
    public void setDetailImages(List<ProductDetailImageDTO> detailImages) {
        this.detailImages = detailImages;
    }
    
    /**
     * 获取显示价格
     * @return 如果有促销价则返回促销价，否则返回原价
     */
    public Double getDisplayPrice() {
        if (hasPromotion != null && hasPromotion && promotionPrice != null) {
            return promotionPrice;
        }
        return price;
    }
} 