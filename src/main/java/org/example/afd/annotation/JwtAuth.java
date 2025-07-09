package org.example.afd.annotation;

import java.lang.annotation.*;

/**
 * JWT认证注解
 * 用于标记需要JWT认证的API接口
 * 
 * @author AFD Team
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JwtAuth {
    /**
     * 是否必须认证，默认为true
     */
    boolean required() default true;
    
    /**
     * 所需的用户角色，为空表示任何已认证用户都可以访问
     */
    String[] roles() default {};
    
    /**
     * 接口描述，用于日志记录
     */
    String value() default "";
} 