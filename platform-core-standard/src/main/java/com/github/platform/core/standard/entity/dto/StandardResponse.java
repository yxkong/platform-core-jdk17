package com.github.platform.core.standard.entity.dto;

/**
 * 标准化API响应结构
 * @Author: yxkong
 * @Date: 2025/4/25
 * @version: 1.0
 */
public class StandardResponse<T> {
    private final int code;
    private final String message;
    private final T data;
    private final long timestamp;
    private StandardResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应快捷方法
     * @param data 响应数据
     */
    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(200, "Success", data);
    }
    /**
     * 成功响应快捷方法
     * @param data 响应数据
     */
    public static <T> StandardResponse<T> success(String message,T data) {
        return new StandardResponse<>(200, message, data);
    }
    /**
     * 错误响应快捷方法
     * @param code 响应码
     * @param message 响应消息
     */
    public static <T> StandardResponse<T> of(int code, String message) {
        return new StandardResponse<>(code, message, null);
    }
    // Getter方法
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public long getTimestamp() { return timestamp; }
    @Override
    public String toString() {
        return "StandardResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
