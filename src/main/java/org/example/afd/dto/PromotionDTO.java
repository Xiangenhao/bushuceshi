package org.example.afd.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 促销活动数据传输对象
 */
public class PromotionDTO implements Serializable {
    
    private Long promotionId;
    private String promotionType; // DISCOUNT: 折扣, FULL_REDUCTION: 满减, GROUP_BUY: 拼团
    private String promotionName;
    private String description;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private String useRange; // ALL: 全场通用, CATEGORY: 指定分类, PRODUCT: 指定商品
    
    // 折扣活动特有属性
    private Integer discountType; // 1-直接打折，2-指定价格
    private Double discountValue; // 折扣率或指定价格
    
    // 满减活动特有属性
    private List<FullReductionRuleDTO> rules; // 满减规则列表
    
    // 拼团活动特有属性
    private Double groupPrice; // 拼团价格
    private Double originalPrice; // 原价
    private Integer groupSize; // 拼团人数
    private Integer groupDuration; // 拼团有效期(小时)
    private Integer limitPerUser; // 每人限购数量
    
    // 关联商品信息
    private List<ProductDTO> products;
    
    // Getters and Setters
    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionType() {
        return promotionType;
    }

    public void setPromotionType(String promotionType) {
        this.promotionType = promotionType;
    }

    public String getPromotionName() {
        return promotionName;
    }

    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUseRange() {
        return useRange;
    }

    public void setUseRange(String useRange) {
        this.useRange = useRange;
    }

    public Integer getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Integer discountType) {
        this.discountType = discountType;
    }

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public List<FullReductionRuleDTO> getRules() {
        return rules;
    }

    public void setRules(List<FullReductionRuleDTO> rules) {
        this.rules = rules;
    }

    public Double getGroupPrice() {
        return groupPrice;
    }

    public void setGroupPrice(Double groupPrice) {
        this.groupPrice = groupPrice;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(Integer groupSize) {
        this.groupSize = groupSize;
    }

    public Integer getGroupDuration() {
        return groupDuration;
    }

    public void setGroupDuration(Integer groupDuration) {
        this.groupDuration = groupDuration;
    }

    public Integer getLimitPerUser() {
        return limitPerUser;
    }

    public void setLimitPerUser(Integer limitPerUser) {
        this.limitPerUser = limitPerUser;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }
} 