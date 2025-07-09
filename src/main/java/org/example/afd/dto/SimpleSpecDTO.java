package org.example.afd.dto;

import java.io.Serializable;

/**
 * 简化商品规格DTO
 * 每个规格包含：名称、价格、库存、图片
 */
public class SimpleSpecDTO implements Serializable {
    
    private Long specId;
    private String specName;      // 规格名称
    private Double price;         // 规格价格
    private Integer stock;        // 规格库存
    private String imageUrl;      // 规格图片URL
    
    public SimpleSpecDTO() {}
    
    public SimpleSpecDTO(String specName, Double price, Integer stock) {
        this.specName = specName;
        this.price = price;
        this.stock = stock;
    }
    
    // Getters and Setters
    public Long getSpecId() {
        return specId;
    }
    
    public void setSpecId(Long specId) {
        this.specId = specId;
    }
    
    public String getSpecName() {
        return specName;
    }
    
    public void setSpecName(String specName) {
        this.specName = specName;
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
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    @Override
    public String toString() {
        return "SimpleSpecDTO{" +
                "specId=" + specId +
                ", specName='" + specName + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
} 