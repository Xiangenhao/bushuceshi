package org.example.afd.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 */
@Data
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品副标题
     */
    private String subtitle;

    /**
     * 商品主图
     */
    private String mainImage;

    /**
     * 商品图集（JSON数组字符串）
     */
    private String subImages;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 商品状态（0:下架, 1:上架）
     */
    private Integer status;

    /**
     * 销量
     */
    private Integer sales = 0;

    /**
     * 单位
     */
    private String unit;

    /**
     * 商品重量（克）
     */
    private Integer weight;

    /**
     * 是否新品（0:否, 1:是）
     */
    private Integer isNew = 0;

    /**
     * 是否热销（0:否, 1:是）
     */
    private Integer isHot = 0;

    /**
     * 是否推荐（0:否, 1:是）
     */
    private Integer isRecommend = 0;

    /**
     * 排序号
     */
    private Integer sortOrder = 0;

    /**
     * 删除状态（0:未删除, 1:已删除）
     */
    private Integer deleteStatus = 0;

    /**
     * 平均评分
     */
    private BigDecimal averageRating;

    /**
     * 评价数量
     */
    private Integer reviewCount = 0;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 规格列表（非数据库字段）
     */
    private transient List<Spec> specList;

    /**
     * SKU列表（非数据库字段）
     */
    private transient List<Sku> skuList;
}

/**
 * 商品规格实体类
 */
@Data
class Spec implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 规格名称
     */
    private String name;

    /**
     * 规格值（JSON数组字符串）
     */
    private String values;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

/**
 * 商品SKU实体类
 */
@Data
class Sku implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 规格组合（JSON对象字符串）
     */
    private String specs;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 图片
     */
    private String image;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 