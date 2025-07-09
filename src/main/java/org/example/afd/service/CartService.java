package org.example.afd.service;

import org.example.afd.dto.ShoppingCartDTO;
import org.example.afd.dto.ShoppingCartItemDTO;

import java.util.Map;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 获取用户购物车
     * @param userId 用户ID
     * @return 购物车信息
     */
    ShoppingCartDTO getCart(Long userId);
    
    /**
     * 添加商品到购物车
     * @param userId 用户ID
     * @param cartItem 购物车项
     * @return 操作结果
     */
    Map<String, Object> addToCart(Long userId, ShoppingCartItemDTO cartItem);
    
    /**
     * 更新购物车商品数量
     * @param userId 用户ID
     * @param itemId 购物车项ID
     * @param cartItem 购物车项
     * @return 操作结果
     */
    Map<String, Object> updateCartItem(Long userId, Long itemId, ShoppingCartItemDTO cartItem);
    
    /**
     * 从购物车中删除商品
     * @param userId 用户ID
     * @param itemId 购物车项ID
     * @return 操作结果
     */
    Map<String, Object> removeCartItem(Long userId, Long itemId);
    
    /**
     * 清空购物车
     * @param userId 用户ID
     * @return 操作结果
     */
    Map<String, Object> clearCart(Long userId);
    
    /**
     * 选中/取消选中购物车中的商品
     * @param userId 用户ID
     * @param itemId 购物车项ID
     * @param selected 是否选中
     * @return 操作结果
     */
    Map<String, Object> setCartItemSelected(Long userId, Long itemId, Boolean selected);
    
    /**
     * 全选/取消全选购物车中的商品
     * @param userId 用户ID
     * @param selected 是否全选
     * @return 操作结果
     */
    Map<String, Object> setAllCartItemsSelected(Long userId, Boolean selected);
} 