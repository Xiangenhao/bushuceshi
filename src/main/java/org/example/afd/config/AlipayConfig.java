package org.example.afd.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    private String appId;
    private String privateKey;
    private String publicKey;
    private String gatewayUrl;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String format = "json";
    
    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${file.upload.base-url}")
    private String baseUrl;

    /**
     * 获取完整的异步通知URL
     * 如果notifyUrl是相对路径，则拼接baseUrl
     */
    public String getFullNotifyUrl() {
        if (notifyUrl != null && !notifyUrl.startsWith("http")) {
            return baseUrl + notifyUrl;
        }
        return notifyUrl;
    }

    /**
     * 获取完整的同步返回URL
     * 如果returnUrl是相对路径，则拼接baseUrl
     */
    public String getFullReturnUrl() {
        if (returnUrl != null && !returnUrl.startsWith("http")) {
            return baseUrl + returnUrl;
        }
        return returnUrl;
    }
} 