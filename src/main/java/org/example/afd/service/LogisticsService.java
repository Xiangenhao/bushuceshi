package org.example.afd.service;

import org.example.afd.model.Result;

import java.util.Map;

/**
 * 物流信息服务接口
 */
public interface LogisticsService {
    
    /**
     * 根据订单ID获取物流信息
     * @param orderId 订单ID
     * @return 物流信息
     */
    Result<Map<String, Object>> getLogisticsByOrderId(Long orderId);
    
    /**
     * 根据订单编号获取物流信息
     * @param orderNo 订单编号
     * @return 物流信息
     */
    Result<Map<String, Object>> getLogisticsByOrderNo(String orderNo);
    
    /**
     * 更新物流信息
     * @param logisticsInfo 物流信息
     * @return 更新结果
     */
    Result<Boolean> updateLogisticsInfo(Map<String, Object> logisticsInfo);
} 