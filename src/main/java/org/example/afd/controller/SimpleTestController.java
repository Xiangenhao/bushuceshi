package org.example.afd.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单测试控制器 - 不使用任何依赖注入
 */
@RestController
@RequestMapping("/api/v1/simple")
public class SimpleTestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/status")
    public String status() {
        return "Service is running";
    }
} 