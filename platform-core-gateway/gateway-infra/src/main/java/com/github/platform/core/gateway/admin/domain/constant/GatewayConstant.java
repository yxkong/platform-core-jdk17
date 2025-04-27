package com.github.platform.core.gateway.admin.domain.constant;

/**
 * 网关常量
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
public interface GatewayConstant {
    String AUTH_TYPE = "authType";
    String AUTH_CONFIG = "authConfig";
    String FILTER = "filter";
    String PREDICATE = "predicate";
    String CUSTOM_TYPE = "customType";
    //认证排除
    String AUTH_EXCLUSION = "authExclusion";
    String ROUTE_ADD_BEAN_NAME = "routeAddEventHandler";
    String ROUTE_UPDATE_BEAN_NAME = "routeUpdateEventHandler";
    String ROUTE_DELETE_BEAN_NAME = "routeDeleteEventHandler";
}
