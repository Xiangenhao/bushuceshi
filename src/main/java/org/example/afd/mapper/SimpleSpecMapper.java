package org.example.afd.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;

/**
 * 简化商品规格数据访问接口
 * 使用shop_product_sku表存储简化规格信息
 */
@Mapper
public interface SimpleSpecMapper {
    
    /**
     * 插入简化规格（使用SKU表）
     * @param productId 商品ID
     * @param specName 规格名称
     * @param skuImage 规格图片
     * @param price 规格价格
     * @param stock 规格库存
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_product_sku(product_id, sku_name, sku_image, price, stock, " +
            "lock_stock, status, create_time, update_time) " +
            "VALUES(#{productId}, #{specName}, #{skuImage}, #{price}, #{stock}, " +
            "0, 1, NOW(), NOW())")
    int insertSimpleSpec(@Param("productId") Long productId,
                        @Param("specName") String specName,
                        @Param("skuImage") String skuImage,
                        @Param("price") Double price,
                        @Param("stock") Integer stock);
    
    /**
     * 批量插入简化规格
     * @param simpleSpecs 规格列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO shop_product_sku(product_id, sku_name, sku_image, price, stock, " +
            "lock_stock, status, create_time, update_time) VALUES " +
            "<foreach collection='simpleSpecs' item='spec' separator=','>" +
            "(#{spec.productId}, #{spec.specName}, #{spec.imageUrl}, #{spec.price}, #{spec.stock}, " +
            "0, 1, NOW(), NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsertSimpleSpecs(@Param("simpleSpecs") List<Map<String, Object>> simpleSpecs);
    
    /**
     * 根据商品ID查询简化规格
     * @param productId 商品ID
     * @return 规格列表
     */
    @Select("SELECT sku_id as specId, product_id as productId, sku_name as specName, " +
            "sku_image as imageUrl, price, stock " +
            "FROM shop_product_sku WHERE product_id = #{productId} AND status = 1")
    List<Map<String, Object>> selectSimpleSpecsByProductId(@Param("productId") Long productId);
    
    /**
     * 根据商品ID删除所有规格
     * @param productId 商品ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_product_sku WHERE product_id = #{productId}")
    int deleteSimpleSpecsByProductId(@Param("productId") Long productId);
    
    /**
     * 根据规格ID删除规格
     * @param specId 规格ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_product_sku WHERE sku_id = #{specId}")
    int deleteSimpleSpecById(@Param("specId") Long specId);
    
    /**
     * 更新规格库存
     * @param specId 规格ID
     * @param stock 库存数量
     * @return 影响行数
     */
    @Select("UPDATE shop_product_sku SET stock = #{stock}, update_time = NOW() " +
            "WHERE sku_id = #{specId}")
    int updateSpecStock(@Param("specId") Long specId, @Param("stock") Integer stock);
} 