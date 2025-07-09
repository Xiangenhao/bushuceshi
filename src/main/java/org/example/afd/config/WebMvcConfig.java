package org.example.afd.config;

import org.example.afd.interceptor.JwtAuthInterceptor;
import org.example.afd.interceptor.UserIdInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;
    
    @Autowired
    private UserIdInterceptor userIdInterceptor;
    
    @Value("${file.upload.base-path:D:/upload/files}")
    private String uploadFilePath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT认证拦截器
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/v1/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/v1/auth/login",   // 排除登录接口
                        "/api/v1/auth/register", // 排除注册接口
                        "/api/v1/auth/refresh-token", // 排除刷新令牌接口
                        "/api/v1/admin/login",   // 排除管理员登录接口
                        "/api/v1/admin/register", // 排除管理员注册接口
                        "/api/v1/users/captcha", // 排除验证码生成接口
                        "/api/v1/users/send-verification-code", // 排除发送验证码接口
                        "/api/v1/users/register", // 排除用户注册接口
                        "/api/v1/users/check-phone", // 排除检查手机号接口
                        "/api/v1/users/check-email", // 排除检查邮箱接口
                        "/api/v1/posts/zones",   // 排除获取区域接口
                        "/api/v1/banners",       // 排除轮播图接口
                        "/api/v1/products",      // 排除商品列表接口（公开浏览）
                        "/api/v1/categories",    // 排除分类接口（公开浏览）
                        "/error"                 // 排除错误页面
                );
                
        // 注册用户ID拦截器，必须在JWT认证拦截器之后执行
        registry.addInterceptor(userIdInterceptor)
                .addPathPatterns("/api/v1/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/v1/auth/login",   // 排除登录接口
                        "/api/v1/auth/register", // 排除注册接口
                        "/api/v1/auth/refresh-token", // 排除刷新令牌接口
                        "/api/v1/admin/login",   // 排除管理员登录接口
                        "/api/v1/admin/register", // 排除管理员注册接口
                        "/api/v1/users/captcha", // 排除验证码生成接口
                        "/api/v1/users/send-verification-code", // 排除发送验证码接口
                        "/api/v1/users/register", // 排除用户注册接口
                        "/api/v1/users/check-phone", // 排除检查手机号接口
                        "/api/v1/users/check-email", // 排除检查邮箱接口
                        "/api/v1/banners",       // 排除轮播图接口
                        "/api/v1/products",      // 排除商品列表接口（公开浏览）
                        "/api/v1/categories",    // 排除分类接口（公开浏览）
                        "/error"                 // 排除错误页面
                );
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源映射，将/files/**请求映射到文件系统中的uploadFilePath目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadFilePath + "/")
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(true);
        
        // 保留默认的静态资源映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 配置跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOriginPatterns("*") // 允许所有域名访问
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 预检请求缓存时间
    }
} 