package org.example.afd.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.afd.mapper.LogisticsMapper;
import org.example.afd.model.Result;
import org.example.afd.service.LogisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流信息服务实现类
 */
@Service
public class LogisticsServiceImpl implements LogisticsService {
    private static final Logger log = LoggerFactory.getLogger(LogisticsServiceImpl.class);
    
    @Autowired
    private LogisticsMapper logisticsMapper;
    
    @Override
    public Result<Map<String, Object>> getLogisticsByOrderId(Long orderId) {
        try {
            log.info("获取订单物流信息: orderId={}", orderId);
            Map<String, Object> logistics = logisticsMapper.getLogisticsByOrderId(orderId);
            
            if (logistics != null) {
                log.info("获取物流信息成功: {}", logistics);
                
                // 解析 tracking_details JSON 字段
                Object trackingDetailsObj = logistics.get("tracking_details");
                if (trackingDetailsObj != null) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String trackingDetailsJson = trackingDetailsObj.toString();
                        
                        // 尝试解析为List<Map<String, Object>>
                        List<Map<String, Object>> trackList = objectMapper.readValue(
                            trackingDetailsJson, 
                            new TypeReference<List<Map<String, Object>>>() {}
                        );
                        
                        logistics.put("track_list", trackList);
                        log.info("成功解析物流跟踪详情，跟踪记录数: {}", trackList.size());
                    } catch (Exception e) {
                        log.warn("解析物流跟踪详情失败: {}", e.getMessage());
                        // 设置为空列表，避免前端处理出错
                        logistics.put("track_list", new ArrayList<>());
                    }
                } else {
                    // 没有跟踪详情时设置为空列表
                    logistics.put("track_list", new ArrayList<>());
                }
                
                return Result.success("获取物流信息成功", logistics);
            } else {
                log.info("未找到物流信息: orderId={}", orderId);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("order_id", orderId);
                emptyResult.put("status", "暂无物流信息");
                emptyResult.put("track_list", new ArrayList<>());
                return Result.success("暂无物流信息", emptyResult);
            }
        } catch (Exception e) {
            log.error("获取订单物流信息失败", e);
            return Result.error("获取物流信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public Result<Map<String, Object>> getLogisticsByOrderNo(String orderNo) {
        try {
            log.info("获取订单物流信息: orderNo={}", orderNo);
            Map<String, Object> logistics = logisticsMapper.getLogisticsByOrderNo(orderNo);
            
            if (logistics != null) {
                log.info("获取物流信息成功: {}", logistics);
                
                // 解析 tracking_details JSON 字段
                Object trackingDetailsObj = logistics.get("tracking_details");
                if (trackingDetailsObj != null) {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String trackingDetailsJson = trackingDetailsObj.toString();
                        
                        // 尝试解析为List<Map<String, Object>>
                        List<Map<String, Object>> trackList = objectMapper.readValue(
                            trackingDetailsJson, 
                            new TypeReference<List<Map<String, Object>>>() {}
                        );
                        
                        logistics.put("track_list", trackList);
                        log.info("成功解析物流跟踪详情，跟踪记录数: {}", trackList.size());
                    } catch (Exception e) {
                        log.warn("解析物流跟踪详情失败: {}", e.getMessage());
                        // 设置为空列表，避免前端处理出错
                        logistics.put("track_list", new ArrayList<>());
                    }
                } else {
                    // 没有跟踪详情时设置为空列表
                    logistics.put("track_list", new ArrayList<>());
                }
                
                return Result.success("获取物流信息成功", logistics);
            } else {
                log.info("未找到物流信息: orderNo={}", orderNo);
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("order_no", orderNo);
                emptyResult.put("status", "暂无物流信息");
                emptyResult.put("track_list", new ArrayList<>());
                return Result.success("暂无物流信息", emptyResult);
            }
        } catch (Exception e) {
            log.error("获取订单物流信息失败", e);
            return Result.error("获取物流信息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Boolean> updateLogisticsInfo(Map<String, Object> logisticsInfo) {
        try {
            log.info("更新物流信息: {}", logisticsInfo);
            
            // 检查物流信息是否存在
            Long logisticsId = (Long) logisticsInfo.get("logistics_id");
            Long orderId = (Long) logisticsInfo.get("order_id");
            String orderNo = (String) logisticsInfo.get("order_no");
            
            Map<String, Object> existingLogistics = null;
            if (logisticsId != null) {
                // 使用物流ID查询
                existingLogistics = logisticsMapper.getLogisticsByOrderId(orderId);
            } else if (orderId != null) {
                // 使用订单ID查询
                existingLogistics = logisticsMapper.getLogisticsByOrderId(orderId);
            } else if (orderNo != null) {
                // 使用订单号查询
                existingLogistics = logisticsMapper.getLogisticsByOrderNo(orderNo);
            }
            
            int result;
            if (existingLogistics != null) {
                // 更新现有物流信息
                logisticsInfo.put("logistics_id", existingLogistics.get("logistics_id"));
                result = logisticsMapper.updateLogistics(logisticsInfo);
            } else {
                // 创建新的物流信息
                result = logisticsMapper.insertLogistics(logisticsInfo);
            }
            
            if (result > 0) {
                log.info("物流信息更新成功");
                return Result.success("物流信息更新成功", true);
            } else {
                log.error("物流信息更新失败");
                return Result.error("物流信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新物流信息失败", e);
            return Result.error("更新物流信息失败: " + e.getMessage());
        }
    }
} 