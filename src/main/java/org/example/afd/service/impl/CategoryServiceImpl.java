package org.example.afd.service.impl;

import org.example.afd.dto.CategoryDTO;
import org.example.afd.mapper.CategoryMapper;
import org.example.afd.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryDTO> getCategories(Long parentId) {
        logger.info("=== CategoryServiceImpl.getCategories ===");
        logger.info("请求参数: parentId = {} (已忽略，直接获取所有分类)", parentId);
        try {
            // 直接获取所有状态为1的分类，不根据parentId过滤
            List<Map<String, Object>> categories = categoryMapper.selectCategories(parentId);
            logger.info("CategoryMapper返回原始数据数量: {}", categories != null ? categories.size() : 0);
            
            if (categories != null && !categories.isEmpty()) {
                logger.info("数据库返回的原始分类数据:");
                for (int i = 0; i < Math.min(categories.size(), 3); i++) {
                    Map<String, Object> category = categories.get(i);
                    logger.info("  原始分类[{}]: ID={}, name={}, parent_id={}, status={}", 
                              i, 
                              category.get("category_id"), 
                              category.get("category_name"),
                              category.get("parent_id"),
                              category.get("status"));
                }
                if (categories.size() > 3) {
                    logger.info("  ... 还有 {} 个分类", categories.size() - 3);
                }
            } else {
                logger.warn("CategoryMapper返回的原始数据为空!");
            }
            
            List<CategoryDTO> result = convertToCategoryDTOList(categories);
            logger.info("转换后的CategoryDTO数量: {}", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("获取分类列表失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public CategoryDTO getCategory(Long categoryId) {
        try {
            Map<String, Object> category = categoryMapper.selectCategoryById(categoryId);
            if (category != null) {
                return convertToCategoryDTO(category);
            }
            return null;
        } catch (Exception e) {
            logger.error("获取分类详情失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<CategoryDTO> getCategoryWithChildren(Long categoryId) {
        try {
            List<Map<String, Object>> categories = categoryMapper.selectCategoryWithChildren(categoryId);
            return convertToCategoryDTOList(categories);
        } catch (Exception e) {
            logger.error("获取分类及其子分类失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<CategoryDTO> getCategoryPath(Long categoryId) {
        try {
            List<Map<String, Object>> categoryPath = categoryMapper.selectCategoryPath(categoryId);
            return convertToCategoryDTOList(categoryPath);
        } catch (Exception e) {
            logger.error("获取分类路径失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<CategoryDTO> getHotCategories(int limit) {
        try {
            List<Map<String, Object>> categories = categoryMapper.selectHotCategories(limit);
            return convertToCategoryDTOList(categories);
        } catch (Exception e) {
            logger.error("获取热门分类失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Map<String, Object>> getCategoriesByMerchantId(Long merchantId) {
        try {
            logger.info("获取商家分类列表: merchantId={}", merchantId);
            return categoryMapper.selectCategoriesByMerchantId(merchantId);
        } catch (Exception e) {
            logger.error("获取商家分类列表失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 将数据库查询结果转换为分类DTO列表
     * @param categories 数据库查询结果
     * @return 分类DTO列表
     */
    private List<CategoryDTO> convertToCategoryDTOList(List<Map<String, Object>> categories) {
        List<CategoryDTO> categoryDTOs = new ArrayList<>();
        if (categories != null && !categories.isEmpty()) {
            for (Map<String, Object> category : categories) {
                categoryDTOs.add(convertToCategoryDTO(category));
            }
        }
        return categoryDTOs;
    }
    
    /**
     * 将数据库查询结果转换为分类DTO
     * @param category 数据库查询结果
     * @return 分类DTO
     */
    private CategoryDTO convertToCategoryDTO(Map<String, Object> category) {
        logger.debug("转换分类数据: {}", category);
        
        CategoryDTO categoryDTO = new CategoryDTO();
        
        // 处理category_id
        Object categoryIdObj = category.get("category_id");
        if (categoryIdObj != null) {
            if (categoryIdObj instanceof Integer) {
                categoryDTO.setCategoryId(((Integer) categoryIdObj).longValue());
            } else if (categoryIdObj instanceof Long) {
                categoryDTO.setCategoryId((Long) categoryIdObj);
            }
        }
        
        // 处理category_name
        categoryDTO.setCategoryName((String) category.get("category_name"));
        
        // 处理parent_id
        Object parentIdObj = category.get("parent_id");
        if (parentIdObj != null) {
            if (parentIdObj instanceof Integer) {
                categoryDTO.setParentId(((Integer) parentIdObj).longValue());
            } else if (parentIdObj instanceof Long) {
                categoryDTO.setParentId((Long) parentIdObj);
            }
        }
        
        // 处理icon
        categoryDTO.setIcon((String) category.get("icon"));
        
        // 处理sort_order
        Object sortOrderObj = category.get("sort_order");
        if (sortOrderObj != null) {
            if (sortOrderObj instanceof Integer) {
                categoryDTO.setSort((Integer) sortOrderObj);
            }
        }
        
        // 处理status (默认为1，表示启用)
        Object statusObj = category.get("status");
        if (statusObj != null) {
            if (statusObj instanceof Integer) {
                categoryDTO.setStatus((Integer) statusObj);
            }
        } else {
            categoryDTO.setStatus(1); // 默认启用状态
        }
        
        logger.debug("转换完成的CategoryDTO: {}", categoryDTO);
        return categoryDTO;
    }
} 