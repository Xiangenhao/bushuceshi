package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.ShoppingCartDTO;
import org.example.afd.dto.ShoppingCartItemDTO;
import org.example.afd.model.Result;
import org.example.afd.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 购物车相关接口的Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/cart")
    public ResponseEntity<Result<Object>> getCartItems(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("getCartItems: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("getCartItems: 获取用户{}的购物车列表", userId);
            ShoppingCartDTO cart = cartService.getCart(userId.longValue());
            log.info("getCartItems: 成功获取购物车列表，商品数量: {}", 
                    cart.getItems() != null ? cart.getItems().size() : 0);
            
            return ResponseEntity.ok(Result.success(cart));
        } catch (Exception e) {
            log.error("getCartItems: 获取购物车列表失败", e);
            return ResponseEntity.ok(Result.error("获取购物车列表失败"));
        }
    }
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/cart")
    public ResponseEntity<Result<Object>> addToCart(
            @RequestBody Map<String, Object> cartItem,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("addToCart: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("addToCart: 用户{}添加商品到购物车, 请求参数: {}", userId, cartItem);
            
            // 构建购物车项
            ShoppingCartItemDTO item = new ShoppingCartItemDTO();
            
            if (cartItem.get("skuId") != null) {
                Long skuId = Long.valueOf(cartItem.get("skuId").toString());
                item.setSkuId(skuId);
                log.debug("addToCart: 设置SKU ID: {}", skuId);
            } else {
                log.error("addToCart: SKU ID不能为空");
                return ResponseEntity.ok(Result.error("SKU ID不能为空"));
            }
            
            if (cartItem.get("quantity") != null) {
                Integer quantity = Integer.valueOf(cartItem.get("quantity").toString());
                item.setQuantity(quantity);
                log.debug("addToCart: 设置商品数量: {}", quantity);
            } else {
                item.setQuantity(1); // 默认数量为1
                log.debug("addToCart: 使用默认数量: 1");
            }
            
            if (cartItem.get("selected") != null) {
                Boolean selected = Boolean.valueOf(cartItem.get("selected").toString());
                item.setSelected(selected);
                log.debug("addToCart: 设置选中状态: {}", selected);
            } else {
                item.setSelected(true); // 默认选中
                log.debug("addToCart: 使用默认选中状态: true");
            }
            
            Map<String, Object> result = cartService.addToCart(userId.longValue(), item);
            
            if ((Boolean) result.get("success")) {
                log.info("addToCart: 用户{}成功添加商品到购物车", userId);
                return ResponseEntity.ok(Result.success("添加成功"));
            } else {
                String errorMsg = result.get("message").toString();
                log.warn("addToCart: 用户{}添加商品到购物车失败: {}", userId, errorMsg);
                return ResponseEntity.ok(Result.error(errorMsg));
            }
        } catch (Exception e) {
            log.error("addToCart: 添加到购物车失败", e);
            return ResponseEntity.ok(Result.error("添加到购物车失败"));
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @PutMapping("/cart/{cartItemId}")
    public ResponseEntity<Result<Object>> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Object> updateData,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("updateCartItem: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("updateCartItem: 用户{}更新购物车商品{}, 更新数据: {}", 
                    userId, cartItemId, updateData);
            
            ShoppingCartItemDTO item = new ShoppingCartItemDTO();
            if (updateData.get("quantity") != null) {
                Integer quantity = Integer.valueOf(updateData.get("quantity").toString());
                item.setQuantity(quantity);
                log.debug("updateCartItem: 更新数量为: {}", quantity);
            }
            
            Map<String, Object> result = cartService.updateCartItem(userId.longValue(), cartItemId, item);
            
            if ((Boolean) result.get("success")) {
                log.info("updateCartItem: 用户{}成功更新购物车商品{}", userId, cartItemId);
                return ResponseEntity.ok(Result.success("更新成功"));
            } else {
                String errorMsg = result.get("message").toString();
                log.warn("updateCartItem: 用户{}更新购物车商品{}失败: {}", userId, cartItemId, errorMsg);
                return ResponseEntity.ok(Result.error(errorMsg));
            }
        } catch (Exception e) {
            log.error("updateCartItem: 更新购物车失败", e);
            return ResponseEntity.ok(Result.error("更新购物车失败"));
        }
    }
    
    /**
     * 删除购物车商品
     */
    @DeleteMapping("/cart/{cartItemId}")
    public ResponseEntity<Result<Object>> deleteCartItem(
            @PathVariable Long cartItemId,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("deleteCartItem: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("deleteCartItem: 用户{}删除购物车商品{}", userId, cartItemId);
            
            Map<String, Object> result = cartService.removeCartItem(userId.longValue(), cartItemId);
            
            if ((Boolean) result.get("success")) {
                log.info("deleteCartItem: 用户{}成功删除购物车商品{}", userId, cartItemId);
                return ResponseEntity.ok(Result.success("删除成功"));
            } else {
                String errorMsg = result.get("message").toString();
                log.warn("deleteCartItem: 用户{}删除购物车商品{}失败: {}", userId, cartItemId, errorMsg);
                return ResponseEntity.ok(Result.error(errorMsg));
            }
        } catch (Exception e) {
            log.error("deleteCartItem: 删除购物车商品失败", e);
            return ResponseEntity.ok(Result.error("删除购物车商品失败"));
        }
    }
    
    /**
     * 选择或取消选择购物车商品
     */
    @PutMapping("/cart/{cartItemId}/select")
    public ResponseEntity<Result<Object>> selectCartItem(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Object> selectData,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("selectCartItem: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            Boolean selected = Boolean.valueOf(selectData.get("selected").toString());
            log.info("selectCartItem: 用户{}设置购物车商品{}选中状态为: {}", 
                    userId, cartItemId, selected);
            
            Map<String, Object> result = cartService.setCartItemSelected(userId.longValue(), cartItemId, selected);
            
            if ((Boolean) result.get("success")) {
                log.info("selectCartItem: 用户{}成功设置购物车商品{}选中状态", userId, cartItemId);
                return ResponseEntity.ok(Result.success("操作成功"));
            } else {
                String errorMsg = result.get("message").toString();
                log.warn("selectCartItem: 用户{}设置购物车商品{}选中状态失败: {}", 
                        userId, cartItemId, errorMsg);
                return ResponseEntity.ok(Result.error(errorMsg));
            }
        } catch (Exception e) {
            log.error("selectCartItem: 选择购物车商品失败", e);
            return ResponseEntity.ok(Result.error("选择购物车商品失败"));
        }
    }
    
    /**
     * 全选或取消全选购物车商品
     */
    @PutMapping("/cart/select-all")
    public ResponseEntity<Result<Object>> selectAllCartItems(
            @RequestBody Map<String, Object> selectAllData,
            HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("selectAllCartItems: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            Boolean selected = Boolean.valueOf(selectAllData.get("selected").toString());
            log.info("selectAllCartItems: 用户{}设置全部购物车商品选中状态为: {}", userId, selected);
            
            Map<String, Object> result = cartService.setAllCartItemsSelected(userId.longValue(), selected);
            
            if ((Boolean) result.get("success")) {
                log.info("selectAllCartItems: 用户{}成功设置全部购物车商品选中状态", userId);
                return ResponseEntity.ok(Result.success("操作成功"));
            } else {
                String errorMsg = result.get("message").toString();
                log.warn("selectAllCartItems: 用户{}设置全部购物车商品选中状态失败: {}", userId, errorMsg);
                return ResponseEntity.ok(Result.error(errorMsg));
            }
        } catch (Exception e) {
            log.error("selectAllCartItems: 全选购物车商品失败", e);
            return ResponseEntity.ok(Result.error("全选购物车商品失败"));
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/cart")
    public ResponseEntity<Result<Object>> clearCart(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("clearCart: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("clearCart: 用户{}清空购物车", userId);
            
            Map<String, Object> result = cartService.clearCart(userId.longValue());
            
            if ((Boolean) result.get("success")) {
                log.info("clearCart: 用户{}成功清空购物车", userId);
                return ResponseEntity.ok(Result.success("清空成功"));
            } else {
                String errorMsg = result.get("message").toString();
                log.warn("clearCart: 用户{}清空购物车失败: {}", userId, errorMsg);
                return ResponseEntity.ok(Result.error(errorMsg));
            }
        } catch (Exception e) {
            log.error("clearCart: 清空购物车失败", e);
            return ResponseEntity.ok(Result.error("清空购物车失败"));
        }
    }
    
    /**
     * 获取购物车商品数量
     */
    @GetMapping("/cart/count")
    public ResponseEntity<Result<Object>> getCartItemCount(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("getCartItemCount: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("getCartItemCount: 获取用户{}的购物车商品数量", userId);
            ShoppingCartDTO cart = cartService.getCart(userId.longValue());
            Integer count = cart.getItems() != null ? cart.getItems().size() : 0;
            log.info("getCartItemCount: 用户{}购物车商品数量: {}", userId, count);
            
            return ResponseEntity.ok(Result.success(count));
        } catch (Exception e) {
            log.error("getCartItemCount: 获取购物车商品数量失败", e);
            return ResponseEntity.ok(Result.error("获取购物车商品数量失败"));
        }
    }
    
    /**
     * 获取选中的购物车商品列表（用于结算）
     */
    @GetMapping("/cart/selected")
    public ResponseEntity<Result<Object>> getSelectedCartItems(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                log.warn("getSelectedCartItems: 用户未授权");
                return ResponseEntity.ok(Result.error("未授权"));
            }
            
            log.info("getSelectedCartItems: 获取用户{}的选中购物车商品", userId);
            ShoppingCartDTO cart = cartService.getCart(userId.longValue());
            
            // 过滤出选中的商品
            java.util.List<ShoppingCartItemDTO> selectedItems = cart.getItems().stream()
                    .filter(item -> item.getSelected())
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("getSelectedCartItems: 用户{}的选中商品数量: {}", userId, selectedItems.size());
            
            return ResponseEntity.ok(Result.success(selectedItems));
        } catch (Exception e) {
            log.error("getSelectedCartItems: 获取选中购物车商品失败", e);
            return ResponseEntity.ok(Result.error("获取选中购物车商品失败"));
        }
    }
} 