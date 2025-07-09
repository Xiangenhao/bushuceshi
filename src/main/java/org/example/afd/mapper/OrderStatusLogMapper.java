package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.OrderStatusLog;

import java.util.List;

/**
 * 订单状态变更日志Mapper
 */
@Mapper
public interface OrderStatusLogMapper {
    
    /**
     * 插入状态变更日志
     */
    @Insert("INSERT INTO afd.order_status_log (order_no, from_status, to_status, " +
            "reason, operator_id, create_time) " +
            "VALUES (#{orderNo}, #{fromStatus}, #{toStatus}, #{reason}, #{operatorId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "logId")
    int insertLog(OrderStatusLog log);
    
    /**
     * 根据订单号查询状态变更历史
     */
    @Select("SELECT * FROM afd.order_status_log " +
            "WHERE order_no = #{orderNo} " +
            "ORDER BY create_time ASC")
    List<OrderStatusLog> selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 查询最近的状态变更记录
     */
    @Select("SELECT * FROM afd.order_status_log " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<OrderStatusLog> selectRecent(@Param("limit") Integer limit);
    
    /**
     * 根据操作员ID查询操作记录
     */
    @Select("SELECT * FROM afd.order_status_log " +
            "WHERE operator_id = #{operatorId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<OrderStatusLog> selectByOperatorId(@Param("operatorId") Long operatorId, 
                                          @Param("limit") Integer limit);
    
    /**
     * 统计某个状态转换的次数
     */
    @Select("SELECT COUNT(*) FROM afd.order_status_log " +
            "WHERE from_status = #{fromStatus} AND to_status = #{toStatus}")
    int countByStatusTransition(@Param("fromStatus") Integer fromStatus, 
                              @Param("toStatus") Integer toStatus);
} 