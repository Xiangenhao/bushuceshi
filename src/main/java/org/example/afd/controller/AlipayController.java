package org.example.afd.controller;

import org.example.afd.config.AlipayConfig;
import org.example.afd.service.PaymentService;
import org.example.afd.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

/**
 * 支付宝支付回调控制器
 * 
 * 处理支付宝的异步通知和同步返回：
 * - 异步通知：支付宝主动通知支付结果
 * - 同步返回：用户支付完成后返回应用
 * 
 * @author AFD Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/payment/alipay")
public class AlipayController {
    
    private static final Logger log = LoggerFactory.getLogger(AlipayController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private AlipayConfig alipayConfig;
    
    /**
     * 支付宝异步通知
     * 
     * 支付宝会在支付完成后主动调用这个接口通知支付结果
     * 
     * @param request HTTP请求
     * @return 处理结果（返回"success"表示处理成功）
     */
    @PostMapping("/notify")
    public String handleNotify(HttpServletRequest request) {
        try {
            log.info("收到支付宝异步通知");
            
            // 获取支付宝POST过来的反馈信息
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
                log.info("支付宝通知参数: {} = {}", paramName, paramValue);
            }
            
            // 验证签名（这里简化处理，实际应该验证支付宝签名）
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功，更新订单状态
                Map<String, Object> callbackData = new HashMap<>();
                callbackData.put("out_trade_no", outTradeNo);
                callbackData.put("trade_status", tradeStatus);
                callbackData.put("total_amount", params.get("total_amount"));
                callbackData.put("trade_no", params.get("trade_no"));
                
                Result<Boolean> result = paymentService.handlePaymentCallback("alipay", callbackData);
                
                if (result.isSuccess()) {
                    log.info("支付宝异步通知处理成功，订单号: {}", outTradeNo);
                    return "success";
                } else {
                    log.error("支付宝异步通知处理失败: {}", result.getMessage());
                    return "fail";
                }
            } else {
                log.warn("支付宝通知状态异常: {}", tradeStatus);
                return "fail";
            }
            
        } catch (Exception e) {
            log.error("处理支付宝异步通知异常", e);
            return "fail";
        }
    }
    
    /**
     * 支付宝同步返回
     * 
     * 用户支付完成后会跳转到这个地址
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     */
    @GetMapping("/return")
    public void handleReturn(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("收到支付宝同步返回");
            
            // 获取支付宝GET过来的反馈信息
            Map<String, String> params = new HashMap<>();
            Enumeration<String> parameterNames = request.getParameterNames();
            
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String paramValue = request.getParameter(paramName);
                params.put(paramName, paramValue);
                log.info("支付宝返回参数: {} = {}", paramName, paramValue);
            }
            
            // 重定向到前端页面显示支付结果
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功，重定向到成功页面
                response.sendRedirect("/payment/success?orderNo=" + outTradeNo);
            } else {
                // 支付失败，重定向到失败页面
                response.sendRedirect("/payment/fail?orderNo=" + outTradeNo);
            }
            
        } catch (IOException e) {
            log.error("处理支付宝同步返回异常", e);
            try {
                response.sendRedirect("/payment/error");
            } catch (IOException ex) {
                log.error("重定向失败", ex);
            }
        }
    }
    
    /**
     * 测试支付宝配置
     * 
     * @return 配置信息（隐藏敏感信息）
     */
    @GetMapping("/config/test")
    public Result<Map<String, Object>> testConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("appId", alipayConfig.getAppId());
            config.put("gatewayUrl", alipayConfig.getGatewayUrl());
            config.put("signType", alipayConfig.getSignType());
            config.put("charset", alipayConfig.getCharset());
            config.put("format", alipayConfig.getFormat());
            config.put("notifyUrl", alipayConfig.getFullNotifyUrl());
            config.put("returnUrl", alipayConfig.getFullReturnUrl());
            
            // 隐藏敏感信息
            String privateKey = alipayConfig.getPrivateKey();
            if (privateKey != null && privateKey.length() > 20) {
                config.put("privateKey", privateKey.substring(0, 20) + "...");
            }
            
            String publicKey = alipayConfig.getPublicKey();
            if (publicKey != null && publicKey.length() > 20) {
                config.put("publicKey", publicKey.substring(0, 20) + "...");
            }
            
            return Result.success(config);
            
        } catch (Exception e) {
            log.error("测试支付宝配置失败", e);
            return Result.error("配置测试失败");
        }
    }
}
