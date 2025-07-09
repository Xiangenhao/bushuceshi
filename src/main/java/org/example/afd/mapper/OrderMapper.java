package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.Order;
import org.example.afd.dto.OrderDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统一订单Mapper接口
 * 对应数据库表：orders
 */
@Mapper
public interface OrderMapper {
    
    /**
     * 创建订单
     */
    @Insert("INSERT INTO afd.orders (order_no, user_id, order_type, related_id, " +
            "total_amount, paid_amount, shipping_fee, discount_amount, coupon_amount, " +
            "order_status, order_note, address_id, subscription_months, " +
            "subscription_start_time, subscription_end_time, " +
            "create_time, update_time, expire_time) " +
            "VALUES (#{orderNo}, #{userId}, #{orderType}, #{relatedId}, " +
            "#{totalAmount}, #{paidAmount}, #{shippingFee}, #{discountAmount}, #{couponAmount}, " +
            "#{orderStatus}, #{orderNote}, #{addressId}, #{subscriptionMonths}, " +
            "#{subscriptionStartTime}, #{subscriptionEndTime}, " +
            "#{createTime}, #{updateTime}, #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insertOrder(Order order);
    
    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM afd.orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 根据订单号查询订单（别名方法）
     */
    default Order getOrderByOrderNo(String orderNo) {
        return selectByOrderNo(orderNo);
    }
    
    /**
     * 根据订单号查询订单信息（返回Map格式）
     */
    @Select("SELECT * FROM afd.orders WHERE order_no = #{orderNumber}")
    Map<String, Object> getOrderByOrderNumber(@Param("orderNumber") String orderNumber);
    
    /**
     * 根据订单ID查询订单
     */
    @Select("SELECT * FROM afd.orders WHERE order_id = #{orderId}")
    Order selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单ID查询订单（别名方法）
     */
    default Order getOrderById(Long orderId) {
        return selectByOrderId(orderId);
    }
    
    /**
     * 更新订单
     */
    @Update("UPDATE afd.orders SET user_id = #{userId}, " +
            "order_type = #{orderType}, related_id = #{relatedId}, " +
            "total_amount = #{totalAmount}, paid_amount = #{paidAmount}, " +
            "shipping_fee = #{shippingFee}, discount_amount = #{discountAmount}, " +
            "coupon_amount = #{couponAmount}, order_status = #{orderStatus}, " +
            "order_note = #{orderNote}, address_id = #{addressId}, " +
            "logistics_info = #{logisticsInfo}, subscription_months = #{subscriptionMonths}, " +
            "subscription_start_time = #{subscriptionStartTime}, subscription_end_time = #{subscriptionEndTime}, " +
            "update_time = #{updateTime}, expire_time = #{expireTime} " +
            "WHERE order_id = #{orderId}")
    int updateOrder(Order order);
    
    /**
     * 更新订单状态
     */
    @Update("UPDATE afd.orders SET order_status = #{status}, update_time = NOW() " +
            "WHERE order_no = #{orderNo}")
    int updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);
    
    /**
     * 更新订单支付金额
     */
    @Update("UPDATE afd.orders SET paid_amount = #{paidAmount}, update_time = NOW() " +
            "WHERE order_no = #{orderNo}")
    int updatePaidAmount(@Param("orderNo") String orderNo, @Param("paidAmount") BigDecimal paidAmount);
    
