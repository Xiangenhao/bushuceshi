package org.example.afd.service;

import org.example.afd.dto.ProductDTO;
import org.example.afd.dto.ProductDetailDTO;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 获取商品列表
     * @param page 页码
     * @param size 每页数量
     * @param params 查询参数
     * @return 商品列表分页数据
     */
    Map<String, Object> getProducts(int page, int size, Map<String, Object> params);
    
    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductDetailDTO getProductDetail(Long productId);
    
    /**
     * 根据分类获取商品列表
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表分页数据
     */
    Map<String, Object> getProductsByCategory(Long categoryId, int page, int size);
    
    /**
     * 根据关键字搜索商品
     * @param keyword 关键字
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表分页数据
     */
    Map<String, Object> searchProducts(String keyword, int page, int size);
    
    /**
     * 获取热门商品列表
     * @param page 页码
     * @param size 每页数量
     * @return 热门商品列表
     */
    Map<String, Object> getHotProducts(int page, int size);
    
    /**
     * 获取新品列表
     * @param page 页码
     * @param size 每页数量
     * @return 新品列表
     */
    Map<String, Object> getNewProducts(int page, int size);
    
    /**
     * 获取推荐商品列表
     * @param page 页码
     * @param size 每页数量
     * @return 推荐商品列表
     */
    Map<String, Object> getRecommendProducts(int page, int size);
    
    /**
     * 获取促销商品列表
     * @param page 页码
     * @param size 每页数量
     * @return 促销商品列表
     */
    Map<String, Object> getPromotionProducts(int page, int size);
    
    /**
     * 获取商品相似推荐
     * @param productId 商品ID
     * @param limit 数量限制
     * @return 相似商品列表
     */
    List<ProductDTO> getSimilarProducts(Long productId, int limit);
    
    /**
     * 更新商品销量
     * @param productId 商品ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean updateProductSales(Long productId, int quantity);
    
    /**
     * 减少商品库存
     * @param productId 商品ID
     * @param quantity 减少的数量
     * @return 是否成功
     */
    boolean decreaseStock(Long productId, int quantity);
    
    /**
     * 增加商品浏览量
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean incrementViewCount(Long productId);
    
    /**
     * 获取商家的商品列表
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    List<ProductDTO> getMerchantProducts(Long merchantId, int page, int size);
    
    /**
     * 按分类获取商家的商品列表
     * @param merchantId 商家ID
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    List<ProductDTO> getMerchantProductsByCategory(Long merchantId, Long categoryId, int page, int size);
    
    /**
     * 添加商品
     * @param productDTO 商品信息
     * @return 添加后的商品
     */
    ProductDTO addProduct(ProductDTO productDTO);
    
    /**
     * 更新商品
     * @param productId 商品ID
     * @param productDTO 商品信息
     * @return 更新后的商品
     */
    ProductDTO updateProduct(Long productId, ProductDTO productDTO);
    
    /**
     * 更新商品状态（上架/下架）
     * @param productId 商品ID
     * @param status 状态：1-上架，0-下架
     * @return 是否成功
     */
    boolean updateProductStatus(Long productId, Integer status);
    
    /**
     * 删除商品
     * @param productId 商品ID
     * @param merchantId 商家ID（用于验证权限）
     * @return 是否成功
     */
    boolean deleteProduct(Long productId, Long merchantId);
    
    /**
     * 根据用户ID删除商品（会验证用户是否为商品所属商家）
     * @param productId 商品ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteProductByUser(Long productId, Long userId);
    
    /**
     * 根据商品ID获取商品基本信息
     * @param productId 商品ID
     * @return 商品信息Map
     */
    Map<String, Object> getProductById(Long productId);
    
    /**
     * 获取商品销售排行
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 商品销售排行
     */
    List<Map<String, Object>> getProductSalesRanking(Long merchantId, Integer limit);
    
    /**
     * 获取商家的商品列表（按状态筛选）
     * @param merchantId 商家ID
     * @param status 商品状态，1-上架，0-下架，null-全部
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    List<ProductDTO> getMerchantProductsByStatus(Long merchantId, Integer status, int page, int size);
    
    /**
     * 更新商品基本信息
     * @param productId 商品ID
     * @param productName 商品名称
     * @param productBrief 商品简介
     * @param price 商品价格
     * @param stock 库存
     * @param unit 单位
     * @param keywords 关键词
     * @return 是否成功
     */
    boolean updateProductBasicInfo(Long productId, String productName, String productBrief, 
                                 Double price, Integer stock, String unit, String keywords);
    
    /**
     * 更新商品主图
     * @param productId 商品ID
     * @param mainImage 主图URL
     * @return 是否成功
     */
    boolean updateProductMainImage(Long productId, String mainImage);
    
    /**
     * 更新商品详情
     * @param productId 商品ID
     * @param detail 商品详情
     * @return 是否成功
     */
    boolean updateProductDetail(Long productId, String detail);
    
    /**
     * 更新商品分类
     * @param productId 商品ID
     * @param categoryId 分类ID
     * @return 是否成功
     */
    boolean updateProductCategory(Long productId, Long categoryId);
}
