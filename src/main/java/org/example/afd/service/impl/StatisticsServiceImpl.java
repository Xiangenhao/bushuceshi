package org.example.afd.service.impl;

import org.example.afd.dto.StatisticsDTO;
import org.example.afd.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Override
    public StatisticsDTO getMerchantStatistics(Long merchantId) {
        // 实际项目中应该从数据库查询相关数据
        // 这里为了解决编译错误，临时返回一个示例数据
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        statisticsDTO.setTotalSales(new BigDecimal("10000.00"));
        statisticsDTO.setTodaySales(new BigDecimal("500.00"));
        statisticsDTO.setTotalOrders(100);
        statisticsDTO.setTodayOrders(10);
        statisticsDTO.setTotalProducts(50);
        statisticsDTO.setTotalCustomers(200);
        
        // 设置销售趋势
        Map<String, BigDecimal> salesTrend = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            salesTrend.put(date.toString(), new BigDecimal(Math.random() * 1000).setScale(2, RoundingMode.HALF_UP));
        }
        statisticsDTO.setSalesTrend(salesTrend);
        
        // 设置订单趋势
        Map<String, Integer> orderTrend = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            orderTrend.put(date.toString(), (int)(Math.random() * 20));
        }
        statisticsDTO.setOrderTrend(orderTrend);
        
        // 设置商品销售排行
        List<StatisticsDTO.ProductRank> productRanking = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            StatisticsDTO.ProductRank rank = new StatisticsDTO.ProductRank();
            rank.setProductId((long)i);
            rank.setProductName("商品" + i);
            rank.setProductImage("http://example.com/image" + i + ".jpg");
            rank.setSalesCount((int)(Math.random() * 100));
            rank.setSalesAmount(new BigDecimal(Math.random() * 5000).setScale(2, RoundingMode.HALF_UP));
            productRanking.add(rank);
        }
        statisticsDTO.setProductRanking(productRanking);
        
        // 设置订单状态统计
        Map<String, Integer> orderStatusCounts = new HashMap<>();
        orderStatusCounts.put("待付款", 5);
        orderStatusCounts.put("待发货", 10);
        orderStatusCounts.put("待收货", 15);
        orderStatusCounts.put("已完成", 50);
        orderStatusCounts.put("已取消", 10);
        orderStatusCounts.put("已退款", 10);
        statisticsDTO.setOrderStatusCounts(orderStatusCounts);
        
        // 设置商品分类销售占比
        Map<String, BigDecimal> categorySalesPercentage = new HashMap<>();
        categorySalesPercentage.put("食品", new BigDecimal("0.3"));
        categorySalesPercentage.put("电子", new BigDecimal("0.25"));
        categorySalesPercentage.put("服装", new BigDecimal("0.2"));
        categorySalesPercentage.put("家居", new BigDecimal("0.15"));
        categorySalesPercentage.put("其他", new BigDecimal("0.1"));
        statisticsDTO.setCategorySalesPercentage(categorySalesPercentage);
        
        return statisticsDTO;
    }

    @Override
    public Map<String, Integer> getMerchantOrderStatistics(Long merchantId) {
        // 返回示例数据
        Map<String, Integer> orderStats = new HashMap<>();
        orderStats.put("total", 100);
        orderStats.put("pending", 20);
        orderStats.put("processing", 30);
        orderStats.put("completed", 40);
        orderStats.put("cancelled", 10);
        return orderStats;
    }

    @Override
    public Map<String, Double> getMerchantSalesStatistics(Long merchantId, LocalDate startDate, LocalDate endDate, String type) {
        // 返回示例数据
        Map<String, Double> salesStats = new LinkedHashMap<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            String key = current.toString();
            if ("week".equals(type)) {
                key = "Week " + current.get(java.time.temporal.WeekFields.ISO.weekOfYear());
                current = current.plusWeeks(1);
            } else if ("month".equals(type)) {
                key = current.getMonth().toString() + " " + current.getYear();
                current = current.plusMonths(1);
            } else {
                // 默认按天统计
                current = current.plusDays(1);
            }
            
            salesStats.put(key, Math.random() * 10000);
        }
        
        return salesStats;
    }

    @Override
    public List<Map<String, Object>> getProductSalesRanking(Long merchantId, Integer limit) {
        // 返回示例数据
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (int i = 1; i <= limit; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", i);
            product.put("name", "热销商品" + i);
            product.put("image", "http://example.com/image" + i + ".jpg");
            product.put("sales", (int)(Math.random() * 1000));
            product.put("amount", Math.random() * 50000);
            ranking.add(product);
        }
        return ranking;
    }
} 