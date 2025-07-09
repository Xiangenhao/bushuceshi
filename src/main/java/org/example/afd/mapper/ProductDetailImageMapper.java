package org.example.afd.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;

/**
 * 商品详情图片数据访问接口
 */
@Mapper
public interface ProductDetailImageMapper {
    
    /**
     * 插入商品详情图片
     * @param productId 商品ID
     * @param imageUrl 图片URL
     * @param sortOrder 排序顺序
     * @param imageType 图片类型(1=主图,2=详情图,3=规格图)
     * @param description 图片描述
     * @param width 图片宽度
     * @param height 图片高度
     * @param isThumbnail 是否为缩略图
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_product_detail_images(product_id, image_url, sort_order, image_type, " +
            "description, width, height, is_thumbnail, create_time) " +
            "VALUES(#{productId}, #{imageUrl}, #{sortOrder}, #{imageType}, #{description}, " +
            "#{width}, #{height}, #{isThumbnail}, NOW())")
    int insertProductDetailImage(@Param("productId") Long productId,
                                @Param("imageUrl") String imageUrl,
                                @Param("sortOrder") Integer sortOrder,
                                @Param("imageType") Integer imageType,
                                @Param("description") String description,
                                @Param("width") Integer width,
                                @Param("height") Integer height,
                                @Param("isThumbnail") Boolean isThumbnail);
    
    /**
     * 批量插入商品详情图片
     * @param detailImages 详情图片列表
     * @return 影响行数
     */
    @Insert("<script>" +
            "INSERT INTO shop_product_detail_images(product_id, image_url, sort_order, image_type, " +
            "description, width, height, is_thumbnail, create_time) VALUES " +
            "<foreach collection='detailImages' item='image' separator=','>" +
            "(#{image.productId}, #{image.imageUrl}, #{image.sortOrder}, #{image.imageType}, " +
            "#{image.description}, #{image.width}, #{image.height}, #{image.isThumbnail}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsertProductDetailImages(@Param("detailImages") List<Map<String, Object>> detailImages);
    
    /**
     * 根据商品ID查询详情图片
     * @param productId 商品ID
     * @return 详情图片列表
     */
    @Select("SELECT * FROM shop_product_detail_images WHERE product_id = #{productId} ORDER BY sort_order")
    List<Map<String, Object>> selectDetailImagesByProductId(@Param("productId") Long productId);
    
    /**
     * 根据商品ID和图片类型查询图片
     * @param productId 商品ID
     * @param imageType 图片类型(1=主图,2=详情图,3=规格图)
     * @return 图片列表
     */
    @Select("SELECT * FROM shop_product_detail_images WHERE product_id = #{productId} " +
            "AND image_type = #{imageType} ORDER BY sort_order")
    List<Map<String, Object>> selectDetailImagesByProductIdAndType(@Param("productId") Long productId,
                                                                  @Param("imageType") Integer imageType);
    
    /**
     * 删除商品的所有详情图片
     * @param productId 商品ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_product_detail_images WHERE product_id = #{productId}")
    int deleteDetailImagesByProductId(@Param("productId") Long productId);
    
    /**
     * 根据图片ID删除图片
     * @param imageId 图片ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_product_detail_images WHERE image_id = #{imageId}")
    int deleteDetailImageById(@Param("imageId") Long imageId);
} 