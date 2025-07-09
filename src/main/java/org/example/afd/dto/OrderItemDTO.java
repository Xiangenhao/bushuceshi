package org.example.afd.dto;

import java.io.Serializable;

/**
 * 订单项数据传输对象
 */
public class OrderItemDTO implements Serializable {
    
    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private String productName;
    private String productImage;
    private String skuProperties;
    private String skuName;
    private String skuImage;
    private Double price;
    private Double promotionPrice;
    private Integer quantity;
    private Double totalPrice;
    private Boolean hasReviewed;
    private ProductDTO product;
    
    // Getters and Setters
    public Long getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getSkuId() {
        return skuId;
    }
    
    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductImage() {
        return productImage;
    }
    
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
    
    public String getSkuProperties() {
        return skuProperties;
    }
    
    public void setSkuProperties(String skuProperties) {
        this.skuProperties = skuProperties;
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public Boolean getHasReviewed() {
        return hasReviewed;
    }
    
    public void setHasReviewed(Boolean hasReviewed) {
        this.hasReviewed = hasReviewed;
    }
    
    public ProductDTO getProduct() {
        return product;
    }
    
    public void setProduct(ProductDTO product) {
        this.product = product;
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