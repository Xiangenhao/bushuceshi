package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.model.Result;
import org.example.afd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 商家用户关联控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantUserController {

    @Autowired
    private UserService userService;

    /**
     * 通过商家ID获取对应的用户ID
     */
    @GetMapping("/{merchantId}/user")
    public ResponseEntity<Result<Long>> getMerchantUserId(@PathVariable Long merchantId) {
        try {
            log.info("=== 获取商家对应的用户ID ===");
            log.info("请求参数: merchantId={}", merchantId);
            
            Long userId = userService.getMerchantUserId(merchantId);
            
            if (userId == null) {
                log.warn("未找到商家对应的用户ID: merchantId={}", merchantId);
                return ResponseEntity.ok(Result.error("商家信息不存在"));
            }
            
            log.info("获取商家用户ID成功: merchantId={}, userId={}", merchantId, userId);
            return ResponseEntity.ok(Result.success(userId));
            
        } catch (Exception e) {
            log.error("获取商家用户ID失败: merchantId={}", merchantId, e);
            return ResponseEntity.ok(Result.error("获取商家信息失败: " + e.getMessage()));
        }
    }
} 