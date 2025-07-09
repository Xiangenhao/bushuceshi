package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.StockOperationLog;

import java.util.List;

/**
 * 库存操作日志Mapper
 */
@Mapper
public interface StockOperationLogMapper {
    
    /**
     * 插入库存操作日志
     */
    @Insert("INSERT INTO afd.stock_operation_log (sku_id, operation_type, quantity, " +
            "before_stock, after_stock, before_lock_stock, after_lock_stock, " +
            "order_no, operator_id, create_time) " +
            "VALUES (#{skuId}, #{operationType}, #{quantity}, #{beforeStock}, #{afterStock}, " +
            "#{beforeLockStock}, #{afterLockStock}, #{orderNo}, #{operatorId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "logId")
    int insertLog(StockOperationLog log);
    
    /**
     * 根据订单号和操作类型查询日志
     */
    @Select("SELECT * FROM afd.stock_operation_log " +
            "WHERE order_no = #{orderNo} AND operation_type = #{operationType} " +
            "ORDER BY create_time ASC")
    List<StockOperationLog> selectByOrderNoAndType(@Param("orderNo") String orderNo, 
                                                  @Param("operationType") Integer operationType);
    
    /**
     * 根据SKU ID查询最近的操作日志
     */
    @Select("SELECT * FROM afd.stock_operation_log " +
            "WHERE sku_id = #{skuId} " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<StockOperationLog> selectRecentBySkuId(@Param("skuId") Long skuId, 
                                              @Param("limit") Integer limit);
    
    /**
     * 根据订单号查询所有操作日志
     */
    @Select("SELECT * FROM afd.stock_operation_log " +
            "WHERE order_no = #{orderNo} " +
            "ORDER BY create_time ASC")
    List<StockOperationLog> selectByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 统计某个SKU的操作次数
     */
    @Select("SELECT COUNT(*) FROM afd.stock_operation_log " +
            "WHERE sku_id = #{skuId} AND operation_type = #{operationType}")
    int countBySkuIdAndType(@Param("skuId") Long skuId, 
                          @Param("operationType") Integer operationType);

    /**
     * 根据条件查询操作日志（分页）
     * 支持orderNo、skuId、operationType多条件查询
     */
    @Select("<script>" +
            "SELECT * FROM afd.stock_operation_log WHERE 1=1 " +
            "<if test='orderNo != null and orderNo != \"\"'>" +
            "AND order_no = #{orderNo} " +
            "</if>" +
            "<if test='skuId != null'>" +
            "AND sku_id = #{skuId} " +
            "</if>" +
            "<if test='operationType != null'>" +
            "AND operation_type = #{operationType} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<StockOperationLog> selectByConditions(@Param("orderNo") String orderNo,
                                             @Param("skuId") Long skuId,
                                             @Param("operationType") Integer operationType,
                                             @Param("offset") Integer offset,
                                             @Param("size") Integer size);
} 