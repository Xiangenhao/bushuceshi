package org.example.afd.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动态图片实体类
 */
@Data
public class DynamicImage {
    private Long imageId;      // 图片ID
    private Long dynamicId;    // 关联的动态ID
    private String imageUrl;   // 图片URL
    private Integer imageOrder; // 图片顺序
    private Integer width;     // 图片宽度
    private Integer height;    // 图片高度
    private Integer size;      // 图片大小(KB)
    private LocalDateTime createdAt; // 创建时间
} 