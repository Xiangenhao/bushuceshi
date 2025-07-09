package org.example.afd.service;

import org.example.afd.dto.PaymentDTO;
import org.example.afd.model.Result;

import java.util.List;
import java.util.Map;

/**
 * 统一支付服务接口
 * 支持购物订单和订阅订单的支付
 */
public interface PaymentService {
    
    /**
     * 创建支付
     * 
     * @param orderNo 订单号
     * @param paymentMethod 支付方式：1-微信，2-支付宝，3-银行卡，4-余额
     * @param userId 用户ID
     * @return 支付参数
     */
    Result<Map<String, Object>> createPayment(String orderNo, Integer paymentMethod, Long userId);
    
    /**
     * 查询支付状态
     * 
     * @param paymentNo 支付流水号
     * @param userId 用户ID
     * @return 支付状态
     */
    Result<Map<String, Object>> getPaymentStatus(String paymentNo, Long userId);
    
    /**
     * 确认支付完成
     * 
     * @param paymentNo 支付流水号
     * @param paymentData 支付回调数据
     * @return 确认结果
     */
    Result<Boolean> confirmPayment(String paymentNo, Map<String, Object> paymentData);
    
    /**
     * 取消支付
     * 
     * @param paymentNo 支付流水号
     * @param userId 用户ID
     * @return 取消结果
     */
    Result<Boolean> cancelPayment(String paymentNo, Long userId);
    
    /**
     * 处理支付回调
     * 
     * @param paymentChannel 支付渠道
     * @param callbackData 回调数据
     * @return 处理结果
     */
    Result<Boolean> handlePaymentCallback(String paymentChannel, Map<String, Object> callbackData);
    
    /**
     * 获取用户支付记录
     * 
     * @param userId 用户ID
     * @param orderType 订单类型：1-购物订单，2-订阅订单，null-全部
     * @param page 页码
     * @param size 每页大小
     * @return 支付记录列表
     */
    Result<List<PaymentDTO>> getUserPayments(Long userId, Integer orderType, Integer page, Integer size);
    
    /**
     * 获取支付方式列表
     * 
     * @param orderType 订单类型
     * @return 支付方式列表
     */
    Result<List<Map<String, Object>>> getPaymentMethods(Integer orderType);
    
    /**
     * 申请退款
     * 
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @param userId 用户ID
     * @return 退款申请结果
     */
    Result<Map<String, Object>> applyRefund(String orderNo, java.math.BigDecimal refundAmount, String refundReason, Long userId);
    
    /**
     * 查询退款状态
     * 
     * @param refundNo 退款单号
     * @param userId 用户ID
     * @return 退款状态
     */
    Result<Map<String, Object>> getRefundStatus(String refundNo, Long userId);
} 