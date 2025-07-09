package org.example.afd.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品SKU数据传输对象
 */
public class ProductSkuDTO implements Serializable {
    
    private Long skuId;
    private Long productId;
    private String skuCode;
    private String skuName;
    private String skuImage;
    private BigDecimal price;
    private Integer stock;
    private Map<String, String> specs;
    private Integer status;
    private BigDecimal promotionPrice;
    private Boolean hasPromotion;
    
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

    public String getSkuImage() {
        return skuImage;
    }

    public void setSkuImage(String skuImage) {
        this.skuImage = skuImage;
    }

    /**
     * 获取BigDecimal类型的价格
     */
    public BigDecimal getPriceBigDecimal() {
        return price;
    }
    
    public Double getPrice() {
        return price != null ? price.doubleValue() : null;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    /**
     * 设置价格，接收double类型并转换为BigDecimal
     * @param price 价格(double类型)
     */
    public void setPrice(Double price) {
        if (price != null) {
            this.price = BigDecimal.valueOf(price);
        } else {
            this.price = null;
        }
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Map<String, String> getSpecs() {
        return specs;
    }

    public void setSpecs(Map<String, String> specs) {
        this.specs = specs;
    }
    
    public Map<String, String> getAttributes() {
        return specs;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取BigDecimal类型的促销价格
     */
    public BigDecimal getPromotionPriceBigDecimal() {
        return promotionPrice;
    }
    
    public Double getPromotionPrice() {
        return promotionPrice != null ? promotionPrice.doubleValue() : null;
    }

    public void setPromotionPrice(BigDecimal promotionPrice) {
        this.promotionPrice = promotionPrice;
    }
    
    /**
     * 设置促销价格，接收double类型并转换为BigDecimal
     * @param promotionPrice 促销价格(double类型)
     */
    public void setPromotionPrice(Double promotionPrice) {
        if (promotionPrice != null) {
            this.promotionPrice = BigDecimal.valueOf(promotionPrice);
        } else {
            this.promotionPrice = null;
        }
    }

    public Boolean getHasPromotion() {
        return hasPromotion;
    }

    public void setHasPromotion(Boolean hasPromotion) {
        this.hasPromotion = hasPromotion;
    }
    
    /**
     * 获取展示价格，如果有促销价则返回促销价，否则返回原价
     */
    public BigDecimal getDisplayPrice() {
        if (hasPromotion != null && hasPromotion && promotionPrice != null) {
            return promotionPrice;
        }
        return price;
    }
} 