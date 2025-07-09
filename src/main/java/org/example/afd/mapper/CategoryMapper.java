package org.example.afd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品分类数据访问接口
 */
@Mapper
public interface CategoryMapper {

    /**
     * 查询所有分类列表（不按层级，直接获取全部）
     * @param parentId 父分类ID（此参数保留兼容性，但不使用）
     * @return 所有分类列表
     */
    @Select("SELECT * FROM shop_category WHERE status = 1 ORDER BY sort_order ASC")
    List<Map<String, Object>> selectCategories(@Param("parentId") Long parentId);
    
    /**
     * 根据ID查询分类信息
     * @param categoryId 分类ID
     * @return 分类信息
     */
    @Select("SELECT * FROM shop_category WHERE category_id = #{categoryId}")
    Map<String, Object> selectCategoryById(@Param("categoryId") Long categoryId);
    
    /**
     * 查询分类及其子分类
     * @param categoryId 分类ID
     * @return 分类及子分类列表
     */
    @Select("SELECT * FROM shop_category WHERE category_id = #{categoryId} OR parent_id = #{categoryId} ORDER BY sort_order ASC")
    List<Map<String, Object>> selectCategoryWithChildren(@Param("categoryId") Long categoryId);
    
    /**
     * 查询分类路径
     * @param categoryId 分类ID
     * @return 分类路径列表
     */
    @Select("WITH RECURSIVE category_path AS (" +
            "  SELECT * FROM shop_category WHERE category_id = #{categoryId}" +
            "  UNION ALL" +
            "  SELECT c.* FROM shop_category c" +
            "  JOIN category_path cp ON c.category_id = cp.parent_id" +
            ") SELECT * FROM category_path ORDER BY sort_order ASC")
    List<Map<String, Object>> selectCategoryPath(@Param("categoryId") Long categoryId);
    
    /**
     * 查询热门分类
     * @param limit 数量限制
     * @return 热门分类列表
     */
    @Select("SELECT * FROM shop_category WHERE status = 1 ORDER BY sort_order ASC LIMIT #{limit}")
    List<Map<String, Object>> selectHotCategories(@Param("limit") int limit);
    
    /**
     * 获取所有分类列表（不再与商家关联）
     * @param merchantId 商家ID (为了保持接口一致，参数保留但不使用)
     * @return 分类列表
     */
    @Select("SELECT * FROM shop_category WHERE status = 1 ORDER BY sort_order ASC")
    List<Map<String, Object>> selectCategoriesByMerchantId(@Param("merchantId") Long merchantId);
} 