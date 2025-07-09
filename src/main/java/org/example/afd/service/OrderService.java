package org.example.afd.service;

import org.example.afd.dto.OrderDTO;
import org.example.afd.entity.Order;
import org.example.afd.model.PageResult;
import org.example.afd.model.Result;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 统一订单服务接口
 * 支持购物订单和订阅订单
 */
public interface OrderService {
    
    /**
     * 创建订阅订单
     * 
     * @param userId 用户ID
     * @param planId 订阅计划ID
     * @param months 订阅月数
     * @return 订单信息
     */
    Result<Map<String, Object>> createSubscriptionOrder(Long userId, Long planId, Integer months);
    
    /**
     * 创建购物订单
     * 
     * @param userId 用户ID
     * @param cartItems 购物车商品列表
     * @param addressId 收货地址ID
     * @param orderNote 订单备注
     * @return 订单信息
     */
    Result<Map<String, Object>> createShoppingOrder(Long userId, List<Map<String, Object>> cartItems, Long addressId, String orderNote);
    
    /**
     * 创建商品订单
     * 
     * @param userId 用户ID
     * @param orderData 订单数据
     * @return 订单信息
     */
    Result<Map<String, Object>> createProductOrder(Long userId, Map<String, Object> orderData);
    
    /**
     * 创建购物车订单
     * 
     * @param userId 用户ID
     * @param orderData 订单数据
     * @return 订单信息
     */
    Result<Map<String, Object>> createCartOrder(Long userId, Map<String, Object> orderData);
    
    /**
     * 创建通用订单（支持商品备注）
     * 
     * @param userId 用户ID
     * @param orderData 订单数据
     * @return 订单信息
     */
    Result<Map<String, Object>> createUnifiedOrder(Long userId, Map<String, Object> orderData);
    
    /**
     * 获取订单详情
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 订单详情
     */
    Result<OrderDTO> getOrderDetail(String orderNo, Long userId);
    
    /**
     * 获取用户订单详情
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    Result<OrderDTO> getUserOrderDetail(Long orderId, Long userId);
    
    /**
     * 获取用户订单列表
     * 
     * @param userId 用户ID
     * @param orderType 订单类型：1-购物订单，2-订阅订单，null-全部
     * @param orderStatus 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Result<List<OrderDTO>> getUserOrders(Long userId, Integer orderType, Integer orderStatus, Integer page, Integer size);
    
    /**
     * 取消订单
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 取消结果
     */
    Result<Boolean> cancelOrder(String orderNo, Long userId);
    
    /**
     * 取消用户订单
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 取消结果
     */
    Result<Boolean> cancelUserOrder(Long orderId, Long userId);
    
    /**
     * 确认收货
     * 
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 确认结果
     */
    Result<Boolean> confirmOrder(String orderNo, Long userId);
    
    /**
     * 确认用户订单收货
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 确认结果
     */
    Result<Boolean> confirmUserOrder(Long orderId, Long userId);
    
    /**
     * 删除用户订单
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<Boolean> deleteUserOrder(Long orderId, Long userId);
    
    /**
     * 更新订单状态
     * 
     * @param orderNo 订单号
     * @param status 新状态
     * @return 更新结果
     */
    Result<Boolean> updateOrderStatus(String orderNo, Integer status);
    
    /**
     * 获取订单统计信息
     * 
     * @param userId 用户ID
     * @param orderType 订单类型
     * @return 统计信息
     */
    Result<Map<String, Object>> getOrderStatistics(Long userId, Integer orderType);
    
    /**
     * 获取商家各状态订单数量统计
     * 
     * @param merchantId 商家ID
     * @return 各状态订单数量
     */
    Map<String, Integer> getOrderCountByStatusForMerchant(Long merchantId);
    
    /**
     * 获取商家待处理的退款申请列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 退款申请列表
     */
    List<Order> getPendingRefundsByMerchantId(Long merchantId, int page, int size);
    
    /**
     * 获取商家的订单列表（支持搜索和筛选）
     * 
     * @param merchantId 商家ID
     * @param status 订单状态（可选）
     * @param keyword 搜索关键词（订单号、用户昵称、商品名称）
     * @param timeFilter 时间筛选（今天、昨天、本周、本月）
     * @param refundStatus 退款状态（0:待处理，1:已同意，2:已拒绝）
     * @param page 页码
     * @param size 每页数量
     * @return 订单列表
     */
    Result<List<OrderDTO>> getMerchantOrdersWithFilter(Long merchantId, Integer status, String keyword, 
                                                       String timeFilter, Integer refundStatus, int page, int size);

    // ===== 高级订单管理方法 =====

    /**
     * 获取商家订单列表（高级版本，支持多种筛选和分页）
     */
    PageResult<Map<String, Object>> getMerchantOrdersAdvanced(Map<String, Object> queryParams);

    /**
     * 获取订单统计数据（基于日期）
     */
    Map<String, Object> getOrderStatistics(Long merchantId, Date targetDate);

