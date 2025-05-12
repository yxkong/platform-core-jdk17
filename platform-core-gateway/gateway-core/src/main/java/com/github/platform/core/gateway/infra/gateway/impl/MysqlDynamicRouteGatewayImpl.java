package com.github.platform.core.gateway.infra.gateway.impl;

import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.gateway.admin.domain.context.GatewayRouteContext;
import com.github.platform.core.gateway.admin.domain.dto.GatewayRouteConditionDto;
import com.github.platform.core.gateway.admin.domain.dto.GatewayRouteDto;
import com.github.platform.core.gateway.admin.domain.gateway.IGatewayRouteConditionGateway;
import com.github.platform.core.gateway.admin.domain.gateway.IGatewayRouteGateway;
import com.github.platform.core.gateway.admin.infra.util.RouteInfoUtil;
import com.github.platform.core.gateway.domain.gateway.RouteDataGateway;
import com.github.platform.core.gateway.infra.configuration.properties.PlatformGatewayProperties;
import com.github.platform.core.gateway.infra.service.IRouteOperatorService;
import com.github.platform.core.standard.constant.StatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网关动态路由mysql实现
 * @author: yxkong
 * @date: 2021/12/7 8:00 PM
 * @version: 1.0
 */
@Slf4j
public class MysqlDynamicRouteGatewayImpl implements RouteDataGateway {
    private IRouteOperatorService routeOperatorService;
    private IGatewayRouteGateway gatewayRouteGateway;
    private IGatewayRouteConditionGateway gatewayRouteConditionGateway;
    private PlatformGatewayProperties gatewayProperties;
    public MysqlDynamicRouteGatewayImpl(IRouteOperatorService routeOperatorService, IGatewayRouteGateway gatewayRouteGateway, IGatewayRouteConditionGateway gatewayRouteConditionGateway, PlatformGatewayProperties gatewayProperties) {
        this.routeOperatorService = routeOperatorService;
        this.gatewayRouteGateway = gatewayRouteGateway;
        this.gatewayRouteConditionGateway = gatewayRouteConditionGateway;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public void init() {
        gatewayInit("初始化");
    }

    private void gatewayInit(String opt) {
        List<GatewayRouteDto> routes = gatewayRouteGateway.findBy(GatewayRouteContext.builder().gateway(gatewayProperties.getName()).status(StatusEnum.ON.getStatus()).build());
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(routes)){
            routes.forEach(route->{
                List<GatewayRouteConditionDto> list = gatewayRouteConditionGateway.findByRouteId(route.getId());
                Map<String, Object> result = RouteInfoUtil.getResult(route, list);
                RouteDefinition definition = JsonUtils.fromJson(JsonUtils.toJson(result), RouteDefinition.class);
                routeDefinitions.add(definition);
            });
        }
        if (CollectionUtil.isNotEmpty(routeDefinitions)){
            routeOperatorService.refresh(routeDefinitions);
            log.warn("网关：{} {} MySql配置完成:{}",gatewayProperties.getName(), opt,JsonUtils.toJson(routeDefinitions));
        } else {
            log.warn("网关：{} {} 未找到对应的MySql配置",gatewayProperties.getName(), opt);
        }
    }

    @Override
    public boolean add(RouteDefinition routeDefinition) {
        return false;
    }

    @Override
    public Boolean delete(String id) {
        return null;
    }

    @Override
    public void refresh() {
        gatewayInit("刷新");
    }
}
