package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CartMapper {
    
    /**
     * 查询用户购物车项
     * @param userId 用户ID
     * @return 购物车项列表
     */
    @Select("SELECT c.cart_id, c.user_id, c.sku_id, c.quantity, c.selected, c.selected_sku_attributes, " +
            "c.price_snapshot, c.create_time, c.update_time, " +
            "s.sku_name, s.sku_image, s.price as current_price, s.product_id, " +
            "p.product_name, p.main_image " +
            "FROM shop_cart c " +
            "LEFT JOIN shop_product_sku s ON c.sku_id = s.sku_id " +
            "LEFT JOIN shop_product p ON s.product_id = p.product_id " +
            "WHERE c.user_id = #{userId} " +
            "ORDER BY c.create_time DESC")
    List<Map<String, Object>> selectCartItems(@Param("userId") Long userId);
    
    /**
     * 查询单个购物车项
     * @param userId 用户ID
     * @param productId 商品ID
     * @param skuId SKU ID
     * @return 购物车项
     */
    @Select("SELECT c.cart_id, c.user_id, c.sku_id, c.quantity, c.selected, c.selected_sku_attributes, " +
            "c.price_snapshot, c.create_time, c.update_time, " +
            "s.sku_name, s.sku_image, s.price as current_price, s.product_id, " +
            "p.product_name, p.main_image " +
            "FROM shop_cart c " +
            "LEFT JOIN shop_product_sku s ON c.sku_id = s.sku_id " +
            "LEFT JOIN shop_product p ON s.product_id = p.product_id " +
            "WHERE c.user_id = #{userId} AND s.product_id = #{productId} AND c.sku_id = #{skuId}")
    Map<String, Object> selectCartItem(@Param("userId") Long userId, 
                                     @Param("productId") Long productId, 
                                     @Param("skuId") Long skuId);
    
    /**
     * 根据ID查询购物车项
     * @param cartItemId 购物车项ID
     * @return 购物车项
     */
    @Select("SELECT c.cart_id, c.user_id, c.sku_id, c.quantity, c.selected, c.selected_sku_attributes, " +
            "c.price_snapshot, c.create_time, c.update_time, " +
            "s.sku_name, s.sku_image, s.price as current_price, s.product_id, " +
            "p.product_name, p.main_image " +
            "FROM shop_cart c " +
            "LEFT JOIN shop_product_sku s ON c.sku_id = s.sku_id " +
            "LEFT JOIN shop_product p ON s.product_id = p.product_id " +
            "WHERE c.cart_id = #{cartItemId}")
    Map<String, Object> selectCartItemById(@Param("cartItemId") Long cartItemId);
    
    /**
     * 添加购物车项
     * @param cartItem 购物车项
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_cart (user_id, sku_id, quantity, selected, selected_sku_attributes, price_snapshot, product_snapshot) " +
            "VALUES (#{user_id}, #{sku_id}, #{quantity}, #{selected}, #{selected_sku_attributes}, #{price_snapshot}, #{product_snapshot})")
    @Options(useGeneratedKeys = true, keyProperty = "cart_id")
    int insertCartItem(Map<String, Object> cartItem);
    
    /**
     * 更新购物车项数量
     * @param cartItemId 购物车项ID
     * @param quantity 数量
     * @return 影响行数
     */
    @Update("UPDATE shop_cart SET quantity = #{quantity}, update_time = NOW() WHERE cart_id = #{cartItemId}")
    int updateCartItemQuantity(@Param("cartItemId") Long cartItemId, 
                             @Param("quantity") Integer quantity);
    
    /**
     * 删除购物车项
     * @param cartItemId 购物车项ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_cart WHERE cart_id = #{cartItemId}")
    int deleteCartItem(@Param("cartItemId") Long cartItemId);
    
    /**
     * 清空用户购物车
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM shop_cart WHERE user_id = #{userId}")
    int clearCart(@Param("userId") Long userId);
    
    /**
     * 更新购物车项选中状态
     * @param cartItemId 购物车项ID
     * @param selected 是否选中
     * @return 影响行数
     */
    @Update("UPDATE shop_cart SET selected = #{selected}, update_time = NOW() WHERE cart_id = #{cartItemId}")
    int updateCartItemSelected(@Param("cartItemId") Long cartItemId, 
                             @Param("selected") Boolean selected);
    
    /**
     * 更新用户所有购物车项选中状态
     * @param userId 用户ID
     * @param selected 是否选中
     * @return 影响行数
     */
    @Update("UPDATE shop_cart SET selected = #{selected}, update_time = NOW() WHERE user_id = #{userId}")
    int updateAllCartItemsSelected(@Param("userId") Long userId, 
                                 @Param("selected") Boolean selected);
    
    /**
     * 根据用户ID和SKU ID查询购物车项
     * @param userId 用户ID
     * @param skuId SKU ID
     * @return 购物车项
     */
    @Select("SELECT c.cart_id, c.user_id, c.sku_id, c.quantity, c.selected, c.selected_sku_attributes, " +
            "c.price_snapshot, c.create_time, c.update_time, " +
            "s.sku_name, s.sku_image, s.price as current_price, s.product_id, " +
            "p.product_name, p.main_image " +
            "FROM shop_cart c " +
            "LEFT JOIN shop_product_sku s ON c.sku_id = s.sku_id " +
            "LEFT JOIN shop_product p ON s.product_id = p.product_id " +
            "WHERE c.user_id = #{userId} AND c.sku_id = #{skuId}")
    Map<String, Object> selectCartItemBySkuId(@Param("userId") Long userId, 
                                            @Param("skuId") Long skuId);
} 