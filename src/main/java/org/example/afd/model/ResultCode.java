package org.example.afd.model;

/**
 * 统一返回状态码
 */
public interface ResultCode {
    
    // 成功状态码
    int SUCCESS = 200;
    
    // 系统级错误状态码
    int SYSTEM_ERROR = 500;
    int PARAM_ERROR = 400;
    int INVALID_PARAM = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    
    // 业务级错误状态码
    int USER_NOT_EXIST = 1001;
    int PASSWORD_ERROR = 1002;
    int ACCOUNT_LOCKED = 1003;
    int TOKEN_EXPIRED = 1004;
    int TOKEN_INVALID = 1005;
    
    // 商品相关错误状态码
    int PRODUCT_NOT_EXIST = 2001;
    int PRODUCT_OUT_OF_STOCK = 2002;
    
    // 订单相关错误状态码
    int ORDER_NOT_EXIST = 3001;
    int ORDER_STATUS_ERROR = 3002;
    int ORDER_CREATE_FAIL = 3003;
    
    // 商家相关错误状态码
    int MERCHANT_NOT_EXIST = 4001;
    int MERCHANT_STATUS_ERROR = 4002;
    int BUSINESS_ERROR = 4003;
    
    // 支付相关错误状态码
    int PAYMENT_FAIL = 5001;
    int REFUND_FAIL = 5002;
    
    // 文件上传相关错误状态码
    int FILE_UPLOAD_FAIL = 6001;
    int FILE_TYPE_ERROR = 6002;
    int FILE_SIZE_EXCEED = 6003;
} 