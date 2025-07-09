package org.example.afd.service.impl;

import org.example.afd.dto.FullReductionRuleDTO;
import org.example.afd.dto.ProductDTO;
import org.example.afd.dto.PromotionDTO;
import org.example.afd.mapper.ProductMapper;
import org.example.afd.mapper.PromotionMapper;
import org.example.afd.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 促销活动服务实现类
 */
@Service
public class PromotionServiceImpl implements PromotionService {

    @Autowired
    private PromotionMapper promotionMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public Map<String, Object> getPromotions(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        List<PromotionDTO> promotions = new ArrayList<>();
        
        // 计算分页参数
        int offset = (page - 1) * size;
        
        // 获取折扣活动列表
        List<Map<String, Object>> discounts = promotionMapper.selectDiscounts(offset, size / 3);
        if (discounts != null && !discounts.isEmpty()) {
            for (Map<String, Object> discount : discounts) {
                PromotionDTO promotionDTO = convertToPromotionDTO("DISCOUNT", discount);
                promotions.add(promotionDTO);
            }
        }
        
        // 获取满减活动列表
        List<Map<String, Object>> fullReductions = promotionMapper.selectFullReductions(offset, size / 3);
        if (fullReductions != null && !fullReductions.isEmpty()) {
            for (Map<String, Object> fullReduction : fullReductions) {
                PromotionDTO promotionDTO = convertToPromotionDTO("FULL_REDUCTION", fullReduction);
                promotions.add(promotionDTO);
            }
        }
        
        // 获取拼团活动列表
        List<Map<String, Object>> groupBuys = promotionMapper.selectGroupBuys(offset, size / 3);
        if (groupBuys != null && !groupBuys.isEmpty()) {
            for (Map<String, Object> groupBuy : groupBuys) {
                PromotionDTO promotionDTO = convertToPromotionDTO("GROUP_BUY", groupBuy);
                promotions.add(promotionDTO);
            }
        }
        
        // 计算总数
        int total = promotionMapper.countDiscounts() + 
                   promotionMapper.countFullReductions() + 
                   promotionMapper.countGroupBuys();
        
        // 按时间排序
        Collections.sort(promotions, (p1, p2) -> p2.getStartTime().compareTo(p1.getStartTime()));
        
        // 最多返回size个
        if (promotions.size() > size) {
            promotions = promotions.subList(0, size);
        }
        
        result.put("list", promotions);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        
        return result;
    }
    
    @Override
    public PromotionDTO getPromotionDetail(Long promotionId) {
        // 尝试查找不同类型的促销活动
        PromotionDTO promotionDTO = null;
        
        // 尝试查找折扣活动
        Map<String, Object> discount = promotionMapper.selectDiscountById(promotionId);
        if (discount != null) {
            promotionDTO = convertToPromotionDTO("DISCOUNT", discount);
            
            // 获取相关商品
            List<Map<String, Object>> products = promotionMapper.selectRelatedProducts("discount", promotionId);
            if (products != null && !products.isEmpty()) {
                List<ProductDTO> productDTOs = convertToProductDTOs(products);
                promotionDTO.setProducts(productDTOs);
            }
            
            return promotionDTO;
        }
        
        // 尝试查找满减活动
        Map<String, Object> fullReduction = promotionMapper.selectFullReductionById(promotionId);
        if (fullReduction != null) {
            promotionDTO = convertToPromotionDTO("FULL_REDUCTION", fullReduction);
            
            // 获取满减规则
            List<Map<String, Object>> rules = promotionMapper.selectFullReductionRules(promotionId);
            if (rules != null && !rules.isEmpty()) {
                List<FullReductionRuleDTO> ruleDTOs = new ArrayList<>();
                for (Map<String, Object> rule : rules) {
                    FullReductionRuleDTO ruleDTO = new FullReductionRuleDTO();
                    ruleDTO.setRuleId(getLong(rule.get("rule_id")));
                    ruleDTO.setReductionId(getLong(rule.get("reduction_id")));
                    ruleDTO.setFullAmount(getDouble(rule.get("full_amount")));
                    ruleDTO.setReductionAmount(getDouble(rule.get("reduction_amount")));
                    ruleDTOs.add(ruleDTO);
                }
                promotionDTO.setRules(ruleDTOs);
            }
            
            // 获取相关商品
            List<Map<String, Object>> products = promotionMapper.selectRelatedProducts("full_reduction", promotionId);
            if (products != null && !products.isEmpty()) {
                List<ProductDTO> productDTOs = convertToProductDTOs(products);
                promotionDTO.setProducts(productDTOs);
            }
            
            return promotionDTO;
        }
        
        // 尝试查找拼团活动
        Map<String, Object> groupBuy = promotionMapper.selectGroupBuyById(promotionId);
        if (groupBuy != null) {
            promotionDTO = convertToPromotionDTO("GROUP_BUY", groupBuy);
            
            // 获取相关商品
            List<Map<String, Object>> products = promotionMapper.selectRelatedProducts("group_buy", promotionId);
            if (products != null && !products.isEmpty()) {
                List<ProductDTO> productDTOs = convertToProductDTOs(products);
                promotionDTO.setProducts(productDTOs);
            }
            
            return promotionDTO;
        }
        
        return null;
    }
    
