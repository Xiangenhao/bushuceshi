package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.Payment;
import org.example.afd.dto.PaymentDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统一支付Mapper接口
 * 对应数据库表：payments
 */
@Mapper
public interface PaymentMapper {
    
    /**
     * 创建支付记录
     */
    @Insert("INSERT INTO afd.payments (payment_no, order_id, user_id, channel_id, " +
            "payment_amount, payment_status, create_time, update_time) " +
            "VALUES (#{paymentNo}, #{orderId}, #{userId}, #{channelId}, " +
            "#{paymentAmount}, #{paymentStatus}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "paymentId")
    int insertPayment(Payment payment);
    
    /**
     * 根据支付流水号查询支付记录
     */
    @Select("SELECT * FROM afd.payments WHERE payment_no = #{paymentNo}")
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);
    
    /**
     * 根据支付流水号查询支付记录（别名方法）
     */
    default Payment getPaymentByPaymentNo(String paymentNo) {
        return selectByPaymentNo(paymentNo);
    }
    
    /**
     * 根据订单ID查询支付记录
     */
    @Select("SELECT * FROM afd.payments WHERE order_id = #{orderId}")
    List<Payment> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单ID查询单个支付记录（取最新的一条）
     */
    @Select("SELECT * FROM afd.payments WHERE order_id = #{orderId} ORDER BY create_time DESC LIMIT 1")
    Payment getPaymentByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 更新支付记录
     */
    @Update("UPDATE afd.payments SET order_id = #{orderId}, user_id = #{userId}, " +
            "channel_id = #{channelId}, " +
            "payment_amount = #{paymentAmount}, payment_status = #{paymentStatus}, " +
            "third_party_order_no = #{thirdPartyOrderNo}, " +
            "third_party_transaction_id = #{thirdPartyTransactionId}, " +
            "payment_time = #{paymentTime}, callback_time = #{callbackTime}, " +
            "update_time = #{updateTime} " +
            "WHERE payment_id = #{paymentId}")
    int updatePayment(Payment payment);
    
    /**
     * 更新支付状态
     */
    @Update("UPDATE afd.payments SET payment_status = #{status}, " +
            "third_party_order_no = #{thirdPartyOrderNo}, " +
            "third_party_transaction_id = #{thirdPartyTransactionId}, " +
            "payment_time = #{paymentTime}, " +
            "callback_time = #{callbackTime}, " +
            "update_time = NOW() " +
            "WHERE payment_no = #{paymentNo}")
    int updatePaymentStatus(@Param("paymentNo") String paymentNo, 
                           @Param("status") Integer status,
                           @Param("thirdPartyOrderNo") String thirdPartyOrderNo,
                           @Param("thirdPartyTransactionId") String thirdPartyTransactionId,
                           @Param("paymentTime") LocalDateTime paymentTime,
                           @Param("callbackTime") LocalDateTime callbackTime);
    
    /**
     * 根据第三方订单号查询支付记录
     */
    @Select("SELECT * FROM afd.payments WHERE third_party_order_no = #{thirdPartyOrderNo}")
    Payment selectByThirdPartyOrderNo(@Param("thirdPartyOrderNo") String thirdPartyOrderNo);
    
    /**
     * 获取用户支付记录列表（返回Payment实体）
     */
    @Select("<script>" +
            "SELECT p.* " +
            "FROM afd.payments p " +
            "JOIN afd.orders o ON p.order_id = o.order_id " +
            "WHERE p.user_id = #{userId} " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if> " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Payment> getUserPayments(@Param("userId") Integer userId,
                                 @Param("orderType") Integer orderType,
                                 @Param("offset") Integer offset,
                                 @Param("size") Integer size);
    
