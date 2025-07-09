package org.example.afd.service;

import org.example.afd.dto.StatisticsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计数据服务接口
 */
public interface StatisticsService {

    /**
     * 获取商家统计数据
     *
     * @param merchantId 商家ID
     * @return 商家统计数据
     */
    StatisticsDTO getMerchantStatistics(Long merchantId);

    /**
     * 获取商家订单统计数据
     *
     * @param merchantId 商家ID
     * @return 订单统计数据
     */
    Map<String, Integer> getMerchantOrderStatistics(Long merchantId);

    /**
     * 获取商家销售统计数据
     *
     * @param merchantId 商家ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param type       统计类型（day, week, month）
     * @return 销售统计数据
     */
    Map<String, Double> getMerchantSalesStatistics(Long merchantId, LocalDate startDate, LocalDate endDate, String type);

    /**
     * 获取商品销售排行
     *
     * @param merchantId 商家ID
     * @param limit      限制数量
     * @return 商品销售排行列表
     */
    List<Map<String, Object>> getProductSalesRanking(Long merchantId, Integer limit);
} 