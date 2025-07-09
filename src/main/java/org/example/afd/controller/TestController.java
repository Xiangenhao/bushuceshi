package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.mapper.OrderMapper;
import org.example.afd.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 仅用于开发测试
 */
@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * 创建测试商家和订单数据
     */
    @PostMapping("/create-test-data")
    public Result<String> createTestData() {
        try {
            log.info("=== 创建测试数据 ===");
            
            // 1. 创建商家记录
            Map<String, Object> merchant = new HashMap<>();
            merchant.put("user_id", 5L);
            merchant.put("merchant_name", "测试商家");
            merchant.put("logo", "http://test.com/logo.jpg");
            merchant.put("description", "这是一个测试商家");
            merchant.put("contact_name", "测试联系人");
            merchant.put("contact_phone", "15821007540");
            merchant.put("status", 1);
            
            // 使用原生SQL插入商家数据
            orderMapper.insertTestMerchant(merchant);
            log.info("商家创建成功，ID: {}", merchant.get("merchant_id"));
            
            // 2. 创建测试订单
            Map<String, Object> order = new HashMap<>();
            order.put("order_no", "TEST" + System.currentTimeMillis());
            order.put("user_id", 1); // 假设用户ID为1
            order.put("order_type", 1); // 商品订单
            order.put("order_status", 2); // 已支付状态
            order.put("total_amount", 99.99);
            order.put("merchant_id", merchant.get("merchant_id"));
            order.put("receiver_name", "测试收货人");
            order.put("receiver_phone", "13800138000");
            order.put("shipping_address", "测试地址");
            order.put("order_note", "测试发货功能");
            order.put("create_time", LocalDateTime.now());
            order.put("update_time", LocalDateTime.now());
            
            orderMapper.insertTestOrder(order);
            log.info("订单创建成功，ID: {}", order.get("order_id"));
            
            String result = String.format("测试数据创建成功！商家ID: %s, 订单ID: %s, 订单号: %s", 
                    merchant.get("merchant_id"), order.get("order_id"), order.get("order_no"));
            
            return Result.success("测试数据创建成功", result);
            
        } catch (Exception e) {
            log.error("创建测试数据失败", e);
            return Result.error("创建测试数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试接口
     */
    @GetMapping("")
    public Result<String> test() {
        return Result.success("测试接口正常", "Hello Test!");
    }
} 