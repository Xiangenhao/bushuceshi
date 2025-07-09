package org.example.afd.service;

import org.example.afd.dto.CategoryDTO;

import java.util.List;
import java.util.Map;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取分类列表
     * @param parentId 父分类ID，如果为null，则获取一级分类
     * @return 分类列表
     */
    List<CategoryDTO> getCategories(Long parentId);
    
    /**
     * 获取分类详情
     * @param categoryId 分类ID
     * @return 分类详情
     */
    CategoryDTO getCategory(Long categoryId);
    
    /**
     * 获取分类及其子分类
     * @param categoryId 分类ID
     * @return 分类及其子分类列表
     */
    List<CategoryDTO> getCategoryWithChildren(Long categoryId);
    
    /**
     * 获取分类路径（面包屑导航）
     * @param categoryId 分类ID
     * @return 分类路径列表
     */
    List<CategoryDTO> getCategoryPath(Long categoryId);
    
    /**
     * 获取热门分类列表
     * @param limit 数量限制
     * @return 热门分类列表
     */
    List<CategoryDTO> getHotCategories(int limit);
    
    /**
     * 根据商家ID获取分类列表
     * @param merchantId 商家ID
     * @return 该商家的商品分类列表
     */
    List<Map<String, Object>> getCategoriesByMerchantId(Long merchantId);
} 