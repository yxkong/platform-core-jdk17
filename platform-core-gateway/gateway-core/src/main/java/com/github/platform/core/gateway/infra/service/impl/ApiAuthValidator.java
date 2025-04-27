package com.github.platform.core.gateway.infra.service.impl;

import com.github.platform.core.auth.service.IGatewayTokenService;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.gateway.admin.domain.constant.GatewayAuthEnum;
import com.github.platform.core.gateway.infra.exception.AuthException;
import com.github.platform.core.gateway.infra.service.AuthValidator;
import com.github.platform.core.standard.constant.HeaderConstant;
import com.github.platform.core.standard.entity.common.LoginInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * JWT验证器实现（对应原ApiAuthFilter逻辑）
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */

@Component
public class ApiAuthValidator implements AuthValidator {

    private final IGatewayTokenService tokenService;
    public ApiAuthValidator(@Qualifier("apiTokenService") IGatewayTokenService tokenService) {
        this.tokenService = tokenService;
    }
    @Override
    public boolean support(String authType) {
        return GatewayAuthEnum.isApi(authType);
    }

    @Override
    public Mono<Void> validate(ServerWebExchange exchange) {
        String token = exchange.getRequest().getHeaders().getFirst(HeaderConstant.TOKEN);
        if (StringUtils.isEmpty(token)) {
            return Mono.error(new AuthException("Missing api token"));
        }

        String loginInfoStr = tokenService.getLoginInfoStr(token);
        if (Objects.isNull(loginInfoStr)) {
            return Mono.error(new AuthException("Token has expired"));
        }
        LoginInfo loginInfo = JsonUtils.fromJson(loginInfoStr, LoginInfo.class);
        assert loginInfo != null;
        // 验证通过，将用户登录信息存入exchange属性
        exchange.getAttributes().put(HeaderConstant.LOGIN_INFO, loginInfoStr);
        exchange.getAttributes().put(HeaderConstant.TENANT_ID, loginInfo.getTenantId());
        return Mono.empty();
    }
}
