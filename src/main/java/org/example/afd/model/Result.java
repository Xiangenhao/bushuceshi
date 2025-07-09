package org.example.afd.model;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code; // 编码：200成功，其他值为失败
    private String message; // 消息
    private T data; // 数据

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    // 添加 failure 方法，兼容 MerchantController 中的调用
    public static <T> Result<T> failure(int code, String message) {
        return error(code, message);
    }
    
    // 提供一个简单的接受任意对象的failure方法
    public static <T> Result<T> failure(Object codeSource, String message) {
        return error(500, message); // 简单返回一个默认的500错误
    }
    
    // 判断是否成功的方法，便于后端使用
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}