    /**
     * 将Map转换为PromotionDTO
     * @param type 促销类型
     * @param promotion 促销活动信息
     * @return PromotionDTO
     */
    private PromotionDTO convertToPromotionDTO(String type, Map<String, Object> promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionType(type);
        
        if ("DISCOUNT".equals(type)) {
            dto.setPromotionId(getLong(promotion.get("discount_id")));
            dto.setPromotionName((String) promotion.get("discount_name"));
            dto.setStartTime((Date) promotion.get("start_time"));
            dto.setEndTime((Date) promotion.get("end_time"));
            dto.setStatus(getInteger(promotion.get("status")));
            dto.setUseRange(getRangeType(getInteger(promotion.get("use_range"))));
            dto.setDiscountType(getInteger(promotion.get("discount_type")));
            dto.setDiscountValue(getDouble(promotion.get("discount_value")));
        } else if ("FULL_REDUCTION".equals(type)) {
            dto.setPromotionId(getLong(promotion.get("reduction_id")));
            dto.setPromotionName((String) promotion.get("reduction_name"));
            dto.setStartTime((Date) promotion.get("start_time"));
            dto.setEndTime((Date) promotion.get("end_time"));
            dto.setStatus(getInteger(promotion.get("status")));
            dto.setUseRange(getRangeType(getInteger(promotion.get("use_range"))));
        } else if ("GROUP_BUY".equals(type)) {
            dto.setPromotionId(getLong(promotion.get("group_buy_id")));
            dto.setPromotionName((String) promotion.get("group_name"));
            dto.setStartTime((Date) promotion.get("start_time"));
            dto.setEndTime((Date) promotion.get("end_time"));
            dto.setStatus(getInteger(promotion.get("status")));
            dto.setUseRange("PRODUCT");
            dto.setGroupPrice(getDouble(promotion.get("group_price")));
            dto.setOriginalPrice(getDouble(promotion.get("original_price")));
            dto.setGroupSize(getInteger(promotion.get("group_size")));
            dto.setGroupDuration(getInteger(promotion.get("group_duration")));
            dto.setLimitPerUser(getInteger(promotion.get("limit_per_user")));
        }
        
        return dto;
    }
    
    /**
     * 将商品列表转换为ProductDTO列表
     * @param products 商品列表
     * @return ProductDTO列表
     */
    private List<ProductDTO> convertToProductDTOs(List<Map<String, Object>> products) {
        List<ProductDTO> productDTOs = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Map<String, Object> product : products) {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(getLong(product.get("product_id")));
                productDTO.setProductName((String) product.get("product_name"));
                productDTO.setProductBrief((String) product.get("product_brief"));
                productDTO.setMainImage((String) product.get("main_image"));
                productDTO.setPrice(getDouble(product.get("price")));
                productDTO.setStock(getInteger(product.get("stock")));
                productDTO.setSales(getInteger(product.get("sales")));
                productDTOs.add(productDTO);
            }
        }
        return productDTOs;
    }
    
    /**
     * 获取使用范围类型
     * @param useRange 使用范围值
     * @return 使用范围类型
     */
    private String getRangeType(Integer useRange) {
        if (useRange == null) return "ALL";
        switch (useRange) {
            case 1: return "ALL";
            case 2: return "CATEGORY";
            case 3: return "PRODUCT";
            default: return "ALL";
        }
    }
    
    /**
     * 获取Long值
     * @param obj 对象
     * @return Long值
     */
    private Long getLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
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
     * 获取Integer值
     * @param obj 对象
     * @return Integer值
     */
    private Integer getInteger(Object obj) {
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
    private Double getDouble(Object obj) {
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

    @Override
    public PromotionDTO getProductPromotion(Long productId) {
        // 此方法用于获取指定商品的促销活动
        // 实际实现应该查询当前有效的折扣、满减或拼团活动中是否包含该商品
        
        // TODO: 实际业务逻辑应该根据数据库查询商品相关的活动
        // 简单的实现，实际项目中需要根据具体需求完善
        
        // 这里只是示例返回，实际应该根据数据库查询结果
        return null;
    }
    
    @Override
    public Map<String, Object> getActivePromotions(int page, int size) {
        // 此方法用于获取正在进行中的促销活动列表
        // 实际上与getPromotions逻辑类似，但可能需要按特定条件排序或筛选
        
        // 在实际项目中可以根据业务需求定制不同的查询逻辑
        // 这里简单复用getPromotions的实现
        return getPromotions(page, size);
    }
} 