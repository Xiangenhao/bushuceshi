package org.example.afd.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;

/**
 * 商品图片数据访问接口
 */
@Mapper
public interface ProductImageMapper {
    
    /**
     * 插入商品图片
     * @param productId 商品ID
     * @param imageUrl 图片URL
     * @param sortOrder 排序顺序
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_product_image(product_id, image_url, sort_order, create_time) " +
            "VALUES(#{productId}, #{imageUrl}, #{sortOrder}, NOW())")
    int insertProductImage(@Param("productId") Long productId, 
                          @Param("imageUrl") String imageUrl, 
                          @Param("sortOrder") Integer sortOrder);
    
    /**
     * 批量插入商品图片
     * @param productImages 商品图片列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO shop_product_image(product_id, image_url, sort_order, create_time) VALUES " +
            "<foreach collection='productImages' item='image' separator=','>" +
            "(#{image.productId}, #{image.imageUrl}, #{image.sortOrder}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsertProductImages(@Param("productImages") List<Map<String, Object>> productImages);
    
    /**
     * 根据商品ID查询商品图片
     * @param productId 商品ID
     * @return 商品图片列表
     */
    @Select("SELECT * FROM shop_product_image WHERE product_id = #{productId} ORDER BY sort_order")
    List<Map<String, Object>> selectProductImagesByProductId(@Param("productId") Long productId);
    
    /**
     * 删除商品的所有图片
     * @param productId 商品ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_product_image WHERE product_id = #{productId}")
    int deleteProductImagesByProductId(@Param("productId") Long productId);
    
    /**
     * 根据图片ID删除图片
     * @param imageId 图片ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_product_image WHERE image_id = #{imageId}")
    int deleteProductImageById(@Param("imageId") Long imageId);
} 