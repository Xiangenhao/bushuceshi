package org.example.afd;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.utils.AliyunOSSOperator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@MapperScan("org.example.afd.mapper")
@EnableScheduling
public class AfdApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfdApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner initializeOSS(AliyunOSSOperator aliyunOSSOperator) {
        return args -> {
            // 初始化OSS配置，但不输出日志
            aliyunOSSOperator.init();
        };
    }
}