    /**
     * 批量发货
     */
    Map<String, Object> batchShipOrders(Map<String, Object> shipmentInfo);

    /**
     * 检查订单是否可以发货
     */
    boolean canShipOrder(String orderNumber, Long merchantId);

    /**
     * 单个订单发货
     */
    boolean shipOrder(Map<String, Object> shipmentInfo);

    /**
     * 批量添加订单备注
     */
    Map<String, Object> batchAddOrderRemark(Map<String, Object> remarkInfo);

    /**
     * 检查订单是否属于指定商家
     */
    boolean isOrderBelongToMerchant(String orderNumber, Long merchantId);

    /**
     * 添加订单备注
     */
    boolean addOrderRemark(String orderNumber, Long merchantId, String remark);

    /**
     * 检查订单是否可以取消
     */
    boolean canCancelOrder(String orderNumber, Long merchantId);

    /**
     * 取消订单（商家操作）
     */
    boolean cancelOrder(String orderNumber, Long merchantId, String cancelReason);

    /**
     * 检查订单是否有退款申请
     */
    boolean hasRefundRequest(String orderNumber, Long merchantId);

    /**
     * 处理退款申请
     */
    boolean handleRefundRequest(Map<String, Object> refundInfo);

    /**
     * 获取物流信息
     */
    Map<String, Object> getLogisticsInfo(String orderNumber);

    /**
     * 更新物流信息
     */
    boolean updateLogisticsInfo(Map<String, Object> logisticsInfo);

    /**
     * 导出订单数据
     */
    String exportOrders(Map<String, Object> exportParams);

    /**
     * 获取订单分析报告
     */
    Map<String, Object> getOrderAnalysis(Map<String, Object> analysisParams);

    /**
     * 获取热销商品排行
     */
    List<Map<String, Object>> getHotProducts(Map<String, Object> params);

    /**
     * 获取客户排行
     */
    List<Map<String, Object>> getTopCustomers(Map<String, Object> params);
    
    // ===== 新增的订单管理方法 =====
    
    /**
     * 获取今日订单统计数据
     */
    Map<String, Object> getTodayOrderStatistics(Long merchantId);
    
    /**
     * 获取各状态订单数量
     */
    Map<String, Integer> getOrderCountByStatus(Long merchantId);
    
    /**
     * 获取本月销售额
     */
    BigDecimal getMonthlySales(Long merchantId);
    
    /**
     * 获取总订单数
     */
    Integer getTotalOrderCount(Long merchantId);
    
    /**
     * 获取超时未发货订单数量
     */
    Integer getOvertimeOrderCount(Long merchantId);
    
    /**
     * 获取待处理退款订单数量
     */
    Integer getPendingRefundOrderCount(Long merchantId);
    
    /**
     * 获取24小时内需发货订单数量
     */
    Integer getUrgentShipOrderCount(Long merchantId);
    
    /**
     * 获取超时订单列表
     */
    List<OrderDTO> getOvertimeOrders(Long merchantId, int page, int size);
    
    /**
     * 获取待处理退款订单列表
     */
    List<OrderDTO> getPendingRefundOrders(Long merchantId, int page, int size);

    /**
     * 获取商家订单列表
     * 
     * @param userId 用户ID（商家用户ID）
     * @param status 订单状态
     * @param orderType 订单类型
     * @param page 页码
     * @param size 每页大小
     * @return 商家订单列表
     */
    Result<Map<String, Object>> getMerchantOrders(Integer userId, Integer status, Integer orderType, Integer page, Integer size);
    
    /**
     * 商家发货
     * 
     * @param orderId 订单ID
     * @param userId 商家用户ID
     * @param shipmentData 发货信息
     * @return 发货结果
     */
    Result<Boolean> shipOrder(Long orderId, Integer userId, Map<String, Object> shipmentData);
    
    /**
     * 商家处理退款申请
     * 
     * @param orderId 订单ID
     * @param userId 商家用户ID
     * @param refundData 退款处理信息
     * @return 处理结果
     */
    Result<Boolean> processMerchantRefund(Long orderId, Integer userId, Map<String, Object> refundData);
    
    /**
     * 获取商家订单详情
     * 
     * @param orderId 订单ID
     * @param userId 商家用户ID
     * @return 订单详情
     */
    Result<Map<String, Object>> getMerchantOrderDetail(Long orderId, Integer userId);

    /**
     * 用户申请退款
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param refundData 退款申请信息
     * @return 申请结果
     */
    Result<Boolean> applyUserRefund(Long orderId, Long userId, Map<String, Object> refundData);
    
    /**
     * 商家取消订单
     * 
     * @param orderId 订单ID
     * @param userId 商家用户ID
     * @param cancelData 取消原因等信息
     * @return 取消结果
     */
    Result<Boolean> cancelMerchantOrder(Long orderId, Integer userId, Map<String, Object> cancelData);
} 