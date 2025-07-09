package org.example.afd.dto;

import java.io.Serializable;

/**
 * 购物车项数据传输对象
 */
public class ShoppingCartItemDTO implements Serializable {
    
    private Long cartItemId;
    private Long userId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Boolean selected;
    private ProductDTO product;
    private SkuDTO sku;
    
    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }
    
    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Boolean getSelected() {
        return selected != null && selected;
    }
    
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    
    public ProductDTO getProduct() {
        return product;
    }
    
    public void setProduct(ProductDTO product) {
        this.product = product;
    }
    
    public SkuDTO getSku() {
        return sku;
    }
    
    public void setSku(SkuDTO sku) {
        this.sku = sku;
    }
    
    /**
     * 获取商品单价
     * @return 商品单价
     */
    public Double getUnitPrice() {
        if (sku != null) {
            return sku.getDisplayPrice();
        } else if (product != null) {
            return product.getDisplayPrice();
        }
        return 0.0;
    }
    
    /**
     * 获取商品总价
     * @return 商品总价
     */
    public Double getTotalPrice() {
        return getUnitPrice() * quantity;
    }
} 