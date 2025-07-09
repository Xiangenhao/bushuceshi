package org.example.afd.dto;

import java.io.Serializable;

/**
 * 满减规则数据传输对象
 */
public class FullReductionRuleDTO implements Serializable {
    
    private Long ruleId;
    private Long reductionId;
    private Double fullAmount; // 满足金额
    private Double reductionAmount; // 减免金额
    
    // Getters and Setters
    public Long getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
    
    public Long getReductionId() {
        return reductionId;
    }
    
    public void setReductionId(Long reductionId) {
        this.reductionId = reductionId;
    }
    
    public Double getFullAmount() {
        return fullAmount;
    }
    
    public void setFullAmount(Double fullAmount) {
        this.fullAmount = fullAmount;
    }
    
    public Double getReductionAmount() {
        return reductionAmount;
    }
    
    public void setReductionAmount(Double reductionAmount) {
        this.reductionAmount = reductionAmount;
    }
} 