    /**
     * 查询用户订单列表（排除已删除状态）
     */
    @Select("<script>" +
            "SELECT o.*, " +
            "CASE WHEN o.order_type = 2 THEN p.title ELSE NULL END as plan_title, " +
            "CASE WHEN o.order_type = 2 THEN p.cover_url ELSE NULL END as plan_cover_url, " +
            "CASE WHEN o.order_type = 1 THEN m.merchant_name ELSE NULL END as merchant_name, " +
            "CASE WHEN o.order_type = 1 THEN m.logo ELSE NULL END as merchant_logo " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.post_subscription_plan p ON o.order_type = 2 AND o.related_id = p.plan_id " +
            "LEFT JOIN afd.shop_merchant m ON o.order_type = 1 AND o.related_id = m.merchant_id " +
            "WHERE o.user_id = #{userId} " +
            "AND o.order_status != 0 " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if> " +
            "<if test='orderStatus != null'>AND o.order_status = #{orderStatus}</if> " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectUserOrders(@Param("userId") Long userId,
                                                @Param("orderType") Integer orderType, 
                                                @Param("orderStatus") Integer orderStatus,
                                                @Param("offset") int offset, 
                                                @Param("size") int size);
    
    /**
     * 获取订单统计信息
     */
    @Select("<script>" +
            "SELECT " +
            "COUNT(*) as total_count, " +
            "COALESCE(SUM(paid_amount), 0) as total_amount, " +
            "COUNT(CASE WHEN order_status = 1 THEN 1 END) as pending_count, " +
            "COUNT(CASE WHEN order_status = 4 THEN 1 END) as completed_count, " +
            "COUNT(CASE WHEN order_status = 5 THEN 1 END) as cancelled_count " +
            "FROM afd.orders " +
            "WHERE user_id = #{userId} " +
            "<if test='orderType != null'>AND order_type = #{orderType}</if>" +
            "</script>")
    Map<String, Object> selectOrderStatistics(@Param("userId") Long userId, @Param("orderType") Integer orderType);
    
    /**
     * 检查用户是否有某个计划的有效订阅订单
     */
    @Select("SELECT COUNT(*) > 0 FROM afd.orders o " +
            "JOIN afd.post_user_subscription s ON o.user_id = s.user_id AND o.related_id = s.plan_id " +
            "WHERE o.user_id = #{userId} AND o.related_id = #{planId} " +
            "AND o.order_type = 2 AND o.order_status = 4 " +
            "AND s.status = 1 AND s.end_time > NOW()")
    boolean hasActiveSubscription(@Param("userId") Long userId, @Param("planId") Long planId);
    
    /**
     * 删除过期未支付订单
     */
    @Update("UPDATE afd.orders SET order_status = 5, update_time = NOW() " +
            "WHERE order_status = 1 AND expire_time < NOW()")
    int expireUnpaidOrders();
    
    /**
     * 根据订单号和用户ID查询订单详情（包含关联信息和地址信息）
     */
    @Select("SELECT o.*, " +
            "CASE WHEN o.order_type = 2 THEN p.title ELSE NULL END as plan_title, " +
            "CASE WHEN o.order_type = 2 THEN p.cover_url ELSE NULL END as plan_cover_url, " +
            "CASE WHEN o.order_type = 2 THEN p.creator_id ELSE NULL END as creator_id, " +
            "CASE WHEN o.order_type = 2 THEN u.username ELSE NULL END as creator_name, " +
            "CASE WHEN o.order_type = 1 THEN m.merchant_name ELSE NULL END as merchant_name, " +
            "CASE WHEN o.order_type = 1 THEN m.logo ELSE NULL END as merchant_logo, " +
            "addr.receiver_name, addr.receiver_phone, " +
            "uaj.postal_code, uaj.prefecture, uaj.municipality as city, uaj.town, " +
            "uaj.chome, uaj.banchi, uaj.building, uaj.room_number, " +
            "CONCAT(uaj.address_line1, IFNULL(CONCAT(' ', uaj.address_line2), '')) as shipping_address, " +
            "addr.is_deleted as address_deleted " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.post_subscription_plan p ON o.order_type = 2 AND o.related_id = p.plan_id " +
            "LEFT JOIN afd.users u ON p.creator_id = u.user_id " +
            "LEFT JOIN afd.shop_merchant m ON o.order_type = 1 AND o.related_id = m.merchant_id " +
            "LEFT JOIN afd.user_address addr ON o.address_id = addr.address_id " +
            "LEFT JOIN afd.user_address_japan uaj ON addr.address_id = uaj.address_id " +
            "WHERE o.order_no = #{orderNo} AND o.user_id = #{userId}")
    Map<String, Object> selectOrderDetailByNoAndUser(@Param("orderNo") String orderNo, @Param("userId") Long userId);
    
    /**
     * 删除订单
     */
    @Delete("DELETE FROM afd.orders WHERE order_id = #{orderId}")
    int deleteOrder(@Param("orderId") Long orderId);
    
    // ==================== Order Items 相关操作 ====================
    
    /**
     * 插入订单项
     */
    @Insert("INSERT INTO afd.order_items (order_id, item_type, product_id, sku_id, " +
            "unit_price, quantity, " +
            "plan_id, plan_name, plan_description, monthly_price, item_amount, create_time) " +
            "VALUES (#{orderId}, #{itemType}, #{productId}, #{skuId}, " +
            "#{unitPrice}, #{quantity}, " +
            "#{planId}, #{planName}, #{planDescription}, #{monthlyPrice}, #{itemAmount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "itemId")
    int insertOrderItem(Map<String, Object> orderItem);
    
    /**
     * 批量插入订单项
     */
    @Insert("<script>" +
            "INSERT INTO afd.order_items (order_id, item_type, product_id, sku_id, " +
            "unit_price, quantity, item_amount, create_time) " +
            "VALUES " +
            "<foreach collection='orderItems' item='item' separator=','>" +
            "(#{item.orderId}, #{item.itemType}, #{item.productId}, #{item.skuId}, " +
            "#{item.unitPrice}, #{item.quantity}, #{item.itemAmount}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsertOrderItems(@Param("orderItems") List<Map<String, Object>> orderItems);
    
    /**
     * 根据订单ID查询订单项
     */
    @Select("SELECT oi.*, " +
            "p.product_name, " +
            "s.sku_name, s.sku_image " +
            "FROM afd.order_items oi " +
            "LEFT JOIN afd.shop_product p ON oi.product_id = p.product_id " +
            "LEFT JOIN afd.shop_product_sku s ON oi.sku_id = s.sku_id " +
            "WHERE oi.order_id = #{orderId}")
    List<Map<String, Object>> selectOrderItemsByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 更新订单项
     */
    @Update("UPDATE afd.order_items SET unit_price = #{unitPrice}, " +
            "quantity = #{quantity}, item_amount = #{itemAmount} " +
            "WHERE item_id = #{itemId}")
    int updateOrderItem(Map<String, Object> orderItem);
    
    /**
     * 删除订单项
     */
    @Delete("DELETE FROM afd.order_items WHERE order_id = #{orderId}")
    int deleteOrderItemsByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据商品信息获取商家ID
     */
    @Select("SELECT merchant_id FROM afd.shop_product WHERE product_id = #{productId}")
    Long getMerchantIdByProductId(@Param("productId") Long productId);
    
    /**
     * 批量删除购物车商品
     */
    @Delete("<script>" +
            "DELETE FROM afd.shop_cart WHERE user_id = #{userId} AND sku_id IN " +
            "<foreach collection='skuIds' item='skuId' open='(' close=')' separator=','>" +
            "#{skuId}" +
            "</foreach>" +
            "</script>")
    int deleteCartItemsBySkuIds(@Param("userId") Long userId, @Param("skuIds") List<Long> skuIds);
    
    /**
     * 统计商家指定状态的订单数量
     */
    @Select("SELECT COUNT(*) FROM afd.orders o " +
            "LEFT JOIN afd.shop_product p ON o.order_type = 1 AND o.related_id = p.merchant_id " +
            "WHERE (o.order_type = 1 AND p.merchant_id = #{merchantId}) " +
            "AND o.order_status = #{status}")
    int countOrdersByMerchantAndStatus(@Param("merchantId") Long merchantId, @Param("status") int status);
    
    /**
     * 获取商家待处理的退款申请
     */
    @Select("SELECT o.* FROM afd.orders o " +
            "LEFT JOIN afd.shop_product p ON o.order_type = 1 AND o.related_id = p.merchant_id " +
            "WHERE (o.order_type = 1 AND p.merchant_id = #{merchantId}) " +
            "AND o.order_status IN (7, 8) " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Order> selectPendingRefundsByMerchant(@Param("merchantId") Long merchantId, 
                                               @Param("offset") int offset, 
                                               @Param("size") int size);
    
    /**
     * 获取商家订单列表（支持筛选）
     */
    @Select("<script>" +
            "SELECT o.*, " +
            "CASE WHEN o.order_type = 1 THEN m.merchant_name ELSE NULL END as merchant_name, " +
            "CASE WHEN o.order_type = 1 THEN m.logo ELSE NULL END as merchant_logo, " +
            "u.username as user_name " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.shop_product p ON o.order_type = 1 AND o.related_id = p.merchant_id " +
            "LEFT JOIN afd.shop_merchant m ON o.order_type = 1 AND o.related_id = m.merchant_id " +
            "LEFT JOIN afd.users u ON o.user_id = u.user_id " +
            "WHERE (o.order_type = 1 AND p.merchant_id = #{merchantId}) " +
            "<if test='status != null'>AND o.order_status = #{status}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.username LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "<if test='timeFilter != null and timeFilter != \"\"'>" +
            "<choose>" +
            "<when test='timeFilter == \"today\"'>AND DATE(o.create_time) = CURDATE()</when>" +
            "<when test='timeFilter == \"yesterday\"'>AND DATE(o.create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)</when>" +
            "<when test='timeFilter == \"week\"'>AND YEARWEEK(o.create_time) = YEARWEEK(NOW())</when>" +
            "<when test='timeFilter == \"month\"'>AND YEAR(o.create_time) = YEAR(NOW()) AND MONTH(o.create_time) = MONTH(NOW())</when>" +
            "</choose>" +
            "</if> " +
            "<if test='refundStatus != null'>" +
            "<choose>" +
            "<when test='refundStatus == 0'>AND o.order_status = 7</when>" +
            "<when test='refundStatus == 1'>AND o.order_status = 8</when>" +
            "<when test='refundStatus == 2'>AND o.order_status = 6</when>" +
            "</choose>" +
            "</if> " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectMerchantOrdersWithFilter(@Param("merchantId") Long merchantId,
                                                             @Param("status") Integer status,
                                                             @Param("keyword") String keyword,
                                                             @Param("timeFilter") String timeFilter,
                                                             @Param("refundStatus") Integer refundStatus,
                                                             @Param("offset") int offset,
                                                             @Param("size") int size);
    
    // ================ 新增的库存管理相关方法 ================
    
    /**
     * 悲观锁查询订单（FOR UPDATE）
     */
    @Select("SELECT * FROM afd.orders WHERE order_no = #{orderNo} FOR UPDATE")
    Order selectByOrderNoForUpdate(@Param("orderNo") String orderNo);
    
    // ================ 新增的统计分析方法 ================
    
    /**
     * 获取今日订单数量
     */
    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_type = 1 AND DATE(create_time) = CURDATE()")
    Integer getTodayOrderCount(@Param("merchantId") Long merchantId);
    
    /**
     * 获取今日销售额
     */
    @Select("SELECT COALESCE(SUM(o.paid_amount), 0) FROM afd.orders o " +
            "LEFT JOIN afd.shop_product p ON o.order_type = 1 AND o.related_id = p.merchant_id " +
            "WHERE (o.order_type = 1 AND p.merchant_id = #{merchantId}) " +
            "AND DATE(o.create_time) = CURDATE() " +
            "AND o.order_status IN (2, 3, 4)")
    BigDecimal getTodaySales(@Param("merchantId") Long merchantId);
    
    /**
     * 获取今日新客户数量
     */
    @Select("SELECT COUNT(DISTINCT o.user_id) FROM afd.orders o " +
            "WHERE o.order_type = 1 AND o.related_id = #{merchantId} " +
            "AND DATE(o.create_time) = CURDATE() " +
            "AND NOT EXISTS (SELECT 1 FROM afd.orders o2 " +
            "WHERE o2.user_id = o.user_id AND o2.related_id = #{merchantId} " +
            "AND DATE(o2.create_time) < CURDATE())")
    Integer getTodayNewCustomers(@Param("merchantId") Long merchantId);
    
    /**
     * 获取指定状态列表的订单数量
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM afd.orders",
        "WHERE related_id = #{merchantId} AND order_type = 1",
        "AND order_status IN",
        "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>",
        "#{status}",
        "</foreach>",
        "</script>"
    })
    Integer getOrderCountByStatuses(@Param("merchantId") Long merchantId, @Param("statuses") List<Integer> statuses);
    
    /**
     * 获取指定状态的订单数量
     */
    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_status = #{status}")
    Integer getOrderCountByStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    
    /**
     * 获取本月销售额
     */
    @Select("SELECT COALESCE(SUM(o.paid_amount), 0) FROM afd.orders o " +
            "WHERE o.order_type = 1 AND o.related_id = #{merchantId} " +
            "AND YEAR(o.create_time) = YEAR(NOW()) " +
            "AND MONTH(o.create_time) = MONTH(NOW()) " +
            "AND o.order_status IN (2, 3, 4)")
    BigDecimal getMonthlySales(@Param("merchantId") Long merchantId);
    
    /**
     * 获取总订单数
     */
    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_type = 1")
    Integer getTotalOrderCount(@Param("merchantId") Long merchantId);
    
    /**
     * 获取超时未发货订单数量（已支付超过24小时未发货）
     */
    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_status = 2 AND update_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    Integer getOverTimeOrderCount(@Param("merchantId") Long merchantId);
    
    /**
     * 获取24小时内需发货订单数量
     */
    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_status = 2 AND update_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    Integer getUrgentShipOrderCount(@Param("merchantId") Long merchantId);
    
    /**
     * 获取超时订单列表
     */
    @Select("SELECT o.*, u.username as user_name FROM afd.orders o " +
            "LEFT JOIN afd.shop_product p ON o.order_type = 1 AND o.related_id = p.merchant_id " +
            "LEFT JOIN afd.users u ON o.user_id = u.user_id " +
            "WHERE (o.order_type = 1 AND p.merchant_id = #{merchantId}) " +
            "AND o.order_status = 2 " +
            "AND o.update_time < DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "ORDER BY o.update_time ASC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getOvertimeOrders(@Param("merchantId") Long merchantId, 
                                                @Param("offset") int offset, 
                                                @Param("size") int size);
    
    /**
     * 获取待处理退款订单列表
     */
    @Select("SELECT o.*, u.username as user_name FROM afd.orders o " +
            "LEFT JOIN afd.shop_product p ON o.order_type = 1 AND o.related_id = p.merchant_id " +
            "LEFT JOIN afd.users u ON o.user_id = u.user_id " +
            "WHERE (o.order_type = 1 AND p.merchant_id = #{merchantId}) " +
            "AND o.order_status = 7 " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getPendingRefundOrders(@Param("merchantId") Long merchantId, 
                                                     @Param("offset") int offset, 
                                                     @Param("size") int size);
    
    /**
     * 根据订单ID查询订单（别名方法）
     */
    default Order selectById(Long orderId) {
        return selectByOrderId(orderId);
    }
    
    /**
     * 更新订单状态（支持订单ID和时间）
     */
    @Update("UPDATE afd.orders SET order_status = #{status}, update_time = #{updateTime} " +
            "WHERE order_id = #{orderId}")
    int updateOrderStatusById(@Param("orderId") Long orderId, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 获取商家订单列表（包含完整商品和用户信息以及物流信息）
     */
    @Select("<script>" +
            "SELECT o.*, " +
            "u.username as user_name, u.avatar as user_avatar, u.phone_number as user_phone, " +
            "m.merchant_name, m.logo as merchant_logo, " +
            "oi.product_id, oi.sku_id, oi.unit_price as sku_price, oi.quantity, " +
            "p.product_name, p.main_image as product_image, " +
            "s.sku_name, s.sku_image, " +
            "sl.shipping_company as logistics_company, sl.tracking_no as tracking_number " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.users u ON o.user_id = u.user_id " +
            "LEFT JOIN afd.shop_merchant m ON o.related_id = m.merchant_id " +
            "LEFT JOIN afd.order_items oi ON o.order_id = oi.order_id " +
            "LEFT JOIN afd.shop_product p ON oi.product_id = p.product_id " +
            "LEFT JOIN afd.shop_product_sku s ON oi.sku_id = s.sku_id " +
            "LEFT JOIN afd.shop_logistics sl ON o.order_id = sl.order_id " +
            "WHERE o.order_type = 1 AND o.related_id = #{merchantId} " +
            "<if test='status != null'>AND o.order_status = #{status}</if> " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if> " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> getMerchantOrders(@Param("merchantId") Long merchantId,
                                                @Param("status") Integer status,
                                                @Param("orderType") Integer orderType,
                                                @Param("offset") int offset,
                                                @Param("size") Integer size);
    
    /**
     * 获取商家订单总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM afd.orders o " +
            "WHERE o.order_type = 1 AND o.related_id = #{merchantId} " +
            "<if test='status != null'>AND o.order_status = #{status}</if> " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if>" +
            "</script>")
    int getMerchantOrderCount(@Param("merchantId") Long merchantId,
                             @Param("status") Integer status,
                             @Param("orderType") Integer orderType);
    
    /**
     * 获取商家订单详情（包含软删除的地址信息）
     */
    @Select("SELECT o.*, " +
            "u.username as user_name, u.phone_number as user_phone, u.avatar as user_avatar, " +
            "m.merchant_name, m.logo as merchant_logo, " +
            "o.related_id as merchant_id, " +
            "addr.receiver_name, addr.receiver_phone, " +
            "uaj.postal_code, uaj.prefecture, uaj.municipality as city, uaj.town, " +
            "uaj.chome, uaj.banchi, uaj.building, uaj.room_number, " +
            "CONCAT(uaj.address_line1, IFNULL(CONCAT(' ', uaj.address_line2), '')) as shipping_address, " +
            "addr.is_deleted as address_deleted " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.users u ON o.user_id = u.user_id " +
            "LEFT JOIN afd.shop_merchant m ON o.related_id = m.merchant_id " +
            "LEFT JOIN afd.user_address addr ON o.address_id = addr.address_id " +
            "LEFT JOIN afd.user_address_japan uaj ON addr.address_id = uaj.address_id " +
            "WHERE o.order_id = #{orderId} AND o.order_type = 1")
    Map<String, Object> getMerchantOrderDetail(@Param("orderId") Long orderId);

    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_status = 7")
    Integer getPendingRefundCount(@Param("merchantId") Long merchantId);

    @Select({
        "SELECT o.* FROM afd.orders o",
        "WHERE o.related_id = #{merchantId} AND o.order_type = 1",
        "<if test='status != null'>AND o.order_status = #{status}</if>",
        "ORDER BY o.create_time DESC",
        "LIMIT #{offset}, #{limit}"
    })
    @Results({
        @Result(column = "order_id", property = "orderId"),
        @Result(column = "order_no", property = "orderNo"),
        @Result(column = "user_id", property = "userId"),
        @Result(column = "order_type", property = "orderType"),
        @Result(column = "order_status", property = "orderStatus"),
        @Result(column = "total_amount", property = "totalAmount"),
        @Result(column = "discount_amount", property = "discountAmount"),
        @Result(column = "shipping_fee", property = "shippingFee"),
        @Result(column = "coupon_amount", property = "couponAmount"),
        @Result(column = "paid_amount", property = "paidAmount"),
        @Result(column = "currency", property = "currency"),
        @Result(column = "address_id", property = "addressId"),
        @Result(column = "address_country", property = "addressCountry"),
        @Result(column = "logistics_info", property = "logisticsInfo"),
        @Result(column = "subscription_months", property = "subscriptionMonths"),
        @Result(column = "subscription_start_time", property = "subscriptionStartTime"),
        @Result(column = "subscription_end_time", property = "subscriptionEndTime"),
        @Result(column = "order_note", property = "orderNote"),
        @Result(column = "create_time", property = "createTime"),
        @Result(column = "update_time", property = "updateTime"),
        @Result(column = "expire_time", property = "expireTime"),
        @Result(column = "related_id", property = "relatedId")
    })
    List<OrderDTO> getMerchantOrdersByStatus(@Param("merchantId") Long merchantId, 
                                           @Param("status") Integer status,
                                           @Param("offset") int offset, 
                                           @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM afd.orders WHERE related_id = #{merchantId} AND order_type = 1 " +
            "<if test='status != null'>AND order_status = #{status}</if>")
    Integer getMerchantOrdersCountByStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    
    /**
     * 根据用户ID查询商家信息
     */
    @Select("SELECT merchant_id, merchant_name FROM afd.shop_merchant WHERE user_id = #{userId}")
    Map<String, Object> getMerchantByUserId(@Param("userId") Long userId);
    
    /**
     * 插入物流记录到shop_logistics表
     */
    @Insert("INSERT INTO afd.shop_logistics (order_id, order_no, shipping_company, " +
            "tracking_no, delivery_status, delivery_time, create_time, update_time) " +
            "VALUES (#{orderId}, #{orderNo}, #{shippingCompany}, #{trackingNo}, " +
            "#{deliveryStatus}, #{deliveryTime}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "logisticsId")
    int insertLogistics(Map<String, Object> logistics);
    
    /**
     * 更新订单状态和物流信息
     */
    @Update("UPDATE afd.orders SET order_status = #{status}, logistics_info = #{logisticsInfo}, " +
            "update_time = #{updateTime} WHERE order_id = #{orderId}")
    int updateOrderLogisticsInfo(@Param("orderId") Long orderId, 
                                @Param("status") Integer status,
                                @Param("logisticsInfo") String logisticsInfo, 
                                @Param("updateTime") LocalDateTime updateTime);
    
    /**
     * 根据订单ID查询物流信息
     */
    @Select("SELECT * FROM afd.shop_logistics WHERE order_id = #{orderId} ORDER BY create_time DESC LIMIT 1")
    Map<String, Object> getLogisticsByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 插入测试商家数据
     */
    @Insert("INSERT INTO afd.shop_merchant (user_id, merchant_name, logo, description, contact_name, contact_phone, status, create_time, update_time) " +
            "VALUES (#{user_id}, #{merchant_name}, #{logo}, #{description}, #{contact_name}, #{contact_phone}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "merchant_id")
    int insertTestMerchant(Map<String, Object> merchant);
    
    /**
     * 插入测试订单数据
     */
    @Insert("INSERT INTO afd.orders (order_no, user_id, order_type, order_status, total_amount, merchant_id, " +
            "receiver_name, receiver_phone, shipping_address, order_note, create_time, update_time) " +
            "VALUES (#{order_no}, #{user_id}, #{order_type}, #{order_status}, #{total_amount}, #{merchant_id}, " +
            "#{receiver_name}, #{receiver_phone}, #{shipping_address}, #{order_note}, #{create_time}, #{update_time})")
    @Options(useGeneratedKeys = true, keyProperty = "order_id")
    int insertTestOrder(Map<String, Object> order);

    /**
     * 获取商家订单列表（支持多状态查询）
     */
    @Select("<script>" +
            "SELECT o.*, " +
            "u.username as user_name, u.avatar as user_avatar, u.phone_number as user_phone, " +
            "m.merchant_name, m.logo as merchant_logo, " +
            "oi.product_id, oi.sku_id, oi.unit_price as sku_price, oi.quantity, " +
            "p.product_name, p.main_image as product_image, " +
            "s.sku_name, s.sku_image, " +
            "sl.shipping_company as logistics_company, sl.tracking_no as tracking_number " +
            "FROM afd.orders o " +
            "LEFT JOIN afd.users u ON o.user_id = u.user_id " +
            "LEFT JOIN afd.shop_merchant m ON o.related_id = m.merchant_id " +
            "LEFT JOIN afd.order_items oi ON o.order_id = oi.order_id " +
            "LEFT JOIN afd.shop_product p ON oi.product_id = p.product_id " +
            "LEFT JOIN afd.shop_product_sku s ON oi.sku_id = s.sku_id " +
            "LEFT JOIN afd.shop_logistics sl ON o.order_id = sl.order_id " +
            "WHERE o.order_type = 1 AND o.related_id = #{merchantId} " +
            "<if test='statuses != null and statuses.size() > 0'>" +
            "AND o.order_status IN " +
            "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>" +
            "#{status}" +
            "</foreach> " +
            "</if> " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if> " +
            "ORDER BY o.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> getMerchantOrdersByStatuses(@Param("merchantId") Long merchantId,
                                                          @Param("statuses") List<Integer> statuses,
                                                          @Param("orderType") Integer orderType,
                                                          @Param("offset") int offset,
                                                          @Param("size") Integer size);

    /**
     * 获取商家订单总数（支持多状态查询）
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM afd.orders o " +
            "WHERE o.order_type = 1 AND o.related_id = #{merchantId} " +
            "<if test='statuses != null and statuses.size() > 0'>" +
            "AND o.order_status IN " +
            "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>" +
            "#{status}" +
            "</foreach> " +
            "</if> " +
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if>" +
            "</script>")
    int getMerchantOrderCountByStatuses(@Param("merchantId") Long merchantId,
                                       @Param("statuses") List<Integer> statuses,
                                       @Param("orderType") Integer orderType);
} 