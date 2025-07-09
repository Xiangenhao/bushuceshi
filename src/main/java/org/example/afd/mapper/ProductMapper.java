package org.example.afd.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import org.example.afd.dto.ProductDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper {
    
    /**
     * 根据条件查询商品列表
     * @param params 查询条件
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 商品列表
     */
    @Select("<script>" +
            "SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.status = 1 AND p.is_deleted = 0 " +
            "<if test='params.categoryId != null'> AND p.category_id = #{params.categoryId} </if>" +
            "<if test='params.merchantId != null'> AND p.merchant_id = #{params.merchantId} </if>" +
            "<if test='params.keyword != null and params.keyword != \"\"'> " +
            "AND (p.product_name LIKE CONCAT('%', #{params.keyword}, '%') OR p.keywords LIKE CONCAT('%', #{params.keyword}, '%')) " +
            "</if>" +
            "<if test='params.minPrice != null'> AND p.price >= #{params.minPrice} </if>" +
            "<if test='params.maxPrice != null'> AND p.price &lt;= #{params.maxPrice} </if>" +
            "<if test='params.isHot != null'> AND p.is_hot = #{params.isHot} </if>" +
            "<if test='params.isRecommend != null'> AND p.is_recommend = #{params.isRecommend} </if>" +
            "<if test='params.isNew != null'> AND p.is_new = #{params.isNew} </if>" +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> selectProducts(@Param("params") Map<String, Object> params, 
                                           @Param("offset") int offset, 
                                           @Param("limit") int limit);
    
    /**
     * 根据条件查询商品总数
     * @param params 查询条件
     * @return 商品总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM shop_product p " +
            "WHERE p.status = 1 AND p.is_deleted = 0 " +
            "<if test='params.categoryId != null'> AND p.category_id = #{params.categoryId} </if>" +
            "<if test='params.merchantId != null'> AND p.merchant_id = #{params.merchantId} </if>" +
            "<if test='params.keyword != null and params.keyword != \"\"'> " +
            "AND (p.product_name LIKE CONCAT('%', #{params.keyword}, '%') OR p.keywords LIKE CONCAT('%', #{params.keyword}, '%')) " +
            "</if>" +
            "<if test='params.minPrice != null'> AND p.price >= #{params.minPrice} </if>" +
            "<if test='params.maxPrice != null'> AND p.price &lt;= #{params.maxPrice} </if>" +
            "<if test='params.isHot != null'> AND p.is_hot = #{params.isHot} </if>" +
            "<if test='params.isRecommend != null'> AND p.is_recommend = #{params.isRecommend} </if>" +
            "<if test='params.isNew != null'> AND p.is_new = #{params.isNew} </if>" +
            "</script>")
    int countProducts(@Param("params") Map<String, Object> params);
    
    /**
     * 根据ID查询商品详情
     * @param productId 商品ID
     * @return 商品详情
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.product_id = #{productId} AND p.is_deleted = 0")
    Map<String, Object> selectProductById(@Param("productId") Long productId);
    
    /**
     * 查询商品的SKU列表
     * @param productId 商品ID
     * @return SKU列表
     */
    @Select("SELECT * FROM shop_product_sku WHERE product_id = #{productId}")
    List<Map<String, Object>> selectSkusByProductId(@Param("productId") Long productId);
    
    /**
     * 查询商品的评论列表
     * @param productId 商品ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 评论列表
     */
    @Select("SELECT r.*, u.username as user_nickname, u.avatar as user_avatar " +
            "FROM shop_product_review r " +
            "LEFT JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.product_id = #{productId} " +
            "ORDER BY r.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectProductReviews(@Param("productId") Long productId, 
                                                 @Param("offset") int offset, 
                                                 @Param("limit") int limit);
    
    /**
     * 查询商品的评论总数
     * @param productId 商品ID
     * @return 评论总数
     */
    @Select("SELECT COUNT(*) FROM shop_product_review WHERE product_id = #{productId}")
    int countProductReviews(@Param("productId") Long productId);
    
    /**
     * 查询商品的平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    @Select("SELECT AVG(rating) FROM shop_product_review WHERE product_id = #{productId}")
    Double selectProductRating(@Param("productId") Long productId);
    
    /**
     * 查询热门商品列表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 热门商品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.status = 1 AND p.is_hot = 1 AND p.is_deleted = 0 " +
            "ORDER BY p.sales DESC, p.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectHotProducts(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计热门商品数量
     * @return 热门商品数量
     */
    @Select("SELECT COUNT(*) FROM shop_product WHERE status = 1 AND is_hot = 1 AND is_deleted = 0")
    int countHotProducts();
    
    /**
     * 查询新品列表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 新品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.status = 1 AND p.is_new = 1 AND p.is_deleted = 0 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectNewProducts(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计新品数量
     * @return 新品数量
     */
    @Select("SELECT COUNT(*) FROM shop_product WHERE status = 1 AND is_new = 1 AND is_deleted = 0")
    int countNewProducts();
    
    /**
     * 查询推荐商品列表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 推荐商品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.status = 1 AND p.is_recommend = 1 AND p.is_deleted = 0 " +
            "ORDER BY p.sales DESC, p.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectRecommendProducts(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计推荐商品数量
     * @return 推荐商品数量
     */
    @Select("SELECT COUNT(*) FROM shop_product WHERE status = 1 AND is_recommend = 1 AND is_deleted = 0")
    int countRecommendProducts();
    
    /**
     * 查询促销商品列表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 促销商品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.status = 1 AND p.is_hot = 1 AND p.is_deleted = 0 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectPromotionProducts(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计促销商品数量
     * @return 促销商品数量
     */
    @Select("SELECT COUNT(*) FROM shop_product WHERE status = 1 AND is_hot = 1 AND is_deleted = 0")
    int countPromotionProducts();
    
    /**
     * 查询相似商品列表
     * @param productId 商品ID
     * @param categoryId 分类ID
     * @param limit 数量限制
     * @return 相似商品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.status = 1 AND p.category_id = #{categoryId} AND p.product_id != #{productId} AND p.is_deleted = 0 " +
            "ORDER BY p.sales DESC, p.create_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectSimilarProducts(@Param("productId") Long productId, 
                                                  @Param("categoryId") Long categoryId, 
                                                  @Param("limit") int limit);
    
    /**
     * 更新商品销量
     * @param productId 商品ID
     * @param quantity 销量增量
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET sales = sales + #{quantity}, update_time = NOW() WHERE product_id = #{productId}")
    int updateProductSales(@Param("productId") Long productId, @Param("quantity") int quantity);
    
    /**
     * 增加商品库存
     * @param productId 商品ID
     * @param quantity 库存增量
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET stock = stock + #{quantity}, update_time = NOW() WHERE product_id = #{productId}")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 减少商品库存
     * @param productId 商品ID
     * @param quantity 库存减量
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET stock = stock - #{quantity}, update_time = NOW() WHERE product_id = #{productId} AND stock >= #{quantity}")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 增加商品浏览量
     * @param productId 商品ID
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET update_time = NOW() WHERE product_id = #{productId}")
    int incrementViewCount(@Param("productId") Long productId);
    
    /**
     * 获取商家商品列表
     * @param merchantId 商家ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 商品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.merchant_id = #{merchantId} AND p.is_deleted = 0 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ProductDTO> selectMerchantProducts(@Param("merchantId") Long merchantId, 
                                         @Param("offset") int offset, 
                                         @Param("limit") int limit);
    
    /**
     * 统计商家商品总数
     * @param merchantId 商家ID
     * @return 商品总数
     */
    @Select("SELECT COUNT(*) FROM shop_product WHERE merchant_id = #{merchantId} AND is_deleted = 0")
    int countMerchantProducts(@Param("merchantId") Long merchantId);
    
    /**
     * 获取商家分类下的商品列表
     * @param merchantId 商家ID
     * @param categoryId 分类ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 商品列表
     */
    @Select("SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.merchant_id = #{merchantId} AND p.category_id = #{categoryId} AND p.is_deleted = 0 " +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<ProductDTO> selectMerchantProductsByCategory(@Param("merchantId") Long merchantId, 
                                                   @Param("categoryId") Long categoryId, 
                                                   @Param("offset") int offset, 
                                                   @Param("limit") int limit);
    
    /**
     * 统计商家分类下的商品总数
     * @param merchantId 商家ID
     * @param categoryId 分类ID
     * @return 商品总数
     */
    @Select("SELECT COUNT(*) FROM shop_product WHERE merchant_id = #{merchantId} AND category_id = #{categoryId} AND is_deleted = 0")
    int countMerchantProductsByCategory(@Param("merchantId") Long merchantId, @Param("categoryId") Long categoryId);
    
    /**
     * 添加商品
     * @param product 商品信息
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_product(merchant_id, category_id, product_name, product_brief, " +
            "main_image, detail, product_explain, price, stock, unit, keywords, is_hot, is_recommend, is_new, " +
            "status, product_type, create_time, update_time) " +
            "VALUES(#{merchantId}, #{categoryId}, #{productName}, #{productBrief}, #{mainImage}, " +
            "#{detail}, #{productExplain}, #{price}, #{stock}, #{unit}, #{keywords}, #{isHot}, #{isRecommend}, " +
            "#{isNew}, #{status}, #{productType}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    int insertProduct(ProductDTO product);
    
    /**
     * 更新商品信息
     * @param product 商品信息
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "category_id = #{categoryId}, " +
            "product_name = #{productName}, " +
            "product_brief = #{productBrief}, " +
            "main_image = #{mainImage}, " +
            "detail = #{detail}, " +
            "price = #{price}, " +
            "stock = #{stock}, " +
            "unit = #{unit}, " +
            "keywords = #{keywords}, " +
            "is_hot = #{isHot}, " +
            "is_recommend = #{isRecommend}, " +
            "is_new = #{isNew}, " +
            "product_type = #{productType}, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId} AND merchant_id = #{merchantId}")
    int updateProduct(ProductDTO product);
    
    /**
     * 更新商品状态
     * @param productId 商品ID
     * @param status 状态，1-上架，0-下架
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "status = #{status}, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int updateProductStatus(@Param("productId") Long productId, @Param("status") Integer status);
    
    /**
     * 删除商品（软删除）
     * @param productId 商品ID
     * @param merchantId 商家ID（用于验证权限）
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "is_deleted = 1, " + // 软删除：设置删除标记
            "update_time = NOW() " +
            "WHERE product_id = #{productId} AND merchant_id = #{merchantId} AND is_deleted = 0")
    int deleteProduct(@Param("productId") Long productId, @Param("merchantId") Long merchantId);
    
    /**
     * 获取商品销售排行
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 商品销售排行
     */
    @Select("SELECT p.product_id, p.product_name, p.main_image, p.price, SUM(oi.quantity) AS sales_count, " +
            "SUM(oi.total_price) AS sales_amount " +
            "FROM shop_product p " +
            "JOIN shop_order_item oi ON p.product_id = oi.product_id " +
            "JOIN shop_order o ON oi.order_id = o.order_id " +
            "WHERE p.merchant_id = #{merchantId} AND o.order_status >= 1 AND p.is_deleted = 0 " +
            "GROUP BY p.product_id " +
            "ORDER BY sales_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getProductSalesRanking(@Param("merchantId") Long merchantId, @Param("limit") Integer limit);
    
    /**
     * 获取商家商品列表（按状态筛选）
     * @param merchantId 商家ID
     * @param status 商品状态，1-上架，0-下架，null-全部
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 商品列表
     */
    @Select("<script>" +
            "SELECT p.*, c.category_name FROM shop_product p " +
            "LEFT JOIN shop_category c ON p.category_id = c.category_id " +
            "WHERE p.merchant_id = #{merchantId} AND p.is_deleted = 0 " +
            "<if test='status != null'> AND p.status = #{status} </if>" +
            "ORDER BY p.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<ProductDTO> selectMerchantProductsByStatus(@Param("merchantId") Long merchantId,
                                                   @Param("status") Integer status,
                                                   @Param("offset") int offset, 
                                                   @Param("limit") int limit);
    
    /**
     * 统计商家商品总数（按状态筛选）
     * @param merchantId 商家ID
     * @param status 商品状态，1-上架，0-下架，null-全部
     * @return 商品总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM shop_product " +
            "WHERE merchant_id = #{merchantId} AND is_deleted = 0 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "</script>")
    int countMerchantProductsByStatus(@Param("merchantId") Long merchantId, @Param("status") Integer status);
    
    /**
     * 更新商品基本信息
     * @param productId 商品ID
     * @param productName 商品名称
     * @param productBrief 商品简介
     * @param price 商品价格
     * @param stock 库存
     * @param unit 单位
     * @param keywords 关键词
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "product_name = #{productName}, " +
            "product_brief = #{productBrief}, " +
            "price = #{price}, " +
            "stock = #{stock}, " +
            "unit = #{unit}, " +
            "keywords = #{keywords}, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int updateProductBasicInfo(@Param("productId") Long productId,
                              @Param("productName") String productName,
                              @Param("productBrief") String productBrief,
                              @Param("price") Double price,
                              @Param("stock") Integer stock,
                              @Param("unit") String unit,
                              @Param("keywords") String keywords);
    
    /**
     * 更新商品主图
     * @param productId 商品ID
     * @param mainImage 主图URL
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "main_image = #{mainImage}, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int updateProductMainImage(@Param("productId") Long productId, @Param("mainImage") String mainImage);
    
    /**
     * 更新商品详情
     * @param productId 商品ID
     * @param detail 商品详情
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "detail = #{detail}, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int updateProductDetail(@Param("productId") Long productId, @Param("detail") String detail);
    
    /**
     * 更新商品分类
     * @param productId 商品ID
     * @param categoryId 分类ID
     * @return 影响行数
     */
    @Update("UPDATE shop_product SET " +
            "category_id = #{categoryId}, " +
            "update_time = NOW() " +
            "WHERE product_id = #{productId}")
    int updateProductCategory(@Param("productId") Long productId, @Param("categoryId") Long categoryId);
    
    // ================ 新增的库存管理相关方法 ================
    
    /**
     * 悲观锁查询SKU（FOR UPDATE）
     */
    @Select("SELECT * FROM shop_product_sku WHERE sku_id = #{skuId} FOR UPDATE")
    Map<String, Object> selectSkuForUpdate(@Param("skuId") Long skuId);
    
    /**
     * 乐观锁锁定库存
     */
    @Update("UPDATE shop_product_sku SET " +
            "lock_stock = lock_stock + #{quantity}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE sku_id = #{skuId} " +
            "AND version = #{version} " +
            "AND (stock - lock_stock) >= #{quantity}")
    int lockStockWithVersion(@Param("skuId") Long skuId, 
                           @Param("quantity") Integer quantity,
                           @Param("version") Integer version);
    
    /**
     * 确认扣减库存（将锁定库存转为实际扣减）
     */
    @Update("UPDATE shop_product_sku SET " +
            "stock = stock - #{quantity}, " +
            "lock_stock = lock_stock - #{quantity}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE sku_id = #{skuId} AND lock_stock >= #{quantity}")
    int confirmStockDeduction(@Param("skuId") Long skuId, 
                            @Param("quantity") Integer quantity);
    
    /**
     * 释放锁定库存
     */
    @Update("UPDATE shop_product_sku SET " +
            "lock_stock = lock_stock - #{quantity}, " +
            "version = version + 1, " +
            "update_time = NOW() " +
            "WHERE sku_id = #{skuId} AND lock_stock >= #{quantity}")
    int releaseStockLock(@Param("skuId") Long skuId, 
                       @Param("quantity") Integer quantity);
    
    /**
     * 更新库存（测试用）
     */
    @Update("UPDATE shop_product_sku SET " +
            "stock = #{stock}, " +
            "update_time = NOW() " +
            "WHERE sku_id = #{skuId}")
    int updateStock(@Param("skuId") Long skuId, @Param("stock") Integer stock);
    
    /**
     * 根据ID查询SKU详情
     */
    @Select("SELECT * FROM shop_product_sku WHERE sku_id = #{skuId}")
    Map<String, Object> selectSkuById(@Param("skuId") Long skuId);
}
