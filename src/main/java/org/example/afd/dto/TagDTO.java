package org.example.afd.dto;

import lombok.Data;

/**
 * 标签数据传输对象
 */
@Data
public class TagDTO {
    private Long tagId;       // 标签ID
    private String tagName;   // 标签名称
    private Integer count;    // 使用次数
    private Boolean isHot;    // 是否热门
} 