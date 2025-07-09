package org.example.afd.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 商品规格数据传输对象
 */
public class SpecDTO implements Serializable {
    
    private String name; // 规格名称，如"颜色"、"尺寸"
    private List<String> options; // 规格选项，如["红色", "蓝色", "黑色"]
    private Integer sortOrder; // 排序顺序
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
} 