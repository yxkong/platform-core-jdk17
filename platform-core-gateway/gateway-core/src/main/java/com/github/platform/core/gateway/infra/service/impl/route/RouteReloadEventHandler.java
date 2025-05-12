package com.github.platform.core.gateway.infra.service.impl.route;

import com.github.platform.core.common.service.IEventHandlerService;
import com.github.platform.core.gateway.admin.domain.constant.GatewayConstant;
import com.github.platform.core.gateway.domain.gateway.RouteDataGateway;
import com.github.platform.core.gateway.infra.configuration.properties.PlatformGatewayProperties;
import com.github.platform.core.standard.entity.dto.CommonPublishDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 刷新路由事件处理
 * @Author: yxkong
 * @Date: 2025/5/12
 * @version: 1.0
 */
@Service(GatewayConstant.ROUTE_RELOAD_BEAN_NAME)
@Slf4j
public class RouteReloadEventHandler implements IEventHandlerService<String> {
    @Resource
    private RouteDataGateway routeDataGateway;
    @Resource(name = "platformGatewayProperties")
    private PlatformGatewayProperties gatewayProperties;
    @Override
    public Pair<Boolean, String> handler(CommonPublishDto<String> dto) {
        log.info("接收到刷新路由配置事件");
        if (!Objects.equals(dto.getTargetService(), gatewayProperties.getName())){
            return Pair.of(true,"非本网关数据，不处理！");
        }
        routeDataGateway.refresh();
        return Pair.of(true,"刷新网关成功！");
    }
}
