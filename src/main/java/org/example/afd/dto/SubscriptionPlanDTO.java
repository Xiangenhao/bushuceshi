package org.example.afd.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订阅计划数据传输对象
 */
@Data
@Slf4j
public class SubscriptionPlanDTO {
    private Long planId;                  // 计划ID
    private Long creatorId;               // 创作者ID
    private String title;                 // 订阅计划标题
    private String description;           // 订阅计划详细描述
    private String coverUrl;              // 封面图片URL
    private BigDecimal monthlyPrice;      // 每30天的订阅价格
    private Integer subscriberCount;      // 订阅人数
    private Date createTime;              // 创建时间
    private Date updateTime;              // 更新时间
    private Integer status;               // 状态，1-正常，0-下架，2-审核中
    private String tag;                   // 标签
    
    // 权益相关字段
    private List<String> benefits;        // 用户权益列表（前端使用）
    @JsonIgnore
    private String benefitsJson;          // 权益JSON字符串（数据库存储）
    
    // 额外信息
    private String creatorName;           // 创作者名称
    private String creatorAvatar;         // 创作者头像
    private Boolean isSubscribed;         // 当前用户是否已订阅
    private Date subscribeExpireTime;     // 当前用户订阅到期时间
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取权益列表，从JSON字符串转换
     */
    public List<String> getBenefits() {
        if (benefits != null) {
            return benefits;
        }
        
        if (benefitsJson != null && !benefitsJson.trim().isEmpty()) {
            try {
                benefits = objectMapper.readValue(benefitsJson, new TypeReference<List<String>>() {});
                return benefits;
            } catch (JsonProcessingException e) {
                log.warn("解析权益JSON失败: {}", benefitsJson, e);
                benefits = new ArrayList<>();
            }
        } else {
            benefits = new ArrayList<>();
        }
        
        return benefits;
    }
    
    /**
     * 设置权益列表，同时更新JSON字符串
     */
    public void setBenefits(List<String> benefits) {
        this.benefits = benefits;
        
        if (benefits != null && !benefits.isEmpty()) {
            try {
                this.benefitsJson = objectMapper.writeValueAsString(benefits);
            } catch (JsonProcessingException e) {
                log.error("转换权益列表为JSON失败", e);
                this.benefitsJson = "[]";
            }
        } else {
            this.benefitsJson = "[]";
        }
    }
    
    /**
     * 获取权益JSON字符串（用于数据库存储）
     */
    @JsonIgnore
    public String getBenefitsJson() {
        if (benefitsJson != null) {
            return benefitsJson;
        }
        
        if (benefits != null && !benefits.isEmpty()) {
            try {
                benefitsJson = objectMapper.writeValueAsString(benefits);
            } catch (JsonProcessingException e) {
                log.error("转换权益列表为JSON失败", e);
                benefitsJson = "[]";
            }
        } else {
            benefitsJson = "[]";
        }
        
        return benefitsJson;
    }
    
    /**
     * 设置权益JSON字符串（从数据库读取时使用）
     */
    @JsonIgnore
    public void setBenefitsJson(String benefitsJson) {
        this.benefitsJson = benefitsJson;
        // 清空缓存的List，强制下次getBenefits时重新解析
        this.benefits = null;
    }
} 