package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 物流信息数据访问接口
 */
@Mapper
public interface LogisticsMapper {
    
    /**
     * 根据订单ID获取物流信息
     */
    @Select("SELECT * FROM shop_logistics WHERE order_id = #{orderId}")
    Map<String, Object> getLogisticsByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据订单编号获取物流信息
     */
    @Select("SELECT * FROM shop_logistics WHERE order_no = #{orderNo}")
    Map<String, Object> getLogisticsByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 更新物流信息
     */
    @Update({
        "<script>",
        "UPDATE shop_logistics",
        "<set>",
        "  <if test='shipping_company != null'>shipping_company = #{shipping_company},</if>",
        "  <if test='tracking_no != null'>tracking_no = #{tracking_no},</if>",
        "  <if test='delivery_status != null'>delivery_status = #{delivery_status},</if>",
        "  <if test='delivery_time != null'>delivery_time = #{delivery_time},</if>",
        "  <if test='receive_time != null'>receive_time = #{receive_time},</if>",
        "  <if test='tracking_details != null'>tracking_details = #{tracking_details},</if>",
        "  update_time = NOW()",
        "</set>",
        "WHERE logistics_id = #{logistics_id}",
        "</script>"
    })
    int updateLogistics(Map<String, Object> logistics);
    
    /**
     * 插入物流信息
     */
    @Insert({
        "INSERT INTO shop_logistics",
        "(order_id, order_no, shipping_company, tracking_no, delivery_status, delivery_time, tracking_details)",
        "VALUES",
        "(#{order_id}, #{order_no}, #{shipping_company}, #{tracking_no}, #{delivery_status}, #{delivery_time}, #{tracking_details})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "logistics_id", keyColumn = "logistics_id")
    int insertLogistics(Map<String, Object> logistics);
} 