package org.example.afd.service.impl;

import org.example.afd.dto.ProductDTO;
import org.example.afd.dto.ProductDetailDTO;
import org.example.afd.dto.ProductDetailImageDTO;
import org.example.afd.dto.CategoryDTO;
import org.example.afd.dto.SkuDTO;
import org.example.afd.dto.SimpleSpecDTO;
import org.example.afd.dto.MerchantDTO;
import org.example.afd.dto.ProductSkuDTO;
import org.example.afd.mapper.ProductMapper;
import org.example.afd.mapper.CategoryMapper;
import org.example.afd.mapper.ProductImageMapper;
import org.example.afd.mapper.ProductDetailImageMapper;
import org.example.afd.mapper.SimpleSpecMapper;
import org.example.afd.mapper.MerchantMapper;
import org.example.afd.model.Merchant;
import org.example.afd.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private ProductImageMapper productImageMapper;
    
    @Autowired
    private ProductDetailImageMapper productDetailImageMapper;
    
    @Autowired
    private SimpleSpecMapper simpleSpecMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public Map<String, Object> getProducts(int page, int size, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询商品列表
        List<Map<String, Object>> products = productMapper.selectProducts(params, offset, size);
        int total = productMapper.countProducts(params);
        
        // 转换为DTO
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Map<String, Object> product : products) {
                ProductDTO productDTO = convertToProductDTO(product);
                productDTOs.add(productDTO);
            }
        }
        
        result.put("list", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        logger.info("开始查询商品详情，商品ID: {}", productId);
        
        // 查询商品基本信息
        Map<String, Object> product = productMapper.selectProductById(productId);
        if (product == null) {
            logger.warn("商品不存在，商品ID: {}", productId);
            return null;
        }
        
        logger.info("查询到商品基本信息: {}", product);
        
        // 详细记录每个字段的值
        logger.info("=== 商品字段详细信息 ===");
        logger.info("product_id: {}", product.get("product_id"));
        logger.info("product_name: [{}]", product.get("product_name"));
        logger.info("product_brief: [{}]", product.get("product_brief"));
        logger.info("main_image: [{}]", product.get("main_image"));
        logger.info("detail: [{}]", product.get("detail"));
        logger.info("product_explain: [{}]", product.get("product_explain"));
        logger.info("price: {}", product.get("price"));
        logger.info("stock: {}", product.get("stock"));
        logger.info("merchant_id: {}", product.get("merchant_id"));
        logger.info("category_id: {}", product.get("category_id"));
        logger.info("=== 字段信息记录完毕 ===");
        
        ProductDetailDTO detailDTO = new ProductDetailDTO();
        
        // 设置基本信息
        detailDTO.setProductId((Long) product.get("product_id"));
        logger.info("设置ProductId: {}", detailDTO.getProductId());
        
        detailDTO.setProductName((String) product.get("product_name"));
        logger.info("设置ProductName: [{}]", detailDTO.getProductName());
        
        detailDTO.setCategoryId((Long) product.get("category_id"));
        logger.info("设置CategoryId: {}", detailDTO.getCategoryId());
        
        detailDTO.setMerchantId((Long) product.get("merchant_id"));
        logger.info("设置MerchantId: {}", detailDTO.getMerchantId());
        
        detailDTO.setMainImage((String) product.get("main_image"));
        logger.info("设置MainImage: [{}]", detailDTO.getMainImage());
        
        detailDTO.setDetail((String) product.get("detail"));
        logger.info("设置Detail: [{}]", detailDTO.getDetail());
        
        detailDTO.setProductBrief((String) product.get("product_brief"));
        logger.info("设置ProductBrief: [{}]", detailDTO.getProductBrief());
        
        detailDTO.setProductExplain((String) product.get("product_explain"));
        logger.info("设置ProductExplain: [{}]", detailDTO.getProductExplain());
        
        detailDTO.setPrice(getDoubleValue(product.get("price")) != null ? 
                          BigDecimal.valueOf(getDoubleValue(product.get("price"))) : null);
        detailDTO.setPromotionPrice(getDoubleValue(product.get("promotion_price")) != null ? 
                                   BigDecimal.valueOf(getDoubleValue(product.get("promotion_price"))) : null);
        
        // 修复boolean类型转换问题
        Object hasPromotionObj = product.get("has_promotion");
        if (hasPromotionObj instanceof Boolean) {
            detailDTO.setHasPromotion((Boolean) hasPromotionObj);
        } else if (hasPromotionObj instanceof Integer) {
            detailDTO.setHasPromotion(((Integer) hasPromotionObj) == 1);
        } else if (hasPromotionObj instanceof Number) {
            detailDTO.setHasPromotion(((Number) hasPromotionObj).intValue() == 1);
        } else {
            detailDTO.setHasPromotion(false);
        }
        
        detailDTO.setStock((Integer) product.get("stock"));
        detailDTO.setSales((Integer) product.get("sales"));
        detailDTO.setUnit((String) product.get("unit"));
        detailDTO.setKeywords((String) product.get("keywords"));
        
        logger.info("设置商品基本信息完成，商品名称: [{}], 价格: {}", detailDTO.getProductName(), detailDTO.getPrice());
        
        // 查询商品轮播图片（从shop_product_image表）
        try {
            List<Map<String, Object>> productImageMaps = productImageMapper.selectProductImagesByProductId(productId);
            logger.info("查询到原始图片数据: {}", productImageMaps);
            if (productImageMaps != null && !productImageMaps.isEmpty()) {
                List<String> images = new ArrayList<>();
                for (Map<String, Object> imageMap : productImageMaps) {
                    String imageUrl = (String) imageMap.get("image_url");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        images.add(imageUrl);
                        logger.info("添加轮播图片: {}", imageUrl);
                    }
                }
                detailDTO.setImages(images);
                logger.info("设置轮播图片完成，数量: {}, 内容: {}", images.size(), images);
            } else {
                logger.warn("未找到商品轮播图片，商品ID: {}", productId);
                detailDTO.setImages(new ArrayList<>());
                logger.info("设置空的轮播图片列表");
            }
        } catch (Exception e) {
            logger.error("查询商品轮播图片失败，商品ID: {}", productId, e);
            detailDTO.setImages(new ArrayList<>());
        }
        
        // 查询商品详情图片（从shop_product_detail_images表）
        try {
            List<Map<String, Object>> detailImageMaps = productDetailImageMapper.selectDetailImagesByProductId(productId);
            logger.info("查询到原始详情图片数据: {}", detailImageMaps);
            if (detailImageMaps != null && !detailImageMaps.isEmpty()) {
                List<String> detailImages = new ArrayList<>();
                for (Map<String, Object> imageMap : detailImageMaps) {
                    String imageUrl = (String) imageMap.get("image_url");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        detailImages.add(imageUrl);
                        logger.info("添加详情图片: {}", imageUrl);
                    }
                }
                detailDTO.setDetailImages(detailImages);
                logger.info("设置详情图片完成，数量: {}, 内容: {}", detailImages.size(), detailImages);
            } else {
                logger.warn("未找到商品详情图片，商品ID: {}", productId);
                detailDTO.setDetailImages(new ArrayList<>());
                logger.info("设置空的详情图片列表");
            }
        } catch (Exception e) {
            logger.error("查询商品详情图片失败，商品ID: {}", productId, e);
            detailDTO.setDetailImages(new ArrayList<>());
        }
        
        // 查询商品分类
        Long categoryId = (Long) product.get("category_id");
        if (categoryId != null) {
            try {
                Map<String, Object> category = categoryMapper.selectCategoryById(categoryId);
                if (category != null) {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setCategoryId(categoryId);
                    categoryDTO.setCategoryName((String) category.get("category_name"));
                    categoryDTO.setParentId((Long) category.get("parent_id"));
                    categoryDTO.setLevel((Integer) category.get("level"));
                    categoryDTO.setSort((Integer) category.get("sort"));
                    categoryDTO.setIcon((String) category.get("icon"));
                    logger.info("查询到商品分类: {}", categoryDTO.getCategoryName());
                }
            } catch (Exception e) {
                logger.error("查询商品分类失败，分类ID: {}", categoryId, e);
            }
        }
        
        // 查询商家信息（包含用户头像和昵称）
        Long merchantId = (Long) product.get("merchant_id");
        if (merchantId != null) {
            try {
                MerchantDTO merchantDTO = merchantMapper.selectMerchantWithUserInfoById(merchantId);
                if (merchantDTO != null) {
                    detailDTO.setMerchant(merchantDTO);
                    logger.info("查询到商家信息: {}, 用户头像: {}", merchantDTO.getMerchantName(), merchantDTO.getUserAvatar());
                } else {
                    logger.warn("未找到商家信息，商家ID: {}", merchantId);
                }
            } catch (Exception e) {
                logger.error("查询商家信息失败，商家ID: {}", merchantId, e);
            }
        }
        
        // 查询SKU列表
        try {
            List<Map<String, Object>> skus = productMapper.selectSkusByProductId(productId);
            logger.info("=== SKU原始数据调试 ===");
            logger.info("从数据库查询到的SKU数量: {}", skus != null ? skus.size() : 0);
            
            if (skus != null && !skus.isEmpty()) {
                // 添加原始数据日志
                for (int i = 0; i < skus.size(); i++) {
                    Map<String, Object> sku = skus.get(i);
                    logger.info("原始SKU[{}]数据:", i);
                    for (Map.Entry<String, Object> entry : sku.entrySet()) {
                        logger.info("  {}: [{}]", entry.getKey(), entry.getValue());
                    }
                }
                
                List<ProductSkuDTO> skuDTOs = new ArrayList<>();
                for (Map<String, Object> sku : skus) {
                    ProductSkuDTO skuDTO = new ProductSkuDTO();
                    skuDTO.setSkuId((Long) sku.get("sku_id"));
                    skuDTO.setProductId(productId);
                    skuDTO.setSkuCode((String) sku.get("sku_code"));
                    
                    // 设置SKU名称 - 从sku_name字段获取，如果为空则使用sku_code
                    String skuName = (String) sku.get("sku_name");
                    if (skuName == null || skuName.trim().isEmpty()) {
                        skuName = (String) sku.get("sku_code");
                    }
                    if (skuName == null || skuName.trim().isEmpty()) {
                        skuName = "默认规格";
                    }
                    skuDTO.setSkuName(skuName);
                    
                    // 设置SKU图片
                    String skuImage = (String) sku.get("sku_image");
                    skuDTO.setSkuImage(skuImage);
                    
                    // 设置价格
                    Double price = getDoubleValue(sku.get("price"));
                    if (price != null) {
                        skuDTO.setPrice(price);
                    }
                    
                    // 设置促销价格
                    Double promotionPrice = getDoubleValue(sku.get("promotion_price"));
                    if (promotionPrice != null) {
                        skuDTO.setPromotionPrice(promotionPrice);
                    }
                    
                    // 修复boolean类型转换问题
                    Object skuHasPromotionObj = sku.get("has_promotion");
                    if (skuHasPromotionObj instanceof Boolean) {
                        skuDTO.setHasPromotion((Boolean) skuHasPromotionObj);
                    } else if (skuHasPromotionObj instanceof Integer) {
                        skuDTO.setHasPromotion(((Integer) skuHasPromotionObj) == 1);
                    } else if (skuHasPromotionObj instanceof Number) {
                        skuDTO.setHasPromotion(((Number) skuHasPromotionObj).intValue() == 1);
                    } else {
                        skuDTO.setHasPromotion(false);
                    }
                    
                    skuDTO.setStock((Integer) sku.get("stock"));
                    
                    // 设置规格属性 - 从specs JSON字段解析
                    String specsJson = (String) sku.get("specs");
                    if (specsJson != null && !specsJson.trim().isEmpty()) {
                        try {
                            // 这里可以使用JSON解析器解析specs字段
                            // 暂时保持简单的字符串解析方式
                            Map<String, String> specsMap = new HashMap<>();
                            // 如果specs是JSON格式，应该用JSON解析
                            // 这里暂时假设specs字段是"key:value;key:value"格式
                            String[] specs = specsJson.split(";");
                            for (String spec : specs) {
                                if (spec.contains(":")) {
                                    String[] parts = spec.split(":", 2);
                                    if (parts.length == 2) {
                                        specsMap.put(parts[0].trim(), parts[1].trim());
                                    }
                                }
                            }
                            skuDTO.setSpecs(specsMap);
                        } catch (Exception e) {
                            logger.error("解析SKU规格属性失败，SKU ID: {}, specs: {}", sku.get("sku_id"), specsJson, e);
                        }
                    }
                    
                    skuDTOs.add(skuDTO);
                    logger.info("添加SKU: ID={}, 名称={}, 图片={}, 价格={}, 库存={}", 
                            skuDTO.getSkuId(), skuDTO.getSkuName(), skuDTO.getSkuImage(), 
                            skuDTO.getPrice(), skuDTO.getStock());
                }
                detailDTO.setSkus(skuDTOs);
                logger.info("设置SKU完成，数量: {}", skuDTOs.size());
            } else {
                logger.warn("未找到SKU信息，商品ID: {}", productId);
                detailDTO.setSkus(new ArrayList<>());
                logger.info("设置空的SKU列表");
            }
        } catch (Exception e) {
            logger.error("查询SKU信息失败，商品ID: {}", productId, e);
            detailDTO.setSkus(new ArrayList<>());
        }
        
        // 查询商品评价数量和评分
        try {
            int reviewCount = productMapper.countProductReviews(productId);
            Double reviewScore = productMapper.selectProductRating(productId);
            
            detailDTO.setReviewCount(reviewCount);
            detailDTO.setReviewScore(reviewScore);
            logger.info("查询到评价信息 - 数量: {}, 评分: {}", reviewCount, reviewScore);
        } catch (Exception e) {
            logger.error("查询评价信息失败，商品ID: {}", productId, e);
            detailDTO.setReviewCount(0);
            detailDTO.setReviewScore(5.0);
        }
        
        // 查询部分评价
        try {
            List<Map<String, Object>> reviews = productMapper.selectProductReviews(productId, 0, 5);
            if (reviews != null && !reviews.isEmpty()) {
                logger.info("查询到评价数量: {}", reviews.size());
                // 假设我们有ReviewDTO并且已经编写了它的转换逻辑
                // List<ReviewDTO> reviewDTOs = convertToReviewDTOs(reviews);
                // detailDTO.setReviews(reviewDTOs);
            }
        } catch (Exception e) {
            logger.error("查询评价列表失败，商品ID: {}", productId, e);
        }
        
        // 增加商品浏览量
        try {
            productMapper.incrementViewCount(productId);
        } catch (Exception e) {
            logger.error("更新浏览量失败，商品ID: {}", productId, e);
        }
        
        logger.info("商品详情查询完成，返回数据: 商品名称={}, 图片数量={}, 详情图片数量={}, 商家名称={}", 
                detailDTO.getProductName(), 
                detailDTO.getImages() != null ? detailDTO.getImages().size() : 0,
                detailDTO.getDetailImages() != null ? detailDTO.getDetailImages().size() : 0,
                detailDTO.getMerchant() != null ? detailDTO.getMerchant().getMerchantName() : "未知");
        
        // 最终验证DTO内容
        logger.info("=== 最终DTO验证 ===");
        logger.info("ProductName: [{}]", detailDTO.getProductName());
        logger.info("ProductBrief: [{}]", detailDTO.getProductBrief());
        logger.info("MainImage: [{}]", detailDTO.getMainImage());
        logger.info("Detail: [{}]", detailDTO.getDetail());
        logger.info("ProductExplain: [{}]", detailDTO.getProductExplain());
        logger.info("Images size: {}", detailDTO.getImages() != null ? detailDTO.getImages().size() : "null");
        logger.info("DetailImages size: {}", detailDTO.getDetailImages() != null ? detailDTO.getDetailImages().size() : "null");
        logger.info("Skus size: {}", detailDTO.getSkus() != null ? detailDTO.getSkus().size() : "null");
        logger.info("=== DTO验证完毕 ===");
        
        return detailDTO;
    }

    @Override
    public Map<String, Object> getProductsByCategory(Long categoryId, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("categoryId", categoryId);
        
        return getProducts(page, size, params);
    }

    @Override
    public Map<String, Object> searchProducts(String keyword, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        
        return getProducts(page, size, params);
    }

    @Override
    public Map<String, Object> getHotProducts(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询热门商品列表（基于销量排序）
        List<Map<String, Object>> products = productMapper.selectHotProducts(offset, size);
        int total = productMapper.countHotProducts();
        
        // 转换为DTO
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Map<String, Object> product : products) {
                ProductDTO productDTO = convertToProductDTO(product);
                productDTOs.add(productDTO);
            }
        }
        
        result.put("list", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    @Override
    public Map<String, Object> getNewProducts(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询新品列表（基于创建时间排序）
        List<Map<String, Object>> products = productMapper.selectNewProducts(offset, size);
        int total = productMapper.countNewProducts();
        
        // 转换为DTO
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Map<String, Object> product : products) {
                ProductDTO productDTO = convertToProductDTO(product);
                productDTOs.add(productDTO);
            }
        }
        
        result.put("list", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    @Override
    public Map<String, Object> getRecommendProducts(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询推荐商品列表（基于推荐算法，这里简化为随机推荐）
        List<Map<String, Object>> products = productMapper.selectRecommendProducts(offset, size);
        int total = productMapper.countRecommendProducts();
        
        // 转换为DTO
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Map<String, Object> product : products) {
                ProductDTO productDTO = convertToProductDTO(product);
                productDTOs.add(productDTO);
            }
        }
        
        result.put("list", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    @Override
    public Map<String, Object> getPromotionProducts(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 查询促销商品列表
        List<Map<String, Object>> products = productMapper.selectPromotionProducts(offset, size);
        int total = productMapper.countPromotionProducts();
        
        // 转换为DTO
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Map<String, Object> product : products) {
                ProductDTO productDTO = convertToProductDTO(product);
                productDTOs.add(productDTO);
            }
        }
        
        result.put("list", productDTOs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }

    @Override
    public List<ProductDTO> getSimilarProducts(Long productId, int limit) {
        // 先获取商品信息
        Map<String, Object> product = productMapper.selectProductById(productId);
        if (product == null) {
            return Collections.emptyList();
        }
        
        Long categoryId = (Long) product.get("category_id");
        List<Map<String, Object>> similarProducts = productMapper.selectSimilarProducts(productId, categoryId, limit);
        
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (similarProducts != null && !similarProducts.isEmpty()) {
            for (Map<String, Object> similarProduct : similarProducts) {
                ProductDTO productDTO = convertToProductDTO(similarProduct);
                productDTOs.add(productDTO);
            }
        }
        
        return productDTOs;
    }

    @Override
    public boolean updateProductSales(Long productId, int quantity) {
        try {
            int result = productMapper.updateProductSales(productId, quantity);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新商品销量失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean decreaseStock(Long productId, int quantity) {
        try {
            int result = productMapper.decreaseStock(productId, quantity);
            return result > 0;
        } catch (Exception e) {
            logger.error("减少商品库存失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean incrementViewCount(Long productId) {
        try {
            int result = productMapper.incrementViewCount(productId);
            return result > 0;
        } catch (Exception e) {
            logger.error("增加商品浏览量失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<ProductDTO> getMerchantProducts(Long merchantId, int page, int size) {
        logger.info("获取商家商品列表: merchantId={}, page={}, size={}", merchantId, page, size);
        int offset = (page - 1) * size;
        try {
            return productMapper.selectMerchantProducts(merchantId, offset, size);
        } catch (Exception e) {
            logger.error("获取商家商品列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getMerchantProductsByCategory(Long merchantId, Long categoryId, int page, int size) {
        logger.info("按分类获取商家商品列表: merchantId={}, categoryId={}, page={}, size={}", 
                merchantId, categoryId, page, size);
        int offset = (page - 1) * size;
        try {
            return productMapper.selectMerchantProductsByCategory(merchantId, categoryId, offset, size);
        } catch (Exception e) {
            logger.error("按分类获取商家商品列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public ProductDTO addProduct(ProductDTO productDTO) {
        logger.info("添加商品: {}", productDTO);
        try {
            // 设置默认值
            if (productDTO.getStatus() == null) {
                productDTO.setStatus(1); // 默认上架
            }
            if (productDTO.getIsHot() == null) {
                productDTO.setIsHot(false);
            }
            if (productDTO.getIsRecommend() == null) {
                productDTO.setIsRecommend(false);
            }
            if (productDTO.getIsNew() == null) {
                productDTO.setIsNew(true); // 默认为新品
            }
            if (productDTO.getProductType() == null) {
                productDTO.setProductType(1); // 默认为实物商品
            }
            
            // 插入商品信息
            int result = productMapper.insertProduct(productDTO);
            if (result > 0) {
                Long productId = productDTO.getProductId();
                logger.info("商品插入成功，商品ID: {}", productId);
                
                // 保存商品图片（轮播图）
                if (productDTO.getProductImages() != null && !productDTO.getProductImages().isEmpty()) {
                    List<Map<String, Object>> productImages = new ArrayList<>();
                    for (int i = 0; i < productDTO.getProductImages().size(); i++) {
                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put("productId", productId);
                        imageMap.put("imageUrl", productDTO.getProductImages().get(i).getImageUrl());
                        imageMap.put("sortOrder", i);
                        productImages.add(imageMap);
                    }
                    int imageResult = productImageMapper.batchInsertProductImages(productImages);
                    logger.info("保存商品图片成功，数量: {}", imageResult);
                }
                
                // 保存详情图片
                if (productDTO.getDetailImages() != null && !productDTO.getDetailImages().isEmpty()) {
                    List<Map<String, Object>> detailImages = new ArrayList<>();
                    for (int i = 0; i < productDTO.getDetailImages().size(); i++) {
                        ProductDetailImageDTO detailImageDTO = productDTO.getDetailImages().get(i);
                        Map<String, Object> imageMap = new HashMap<>();
                        imageMap.put("productId", productId);
                        imageMap.put("imageUrl", detailImageDTO.getImageUrl());
                        imageMap.put("sortOrder", i);
                        imageMap.put("imageType", 2); // 2=详情图
                        imageMap.put("description", detailImageDTO.getDescription());
                        imageMap.put("width", detailImageDTO.getWidth());
                        imageMap.put("height", detailImageDTO.getHeight());
                        // 为isThumbnail字段提供默认值，避免null导致的数据库约束错误
                        Boolean isThumbnail = detailImageDTO.getIsThumbnail();
                        imageMap.put("isThumbnail", isThumbnail != null ? isThumbnail : false);
                        detailImages.add(imageMap);
                    }
                    int detailResult = productDetailImageMapper.batchInsertProductDetailImages(detailImages);
                    logger.info("保存详情图片成功，数量: {}", detailResult);
                }
                
                // 保存简化规格
                if (productDTO.getSimpleSpecs() != null && !productDTO.getSimpleSpecs().isEmpty()) {
                    List<Map<String, Object>> simpleSpecs = new ArrayList<>();
                    for (SimpleSpecDTO spec : productDTO.getSimpleSpecs()) {
                        Map<String, Object> specMap = new HashMap<>();
                        specMap.put("productId", productId);
                        specMap.put("specName", spec.getSpecName());
                        specMap.put("imageUrl", spec.getImageUrl());
                        specMap.put("price", spec.getPrice());
                        specMap.put("stock", spec.getStock());
                        simpleSpecs.add(specMap);
                    }
                    int specResult = simpleSpecMapper.batchInsertSimpleSpecs(simpleSpecs);
                    logger.info("保存简化规格成功，数量: {}", specResult);
                }
                
                // 返回添加后的商品信息
                Map<String, Object> product = productMapper.selectProductById(productId);
                if (product != null) {
                    return convertToProductDTO(product);
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("添加商品失败: {}", e.getMessage(), e);
            throw new RuntimeException("添加商品失败", e);
        }
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        logger.info("更新商品: productId={}, {}", productId, productDTO);
        try {
            // 设置商品ID
            productDTO.setProductId(productId);
            
            // 更新商品信息
            int result = productMapper.updateProduct(productDTO);
            if (result > 0) {
                // 返回更新后的商品信息
                Map<String, Object> product = productMapper.selectProductById(productId);
                if (product != null) {
                    return convertToProductDTO(product);
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("更新商品失败: {}", e.getMessage());
            throw new RuntimeException("更新商品失败", e);
        }
    }

    @Override
    public boolean updateProductStatus(Long productId, Integer status) {
        logger.info("=== Service层: 更新商品状态开始 ===");
        logger.info("Service层参数: productId={}, status={}", productId, status);
        
        if (productId == null || status == null) {
            logger.error("Service层: 参数校验失败 - productId={}, status={}", productId, status);
            return false;
        }
        
        try {
            logger.info("Service层: 准备调用Mapper层更新商品状态");
            int result = productMapper.updateProductStatus(productId, status);
            logger.info("Service层: Mapper层返回结果 - 影响行数: {}", result);
            
            boolean success = result > 0;
            logger.info("Service层: 最终返回结果: {}", success);
            
            if (success) {
                logger.info("=== Service层: 商品状态更新成功 ===");
            } else {
                logger.warn("=== Service层: 商品状态更新失败，影响行数为0 ===");
            }
            
            return success;
        } catch (Exception e) {
            logger.error("=== Service层: 更新商品状态异常 ===", e);
            logger.error("异常详情: productId={}, status={}, 错误信息: {}", productId, status, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteProduct(Long productId, Long merchantId) {
        logger.info("=== Service层: 删除商品开始 ===");
        logger.info("Service层参数: productId={}, merchantId={}", productId, merchantId);
        
        if (productId == null || merchantId == null) {
            logger.error("Service层: 参数校验失败 - productId={}, merchantId={}", productId, merchantId);
            return false;
        }
        
        try {
            // 先查询商品是否存在，以及是否属于当前商家
            logger.info("Service层: 检查商品权限");
            Map<String, Object> product = productMapper.selectProductById(productId);
            
            if (product == null) {
                logger.warn("Service层: 商品不存在 - productId={}", productId);
                return false;
            }
            
            Long dbMerchantId = getLongValue(product.get("merchant_id"));
            logger.info("Service层: 数据库中的商家ID={}, 当前用户商家ID={}", dbMerchantId, merchantId);
            
            if (!merchantId.equals(dbMerchantId)) {
                logger.warn("Service层: 权限验证失败 - 商品属于商家{}, 但当前用户是商家{}", dbMerchantId, merchantId);
                return false;
            }
            
            logger.info("Service层: 权限验证通过，准备调用Mapper层删除商品");
            int result = productMapper.deleteProduct(productId, merchantId);
            logger.info("Service层: Mapper层返回结果 - 影响行数: {}", result);
            
            boolean success = result > 0;
            logger.info("Service层: 最终返回结果: {}", success);
            
            if (success) {
                logger.info("=== Service层: 商品删除成功 ===");
            } else {
                logger.warn("=== Service层: 商品删除失败，影响行数为0 ===");
            }
            
            return success;
        } catch (Exception e) {
            logger.error("=== Service层: 删除商品异常 ===", e);
            logger.error("异常详情: productId={}, merchantId={}, 错误信息: {}", productId, merchantId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getProductSalesRanking(Long merchantId, Integer limit) {
        logger.info("获取商品销售排行: merchantId={}, limit={}", merchantId, limit);
        try {
            return productMapper.getProductSalesRanking(merchantId, limit);
        } catch (Exception e) {
            logger.error("获取商品销售排行失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getMerchantProductsByStatus(Long merchantId, Integer status, int page, int size) {
        logger.info("获取商家商品列表（按状态筛选）: merchantId={}, status={}, page={}, size={}", 
                merchantId, status, page, size);
        int offset = (page - 1) * size;
        try {
            return productMapper.selectMerchantProductsByStatus(merchantId, status, offset, size);
        } catch (Exception e) {
            logger.error("获取商家商品列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean updateProductBasicInfo(Long productId, String productName, String productBrief, 
                                        Double price, Integer stock, String unit, String keywords) {
        logger.info("更新商品基本信息: productId={}", productId);
        try {
            int result = productMapper.updateProductBasicInfo(productId, productName, productBrief, 
                    price, stock, unit, keywords);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新商品基本信息失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateProductMainImage(Long productId, String mainImage) {
        logger.info("更新商品主图: productId={}, mainImage={}", productId, mainImage);
        try {
            int result = productMapper.updateProductMainImage(productId, mainImage);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新商品主图失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateProductDetail(Long productId, String detail) {
        logger.info("更新商品详情: productId={}", productId);
        try {
            int result = productMapper.updateProductDetail(productId, detail);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新商品详情失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateProductCategory(Long productId, Long categoryId) {
        logger.info("更新商品分类: productId={}, categoryId={}", productId, categoryId);
        try {
            int result = productMapper.updateProductCategory(productId, categoryId);
            return result > 0;
        } catch (Exception e) {
            logger.error("更新商品分类失败: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteProductByUser(Long productId, Long userId) {
        logger.info("=== Service层: 根据用户ID删除商品开始 ===");
        logger.info("Service层参数: productId={}, userId={}", productId, userId);
        
        if (productId == null || userId == null) {
            logger.error("Service层: 参数校验失败 - productId={}, userId={}", productId, userId);
            return false;
        }
        
        try {
            // 1. 先查询商品是否存在
            logger.info("Service层: 检查商品是否存在");
            Map<String, Object> product = productMapper.selectProductById(productId);
            
            if (product == null) {
                logger.warn("Service层: 商品不存在 - productId={}", productId);
                return false;
            }
            
            Long productMerchantId = getLongValue(product.get("merchant_id"));
            logger.info("Service层: 商品所属商家ID={}", productMerchantId);
            
            // 2. 根据商家ID查询商家信息，验证商家的用户ID是否匹配
            logger.info("Service层: 查询商家信息验证权限");
            Merchant merchant = merchantMapper.selectByPrimaryKey(productMerchantId);
            
            if (merchant == null) {
                logger.warn("Service层: 商家信息不存在 - merchantId={}", productMerchantId);
                return false;
            }
            
            Long merchantUserId = merchant.getUserId();
            logger.info("Service层: 商家关联的用户ID={}, 当前操作用户ID={}", merchantUserId, userId);
            
            if (!userId.equals(merchantUserId)) {
                logger.warn("Service层: 权限验证失败 - 商家关联用户ID={}, 但当前操作用户ID={}", merchantUserId, userId);
                return false;
            }
            
            // 3. 权限验证通过，执行删除操作
            logger.info("Service层: 权限验证通过，准备调用Mapper层删除商品");
            int result = productMapper.deleteProduct(productId, productMerchantId);
            logger.info("Service层: Mapper层返回结果 - 影响行数: {}", result);
            
            boolean success = result > 0;
            logger.info("Service层: 最终返回结果: {}", success);
            
            if (success) {
                logger.info("=== Service层: 商品删除成功 ===");
            } else {
                logger.warn("=== Service层: 商品删除失败，影响行数为0 ===");
            }
            
            return success;
        } catch (Exception e) {
            logger.error("=== Service层: 根据用户ID删除商品异常 ===", e);
            logger.error("异常详情: productId={}, userId={}, 错误信息: {}", productId, userId, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getProductById(Long productId) {
        logger.info("Service层: 根据ID获取商品信息, productId={}", productId);
        
        if (productId == null) {
            logger.error("Service层: 商品ID不能为空");
            return null;
        }
        
        try {
            Map<String, Object> product = productMapper.selectProductById(productId);
            logger.info("Service层: 查询到商品信息: {}", product != null ? "存在" : "不存在");
            return product;
        } catch (Exception e) {
            logger.error("Service层: 查询商品信息失败, productId={}", productId, e);
            return null;
        }
    }
    
    /**
     * 将数据库查询结果转换为产品DTO
     * @param product 数据库查询结果
     * @return 产品DTO
     */
    private ProductDTO convertToProductDTO(Map<String, Object> product) {
        ProductDTO dto = new ProductDTO();
        
        // 基本字段映射 - 确保ID类型为Long
        dto.setProductId(getLongValue(product.get("product_id")));
        dto.setProductName((String) product.get("product_name"));
        dto.setCategoryId(getLongValue(product.get("category_id")));
        dto.setBrandId(getLongValue(product.get("brand_id")));
        dto.setMerchantId(getLongValue(product.get("merchant_id")));
        dto.setMainImage((String) product.get("main_image"));
        dto.setSubImages((String) product.get("sub_images"));
        
        // 商品简介和详情 - 根据数据库表结构映射
        dto.setProductBrief((String) product.get("product_brief"));
        dto.setDetail((String) product.get("detail"));
        dto.setDescription((String) product.get("product_explain")); // product_explain作为description
        
        // 价格相关
        dto.setPrice(getDoubleValue(product.get("price")));
        dto.setPromotionPrice(getDoubleValue(product.get("promotion_price")));
        
        // 修复boolean类型转换问题
        Object hasPromotionObj = product.get("has_promotion");
        if (hasPromotionObj instanceof Boolean) {
            dto.setHasPromotion((Boolean) hasPromotionObj);
        } else if (hasPromotionObj instanceof Integer) {
            dto.setHasPromotion(((Integer) hasPromotionObj) == 1);
        } else if (hasPromotionObj instanceof Number) {
            dto.setHasPromotion(((Number) hasPromotionObj).intValue() == 1);
        } else {
            dto.setHasPromotion(false);
        }
        
        // 库存和销量 - 确保为整数类型
        dto.setStock(getIntegerValue(product.get("stock")));
        dto.setSales(getIntegerValue(product.get("sales")));
        
        // 商品属性
        dto.setUnit((String) product.get("unit"));
        dto.setKeywords((String) product.get("keywords"));
        
        // 状态和标记 - 确保为整数类型
        dto.setStatus(getIntegerValue(product.get("status")));
        dto.setIsHot(getBooleanValue(product.get("is_hot")));
        dto.setIsRecommend(getBooleanValue(product.get("is_recommend")));
        dto.setIsNew(getBooleanValue(product.get("is_new")));
        
        // 分类名称（如果查询结果中包含）
        dto.setCategoryName((String) product.get("category_name"));
        
        logger.debug("convertToProductDTO: 转换商品 - ID={}, 名称={}, 简介={}, 详情长度={}", 
                dto.getProductId(), dto.getProductName(), dto.getProductBrief(), 
                dto.getDetail() != null ? dto.getDetail().length() : 0);
        
        return dto;
    }
    
    /**
     * 获取Long值 - 确保ID类型为Long
     * @param obj 对象
     * @return Long值
     */
    private Long getLongValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.valueOf(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取Integer值 - 确保数量类型为Integer
     * @param obj 对象
     * @return Integer值
     */
    private Integer getIntegerValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.valueOf(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取Double值
     * @param obj 对象
     * @return Double值
     */
    private Double getDoubleValue(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Double) {
            return (Double) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.valueOf(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取Boolean值
     * @param obj 对象
     * @return Boolean值
     */
    private Boolean getBooleanValue(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue() == 1;
        }
        if (obj instanceof String) {
            return "1".equals(obj) || "true".equalsIgnoreCase((String) obj);
        }
        return false;
    }
}
