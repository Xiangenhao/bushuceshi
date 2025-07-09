package org.example.afd.controller;

import org.example.afd.dto.CategoryDTO;
import org.example.afd.dto.MerchantDTO;
import org.example.afd.dto.OrderDTO;
import org.example.afd.dto.ProductDTO;
import org.example.afd.dto.StatisticsDTO;
import org.example.afd.entity.Order;
import org.example.afd.model.*;
import org.example.afd.service.MerchantService;
import org.example.afd.service.ProductService;
import org.example.afd.service.OrderService;
import org.example.afd.service.StatisticsService;
import org.example.afd.service.UserService;
import org.example.afd.utils.CommonUtil;
import org.example.afd.utils.FileUtil;
import org.example.afd.utils.UserIdHolder;
import org.example.afd.model.Result;
import org.example.afd.model.ResultCode;
import org.example.afd.exception.OrderStatusException;
import org.example.afd.pojo.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.stream.Collectors;

/**
 * 商家控制器
 */
@RestController
@RequestMapping("/api/v1/merchants")
@Tag(name = "商家管理 API")
public class MerchantController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private UserService userService;

    /**
     * 获取商家基本信息
     *
     * @param merchantId 商家ID
     * @return 商家基本信息
     */
    @GetMapping("/{merchantId}")
    public Result<MerchantDTO> getMerchantInfo(@PathVariable Long merchantId) {
        logger.info("获取商家基本信息, merchantId: {}", merchantId);
        try {
            MerchantDTO merchantDTO = merchantService.getMerchantInfo(merchantId);
            return Result.success(merchantDTO);
        } catch (Exception e) {
            logger.error("获取商家信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家信息失败");
        }
    }

    /**
     * 获取商家详情
     *
     * @param merchantId 商家ID
     * @return 商家详情
     */
    @GetMapping("/{merchantId}/detail")
    public Result<MerchantDTO> getMerchantDetail(@PathVariable Long merchantId) {
        logger.info("获取商家详情, merchantId: {}", merchantId);
        try {
            MerchantDTO merchantDTO = merchantService.getMerchantDetail(merchantId);
            return Result.success(merchantDTO);
        } catch (Exception e) {
            logger.error("获取商家详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家详情失败");
        }
    }

    /**
     * 获取推荐商家列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 商家列表
     */
    @GetMapping("/recommended")
    public Result<List<MerchantDTO>> getRecommendedMerchants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取推荐商家, page: {}, size: {}", page, size);
        try {
            List<MerchantDTO> merchantDTOList = merchantService.getRecommendedMerchants(page, size);
            return Result.success(merchantDTOList);
        } catch (Exception e) {
            logger.error("获取推荐商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取推荐商家失败");
        }
    }

    /**
     * 获取附近商家列表
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param distance  距离（单位：公里）
     * @param page      页码
     * @param size      每页数量
     * @return 商家列表
     */
    @GetMapping("/nearby")
    public Result<List<MerchantDTO>> getNearbyMerchants(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5") double distance,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取附近商家, latitude: {}, longitude: {}, distance: {}, page: {}, size: {}",
                latitude, longitude, distance, page, size);
        try {
            List<MerchantDTO> merchantDTOList = merchantService.getNearbyMerchants(
                    latitude, longitude, distance, page, size);
            return Result.success(merchantDTOList);
        } catch (Exception e) {
            logger.error("获取附近商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取附近商家失败");
        }
    }

    /**
     * 搜索商家
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页数量
     * @return 商家列表
     */
    @GetMapping("/search")
    public Result<List<MerchantDTO>> searchMerchants(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("搜索商家, keyword: {}, page: {}, size: {}", keyword, page, size);
        try {
            List<MerchantDTO> merchantDTOList = merchantService.searchMerchants(keyword, page, size);
            return Result.success(merchantDTOList);
        } catch (Exception e) {
            logger.error("搜索商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "搜索商家失败");
        }
    }

    /**
     * 获取商家商品列表
     *
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页数量
     * @return 商品列表
     */
    @GetMapping("/{merchantId}/products")
    public Result<List<ProductDTO>> getMerchantProducts(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取商家商品, merchantId: {}, page: {}, size: {}", merchantId, page, size);
        try {
            List<ProductDTO> productDTOList = productService.getMerchantProducts(merchantId, page, size);
            return Result.success(productDTOList);
        } catch (Exception e) {
            logger.error("获取商家商品失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家商品失败");
        }
    }

    /**
     * 按分类获取商家商品列表
     *
     * @param merchantId 商家ID
     * @param categoryId 分类ID
     * @param page       页码
     * @param size       每页数量
     * @return 商品列表
     */
    @GetMapping("/{merchantId}/categories/{categoryId}/products")
    public Result<List<ProductDTO>> getMerchantProductsByCategory(
            @PathVariable Long merchantId,
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取商家特定分类商品, merchantId: {}, categoryId: {}, page: {}, size: {}",
                merchantId, categoryId, page, size);
        try {
            List<ProductDTO> productDTOList = productService.getMerchantProductsByCategory(
                    merchantId, categoryId, page, size);
            return Result.success(productDTOList);
        } catch (Exception e) {
            logger.error("获取商家分类商品失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家分类商品失败");
        }
    }

    /**
     * 获取商家分类列表
     *
     * @param merchantId 商家ID
     * @return 分类列表
     */
    @GetMapping("/{merchantId}/categories")
    public Result<List<CategoryDTO>> getMerchantCategories(@PathVariable Long merchantId) {
        logger.info("获取商家分类, merchantId: {}", merchantId);
        try {
            List<CategoryDTO> categoryDTOList = merchantService.getMerchantCategories(merchantId);
            return Result.success(categoryDTOList);
        } catch (Exception e) {
            logger.error("获取商家分类失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家分类失败");
        }
    }

    /**
     * 关注商家
     *
     * @param merchantId 商家ID
     * @return 是否成功
     */
    @PostMapping("/{merchantId}/follow")
    public Result<Boolean> followMerchant(
            @PathVariable Long merchantId,
            @RequestParam Long userId) {
        logger.info("关注商家, merchantId: {}, userId: {}", merchantId, userId);
        try {
            boolean result = merchantService.followMerchant(userId, merchantId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("关注商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "关注商家失败");
        }
    }

    /**
     * 取消关注商家
     *
     * @param merchantId 商家ID
     * @return 是否成功
     */
    @DeleteMapping("/{merchantId}/unfollow")
    public Result<Boolean> unfollowMerchant(
            @PathVariable Long merchantId,
            @RequestParam Long userId) {
        logger.info("取消关注商家, merchantId: {}, userId: {}", merchantId, userId);
        try {
            boolean result = merchantService.unfollowMerchant(userId, merchantId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("取消关注商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "取消关注商家失败");
        }
    }

    /**
     * 获取用户关注的商家列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 商家列表
     */
    @GetMapping("/users/{userId}/followedMerchants")
    public Result<List<MerchantDTO>> getUserFollowedMerchants(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取用户关注的商家, userId: {}, page: {}, size: {}", userId, page, size);
        try {
            List<MerchantDTO> merchantDTOList = merchantService.getUserFollowedMerchants(userId, page, size);
            return Result.success(merchantDTOList);
        } catch (Exception e) {
            logger.error("获取用户关注的商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取用户关注的商家失败");
        }
    }

    /**
     * 评价商家
     *
     * @param merchantId 商家ID
     * @param status 状态
     * @param score 评分
     * @param reason 评价原因
     * @return 是否成功
     */
    @PostMapping("/{merchantId}/reviews")
    public Result<Boolean> reviewMerchant(
            @PathVariable Long merchantId,
            @RequestParam Integer status,
            @RequestParam Double score,
            @RequestParam String reason) {
        logger.info("评价商家, merchantId: {}, status: {}, score: {}", merchantId, status, score);
        try {
            // 将多个参数组织为Map传递给服务层
            Map<String, Object> reviewParams = new HashMap<>();
            reviewParams.put("status", status);
            reviewParams.put("score", score);
            reviewParams.put("reason", reason);
            boolean result = merchantService.reviewMerchant(merchantId, reviewParams);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("评价商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "评价商家失败");
        }
    }

    /**
     * 更新商家信息
     *
     * @param merchantId  商家ID
     * @param merchantDTO 商家信息
     * @return 更新后的商家信息
     */
    @PutMapping("/{merchantId}")
    public Result<MerchantDTO> updateMerchantInfo(
            @PathVariable Long merchantId,
            @RequestBody MerchantDTO merchantDTO) {
        logger.info("更新商家信息, merchantId: {}, merchantDTO: {}", merchantId, merchantDTO);
        try {
            // 确保ID一致
            merchantDTO.setMerchantId(merchantId);
            MerchantDTO updatedDTO = merchantService.updateMerchantInfo(merchantDTO);
            return Result.success(updatedDTO);
        } catch (Exception e) {
            logger.error("更新商家信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商家信息失败");
        }
    }

    /**
     * 上传商家Logo
     *
     * @param merchantId 商家ID
     * @param file       Logo文件
     * @return 更新后的商家信息
     */
    @PostMapping("/{merchantId}/logo")
    public Result<MerchantDTO> uploadLogo(
            @PathVariable Long merchantId,
            @RequestParam("file") MultipartFile file) {
        logger.info("上传商家Logo, merchantId: {}", merchantId);
        try {
            MerchantDTO updatedDTO = merchantService.uploadLogo(merchantId, file);
            return Result.success(updatedDTO);
        } catch (Exception e) {
            logger.error("上传商家Logo失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "上传商家Logo失败");
        }
    }

    /**
     * 上传商家营业执照
     *
     * @param merchantId 商家ID
     * @param file       营业执照文件
     * @return 更新后的商家信息
     */
    @PostMapping("/{merchantId}/license")
    public Result<MerchantDTO> uploadLicense(
            @PathVariable Long merchantId,
            @RequestParam("file") MultipartFile file) {
        logger.info("上传商家营业执照, merchantId: {}", merchantId);
        try {
            MerchantDTO updatedDTO = merchantService.uploadLicense(merchantId, file);
            return Result.success(updatedDTO);
        } catch (Exception e) {
            logger.error("上传商家营业执照失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "上传商家营业执照失败");
        }
    }

    /**
     * 更新商家支付信息
     *
     * @param merchantId  商家ID
     * @param paymentInfo 支付信息
     * @return 更新后的商家信息
     */
    @PutMapping("/{merchantId}/payment")
    public Result<MerchantDTO> updatePaymentInfo(
            @PathVariable Long merchantId,
            @RequestBody Map<String, Object> paymentInfo) {
        logger.info("更新商家支付信息, merchantId: {}, paymentInfo: {}", merchantId, paymentInfo);
        try {
            // 直接传递支付信息Map给service层
            MerchantDTO updatedDTO = merchantService.updatePaymentInfo(merchantId, paymentInfo);
            return Result.success(updatedDTO);
        } catch (Exception e) {
            logger.error("更新商家支付信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商家支付信息失败");
        }
    }

    /**
     * 获取商家统计数据
     *
     * @param merchantId 商家ID
     * @return 统计数据
     */
    @GetMapping("/{merchantId}/statistics")
    public Result<StatisticsDTO> getMerchantStatistics(@PathVariable Long merchantId) {
        logger.info("获取商家统计数据, merchantId: {}", merchantId);
        try {
            StatisticsDTO statisticsDTO = statisticsService.getMerchantStatistics(merchantId);
            return Result.success(statisticsDTO);
        } catch (Exception e) {
            logger.error("获取商家统计数据失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家统计数据失败");
        }
    }

    /**
     * 获取商家订单统计（按状态分类）
     *
     * @param merchantId 商家ID
     * @return 订单统计
     */
    @GetMapping("/{merchantId}/orders/statistics")
    public Result<Map<String, Integer>> getMerchantOrderStatistics(@PathVariable Long merchantId) {
        logger.info("获取商家订单统计, merchantId: {}", merchantId);
        try {
            // 调用已实现的方法获取订单统计
            Map<String, Integer> orderStatistics = orderService.getOrderCountByStatusForMerchant(merchantId);
            return Result.success(orderStatistics);
        } catch (Exception e) {
            logger.error("获取商家订单统计失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家订单统计失败");
        }
    }

    /**
     * 获取商家销售额统计（指定时间段）
     *
     * @param merchantId 商家ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param type       统计类型（day/week/month）
     * @return 销售额统计
     */
    @GetMapping("/{merchantId}/sales/statistics")
    public Result<Map<String, Double>> getMerchantSalesStatistics(
            @PathVariable Long merchantId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String type) {
        logger.info("获取商家销售额统计, merchantId: {}, startDate: {}, endDate: {}, type: {}",
                merchantId, startDate, endDate, type);
        try {
            // 将String日期转换为LocalDate
            LocalDate startLocalDate = LocalDate.parse(startDate);
            LocalDate endLocalDate = LocalDate.parse(endDate);
            
            Map<String, Double> salesStatistics = statisticsService.getMerchantSalesStatistics(
                    merchantId, startLocalDate, endLocalDate, type);
            return Result.success(salesStatistics);
        } catch (Exception e) {
            logger.error("获取商家销售额统计失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家销售额统计失败");
        }
    }

    /**
     * 获取商家商品销售排行
     *
     * @param merchantId 商家ID
     * @param limit      数量限制
     * @return 商品销售排行
     */
    @GetMapping("/{merchantId}/products/ranking")
    public Result<List<Map<String, Object>>> getProductSalesRanking(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "10") Integer limit) {
        logger.info("获取商家商品销售排行, merchantId: {}, limit: {}", merchantId, limit);
        try {
            List<Map<String, Object>> productRanking = productService.getProductSalesRanking(merchantId, limit);
            return Result.success(productRanking);
        } catch (Exception e) {
            logger.error("获取商家商品销售排行失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家商品销售排行失败");
        }
    }

    /**
     * 获取商家待处理的退款申请列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页数量
     * @return 退款申请列表
     */
    @GetMapping("/{merchantId}/refunds")
    public Result<List<Order>> getPendingRefunds(
            @PathVariable("merchantId") Long merchantId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        List<Order> refunds = orderService.getPendingRefundsByMerchantId(merchantId, page, size);
        return Result.success(refunds);
    }

    /**
     * 检查用户是否已注册为商家
     *
     * @param userId 用户ID
     * @return 商家信息，如未注册则返回null
     */
    @GetMapping("/check")
    public Result<MerchantDTO> checkMerchantStatus(@RequestParam Long userId) {
        logger.info("检查用户是否已注册为商家, userId: {}", userId);
        try {
            MerchantDTO merchantDTO = merchantService.getMerchantByUserId(userId);
            return Result.success(merchantDTO);
        } catch (Exception e) {
            logger.error("检查用户商家状态失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "检查用户商家状态失败");
        }
    }
    
    /**
     * 注册成为商家
     *
     * @param merchantDTO 商家信息
     * @return 注册后的商家信息
     */
    @PostMapping("/register")
    public Result<MerchantDTO> registerMerchant(@RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        logger.info("注册成为商家, merchantDTO: {}", merchantDTO);
        try {
            // 从请求头获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("从请求属性获取用户ID: {}", userId);
            
            // 如果没有从请求头获取到用户ID，尝试从UserIdHolder中获取
            if (userId == null) {
                // 直接从UserIdHolder中获取userId
                userId = org.example.afd.utils.UserIdHolder.getUserId();
                logger.info("从UserIdHolder获取用户ID: {}", userId);
            }
            
            // 如果还是获取不到，尝试从请求头中获取X-User-ID
            if (userId == null) {
                String userIdHeader = request.getHeader("X-User-ID");
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        userId = Integer.parseInt(userIdHeader);
                        logger.info("从X-User-ID请求头获取到用户ID: {}", userId);
                    } catch (NumberFormatException e) {
                        logger.warn("X-User-ID格式错误: {}", userIdHeader);
                    }
                }
            }
            
            // 如果从请求头获取不到，再尝试从DTO中获取用户ID
            if (userId == null && merchantDTO.getUserId() != null) {
                userId = merchantDTO.getUserId().intValue();
                logger.info("从请求体获取用户ID: {}", userId);
            }
            
            // 数据验证
            if (userId == null || userId <= 0) {
                logger.error("无法获取有效的用户ID");
                return Result.failure(ResultCode.PARAM_ERROR, "用户ID不能为空");
            }
            
            // 设置用户ID到DTO中
            merchantDTO.setUserId(userId.longValue());
            logger.info("最终设置的用户ID: {}", userId);
            
            if (merchantDTO.getMerchantName() == null || merchantDTO.getMerchantName().isEmpty()) {
                return Result.failure(ResultCode.PARAM_ERROR, "商家名称不能为空");
            }
            
            // 检查用户是否已注册为商家
            MerchantDTO existingMerchant = merchantService.getMerchantByUserId(merchantDTO.getUserId());
            if (existingMerchant != null) {
                return Result.failure(ResultCode.BUSINESS_ERROR, "该用户已注册为商家");
            }
            
            // 注册商家
            merchantDTO.setStatus(0); // 设置状态为待审核
            MerchantDTO registeredMerchant = merchantService.registerMerchant(merchantDTO);
            return Result.success(registeredMerchant);
        } catch (Exception e) {
            logger.error("注册商家失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "注册商家失败");
        }
    }

    /**
     * 按状态获取商家商品列表
     *
     * @param merchantId 商家ID
     * @param status 商品状态（可选）：1-上架，0-下架，null-全部
     * @param page 页码
     * @param size 每页数量
     * @return 商品列表
     */
    @GetMapping("/{merchantId}/products/status")
    public Result<List<ProductDTO>> getMerchantProductsByStatus(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("按状态获取商家商品, merchantId: {}, status: {}, page: {}, size: {}", 
                merchantId, status, page, size);
        try {
            List<ProductDTO> productDTOList = productService.getMerchantProductsByStatus(merchantId, status, page, size);
            return Result.success(productDTOList);
        } catch (Exception e) {
            logger.error("按状态获取商家商品失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商家商品失败");
        }
    }

    /**
     * 更新商品基本信息
     *
     * @param productId 商品ID
     * @param requestData 请求数据
     * @return 更新结果
     */
    @PutMapping("/products/{productId}/basic")
    public Result<Boolean> updateProductBasicInfo(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> requestData) {
        logger.info("更新商品基本信息, productId: {}", productId);
        try {
            String productName = (String) requestData.get("productName");
            String productBrief = (String) requestData.get("productBrief");
            Double price = requestData.get("price") != null ? 
                Double.valueOf(requestData.get("price").toString()) : null;
            Integer stock = requestData.get("stock") != null ? 
                Integer.valueOf(requestData.get("stock").toString()) : null;
            String unit = (String) requestData.get("unit");
            String keywords = (String) requestData.get("keywords");
            
            boolean result = productService.updateProductBasicInfo(productId, productName, 
                    productBrief, price, stock, unit, keywords);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("更新商品基本信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商品基本信息失败");
        }
    }

    /**
     * 更新商品主图
     *
     * @param productId 商品ID
     * @param requestData 请求数据
     * @return 更新结果
     */
    @PutMapping("/products/{productId}/main-image")
    public Result<Boolean> updateProductMainImage(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> requestData) {
        logger.info("更新商品主图, productId: {}", productId);
        try {
            String mainImage = (String) requestData.get("mainImage");
            boolean result = productService.updateProductMainImage(productId, mainImage);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("更新商品主图失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商品主图失败");
        }
    }

    /**
     * 更新商品详情
     *
     * @param productId 商品ID
     * @param requestData 请求数据
     * @return 更新结果
     */
    @PutMapping("/products/{productId}/detail")
    public Result<Boolean> updateProductDetail(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> requestData) {
        logger.info("更新商品详情, productId: {}", productId);
        try {
            String detail = (String) requestData.get("detail");
            boolean result = productService.updateProductDetail(productId, detail);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("更新商品详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商品详情失败");
        }
    }

    /**
     * 更新商品分类
     *
     * @param productId 商品ID
     * @param requestData 请求数据
     * @return 更新结果
     */
    @PutMapping("/products/{productId}/category")
    public Result<Boolean> updateProductCategory(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> requestData) {
        logger.info("更新商品分类, productId: {}", productId);
        try {
            Long categoryId = requestData.get("categoryId") != null ? 
                Long.valueOf(requestData.get("categoryId").toString()) : null;
            boolean result = productService.updateProductCategory(productId, categoryId);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("更新商品分类失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新商品分类失败");
        }
    }

    /**
     * 获取商品详情（用于编辑）
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/api/v1/merchants/products/{productId}")
    public Result<Map<String, Object>> getProductDetail(@PathVariable Long productId, HttpServletRequest request) {
        try {
            logger.info("获取商品详情: productId={}", productId);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            // 查询商品详情
            Map<String, Object> product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            // 验证商家权限
            Object merchantIdObj = product.get("merchant_id");
            Long productMerchantId = merchantIdObj instanceof Number ? 
                ((Number) merchantIdObj).longValue() : null;
            if (productMerchantId == null || !productMerchantId.equals(merchantId)) {
                return Result.error("无权限访问此商品");
            }
            
            return Result.success(product);
            
        } catch (Exception e) {
            logger.error("获取商品详情失败: productId=" + productId, e);
            return Result.error("获取商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品基本信息
     * @param productId 商品ID
     * @param requestData 更新数据
     * @return 更新结果
     */
    @PutMapping("/api/v1/merchants/products/{productId}/basic")
    public Result<Boolean> updateProductBasicInfo(@PathVariable Long productId, 
                                                 @RequestBody Map<String, Object> requestData,
                                                 HttpServletRequest request) {
        try {
            logger.info("更新商品基本信息: productId={}, data={}", productId, requestData);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            // 验证商品权限（简化版本）
            Map<String, Object> product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            Object merchantIdObj = product.get("merchant_id");
            Long productMerchantId = merchantIdObj instanceof Number ? 
                ((Number) merchantIdObj).longValue() : null;
            if (productMerchantId == null || !productMerchantId.equals(merchantId)) {
                return Result.error("无权限操作此商品");
            }
            
            // 提取更新参数
            String productName = (String) requestData.get("productName");
            String productBrief = (String) requestData.get("productBrief");
            Double price = requestData.get("price") instanceof Number ? 
                ((Number) requestData.get("price")).doubleValue() : null;
            Integer stock = requestData.get("stock") instanceof Number ? 
                ((Number) requestData.get("stock")).intValue() : null;
            String unit = (String) requestData.get("unit");
            String keywords = (String) requestData.get("keywords");
            
            // 调用现有的更新方法
            boolean result = productService.updateProductBasicInfo(productId, productName, 
                    productBrief, price, stock, unit, keywords);
            
            if (result) {
                logger.info("商品基本信息更新成功: productId={}", productId);
                return Result.success(true);
            } else {
                return Result.error("更新失败");
            }
            
        } catch (Exception e) {
            logger.error("更新商品基本信息失败: productId=" + productId, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品描述
     * @param productId 商品ID
     * @param requestData 描述数据
     * @return 更新结果
     */
    @PutMapping("/api/v1/merchants/products/{productId}/description")
    public Result<Boolean> updateProductDescription(@PathVariable Long productId,
                                                   @RequestBody Map<String, Object> requestData,
                                                   HttpServletRequest request) {
        try {
            logger.info("更新商品描述: productId={}", productId);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            String description = (String) requestData.get("description");
            if (description == null) {
                return Result.error("描述内容不能为空");
            }
            
            // 验证商品权限
            Map<String, Object> product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            Object merchantIdObj = product.get("merchant_id");
            Long productMerchantId = merchantIdObj instanceof Number ? 
                ((Number) merchantIdObj).longValue() : null;
            if (productMerchantId == null || !productMerchantId.equals(merchantId)) {
                return Result.error("无权限操作此商品");
            }
            
            // 调用现有的更新方法
            boolean result = productService.updateProductDetail(productId, description);
            
            if (result) {
                logger.info("商品描述更新成功: productId={}", productId);
                return Result.success(true);
            } else {
                return Result.error("更新失败");
            }
            
        } catch (Exception e) {
            logger.error("更新商品描述失败: productId=" + productId, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品分类
     * @param productId 商品ID
     * @param requestData 分类数据
     * @return 更新结果
     */
    @PutMapping("/api/v1/merchants/products/{productId}/category")
    public Result<Boolean> updateProductCategory(@PathVariable Long productId,
                                                @RequestBody Map<String, Object> requestData,
                                                HttpServletRequest request) {
        try {
            logger.info("更新商品分类: productId={}", productId);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            Object categoryIdObj = requestData.get("categoryId");
            if (categoryIdObj == null) {
                return Result.error("分类ID不能为空");
            }
            
            Long categoryId;
            if (categoryIdObj instanceof Number) {
                categoryId = ((Number) categoryIdObj).longValue();
            } else {
                return Result.error("分类ID格式错误");
            }
            
            // 验证商品权限
            Map<String, Object> product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            Object merchantIdObj = product.get("merchant_id");
            Long productMerchantId = merchantIdObj instanceof Number ? 
                ((Number) merchantIdObj).longValue() : null;
            if (productMerchantId == null || !productMerchantId.equals(merchantId)) {
                return Result.error("无权限操作此商品");
            }
            
            // 调用现有的更新方法
            boolean result = productService.updateProductCategory(productId, categoryId);
            
            if (result) {
                logger.info("商品分类更新成功: productId={}, categoryId={}", productId, categoryId);
                return Result.success(true);
            } else {
                return Result.error("更新失败");
            }
            
        } catch (Exception e) {
            logger.error("更新商品分类失败: productId=" + productId, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品主图
     * @param productId 商品ID
     * @param imageFile 图片文件
     * @return 更新结果
     */
    @PostMapping("/api/v1/merchants/products/{productId}/image")
    public Result<Boolean> updateProductImage(@PathVariable Long productId,
                                             @RequestParam("image") MultipartFile imageFile,
                                             HttpServletRequest request) {
        try {
            logger.info("更新商品主图: productId={}", productId);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            if (imageFile == null || imageFile.isEmpty()) {
                return Result.error("请选择图片文件");
            }
            
            // 验证商品权限
            Map<String, Object> product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            Object merchantIdObj = product.get("merchant_id");
            Long productMerchantId = merchantIdObj instanceof Number ? 
                ((Number) merchantIdObj).longValue() : null;
            if (productMerchantId == null || !productMerchantId.equals(merchantId)) {
                return Result.error("无权限操作此商品");
            }
            
            // 上传图片（暂时模拟上传成功，返回固定URL）
            String imageUrl = "/uploads/products/" + System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            // TODO: 实际的文件上传逻辑
            logger.info("模拟图片上传成功: {}", imageUrl);
            
            // 调用现有的更新方法
            boolean result = productService.updateProductMainImage(productId, imageUrl);
            
            if (result) {
                logger.info("商品主图更新成功: productId={}, imageUrl={}", productId, imageUrl);
                return Result.success(true);
            } else {
                return Result.error("更新失败");
            }
            
        } catch (Exception e) {
            logger.error("更新商品主图失败: productId=" + productId, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品
     * @param productId 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/api/v1/merchants/products/{productId}")
    public Result<Boolean> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        try {
            logger.info("删除商品: productId={}", productId);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            // 调用现有的删除方法（已包含权限验证）
            boolean result = productService.deleteProductByUser(productId, merchantId);
            
            if (result) {
                logger.info("商品删除成功: productId={}", productId);
                return Result.success(true);
            } else {
                return Result.error("删除失败");
            }
            
        } catch (Exception e) {
            logger.error("删除商品失败: productId=" + productId, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 切换商品状态（上架/下架）
     * @param productId 商品ID
     * @param requestData 状态数据
     * @return 操作结果
     */
    @PutMapping("/api/v1/merchants/products/{productId}/status")
    public Result<Boolean> toggleProductStatus(@PathVariable Long productId,
                                              @RequestBody Map<String, Object> requestData,
                                              HttpServletRequest request) {
        try {
            logger.info("切换商品状态: productId={}", productId);
            
            Long merchantId = (Long) request.getAttribute("userId");
            if (merchantId == null) {
                return Result.error("用户未登录");
            }
            
            Object statusObj = requestData.get("status");
            if (statusObj == null) {
                return Result.error("状态参数不能为空");
            }
            
            Integer newStatus;
            if (statusObj instanceof Number) {
                newStatus = ((Number) statusObj).intValue();
            } else {
                return Result.error("状态参数格式错误");
            }
            
            if (newStatus != 0 && newStatus != 1) {
                return Result.error("状态参数无效，只能是0（下架）或1（上架）");
            }
            
            // 验证商品权限
            Map<String, Object> product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            Object merchantIdObj = product.get("merchant_id");
            Long productMerchantId = merchantIdObj instanceof Number ? 
                ((Number) merchantIdObj).longValue() : null;
            if (productMerchantId == null || !productMerchantId.equals(merchantId)) {
                return Result.error("无权限操作此商品");
            }
            
            // 调用现有的更新状态方法
            boolean result = productService.updateProductStatus(productId, newStatus);
            
            if (result) {
                String statusText = newStatus == 1 ? "上架" : "下架";
                logger.info("商品状态切换成功: productId={}, status={} ({})", productId, newStatus, statusText);
                return Result.success(true);
            } else {
                return Result.error("状态切换失败");
            }
            
        } catch (Exception e) {
            logger.error("切换商品状态失败: productId=" + productId, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    // =========================== 订单管理相关API ===========================

    // 高级订单API实现空的，已删除。请使用MerchantOrderController中的基础API

    /**
     * 获取订单统计数据
     */
    @GetMapping("/orders/statistics")
    public Result<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            Date targetDate = date != null ? date : new Date();
            
            Map<String, Object> statistics = orderService.getOrderStatistics(merchantId, targetDate);
            return Result.success(statistics);
            
        } catch (Exception e) {
            logger.error("获取订单统计失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 批量发货
     */
    @PostMapping("/orders/batch-ship")
    public Result<Map<String, Object>> batchShipOrders(@RequestBody Map<String, Object> request) {
        try {
            Long merchantId = getCurrentMerchantId();
            List<String> orderNumbers = (List<String>) request.get("orderNumbers");
            String logisticsCompany = (String) request.get("logisticsCompany");
            String trackingNumber = (String) request.get("trackingNumber");
            String shipmentNote = (String) request.get("shipmentNote");
            
            if (orderNumbers == null || orderNumbers.isEmpty()) {
                return Result.error("请选择要发货的订单");
            }
            
            if (logisticsCompany == null || logisticsCompany.trim().isEmpty()) {
                return Result.error("请选择物流公司");
            }
            
            Map<String, Object> shipmentInfo = new HashMap<>();
            shipmentInfo.put("merchantId", merchantId);
            shipmentInfo.put("orderNumbers", orderNumbers);
            shipmentInfo.put("logisticsCompany", logisticsCompany);
            shipmentInfo.put("trackingNumber", trackingNumber);
            shipmentInfo.put("shipmentNote", shipmentNote);
            shipmentInfo.put("shipmentTime", LocalDateTime.now());
            
            Map<String, Object> result = orderService.batchShipOrders(shipmentInfo);
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("批量发货失败", e);
            return Result.error("批量发货失败: " + e.getMessage());
        }
    }

    /**
     * 单个订单发货
     */
    @PostMapping("/orders/{orderNumber}/ship")
    public Result<String> shipOrder(
            @PathVariable String orderNumber,
            @RequestBody Map<String, Object> request) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            String logisticsCompany = (String) request.get("logisticsCompany");
            String trackingNumber = (String) request.get("trackingNumber");
            String shipmentNote = (String) request.get("shipmentNote");
            
            if (logisticsCompany == null || logisticsCompany.trim().isEmpty()) {
                return Result.error("请选择物流公司");
            }
            
            // 验证订单是否属于当前商家且状态正确
            if (!orderService.canShipOrder(orderNumber, merchantId)) {
                return Result.error("订单状态不正确或权限不足");
            }
            
            Map<String, Object> shipmentInfo = new HashMap<>();
            shipmentInfo.put("orderNumber", orderNumber);
            shipmentInfo.put("merchantId", merchantId);
            shipmentInfo.put("logisticsCompany", logisticsCompany);
            shipmentInfo.put("trackingNumber", trackingNumber);
            shipmentInfo.put("shipmentNote", shipmentNote);
            shipmentInfo.put("shipmentTime", LocalDateTime.now());
            
            boolean success = orderService.shipOrder(shipmentInfo);
            
            if (success) {
                return Result.success("发货成功");
            } else {
                return Result.error("发货失败");
            }
            
        } catch (Exception e) {
            logger.error("订单发货失败", e);
            return Result.error("发货失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加订单备注
     */
    @PostMapping("/orders/batch-remark")
    public Result<Map<String, Object>> batchAddOrderRemark(@RequestBody Map<String, Object> request) {
        try {
            Long merchantId = getCurrentMerchantId();
            List<String> orderNumbers = (List<String>) request.get("orderNumbers");
            String remark = (String) request.get("remark");
            
            if (orderNumbers == null || orderNumbers.isEmpty()) {
                return Result.error("请选择要备注的订单");
            }
            
            if (remark == null || remark.trim().isEmpty()) {
                return Result.error("请输入备注内容");
            }
            
            Map<String, Object> remarkInfo = new HashMap<>();
            remarkInfo.put("merchantId", merchantId);
            remarkInfo.put("orderNumbers", orderNumbers);
            remarkInfo.put("remark", remark.trim());
            remarkInfo.put("remarkTime", LocalDateTime.now());
            
            Map<String, Object> result = orderService.batchAddOrderRemark(remarkInfo);
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("批量添加备注失败", e);
            return Result.error("批量添加备注失败: " + e.getMessage());
        }
    }

    /**
     * 添加订单备注
     */
    @PostMapping("/orders/{orderNumber}/remark")
    public Result<String> addOrderRemark(
            @PathVariable String orderNumber,
            @RequestBody Map<String, String> request) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            String remark = request.get("remark");
            
            if (remark == null || remark.trim().isEmpty()) {
                return Result.error("请输入备注内容");
            }
            
            // 验证订单是否属于当前商家
            if (!orderService.isOrderBelongToMerchant(orderNumber, merchantId)) {
                return Result.error("权限不足");
            }
            
            boolean success = orderService.addOrderRemark(orderNumber, merchantId, remark.trim());
            
            if (success) {
                return Result.success("备注添加成功");
            } else {
                return Result.error("备注添加失败");
            }
            
        } catch (Exception e) {
            logger.error("添加订单备注失败", e);
            return Result.error("备注添加失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/orders/{orderNumber}/cancel")
    public Result<String> cancelOrder(@PathVariable String orderNumber, @RequestBody Map<String, String> request) {
        try {
            Long merchantId = getCurrentMerchantId();
            if (merchantId == null) {
                logger.warn("取消订单失败: 当前用户无商家权限, orderNumber={}", orderNumber);
                return Result.error(403, "当前用户无商家权限");
            }
            
            // 获取取消原因，如果没有提供则使用默认原因
            String cancelReason = request != null ? request.get("reason") : null;
            if (cancelReason == null || cancelReason.trim().isEmpty()) {
                cancelReason = "商家取消订单";
            }
            
            logger.info("商家取消订单请求: orderNumber={}, merchantId={}, reason={}", orderNumber, merchantId, cancelReason);

            // 检查订单是否可以取消
            if (!orderService.canCancelOrder(orderNumber, merchantId)) {
                logger.warn("订单无法取消: orderNumber={}, merchantId={}", orderNumber, merchantId);
                return Result.error("当前状态无法取消订单或订单不属于该商家");
            }

            // 执行取消订单操作
            boolean success = orderService.cancelOrder(orderNumber, merchantId, cancelReason);
            if (success) {
                logger.info("订单取消成功: orderNumber={}, merchantId={}", orderNumber, merchantId);
                return Result.success("订单取消成功");
            } else {
                logger.error("订单取消失败: orderNumber={}, merchantId={}", orderNumber, merchantId);
                return Result.error("取消订单失败，请稍后重试");
            }
        } catch (OrderStatusException e) {
            logger.warn("取消订单失败 (状态不允许): 订单号={}, 错误: {}", orderNumber, e.getMessage());
            return Result.error(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("取消订单失败 (安全异常): {}", e.getMessage());
            return Result.error(401, e.getMessage());
        } catch (Exception e) {
            logger.error("取消订单时发生意外错误: 订单号={}", orderNumber, e);
            return Result.error(500, "系统内部错误，请联系技术支持");
        }
    }

    /**
     * 商家处理退款申请
     * @param orderNumber 订单号
     * @param requestBody 请求体，包含 "approved" (boolean) 和 "reason" (String)
     * @return 操作结果
     */
    @PostMapping("/orders/{orderNumber}/refund")
    public Result<String> handleRefund(
            @PathVariable String orderNumber,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            Boolean approved = (Boolean) requestBody.get("approved");
            String reason = (String) requestBody.get("reason");
            
            if (approved == null) {
                return Result.error("缺少处理结果（同意或拒绝）");
            }
            
            // 调用重构后的退款处理方法
            Map<String, Object> refundInfo = new HashMap<>();
            refundInfo.put("orderNumber", orderNumber);
            refundInfo.put("merchantId", merchantId);
            refundInfo.put("approved", approved);
            refundInfo.put("reason", reason);
            
            boolean success = orderService.handleRefundRequest(refundInfo);
            
            if (success) {
                return Result.success("退款处理成功");
            } else {
                return Result.error("退款处理失败");
            }
            
        } catch (Exception e) {
            logger.error("处理退款失败", e);
            return Result.error("处理退款失败: " + e.getMessage());
        }
    }

    /**
     * 获取物流信息
     */
    @GetMapping("/orders/{orderNumber}/logistics")
    public Result<Map<String, Object>> getLogisticsInfo(@PathVariable String orderNumber) {
        try {
            Long merchantId = getCurrentMerchantId();
            
            // 验证订单是否属于当前商家
            if (!orderService.isOrderBelongToMerchant(orderNumber, merchantId)) {
                return Result.error("权限不足");
            }
            
            Map<String, Object> logisticsInfo = orderService.getLogisticsInfo(orderNumber);
            return Result.success(logisticsInfo);
            
        } catch (Exception e) {
            logger.error("获取物流信息失败", e);
            return Result.error("获取物流信息失败");
        }
    }

    /**
     * 更新物流信息
     */
    @PutMapping("/orders/{orderNumber}/logistics")
    public Result<String> updateLogisticsInfo(
            @PathVariable String orderNumber,
            @RequestBody Map<String, String> request) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            String logisticsCompany = request.get("logisticsCompany");
            String trackingNumber = request.get("trackingNumber");
            
            // 验证订单是否属于当前商家
            if (!orderService.isOrderBelongToMerchant(orderNumber, merchantId)) {
                return Result.error("权限不足");
            }
            
            Map<String, Object> logisticsInfo = new HashMap<>();
            logisticsInfo.put("orderNumber", orderNumber);
            logisticsInfo.put("logisticsCompany", logisticsCompany);
            logisticsInfo.put("trackingNumber", trackingNumber);
            logisticsInfo.put("updateTime", LocalDateTime.now());
            
            boolean success = orderService.updateLogisticsInfo(logisticsInfo);
            
            if (success) {
                return Result.success("物流信息更新成功");
            } else {
                return Result.error("物流信息更新失败");
            }
            
        } catch (Exception e) {
            logger.error("更新物流信息失败", e);
            return Result.error("更新物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 订单数据导出
     */
    @GetMapping("/orders/export")
    public Result<String> exportOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "excel") String format) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            
            Map<String, Object> exportParams = new HashMap<>();
            exportParams.put("merchantId", merchantId);
            exportParams.put("status", status);
            exportParams.put("keyword", keyword);
            exportParams.put("startDate", startDate);
            exportParams.put("endDate", endDate);
            exportParams.put("format", format);
            
            String downloadUrl = orderService.exportOrders(exportParams);
            return Result.success(downloadUrl);
            
        } catch (Exception e) {
            logger.error("导出订单数据失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详细分析报告
     */
    @GetMapping("/orders/analysis")
    public Result<Map<String, Object>> getOrderAnalysis(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "7") int days) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            
            // 如果没有指定日期范围，使用最近指定天数
            if (startDate == null || endDate == null) {
                Calendar calendar = Calendar.getInstance();
                endDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, -days);
                startDate = calendar.getTime();
            }
            
            Map<String, Object> analysisParams = new HashMap<>();
            analysisParams.put("merchantId", merchantId);
            analysisParams.put("startDate", startDate);
            analysisParams.put("endDate", endDate);
            
            Map<String, Object> analysis = orderService.getOrderAnalysis(analysisParams);
            return Result.success(analysis);
            
        } catch (Exception e) {
            logger.error("获取订单分析失败", e);
            return Result.error("获取订单分析失败");
        }
    }

    /**
     * 获取热销商品排行
     */
    @GetMapping("/orders/hot-products")
    public Result<List<Map<String, Object>>> getHotProducts(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date startDate = calendar.getTime();
            
            Map<String, Object> params = new HashMap<>();
            params.put("merchantId", merchantId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("limit", limit);
            
            List<Map<String, Object>> hotProducts = orderService.getHotProducts(params);
            return Result.success(hotProducts);
            
        } catch (Exception e) {
            logger.error("获取热销商品失败", e);
            return Result.error("获取热销商品失败");
        }
    }

    /**
     * 获取客户排行
     */
    @GetMapping("/orders/top-customers")
    public Result<List<Map<String, Object>>> getTopCustomers(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            Long merchantId = getCurrentMerchantId();
            
            Calendar calendar = Calendar.getInstance();
            Date endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date startDate = calendar.getTime();
            
            Map<String, Object> params = new HashMap<>();
            params.put("merchantId", merchantId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("limit", limit);
            
            List<Map<String, Object>> topCustomers = orderService.getTopCustomers(params);
            return Result.success(topCustomers);
            
        } catch (Exception e) {
            logger.error("获取客户排行失败", e);
            return Result.error("获取客户排行失败");
        }
    }

    /**
     * 获取当前商家ID的辅助方法
     */
    private Long getCurrentMerchantId() {
        Integer userId = UserIdHolder.getUserId();
        if (userId == null) {
            throw new SecurityException("无法获取用户ID，请先登录");
        }
        
        // 通过userId查询商家信息
        MerchantDTO merchant = merchantService.getMerchantByUserId(userId.longValue());
        if (merchant == null) {
            throw new SecurityException("当前用户不是商家或商家信息不存在");
        }
        return merchant.getMerchantId();
    }
    
    // ===== 新增的订单管理API =====
    
    /**
     * 获取订单统计数据（主页用）
     */
    @GetMapping("/{merchantId}/orders/dashboard-statistics")
    public Result<Map<String, Object>> getOrderStatistics(@PathVariable Long merchantId) {
        try {
            logger.info("获取商家订单统计数据，商家ID: {}", merchantId);
            
            Map<String, Object> statistics = new HashMap<>();
            
            // 获取今日订单数据
            Map<String, Object> todayData = orderService.getTodayOrderStatistics(merchantId);
            statistics.put("today", todayData);
            
            // 获取各状态订单数量
            Map<String, Integer> statusCount = orderService.getOrderCountByStatus(merchantId);
            statistics.put("statusCount", statusCount);
            
            // 获取本月销售额
            BigDecimal monthlySales = orderService.getMonthlySales(merchantId);
            statistics.put("monthlySales", monthlySales);
            
            // 获取总订单数
            Integer totalOrders = orderService.getTotalOrderCount(merchantId);
            statistics.put("totalOrders", totalOrders);
            
            logger.info("获取商家订单统计数据成功，商家ID: {}", merchantId);
            return Result.success(statistics);
            
        } catch (Exception e) {
            logger.error("获取商家订单统计数据失败，商家ID: {}", merchantId, e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取紧急订单数据
     */
    @GetMapping("/{merchantId}/orders/urgent")
    public Result<Map<String, Object>> getUrgentOrders(@PathVariable Long merchantId) {
        try {
            logger.info("获取商家紧急订单数据，商家ID: {}", merchantId);
            
            Map<String, Object> urgentData = new HashMap<>();
            
            // 获取超时未发货订单数量
            Integer overtimeCount = orderService.getOvertimeOrderCount(merchantId);
            urgentData.put("overtimeCount", overtimeCount);
            
            // 获取待处理退款订单数量
            Integer pendingRefundCount = orderService.getPendingRefundOrderCount(merchantId);
            urgentData.put("pendingRefundCount", pendingRefundCount);
            
            // 获取24小时内需发货订单数量
            Integer urgentShipCount = orderService.getUrgentShipOrderCount(merchantId);
            urgentData.put("urgentShipCount", urgentShipCount);
            
            logger.info("获取商家紧急订单数据成功，商家ID: {}", merchantId);
            return Result.success(urgentData);
            
        } catch (Exception e) {
            logger.error("获取商家紧急订单数据失败，商家ID: {}", merchantId, e);
            return Result.error("获取紧急订单数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取超时订单列表
     */
    @GetMapping("/{merchantId}/orders/overtime")
    public Result<List<OrderDTO>> getOvertimeOrders(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("获取商家超时订单列表，商家ID: {}, 页码: {}, 大小: {}", merchantId, page, size);
            
            List<OrderDTO> overtimeOrders = orderService.getOvertimeOrders(merchantId, page, size);
            
            logger.info("获取商家超时订单列表成功，商家ID: {}, 数量: {}", merchantId, overtimeOrders.size());
            return Result.success(overtimeOrders);
            
        } catch (Exception e) {
            logger.error("获取商家超时订单列表失败，商家ID: {}", merchantId, e);
            return Result.error("获取超时订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待处理退款订单列表
     */
    @GetMapping("/{merchantId}/orders/pending-refund")
    public Result<List<OrderDTO>> getPendingRefundOrders(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("获取商家待处理退款订单列表，商家ID: {}, 页码: {}, 大小: {}", merchantId, page, size);
            
            List<OrderDTO> pendingRefundOrders = orderService.getPendingRefundOrders(merchantId, page, size);
            
            logger.info("获取商家待处理退款订单列表成功，商家ID: {}, 数量: {}", merchantId, pendingRefundOrders.size());
            return Result.success(pendingRefundOrders);
            
        } catch (Exception e) {
            logger.error("获取商家待处理退款订单列表失败，商家ID: {}", merchantId, e);
            return Result.error("获取待处理退款订单列表失败: " + e.getMessage());
        }
    }
}