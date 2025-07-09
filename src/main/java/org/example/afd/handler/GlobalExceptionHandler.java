package org.example.afd.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.exception.OrderStatusException;
import org.example.afd.exception.StockException;
import org.example.afd.model.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
//@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理库存异常
     */
    @ExceptionHandler(StockException.class)
    public Result<String> handleStockException(StockException e) {
        log.error("库存操作异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理订单状态异常
     */
    @ExceptionHandler(OrderStatusException.class)
    public Result<String> handleOrderStatusException(OrderStatusException e) {
        log.error("订单状态异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理一般异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统内部错误");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return Result.error(500, "系统运行异常: " + e.getMessage());
    }
} 