    /**
     * 获取用户支付记录列表（包含详细信息）
     */
    @Select("<script>" +
            "SELECT p.*, o.order_type, o.related_id, " +
            "CASE WHEN o.order_type = 2 THEN plan.title ELSE NULL END as plan_title " +
            "FROM afd.payments p " +
            "JOIN afd.orders o ON p.order_id = o.order_id " +
            "LEFT JOIN afd.post_subscription_plan plan ON o.order_type = 2 AND o.related_id = plan.plan_id " +
            "WHERE p.user_id = #{userId} " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if> " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectUserPayments(@Param("userId") Long userId,
                                                 @Param("orderType") Integer orderType,
                                                 @Param("offset") Integer offset,
                                                 @Param("size") Integer size);
    
    /**
     * 获取支付统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "COALESCE(SUM(payment_amount), 0) as total_amount, " +
            "COUNT(CASE WHEN payment_status = 1 THEN 1 END) as pending_count, " +
            "COUNT(CASE WHEN payment_status = 2 THEN 1 END) as success_count, " +
            "COUNT(CASE WHEN payment_status = 3 THEN 1 END) as failed_count " +
            "FROM afd.payments " +
            "WHERE user_id = #{userId}")
    Map<String, Object> selectPaymentStatistics(@Param("userId") Long userId);
    
    /**
     * 取消支付
     */
    @Update("UPDATE afd.payments SET payment_status = 3, update_time = NOW() " +
            "WHERE payment_no = #{paymentNo} AND payment_status = 1")
    int cancelPayment(@Param("paymentNo") String paymentNo);
    
    /**
     * 创建退款记录
     */
    @Insert("INSERT INTO afd.refunds (refund_no, order_id, payment_id, user_id, " +
            "refund_amount, refund_reason, refund_status, apply_time) " +
            "VALUES (#{refundNo}, #{orderId}, #{paymentId}, #{userId}, " +
            "#{refundAmount}, #{refundReason}, 1, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "refundId")
    int insertRefund(@Param("refundNo") String refundNo,
                    @Param("orderId") Long orderId,
                    @Param("paymentId") Long paymentId,
                    @Param("userId") Integer userId,
                    @Param("refundAmount") BigDecimal refundAmount,
                    @Param("refundReason") String refundReason);
    
    /**
     * 根据退款单号查询退款记录
     */
    @Select("SELECT * FROM afd.refunds WHERE refund_no = #{refundNo}")
    Map<String, Object> selectRefundByNo(@Param("refundNo") String refundNo);
    
    /**
     * 更新退款状态
     */
    @Update("UPDATE afd.refunds SET refund_status = #{status}, " +
            "process_time = #{processTime}, " +
            "complete_time = #{completeTime}, " +
            "third_party_refund_id = #{thirdPartyRefundId} " +
            "WHERE refund_no = #{refundNo}")
    int updateRefundStatus(@Param("refundNo") String refundNo,
                          @Param("status") Integer status,
                          @Param("processTime") LocalDateTime processTime,
                          @Param("completeTime") LocalDateTime completeTime,
                          @Param("thirdPartyRefundId") String thirdPartyRefundId);
    
    /**
     * 获取用户退款记录
     */
    @Select("SELECT r.*, o.order_type, p.payment_amount " +
            "FROM afd.refunds r " +
            "JOIN afd.orders o ON r.order_id = o.order_id " +
            "JOIN afd.payments p ON r.payment_id = p.payment_id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.apply_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectUserRefunds(@Param("userId") Long userId,
                                               @Param("offset") Integer offset,
                                               @Param("size") Integer size);
    
    /**
     * 获取用户支付记录总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM afd.payments p " +
            "JOIN afd.orders o ON p.order_id = o.order_id " +
            "WHERE p.user_id = #{userId} " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if>" +
            "</script>")
    int getUserPaymentCount(@Param("userId") Integer userId, @Param("orderType") Integer orderType);
    
    /**
     * 获取用户支付记录总数（重载方法）
     */
    default int getUserPaymentCount(Integer userId) {
        return getUserPaymentCount(userId, null);
    }
} 