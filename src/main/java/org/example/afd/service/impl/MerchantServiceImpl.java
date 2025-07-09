package org.example.afd.service.impl;

import org.example.afd.dto.CategoryDTO;
import org.example.afd.dto.MerchantDTO;
import org.example.afd.mapper.CategoryMapper;
import org.example.afd.mapper.MerchantMapper;
import org.example.afd.mapper.UserRelationMapper;
import org.example.afd.model.Merchant;
import org.example.afd.service.CategoryService;
import org.example.afd.service.FileService;
import org.example.afd.service.MerchantService;
import org.example.afd.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商家服务实现类
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserRelationMapper userRelationMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;

    @Override
    public MerchantDTO getMerchantInfo(Long merchantId) {
        logger.info("获取商家信息, merchantId={}", merchantId);
        return merchantMapper.selectMerchantById(merchantId);
    }

    @Override
    public MerchantDTO getMerchantDetail(Long merchantId) {
        logger.info("=== MerchantServiceImpl.getMerchantDetail ===");
        logger.info("获取商家详情, merchantId={}", merchantId);
        
        try {
            MerchantDTO merchantDTO = merchantMapper.selectMerchantDetailById(merchantId);
            
            if (merchantDTO == null) {
                logger.warn("商家不存在: merchantId={}", merchantId);
                return null;
            }
            
            logger.info("成功获取商家详情: merchantId={}, merchantName={}", 
                      merchantDTO.getMerchantId(), merchantDTO.getMerchantName());
            return merchantDTO;
            
        } catch (Exception e) {
            logger.error("获取商家详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取商家详情失败", e);
        }
    }

    @Override
    public List<MerchantDTO> getRecommendedMerchants(int page, int size) {
        logger.info("获取推荐商家列表, page={}, size={}", page, size);
        int offset = (page - 1) * size;
        return merchantMapper.selectRecommendedMerchants(offset, size);
    }

    @Override
    public List<MerchantDTO> getNearbyMerchants(double latitude, double longitude, double distance, int page, int size) {
        logger.info("获取附近商家列表, latitude={}, longitude={}, distance={}, page={}, size={}",
                latitude, longitude, distance, page, size);
        int offset = (page - 1) * size;
        return merchantMapper.selectNearbyMerchants(latitude, longitude, distance, offset, size);
    }

    @Override
    public List<CategoryDTO> getMerchantCategories(Long merchantId) {
        logger.info("获取商家分类列表: merchantId={}", merchantId);
        try {
            // 直接使用CategoryMapper查询，转换结果为CategoryDTO列表
            List<Map<String, Object>> categories = categoryMapper.selectCategoriesByMerchantId(merchantId);
            List<CategoryDTO> categoryDTOList = new ArrayList<>();
            
            if (categories != null && !categories.isEmpty()) {
                for (Map<String, Object> category : categories) {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setCategoryId((Long) category.get("category_id"));
                    categoryDTO.setCategoryName((String) category.get("category_name"));
                    categoryDTO.setParentId((Long) category.get("parent_id"));
                    categoryDTO.setLevel((Integer) category.get("level"));
                    categoryDTO.setSort((Integer) category.get("sort_order"));
                    categoryDTO.setIcon((String) category.get("icon"));
                    categoryDTO.setStatus((Integer) category.get("status"));
                    
                    categoryDTOList.add(categoryDTO);
                }
            }
            
            return categoryDTOList;
        } catch (Exception e) {
            logger.error("获取商家分类列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<MerchantDTO> searchMerchants(String keyword, int page, int size) {
        logger.info("搜索商家, keyword={}, page={}, size={}", keyword, page, size);
        int offset = (page - 1) * size;
        return merchantMapper.searchMerchants(keyword, offset, size);
    }

    @Override
    @Transactional
    public Boolean followMerchant(Long merchantId, Long userId) {
        logger.info("关注商家, merchantId={}, userId={}", merchantId, userId);
        // 检查商家是否存在
        MerchantDTO merchantDTO = merchantMapper.selectMerchantById(merchantId);
        if (merchantDTO == null) {
            logger.error("商家不存在, merchantId={}", merchantId);
            return false;
        }

        // 插入关注关系
        int result = userRelationMapper.insertMerchantFollow(userId, merchantId);
        return result > 0;
    }

    @Override
    @Transactional
    public Boolean unfollowMerchant(Long merchantId, Long userId) {
        logger.info("取消关注商家, merchantId={}, userId={}", merchantId, userId);
        int result = userRelationMapper.deleteMerchantFollow(userId, merchantId);
        return result > 0;
    }

    @Override
    public List<MerchantDTO> getUserFollowedMerchants(Long userId, int page, int size) {
        logger.info("获取用户关注的商家列表, userId={}, page={}, size={}", userId, page, size);
        int offset = (page - 1) * size;
        return merchantMapper.selectUserFollowedMerchants(userId, offset, size);
    }

    @Override
    @Transactional
    public Boolean reviewMerchant(Long merchantId, Map<String, Object> reviewData) {
        logger.info("评价商家, merchantId={}, reviewData={}", merchantId, reviewData);
        try {
            // 验证必要参数
            if (!reviewData.containsKey("userId") || !reviewData.containsKey("rating") || !reviewData.containsKey("content")) {
                logger.error("评价参数不完整");
                return false;
            }

            Long userId = Long.valueOf(reviewData.get("userId").toString());
            Integer rating = Integer.valueOf(reviewData.get("rating").toString());
            String content = reviewData.get("content").toString();

            // 由于数据库中没有商家评价表，暂时移除评价功能
            // int result = merchantMapper.insertMerchantReview(merchantId, userId, rating, content);

            // // 更新商家评分
            // if (result > 0) {
            //     merchantMapper.updateMerchantRating(merchantId);
            // }

            // 暂时返回true，表示评价成功（实际未存储）
            logger.info("商家评价功能暂不可用，数据库中无相关表: merchantId={}, userId={}", merchantId, userId);
            return true;
        } catch (Exception e) {
            logger.error("评价商家失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public MerchantDTO updateMerchantInfo(MerchantDTO merchantDTO) {
        logger.info("更新商家信息, merchantDTO={}", merchantDTO);
        // 更新商家信息
        int result = merchantMapper.updateMerchant(merchantDTO);
        if (result > 0) {
            return merchantMapper.selectMerchantById(merchantDTO.getMerchantId());
        }
        return null;
    }

    @Override
    @Transactional
    public MerchantDTO uploadLogo(Long merchantId, MultipartFile file) {
        logger.info("上传商家Logo, merchantId={}", merchantId);
        try {
            // TODO: 推荐使用FileUploadController进行文件上传操作
            // 上传文件
            String fileUrl = fileService.uploadFile(file, "merchant/logo");

            // 更新商家Logo
            merchantMapper.updateMerchantLogo(merchantId, fileUrl);

            // 返回更新后的商家信息
            return merchantMapper.selectMerchantById(merchantId);
        } catch (Exception e) {
            logger.error("上传商家Logo失败", e);
            return null;
        }
    }

    @Override
    @Transactional
    public MerchantDTO uploadLicense(Long merchantId, MultipartFile file) {
        logger.info("上传商家营业执照, merchantId={}", merchantId);
        try {
            // TODO: 推荐使用FileUploadController进行文件上传操作
            // 上传文件
            String fileUrl = fileService.uploadFile(file, "merchant/license");

            // 更新商家营业执照
            merchantMapper.updateMerchantLicense(merchantId, fileUrl);

            // 返回更新后的商家信息
            return merchantMapper.selectMerchantById(merchantId);
        } catch (Exception e) {
            logger.error("上传商家营业执照失败", e);
            return null;
        }
    }

    @Override
    @Transactional
    public MerchantDTO updatePaymentInfo(Long merchantId, Map<String, Object> paymentInfo) {
        logger.info("更新商家支付信息, merchantId={}, paymentInfo={}", merchantId, paymentInfo);
        try {
            // 从Map中获取参数
            String bankAccount = (String) paymentInfo.get("bankAccount");
            String bankName = (String) paymentInfo.get("bankName");
            Integer settlementCycle = (Integer) paymentInfo.get("settlementCycle");
            
            // 调用Mapper更新支付信息
            int result = merchantMapper.updateMerchantPaymentInfo(merchantId, bankAccount, bankName, settlementCycle);
            
            if (result > 0) {
                // 返回更新后的商家信息
                return merchantMapper.selectMerchantById(merchantId);
            }
            return null;
        } catch (Exception e) {
            logger.error("更新商家支付信息失败", e);
            return null;
        }
    }

    /**
     * 根据用户ID获取商家信息
     *
     * @param userId 用户ID
     * @return 商家信息，如果用户不是商家则返回null
     */
    @Override
    public MerchantDTO getMerchantByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        try {
            // 查询商家信息
            Merchant merchant = merchantMapper.selectByUserId(userId);
            if (merchant == null) {
                return null;
            }

            // 转换为DTO
            return convertToDTO(merchant);
        } catch (Exception e) {
            logger.error("根据用户ID获取商家信息失败, userId: {}", userId, e);
            throw new RuntimeException("获取商家信息失败", e);
        }
    }

    /**
     * 注册商家
     *
     * @param merchantDTO 商家信息
     * @return 注册后的商家信息
     */
    @Override
    public MerchantDTO registerMerchant(MerchantDTO merchantDTO) {
        if (merchantDTO == null) {
            throw new IllegalArgumentException("商家信息不能为空");
        }

        if (merchantDTO.getUserId() == null || merchantDTO.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (merchantDTO.getMerchantName() == null || merchantDTO.getMerchantName().isEmpty()) {
            throw new IllegalArgumentException("商家名称不能为空");
        }

        try {
            // 检查用户是否已注册为商家
            Merchant existingMerchant = merchantMapper.selectByUserId(merchantDTO.getUserId());
            if (existingMerchant != null) {
                throw new RuntimeException("该用户已注册为商家");
            }

            // 转换为实体
            Merchant merchant = new Merchant();
            merchant.setUserId(merchantDTO.getUserId());
            merchant.setMerchantName(merchantDTO.getMerchantName());
            merchant.setDescription(merchantDTO.getDescription());
            merchant.setLogo(merchantDTO.getLogo());
            merchant.setBusinessLicense(merchantDTO.getBusinessLicense());
            merchant.setContactName(merchantDTO.getContactName());
            merchant.setContactPhone(merchantDTO.getContactPhone());
            merchant.setContactEmail(merchantDTO.getContactEmail());
            merchant.setStatus(merchantDTO.getStatus() != null ? merchantDTO.getStatus() : 0); // 0表示待审核
            merchant.setCreateTime(new Date());
            merchant.setUpdateTime(new Date());

            // 插入记录
            merchantMapper.insert(merchant);

            // 返回插入后的商家信息
            return convertToDTO(merchantMapper.selectByPrimaryKey(merchant.getMerchantId()));
        } catch (Exception e) {
            logger.error("注册商家失败, merchantDTO: {}", merchantDTO, e);
            throw new RuntimeException("注册商家失败", e);
        }
    }

    /**
     * 将商家实体转换为DTO
     *
     * @param merchant 商家实体
     * @return 商家DTO
     */
    private MerchantDTO convertToDTO(Merchant merchant) {
        if (merchant == null) {
            return null;
        }

        MerchantDTO dto = new MerchantDTO();
        dto.setMerchantId(merchant.getMerchantId());
        dto.setUserId(merchant.getUserId());
        dto.setMerchantName(merchant.getMerchantName());
        dto.setDescription(merchant.getDescription());
        dto.setLogo(merchant.getLogo());
        dto.setBusinessLicense(merchant.getBusinessLicense());
        dto.setContactName(merchant.getContactName());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setContactEmail(merchant.getContactEmail());
        dto.setStatus(merchant.getStatus());
        dto.setCreateTime(merchant.getCreateTime());
        dto.setUpdateTime(merchant.getUpdateTime());

        return dto;
    }
}