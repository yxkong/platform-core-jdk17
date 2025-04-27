package com.github.platform.core.gateway.admin.domain.constant;

import lombok.Getter;

/**
 * 网关鉴权枚举
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
@Getter
public enum GatewayAuthEnum {

    NONE("NONE","不鉴权"),
    SYS("SYS","sys系统的鉴权逻辑"),
    API("API","api系统的鉴权逻辑"),
    JWT("JWT","JWT鉴权"),
    OAUTH2("OAUTH2","OAuth2鉴权"),
    ;
    private final String type;
    private final String desc;
    GatewayAuthEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static boolean isApi(String type){
        return API.getType().equalsIgnoreCase(type);
    }
    public static boolean isSys(String type){
        return SYS.getType().equalsIgnoreCase(type);
    }


}
