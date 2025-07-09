package org.example.afd.service;

import org.example.afd.dto.PromotionDTO;

import java.util.Map;

/**
 * 促销活动服务接口
 */
public interface PromotionService {

    /**
     * 获取促销活动列表
     * @param page 页码
     * @param size 每页数量
     * @return 促销活动列表
     */
    Map<String, Object> getPromotions(int page, int size);
    
    /**
     * 获取促销活动详情
     * @param promotionId 促销活动ID
     * @return 促销活动详情
     */
    PromotionDTO getPromotionDetail(Long promotionId);
    
    /**
     * 获取商品的促销活动
     * @param productId 商品ID
     * @return 促销活动详情
     */
    PromotionDTO getProductPromotion(Long productId);
    
    /**
     * 获取活跃的促销活动列表
     * @param page 页码
     * @param size 每页数量
     * @return 活跃的促销活动列表分页数据
     */
    Map<String, Object> getActivePromotions(int page, int size);
} 