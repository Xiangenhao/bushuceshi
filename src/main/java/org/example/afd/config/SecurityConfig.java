package org.example.afd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用CSRF保护
        http.csrf(csrf -> csrf.disable());
        
        // 允许所有请求，因为我们使用自定义的JWT拦截器（JwtAuthInterceptor）进行认证
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        
        // 禁用默认的登录页面
        http.formLogin(form -> form.disable());
        http.httpBasic(withDefaults());
        
        return http.build();
    }
} 