package org.example.afd.controller;

import org.example.afd.dto.CategoryDTO;
import org.example.afd.model.Result;
import org.example.afd.model.ResultCode;
import org.example.afd.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类相关接口的Controller
 */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     * @param parentId 父分类ID，如果为null，则获取一级分类
     * @return 分类列表
     */
    @GetMapping("")
    public Result<List<CategoryDTO>> getCategories(@RequestParam(required = false) Long parentId) {
        logger.info("=== CategoryController.getCategories ===");
        logger.info("请求参数: parentId = {}", parentId);
        try {
            List<CategoryDTO> categories = categoryService.getCategories(parentId);
            logger.info("CategoryService返回分类数量: {}", categories != null ? categories.size() : 0);
            if (categories != null && !categories.isEmpty()) {
                logger.info("返回的分类列表:");
                for (int i = 0; i < Math.min(categories.size(), 5); i++) {
                    CategoryDTO category = categories.get(i);
                    logger.info("  分类[{}]: ID={}, 名称={}, 状态={}", i, 
                              category.getCategoryId(), category.getCategoryName(), category.getStatus());
                }
                if (categories.size() > 5) {
                    logger.info("  ... 还有 {} 个分类", categories.size() - 5);
                }
            } else {
                logger.warn("CategoryService返回的分类列表为空!");
            }
            return Result.success(categories);
        } catch (Exception e) {
            logger.error("获取分类列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类详情
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @GetMapping("/{categoryId}")
    public Result<CategoryDTO> getCategory(@PathVariable Long categoryId) {
        logger.info("获取分类详情, categoryId: {}", categoryId);
        try {
            CategoryDTO category = categoryService.getCategory(categoryId);
            if (category != null) {
                return Result.success(category);
            } else {
                return Result.failure(ResultCode.NOT_FOUND, "分类不存在");
            }
        } catch (Exception e) {
            logger.error("获取分类详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类详情失败");
        }
    }

    /**
     * 获取分类及其子分类
     * @param categoryId 分类ID
     * @return 分类及其子分类列表
     */
    @GetMapping("/{categoryId}/children")
    public Result<List<CategoryDTO>> getCategoryWithChildren(@PathVariable Long categoryId) {
        logger.info("获取分类及子分类, categoryId: {}", categoryId);
        try {
            List<CategoryDTO> categories = categoryService.getCategoryWithChildren(categoryId);
            return Result.success(categories);
        } catch (Exception e) {
            logger.error("获取分类及子分类失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类及子分类失败");
        }
    }

    /**
     * 获取分类路径（面包屑导航）
     * @param categoryId 分类ID
     * @return 分类路径列表
     */
    @GetMapping("/{categoryId}/path")
    public Result<List<CategoryDTO>> getCategoryPath(@PathVariable Long categoryId) {
        logger.info("获取分类路径, categoryId: {}", categoryId);
        try {
            List<CategoryDTO> categoryPath = categoryService.getCategoryPath(categoryId);
            return Result.success(categoryPath);
        } catch (Exception e) {
            logger.error("获取分类路径失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类路径失败");
        }
    }

    /**
     * 获取热门分类列表
     * @param limit 数量限制，默认为6
     * @return 热门分类列表
     */
    @GetMapping("/hot")
    public Result<List<CategoryDTO>> getHotCategories(@RequestParam(required = false, defaultValue = "6") int limit) {
        logger.info("获取热门分类, limit: {}", limit);
        try {
            List<CategoryDTO> categories = categoryService.getHotCategories(limit);
            return Result.success(categories);
        } catch (Exception e) {
            logger.error("获取热门分类失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取热门分类失败");
        }
    }
    
    /**
     * 获取所有分类（包含层级结构）
     * @return 层级结构的分类列表
     */
    @GetMapping("/tree")
    public Result<Map<String, Object>> getCategoryTree() {
        logger.info("获取分类树");
        try {
            Map<String, Object> result = new HashMap<>();
            // 获取一级分类
            List<CategoryDTO> rootCategories = categoryService.getCategories(null);
            result.put("categories", rootCategories);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取分类树失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类树失败");
        }
    }
} 