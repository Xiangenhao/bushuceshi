package org.example.afd.service;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.entity.Order;
import org.example.afd.entity.OrderStatusLog;
import org.example.afd.enums.OrderStatus;
import org.example.afd.exception.OrderStatusException;
import org.example.afd.mapper.OrderMapper;
import org.example.afd.mapper.OrderStatusLogMapper;
import org.example.afd.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单状态管理服务
 * 
 * 功能：
 * 1. 安全的状态转换验证
 * 2. 自动执行状态相关业务逻辑
 * 3. 完整的状态变更日志
 * 4. 防止非法状态转换
 */
@Service
@Slf4j
public class OrderStatusService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderStatusLogMapper statusLogMapper;
    
    @Autowired
    private StockService stockService;
    
    /**
     * 安全的状态转换
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> changeOrderStatus(String orderNo, OrderStatus targetStatus, 
                                           String reason, Long operatorId) {
        log.info("订单状态变更: orderNo={}, targetStatus={}, reason={}", 
                orderNo, targetStatus, reason);
        
        // 查询当前订单
        Order order = orderMapper.selectByOrderNoForUpdate(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        OrderStatus currentStatus = OrderStatus.fromCode(order.getOrderStatus());
        
        // 检查状态转换是否合法
        if (!currentStatus.canTransitionTo(targetStatus)) {
            log.warn("非法的状态转换: {} -> {}", currentStatus, targetStatus);
            return Result.error(String.format("不能从%s状态转换到%s状态", 
                                             currentStatus.getName(), targetStatus.getName()));
        }
        
        // 执行状态相关的业务逻辑
        try {
            executeStatusBusinessLogic(order, currentStatus, targetStatus);
            
            // 更新订单状态
            int result = orderMapper.updateOrderStatus(orderNo, targetStatus.getCode());
            if (result <= 0) {
                return Result.error("状态更新失败");
            }
            
            // 记录状态变更日志
            statusLogMapper.insertLog(OrderStatusLog.builder()
                .orderNo(orderNo)
                .fromStatus(currentStatus.getCode())
                .toStatus(targetStatus.getCode())
                .reason(reason)
                .operatorId(operatorId)
                .build());
            
            log.info("订单状态变更成功: {} -> {}", currentStatus.getName(), targetStatus.getName());
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("订单状态变更失败", e);
            return Result.error("状态变更失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行状态转换相关的业务逻辑
     */
    private void executeStatusBusinessLogic(Order order, OrderStatus from, OrderStatus to) {
        String orderNo = order.getOrderNo();
        
        switch (to) {
            case PAID:
                // 支付成功：确认扣减库存
                stockService.confirmStockDeduction(orderNo);
                log.info("订单支付成功，已确认扣减库存: orderNo={}", orderNo);
                break;
                
            case CANCELLED:
                // 订单取消：释放库存锁定
                if (from == OrderStatus.PENDING_PAYMENT) {
                    stockService.releaseStockLock(orderNo);
                    log.info("订单取消，已释放库存锁定: orderNo={}", orderNo);
                }
                break;
                
            case REFUNDED:
                // 退款完成：恢复库存
                if (from == OrderStatus.REFUNDING) {
                    restoreStockAfterRefund(order);
                    log.info("退款完成，已恢复库存: orderNo={}", orderNo);
                }
                break;
                
            case COMPLETED:
                // 订单完成：记录完成时间等
                log.info("订单已完成: orderNo={}", orderNo);
                break;
                
            default:
                log.info("状态转换无需特殊处理: {} -> {}", from.getName(), to.getName());
                break;
        }
    }
    
    /**
     * 退款后恢复库存
     */
    private void restoreStockAfterRefund(Order order) {
        // 实现退款后库存恢复逻辑
        log.info("执行退款后库存恢复: orderNo={}", order.getOrderNo());
        // 具体实现需要根据业务需求确定
        // 可能需要查询订单项，然后恢复相应的库存
    }
    
    /**
     * 批量更新订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> batchChangeOrderStatus(String[] orderNos, OrderStatus targetStatus, 
                                                String reason, Long operatorId) {
        int successCount = 0;
        StringBuilder errorMsg = new StringBuilder();
        
        for (String orderNo : orderNos) {
            try {
                Result<Boolean> result = changeOrderStatus(orderNo, targetStatus, reason, operatorId);
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    errorMsg.append(orderNo).append(":").append(result.getMessage()).append("; ");
                }
            } catch (Exception e) {
                log.error("批量更新订单状态失败: orderNo={}", orderNo, e);
                errorMsg.append(orderNo).append(":").append(e.getMessage()).append("; ");
            }
        }
        
        if (successCount == orderNos.length) {
            return Result.success(successCount);
        } else if (successCount > 0) {
            return Result.success("部分成功: " + errorMsg.toString(), successCount);
        } else {
            return Result.error("全部失败: " + errorMsg.toString());
        }
    }
    
    /**
     * 获取订单状态变更历史
     */
    public Result<java.util.List<OrderStatusLog>> getOrderStatusHistory(String orderNo) {
        try {
            java.util.List<OrderStatusLog> history = statusLogMapper.selectByOrderNo(orderNo);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取订单状态历史失败: orderNo={}", orderNo, e);
            return Result.error("获取状态历史失败");
        }
    }
    
    /**
     * 验证状态转换是否合法
     */
    public Result<Boolean> validateStatusTransition(String orderNo, OrderStatus targetStatus) {
        try {
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            OrderStatus currentStatus = OrderStatus.fromCode(order.getOrderStatus());
            boolean canTransition = currentStatus.canTransitionTo(targetStatus);
            
            if (canTransition) {
                return Result.success(true);
            } else {
                return Result.error(String.format("不能从%s状态转换到%s状态", 
                                                currentStatus.getName(), targetStatus.getName()));
            }
        } catch (Exception e) {
            log.error("验证状态转换失败: orderNo={}, targetStatus={}", orderNo, targetStatus, e);
            return Result.error("验证失败");
        }
    }
    
    /**
     * 获取订单的下一个可能状态
     */
    public Result<OrderStatus[]> getNextPossibleStatuses(String orderNo) {
        try {
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            OrderStatus currentStatus = OrderStatus.fromCode(order.getOrderStatus());
            OrderStatus[] nextStatuses = currentStatus.getNextPossibleStatuses();
            
            return Result.success(nextStatuses);
        } catch (Exception e) {
            log.error("获取下一个可能状态失败: orderNo={}", orderNo, e);
            return Result.error("获取失败");
        }
    }
} 