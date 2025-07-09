package org.example.afd.dto;

import java.io.Serializable;

/**
 * API响应的通用封装类，与前端响应格式一致
 * @param <T> 响应数据类型
 */
public class MyResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码，200表示成功，其他值表示各种错误
     */
    private Integer code;
    
    /**
     * 响应消息，成功时通常为"success"，失败时为错误描述
     */
    private String message;
    
    /**
     * 响应数据，成功时包含实际返回的数据，失败时可能为null
     */
    private T data;

    /**
     * 默认构造函数
     */
    public MyResponse() {
    }

    /**
     * 创建API响应对象
     * @param code 状态码
     * @param message 响应消息
     * @param data 响应数据
     */
    public MyResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功的MyResponse对象
     */
    public static <T> MyResponse<T> success(T data) {
        return new MyResponse<>(200, "success", data);
    }

    /**
     * 创建成功响应（无数据）
     * @param <T> 数据类型
     * @return 成功的MyResponse对象
     */
    public static <T> MyResponse<T> success() {
        return new MyResponse<>(200, "success", null);
    }

    /**
     * 创建错误响应
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误的MyResponse对象
     */
    public static <T> MyResponse<T> error(Integer code, String message) {
        return new MyResponse<>(code, message, null);
    }

    /**
     * 创建错误响应（通用错误码500）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误的MyResponse对象
     */
    public static <T> MyResponse<T> error(String message) {
        return new MyResponse<>(500, message, null);
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置状态码
     * @param code 状态码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 判断响应是否成功
     * @return 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    @Override
    public String toString() {
        return "MyResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
} 