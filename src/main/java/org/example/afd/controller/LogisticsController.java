package org.example.afd.controller;

import org.example.afd.annotation.JwtAuth;
import org.example.afd.model.Result;
import org.example.afd.service.LogisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 物流信息控制器
 */
@RestController
@RequestMapping("/api/v1/logistics")
public class LogisticsController {
    private static final Logger log = LoggerFactory.getLogger(LogisticsController.class);
    
    @Autowired
    private LogisticsService logisticsService;
    
    /**
     * 根据订单ID获取物流信息
     */
    @GetMapping("/order/{orderId}")
    @JwtAuth
    public Result<Map<String, Object>> getLogisticsByOrderId(@PathVariable Long orderId) {
        log.info("获取订单物流信息: orderId={}", orderId);
        return logisticsService.getLogisticsByOrderId(orderId);
    }
    
    /**
     * 根据订单编号获取物流信息
     */
    @GetMapping("/orderNo/{orderNo}")
    @JwtAuth
    public Result<Map<String, Object>> getLogisticsByOrderNo(@PathVariable String orderNo) {
        log.info("获取订单物流信息: orderNo={}", orderNo);
        return logisticsService.getLogisticsByOrderNo(orderNo);
    }
    
    /**
     * 更新物流信息（商家使用）
     */
    @PostMapping("/update")
    @JwtAuth
    public Result<Boolean> updateLogisticsInfo(@RequestBody Map<String, Object> logisticsInfo) {
        log.info("更新物流信息: {}", logisticsInfo);
        return logisticsService.updateLogisticsInfo(logisticsInfo);
    }
} 