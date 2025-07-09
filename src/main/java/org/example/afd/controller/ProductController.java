package org.example.afd.controller;

import org.example.afd.dto.BannerDTO;
import org.example.afd.dto.ProductDTO;
import org.example.afd.dto.ProductDetailDTO;
import org.example.afd.dto.PromotionDTO;
import org.example.afd.dto.ReviewDTO;
import org.example.afd.dto.ProductSkuDTO;
import org.example.afd.model.Result;
import org.example.afd.service.BannerService;
import org.example.afd.service.CategoryService;
import org.example.afd.service.ProductService;
import org.example.afd.service.PromotionService;
import org.example.afd.service.ReviewService;
import org.example.afd.model.Result;
import org.example.afd.model.ResultCode;
import org.example.afd.utils.UserIdHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 商品相关接口的Controller
 */
@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ReviewService reviewService;

    /**
     * 获取商品列表
     *
     * @param page   页码
     * @param size   每页数量
     * @param params 查询参数
     * @return 商品列表
     */
    @GetMapping("/products")
    public Result<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Map<String, Object> params) {
        logger.info("获取商品列表, page: {}, size: {}, params: {}", page, size, params);
        try {
            Map<String, Object> result = productService.getProducts(page, size, params);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取商品列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商品列表失败");
        }
    }

    /**
     * 根据分类获取商品列表
     *
     * @param categoryId 分类ID
     * @param page       页码
     * @param size       每页数量
     * @return 商品列表
     */
    @GetMapping("/categories/{categoryId}/products")
    public Result<List<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("根据分类获取商品列表, categoryId: {}, page: {}, size: {}", categoryId, page, size);
        try {
            Map<String, Object> result = productService.getProductsByCategory(categoryId, page, size);
            @SuppressWarnings("unchecked")
            List<ProductDTO> products = (List<ProductDTO>) result.get("list");
            logger.info("成功获取分类商品列表，分类ID: {}, 商品数量: {}", categoryId, products != null ? products.size() : 0);
            return Result.success(products != null ? products : new ArrayList<>());
        } catch (Exception e) {
            logger.error("根据分类获取商品列表失败, categoryId: {}", categoryId, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类商品列表失败");
        }
    }

    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/products/{productId}")
    public Result<ProductDetailDTO> getProductDetail(@PathVariable Long productId) {
        logger.info("获取商品详情, productId: {}", productId);
        try {
            ProductDetailDTO productDetailDTO = productService.getProductDetail(productId);
            
            // 添加JSON序列化验证日志
            logger.info("=== 准备返回的DTO数据验证 ===");
            logger.info("DTO.getProductName(): [{}]", productDetailDTO.getProductName());
            logger.info("DTO.getProductBrief(): [{}]", productDetailDTO.getProductBrief());
            logger.info("DTO.getMainImage(): [{}]", productDetailDTO.getMainImage());
            logger.info("DTO.getProductExplain(): [{}]", productDetailDTO.getProductExplain());
            logger.info("DTO.getImages() size: {}", productDetailDTO.getImages() != null ? productDetailDTO.getImages().size() : "null");
            logger.info("DTO.getDetailImages() size: {}", productDetailDTO.getDetailImages() != null ? productDetailDTO.getDetailImages().size() : "null");
            
            // 详细记录SKU数据
            logger.info("=== Controller层SKU数据验证 ===");
            if (productDetailDTO.getSkus() != null && !productDetailDTO.getSkus().isEmpty()) {
                logger.info("Controller: SKU列表大小: {}", productDetailDTO.getSkus().size());
                for (int i = 0; i < productDetailDTO.getSkus().size(); i++) {
                    ProductSkuDTO sku = productDetailDTO.getSkus().get(i);
                    logger.info("Controller: SKU[{}] - ID: {}, 名称: [{}], 图片: [{}], 价格: {}, 库存: {}", 
                            i, sku.getSkuId(), sku.getSkuName(), sku.getSkuImage(), sku.getPrice(), sku.getStock());
                }
            } else {
                logger.warn("Controller: SKU列表为空或null");
            }
            logger.info("=== Controller层SKU验证完毕 ===");
            
            Result<ProductDetailDTO> result = Result.success(productDetailDTO);
            logger.info("最终返回的Result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("获取商品详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商品详情失败");
        }
    }

    /**
     * 获取轮播图列表
     *
     * @param position 轮播图位置，如home-首页，category-分类页
     * @return 轮播图列表
     */
    @GetMapping("/banners")
    public Result<List<BannerDTO>> getBanners(
            @RequestParam(defaultValue = "home") String position) {
        logger.info("获取轮播图列表, position: {}", position);
        try {
            List<BannerDTO> banners = bannerService.getBanners(position);
            logger.info("成功获取轮播图列表，共{}张", banners.size());
            return Result.success(banners);
        } catch (Exception e) {
            logger.error("获取轮播图列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取轮播图列表失败");
        }
    }

    /**
     * 获取促销活动列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 促销活动列表
     */
    @GetMapping("/promotions")
    public Map<String, Object> getPromotions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return promotionService.getPromotions(page, size);
    }

    /**
     * 获取促销活动详情
     *
     * @param promotionId 促销活动ID
     * @return 促销活动详情
     */
    @GetMapping("/promotions/{promotionId}")
    public PromotionDTO getPromotionDetail(@PathVariable Long promotionId) {
        return promotionService.getPromotionDetail(promotionId);
    }

    /**
     * 获取商品评价列表
     *
     * @param productId 商品ID
     * @param page      页码
     * @param size      每页数量
     * @return 评价列表
     */
    @GetMapping("/products/{productId}/reviews")
    public Map<String, Object> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reviewService.getProductReviews(productId, page, size);
    }

    /**
     * 发布商品
     *
     * @param productDTO 商品信息
     * @return 发布后的商品信息
     */
    @PostMapping("/products/publish")
    public Result<ProductDTO> publishProduct(@RequestBody ProductDTO productDTO) {
        logger.info("发布商品: {}", productDTO);
        try {
            // 使用addProduct方法来发布商品，因为功能相同
            ProductDTO publishedProduct = productService.addProduct(productDTO);
            return Result.success(publishedProduct);
        } catch (Exception e) {
            logger.error("发布商品失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "发布商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品信息
     *
     * @param productId  商品ID
     * @param productDTO 商品信息
     * @return 更新后的商品信息
     */
    @PutMapping("/products/{productId}")
    public Result<ProductDTO> updateProduct(@PathVariable Long productId, @RequestBody ProductDTO productDTO) {
        logger.info("更新商品, productId: {}, productDTO: {}", productId, productDTO);
        try {
            // 确保ID一致
            productDTO.setProductId(productId);
            ProductDTO updatedProduct = productService.updateProduct(productId, productDTO);
            return Result.success(updatedProduct);
        } catch (Exception e) {
            logger.error("更新商品失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品状态
     *
     * @param productId  商品ID
     * @param statusMap  状态信息，包含status字段
     * @return 操作结果
     */
    @PutMapping("/products/{productId}/status")
    public Result<Boolean> updateProductStatus(@PathVariable Long productId, @RequestBody Map<String, Object> statusMap) {
        logger.info("=== 更新商品状态请求开始 ===");
        logger.info("请求路径: PUT /api/v1/products/{}/status", productId);
        logger.info("请求参数: productId={}, statusMap={}", productId, statusMap);
        
        try {
            // 从UserIdHolder获取当前登录用户的ID
            Integer userId = UserIdHolder.getUserId();
            logger.info("当前登录用户ID: {}", userId);
            
            if (userId == null) {
                logger.warn("更新商品状态失败: 用户未登录");
                return Result.failure(ResultCode.UNAUTHORIZED, "用户未登录或无权限");
            }
            
            Integer status = (Integer) statusMap.get("status");
            logger.info("解析到的状态值: {}", status);
            
            if (status == null) {
                logger.error("更新商品状态失败: 缺少status参数");
                return Result.failure(ResultCode.PARAM_ERROR, "缺少status参数");
            }
            
            logger.info("准备调用Service层更新商品状态: productId={}, status={}", productId, status);
            boolean result = productService.updateProductStatus(productId, status);
            logger.info("Service层返回结果: {}", result);
            
            if (result) {
                logger.info("=== 商品状态更新成功 ===");
                return Result.success(true);
            } else {
                logger.warn("=== 商品状态更新失败: Service层返回false ===");
                return Result.failure(ResultCode.BUSINESS_ERROR, "更新商品状态失败");
            }
        } catch (Exception e) {
            logger.error("=== 更新商品状态时发生异常 ===", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商品状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/products/{productId}")
    public Result<Boolean> deleteProduct(@PathVariable Long productId) {
        logger.info("删除商品请求, productId: {}", productId);
        try {
            // 从UserIdHolder获取当前登录用户的ID
            Integer userId = UserIdHolder.getUserId();
            if (userId == null) {
                logger.warn("删除商品失败: 用户未登录");
                return Result.failure(ResultCode.UNAUTHORIZED, "用户未登录或无权限");
            }
            
            logger.info("当前操作用户ID: {}", userId);
            
            // 需要先根据userId查找对应的merchantId
            // 注入MerchantMapper来查询商家信息
            logger.info("根据用户ID查找商家信息: userId={}", userId);
            
            // 这里需要调用Service层方法来查找商家信息
            // 暂时使用一个简单的逻辑：先查询商品信息，然后验证权限
            try {
                // 先获取商品信息
                Map<String, Object> product = productService.getProductById(productId);
                if (product == null) {
                    logger.warn("商品不存在: productId={}", productId);
                    return Result.failure(ResultCode.BUSINESS_ERROR, "商品不存在");
                }
                
                Long productMerchantId = (Long) product.get("merchant_id");
                if (productMerchantId == null) {
                    logger.warn("商品商家信息不完整: productId={}", productId);
                    return Result.failure(ResultCode.BUSINESS_ERROR, "商品信息不完整");
                }
                
                // 验证当前用户是否是该商品的商家
                // 需要查询商家表，验证该商家的userId是否等于当前用户的userId
                logger.info("验证商家权限: productMerchantId={}, userId={}", productMerchantId, userId);
                
                // 调用Service层方法进行删除，传入商品ID和用户ID，让Service层处理权限验证
                boolean result = productService.deleteProductByUser(productId, userId.longValue());
                
                if (result) {
                    logger.info("商品删除成功: productId={}", productId);
                    return Result.success(true);
                } else {
                    logger.warn("商品删除失败: productId={}, 可能原因：商品不存在或无权限", productId);
                    return Result.failure(ResultCode.BUSINESS_ERROR, "删除失败，商品不存在或无操作权限");
                }
                
            } catch (Exception e) {
                logger.error("查询商品信息失败: productId={}", productId, e);
                return Result.failure(ResultCode.SYSTEM_ERROR, "查询商品信息失败");
            }
            
        } catch (Exception e) {
            logger.error("删除商品时发生异常: productId=" + productId, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "删除商品失败: " + e.getMessage());
        }
    }
}