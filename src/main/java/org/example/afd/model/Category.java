package org.example.afd.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 分类排序号
     */
    private Integer sortOrder = 0;

    /**
     * 分类级别（1:一级分类, 2:二级分类, 3:三级分类）
     */
    private Integer level = 1;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 所属商家ID（为null时表示平台分类）
     */
    private Long merchantId;

    /**
     * 是否显示（0:不显示, 1:显示）
     */
    private Integer isShow = 1;

    /**
     * 是否导航（0:不是, 1:是）
     */
    private Integer isNav = 0;

    /**
     * 分类状态（0:禁用, 1:启用）
     */
    private Integer status = 1;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 