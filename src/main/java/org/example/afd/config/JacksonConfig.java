package org.example.afd.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.afd.utils.JacksonLocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;

/**
 * Jackson配置类
 * 使用自定义序列化器确保日期格式带有时区信息
 * 使用默认的驼峰命名策略，保持与Java代码风格一致
 */
@Configuration
public class JacksonConfig {
    
    /**
     * 配置ObjectMapper，处理日期时间序列化
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 移除snake_case命名策略，使用默认的驼峰命名
        // 这样返回的JSON字段名与Java字段名保持一致
        
        // 创建Java 8时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 注册我们自定义的LocalDateTime序列化器
        javaTimeModule.addSerializer(LocalDateTime.class, new JacksonLocalDateTimeSerializer());
        
        // 注册模块
        objectMapper.registerModule(javaTimeModule);
        
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return objectMapper;
    }
} 