package com.github.platform.core.gateway.infra.exception;

import com.github.platform.core.standard.exception.BaseResult;

/**
 * 网关状态枚举
 * @Author: yxkong
 * @Date: 2025/4/25
 * @version: 1.0
 */
public enum GatewayResultStatus implements BaseResult {
    TOKEN_EMPTY("1001", "请求token为空，请排查token"),
    PARAM_ERROR("1002", "请求参数异常"),
    SOURCE_ERROR("1003", "来源错误"),
    NO_AUTH("1004","您没有操作权限"),

    NO_DATA_AUTH("1004", "您无数据操作权限"),
    SIGN("1005", "验签失败"),
    SIGN_FAIL("1005", "验签失败"),
    REPEAT("1006", "重复提交，请稍后再试！"),
    REPEAT_BIZ("1006", "其他用户正在操作，请您稍后再试！"),
    BAD_REQUEST("1007", "非法请求"),
    TOKEN_INVALID("1008", "token无效，请重新登录"),
    ;
    private String status;

    private String message;
    GatewayResultStatus(String status, String message) {
        this.status = status;
        this.message = message;
    }
    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
