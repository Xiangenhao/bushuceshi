package org.example.afd.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车数据传输对象
 */
public class ShoppingCartDTO implements Serializable {
    
    private Long userId;
    private List<ShoppingCartItemDTO> items;
    private Integer totalQuantity;
    private Double totalPrice;
    private Double totalPromotionPrice;
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public List<ShoppingCartItemDTO> getItems() {
        return items;
    }
    
    public void setItems(List<ShoppingCartItemDTO> items) {
        this.items = items;
        calculateTotal();
    }
    
    public Integer getTotalQuantity() {
        return totalQuantity;
    }
    
    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public Double getTotalPromotionPrice() {
        return totalPromotionPrice;
    }
    
    public void setTotalPromotionPrice(Double totalPromotionPrice) {
        this.totalPromotionPrice = totalPromotionPrice;
    }
    
    /**
     * 计算购物车总价、总数量等信息
     */
    private void calculateTotal() {
        if (items == null || items.isEmpty()) {
            totalQuantity = 0;
            totalPrice = 0.0;
            totalPromotionPrice = 0.0;
            return;
        }
        
        totalQuantity = 0;
        totalPrice = 0.0;
        totalPromotionPrice = 0.0;
        
        for (ShoppingCartItemDTO item : items) {
            if (item.getSelected()) {
                totalQuantity += item.getQuantity();
                
                if (item.getProduct() != null) {
                    Double price = item.getProduct().getPrice();
                    Double promotionPrice = item.getProduct().getPromotionPrice();
                    
                    totalPrice += price * item.getQuantity();
                    if (item.getProduct().getHasPromotion() && promotionPrice != null) {
                        totalPromotionPrice += promotionPrice * item.getQuantity();
                    } else {
                        totalPromotionPrice += price * item.getQuantity();
                    }
                }
            }
        }
    }
    
    /**
     * 获取已选中的商品数量
     * @return 已选中的商品数量
     */
    public Integer getSelectedCount() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (ShoppingCartItemDTO item : items) {
            if (item.getSelected()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取已选中的商品总数量
     * @return 已选中的商品总数量
     */
    public Integer getSelectedTotalQuantity() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        
        int quantity = 0;
        for (ShoppingCartItemDTO item : items) {
            if (item.getSelected()) {
                quantity += item.getQuantity();
            }
        }
        return quantity;
    }
} 