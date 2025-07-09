package org.example.afd.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据DTO
 */
@Data
public class StatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总销售额
     */
    private BigDecimal totalSales;

    /**
     * 今日销售额
     */
    private BigDecimal todaySales;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 今日订单数
     */
    private Integer todayOrders;

    /**
     * 总商品数
     */
    private Integer totalProducts;

    /**
     * 总用户数
     */
    private Integer totalCustomers;

    /**
     * 销售趋势（按日期统计的销售额）
     */
    private Map<String, BigDecimal> salesTrend;

    /**
     * 订单趋势（按日期统计的订单数）
     */
    private Map<String, Integer> orderTrend;

    /**
     * 商品销售排行
     */
    private List<ProductRank> productRanking;

    /**
     * 订单状态统计
     */
    private Map<String, Integer> orderStatusCounts;

    /**
     * 商品分类销售占比
     */
    private Map<String, BigDecimal> categorySalesPercentage;

    /**
     * 商品销售排行类
     */
    @Data
    public static class ProductRank implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品图片
         */
        private String productImage;

        /**
         * 销售数量
         */
        private Integer salesCount;

        /**
         * 销售金额
         */
        private BigDecimal salesAmount;
    }
} 