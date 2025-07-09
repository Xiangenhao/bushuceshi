package org.example.afd.config;

import org.example.afd.handler.ChatWebSocketHandler;
import org.example.afd.interceptor.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 配置WebSocket端点和拦截器
 * 
 * @author AFD Team
 * @version 1.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册聊天WebSocket端点
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*"); // 生产环境应该设置具体的域名
        
        // 注册带SockJS支持的端点（兼容不支持WebSocket的浏览器）
        registry.addHandler(chatWebSocketHandler, "/ws/chat/sockjs")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*")
                .withSockJS();
    }
} 