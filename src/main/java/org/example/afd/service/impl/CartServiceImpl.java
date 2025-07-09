package org.example.afd.service.impl;

import org.example.afd.dto.ProductDTO;
import org.example.afd.dto.ShoppingCartDTO;
import org.example.afd.dto.ShoppingCartItemDTO;
import org.example.afd.mapper.CartMapper;
import org.example.afd.mapper.ProductMapper;
import org.example.afd.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ShoppingCartDTO getCart(Long userId) {
        // 查询用户购物车项
        List<Map<String, Object>> cartItems = cartMapper.selectCartItems(userId);
        
        // 构建购物车DTO
        ShoppingCartDTO cartDTO = new ShoppingCartDTO();
        cartDTO.setUserId(userId);
        
        List<ShoppingCartItemDTO> items = new ArrayList<>();
        if (cartItems != null && !cartItems.isEmpty()) {
            for (Map<String, Object> item : cartItems) {
                ShoppingCartItemDTO cartItemDTO = convertToDTO(item);
                
                // 查询商品信息
                Long productId = cartItemDTO.getProductId();
                if (productId != null) {
                    Map<String, Object> productMap = productMapper.selectProductById(productId);
                    if (productMap != null) {
                        ProductDTO product = new ProductDTO();
                        // 设置商品信息
                        product.setProductId(productId);
                        product.setProductName((String) productMap.get("product_name"));
                        product.setMainImage((String) productMap.get("main_image"));
                        product.setPrice(getDoubleValue(productMap.get("price")));
                        product.setPromotionPrice(getDoubleValue(productMap.get("promotion_price")));
                        
                        // 修复boolean类型转换问题
                        Object hasPromotionObj = productMap.get("has_promotion");
                        if (hasPromotionObj instanceof Boolean) {
                            product.setHasPromotion((Boolean) hasPromotionObj);
                        } else if (hasPromotionObj instanceof Integer) {
                            product.setHasPromotion(((Integer) hasPromotionObj) == 1);
                        } else if (hasPromotionObj instanceof Number) {
                            product.setHasPromotion(((Number) hasPromotionObj).intValue() == 1);
                        } else {
                            product.setHasPromotion(false);
                        }
                        
                        product.setStock((Integer) productMap.get("stock"));
                        
                        cartItemDTO.setProduct(product);
                    }
                }
                
                items.add(cartItemDTO);
            }
        }
        
        cartDTO.setItems(items);
        
        return cartDTO;
    }

    @Override
    @Transactional
    public Map<String, Object> addToCart(Long userId, ShoppingCartItemDTO cartItem) {
        logger.info("=== 添加商品到购物车开始 ===");
        logger.info("用户ID: {}", userId);
        logger.info("购物车项: SKU ID={}, 数量={}, 选中状态={}", 
                cartItem != null ? cartItem.getSkuId() : null, 
                cartItem != null ? cartItem.getQuantity() : null,
                cartItem != null ? cartItem.getSelected() : null);
        
        Map<String, Object> result = new HashMap<>();
        
        if (cartItem == null || cartItem.getSkuId() == null) {
            logger.error("购物车项参数无效: cartItem={}", cartItem);
            result.put("success", false);
            result.put("message", "SKU信息不完整");
            return result;
        }
        
        Long skuId = cartItem.getSkuId();
        logger.info("开始查询SKU信息: {}", skuId);
        
        // 检查SKU是否存在和库存
        try {
            List<Map<String, Object>> skus = productMapper.selectSkusByProductId(null); // 需要修改为根据skuId查询
            logger.debug("查询SKU结果: {}", skus);
            
            // 暂时跳过SKU检查，直接处理购物车逻辑
            logger.info("查询用户{}购物车中是否已存在SKU {}", userId, skuId);
            
            // 查询购物车中是否已存在该SKU
            Map<String, Object> existingItem = cartMapper.selectCartItemBySkuId(userId, skuId);
            logger.debug("现有购物车项查询结果: {}", existingItem);
            
            if (existingItem != null) {
                // 已存在，更新数量
                Long cartItemId = (Long) existingItem.get("cart_id");
                Integer currentQuantity = (Integer) existingItem.get("quantity");
                Integer newQuantity = currentQuantity + cartItem.getQuantity();
                
                logger.info("SKU已存在购物车中，更新数量: {} -> {}", currentQuantity, newQuantity);
                
                int updateResult = cartMapper.updateCartItemQuantity(cartItemId, newQuantity);
                logger.info("更新购物车项数量结果: {}", updateResult);
                
                if (updateResult > 0) {
                    logger.info("购物车项数量更新成功");
                } else {
                    logger.warn("购物车项数量更新失败");
                }
            } else {
                // 不存在，新增购物车项
                logger.info("SKU不存在购物车中，新增购物车项");
                
                Map<String, Object> newCartItem = new HashMap<>();
                newCartItem.put("user_id", userId);
                newCartItem.put("sku_id", cartItem.getSkuId());
                newCartItem.put("quantity", cartItem.getQuantity());
                newCartItem.put("selected", cartItem.getSelected() != null ? cartItem.getSelected() : false);
                
                logger.debug("新购物车项数据: {}", newCartItem);
                
                int insertResult = cartMapper.insertCartItem(newCartItem);
                logger.info("新增购物车项结果: {}", insertResult);
                
                if (insertResult > 0) {
                    logger.info("购物车项新增成功");
                } else {
                    logger.warn("购物车项新增失败");
                }
            }
            
            result.put("success", true);
            result.put("message", "添加成功");
            logger.info("=== 添加商品到购物车完成，结果: 成功 ===");
            
        } catch (Exception e) {
            logger.error("添加商品到购物车时发生异常", e);
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> updateCartItem(Long userId, Long itemId, ShoppingCartItemDTO cartItem) {
        Map<String, Object> result = new HashMap<>();
        
        if (itemId == null || cartItem == null) {
            result.put("success", false);
            result.put("message", "信息不完整");
            return result;
        }
        
        // 查询购物车项
        Map<String, Object> existingItem = cartMapper.selectCartItemById(itemId);
        if (existingItem == null) {
            result.put("success", false);
            result.put("message", "购物车项不存在");
            return result;
        }
        
        Long itemUserId = (Long) existingItem.get("user_id");
        if (!userId.equals(itemUserId)) {
            result.put("success", false);
            result.put("message", "无权限操作该购物车项");
            return result;
        }
        
        // 检查商品库存
        Long productId = (Long) existingItem.get("product_id");
        Map<String, Object> productMap = productMapper.selectProductById(productId);
        if (productMap == null) {
            result.put("success", false);
            result.put("message", "商品不存在");
            return result;
        }
        
        Integer stock = (Integer) productMap.get("stock");
        if (stock == null || stock < cartItem.getQuantity()) {
            result.put("success", false);
            result.put("message", "商品库存不足");
            return result;
        }
        
        // 更新数量
        cartMapper.updateCartItemQuantity(itemId, cartItem.getQuantity());
        
        result.put("success", true);
        result.put("message", "更新成功");
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> removeCartItem(Long userId, Long itemId) {
        Map<String, Object> result = new HashMap<>();
        
        if (itemId == null) {
            result.put("success", false);
            result.put("message", "信息不完整");
            return result;
        }
        
        // 查询购物车项
        Map<String, Object> existingItem = cartMapper.selectCartItemById(itemId);
        if (existingItem == null) {
            result.put("success", false);
            result.put("message", "购物车项不存在");
            return result;
        }
        
        Long itemUserId = (Long) existingItem.get("user_id");
        if (!userId.equals(itemUserId)) {
            result.put("success", false);
            result.put("message", "无权限操作该购物车项");
            return result;
        }
        
        // 删除购物车项
        cartMapper.deleteCartItem(itemId);
        
        result.put("success", true);
        result.put("message", "删除成功");
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> clearCart(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 清空购物车
        cartMapper.clearCart(userId);
        
        result.put("success", true);
        result.put("message", "清空成功");
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> setCartItemSelected(Long userId, Long itemId, Boolean selected) {
        Map<String, Object> result = new HashMap<>();
        
        if (itemId == null) {
            result.put("success", false);
            result.put("message", "信息不完整");
            return result;
        }
        
        // 查询购物车项
        Map<String, Object> existingItem = cartMapper.selectCartItemById(itemId);
        if (existingItem == null) {
            result.put("success", false);
            result.put("message", "购物车项不存在");
            return result;
        }
        
        Long itemUserId = (Long) existingItem.get("user_id");
        if (!userId.equals(itemUserId)) {
            result.put("success", false);
            result.put("message", "无权限操作该购物车项");
            return result;
        }
        
        // 更新选中状态
        cartMapper.updateCartItemSelected(itemId, selected);
        
        result.put("success", true);
        result.put("message", "更新成功");
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> setAllCartItemsSelected(Long userId, Boolean selected) {
        Map<String, Object> result = new HashMap<>();
        
        // 更新所有购物车项选中状态
        cartMapper.updateAllCartItemsSelected(userId, selected);
        
        result.put("success", true);
        result.put("message", "更新成功");
        
        return result;
    }
    
    /**
     * 将数据库查询结果转换为DTO
     * @param item 数据库查询结果
     * @return 购物车项DTO
     */
    private ShoppingCartItemDTO convertToDTO(Map<String, Object> item) {
        ShoppingCartItemDTO dto = new ShoppingCartItemDTO();
        
        dto.setCartItemId((Long) item.get("cart_id"));
        dto.setUserId((Long) item.get("user_id"));
        dto.setProductId((Long) item.get("product_id"));
        dto.setSkuId((Long) item.get("sku_id"));
        dto.setQuantity((Integer) item.get("quantity"));
        
        // 修复boolean类型转换问题
        Object selectedObj = item.get("selected");
        if (selectedObj instanceof Boolean) {
            dto.setSelected((Boolean) selectedObj);
        } else if (selectedObj instanceof Integer) {
            dto.setSelected(((Integer) selectedObj) == 1);
        } else if (selectedObj instanceof Number) {
            dto.setSelected(((Number) selectedObj).intValue() == 1);
        } else {
            dto.setSelected(false); // 默认值
        }
        
        // 构建SKU信息对象
        String skuName = (String) item.get("sku_name");
        String skuImage = (String) item.get("sku_image");
        Object currentPriceObj = item.get("current_price");
        
        // 如果有SKU信息，创建SKU DTO对象
        if (skuName != null || skuImage != null || currentPriceObj != null) {
            org.example.afd.dto.SkuDTO skuDTO = new org.example.afd.dto.SkuDTO();
            skuDTO.setSkuId((Long) item.get("sku_id"));
            skuDTO.setSkuName(skuName);
            skuDTO.setImage(skuImage);
            skuDTO.setProductId((Long) item.get("product_id"));
            
            // 设置价格
            if (currentPriceObj != null) {
                Double price = getDoubleValue(currentPriceObj);
                skuDTO.setPrice(price);
            }
            
            dto.setSku(skuDTO);
            logger.debug("convertToDTO: 设置SKU信息 - skuId={}, skuName={}, price={}", 
                    skuDTO.getSkuId(), skuDTO.getSkuName(), skuDTO.getPrice());
        } else {
            logger.warn("convertToDTO: SKU信息字段为空 - skuName={}, skuImage={}, currentPrice={}", 
                    skuName, skuImage, currentPriceObj);
        }
        
        return dto;
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
} 