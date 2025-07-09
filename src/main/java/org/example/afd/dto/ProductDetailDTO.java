package org.example.afd.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情数据传输对象
 */
public class ProductDetailDTO implements Serializable {
    
    private Long productId;
    private Long merchantId;
    private Long categoryId;
    private String productName;
    private String productBrief;
    private String mainImage;
    private String detail;
    private String productExplain;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String unit;
    private String keywords;
    private List<String> images;
    private List<String> detailImages;
    private List<ProductSkuDTO> skus;
    private List<SpecDTO> specs;
    private MerchantDTO merchant;
    private BigDecimal promotionPrice;
    private Boolean hasPromotion;
    private Long promotionStartTime;
    private Long promotionEndTime;
    private Integer reviewCount;
    private Double reviewScore;
    private List<ReviewDTO> reviews;
    
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
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
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
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public List<String> getDetailImages() {
        return detailImages;
    }
    
    public void setDetailImages(List<String> detailImages) {
        this.detailImages = detailImages;
    }
    
    public List<ProductSkuDTO> getSkus() {
        return skus;
    }
    
    public void setSkus(List<ProductSkuDTO> skus) {
        this.skus = skus;
    }
    
    public List<SpecDTO> getSpecs() {
        return specs;
    }
    
    public void setSpecs(List<SpecDTO> specs) {
        this.specs = specs;
    }
    
    public MerchantDTO getMerchant() {
        return merchant;
    }
    
    public void setMerchant(MerchantDTO merchant) {
        this.merchant = merchant;
    }
    
    public BigDecimal getPromotionPrice() {
        return promotionPrice;
    }
    
    public void setPromotionPrice(BigDecimal promotionPrice) {
        this.promotionPrice = promotionPrice;
    }
    
    public Boolean getHasPromotion() {
        return hasPromotion;
    }
    
    public void setHasPromotion(Boolean hasPromotion) {
        this.hasPromotion = hasPromotion;
    }
    
    public Long getPromotionStartTime() {
        return promotionStartTime;
    }
    
    public void setPromotionStartTime(Long promotionStartTime) {
        this.promotionStartTime = promotionStartTime;
    }
    
    public Long getPromotionEndTime() {
        return promotionEndTime;
    }
    
    public void setPromotionEndTime(Long promotionEndTime) {
        this.promotionEndTime = promotionEndTime;
    }
    
    public Integer getReviewCount() {
        return reviewCount;
    }
    
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
    
    public Double getReviewScore() {
        return reviewScore;
    }
    
    public void setReviewScore(Double reviewScore) {
        this.reviewScore = reviewScore;
    }
    
    public List<ReviewDTO> getReviews() {
        return reviews;
    }
    
    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }
    
    /**
     * 获取显示价格
     * @return 如果有促销价则返回促销价，否则返回原价
     */
    public BigDecimal getDisplayPrice() {
        if (hasPromotion != null && hasPromotion && promotionPrice != null) {
            return promotionPrice;
        }
        return price;
    }
} 