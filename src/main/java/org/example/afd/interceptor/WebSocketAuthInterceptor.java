package org.example.afd.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于验证用户身份和权限
 * 
 * @author AFD Team
 * @version 1.0
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            log.info("WebSocket握手开始: URI={}", request.getURI());
            
            // 从查询参数中获取token
            String query = request.getURI().getQuery();
            String token = null;
            
            if (query != null && query.contains("token=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6); // 去掉"token="
                        log.debug("从查询参数获取原始token: {}...", token.length() > 20 ? token.substring(0, 20) : token);
                        
                        // 还原URL编码的特殊字符（JWT中的+、/、=）
                        try {
                            String decodedToken = token
                                .replace("%2B", "+")
                                .replace("%2F", "/")
                                .replace("%3D", "=");
                            
                            log.debug("Token特殊字符还原: 原始长度={}, 还原后长度={}", token.length(), decodedToken.length());
                            token = decodedToken;
                        } catch (Exception e) {
                            log.warn("Token特殊字符还原失败，使用原始token: {}", e.getMessage());
                        }
                        break;
                    }
                }
            }
            
            // 如果查询参数中没有token，尝试从header中获取
            if (token == null || token.isEmpty()) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    log.debug("从Authorization头获取token: {}...", token.length() > 20 ? token.substring(0, 20) : token);
                }
            }
            
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket握手失败: 缺少token");
                return false;
            }
            
            // 验证JWT token格式
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                log.warn("WebSocket握手失败: token格式错误，应该有3个部分，实际有{}个部分", tokenParts.length);
                log.debug("错误的token前50个字符: {}", token.length() > 50 ? token.substring(0, 50) : token);
                return false;
            }
            
            log.info("准备验证token，长度: {}, JWT格式验证通过", token.length());
            
            // 使用JWT工具类验证token
            boolean isValid = false;
            Integer userId = null;
            String username = null;
            
            try {
                // 先验证token有效性
                isValid = jwtUtils.validateToken(token);
                
                if (isValid) {
                    // 如果token有效，获取用户信息
                    userId = jwtUtils.getUserIdFromToken(token);
                    username = jwtUtils.getUsernameFromToken(token);
                }
                
                log.debug("Token验证结果: isValid={}, userId={}, username={}", isValid, userId, username);
                
            } catch (Exception e) {
                log.warn("Token验证异常: {}", e.getMessage());
                log.debug("Token验证详细异常", e);
                isValid = false;
            }
            
            if (!isValid) {
                log.warn("WebSocket握手失败: token验证失败");
                return false;
            }
            
            if (userId == null || username == null) {
                log.warn("WebSocket握手失败: 无法从token中提取用户信息 - userId={}, username={}", userId, username);
                return false;
            }
            
            // 将用户信息存储到attributes中，供WebSocketHandler使用
            attributes.put("userId", userId);
            attributes.put("username", username);
            attributes.put("token", token);
            
            log.info("WebSocket握手成功: userId={}, username={}", userId, username);
            return true;
            
        } catch (Exception e) {
            log.error("WebSocket握手异常", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手后异常", exception);
        } else {
            log.info("WebSocket握手完成");
        }
    }
} 