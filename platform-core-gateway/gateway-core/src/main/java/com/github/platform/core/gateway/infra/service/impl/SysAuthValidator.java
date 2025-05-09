package com.github.platform.core.gateway.infra.service.impl;

import com.github.platform.core.auth.configuration.properties.AuthProperties;
import com.github.platform.core.auth.entity.LoginUserInfo;
import com.github.platform.core.auth.service.IGatewayTokenService;
import com.github.platform.core.auth.util.AuthUtil;
import com.github.platform.core.auth.util.LoginUserInfoUtil;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.gateway.admin.domain.constant.GatewayAuthEnum;
import com.github.platform.core.gateway.infra.exception.AuthException;
import com.github.platform.core.gateway.infra.service.AuthValidator;
import com.github.platform.core.gateway.infra.utils.WebUtil;
import com.github.platform.core.standard.constant.HeaderConstant;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Objects;

/**
 * Sys系统鉴权
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
@Component
public class SysAuthValidator implements AuthValidator {

    private final IGatewayTokenService tokenService;
    private final AuthProperties authProperties;

    public SysAuthValidator(@Qualifier("sysTokenService") IGatewayTokenService tokenService, AuthProperties authProperties) {
        this.tokenService = tokenService;
        this.authProperties = authProperties;
    }

    @Override
    public boolean support(String authType) {
        return GatewayAuthEnum.isSys(authType);
    }

    @Override
    public Mono<Void> validate(ServerWebExchange exchange) {
         String token = exchange.getRequest().getHeaders().getFirst(HeaderConstant.TOKEN);
        if (StringUtils.isEmpty(token)) {
            return Mono.error(new AuthException("Missing system token"));
        }
        try {
            LoginUserInfoUtil.clearContext();
            String loginInfoStr = tokenService.getLoginInfoStr(token);

            if (Objects.isNull(loginInfoStr)) {
                return Mono.error(new AuthException("System token has expired"));
            }
            LoginUserInfo loginUserInfo = JsonUtils.fromJson(loginInfoStr, LoginUserInfo.class);
            LoginUserInfoUtil.setLoginUserInfo(loginUserInfo);
            assert loginUserInfo != null;
            if (loginUserInfo.getPerms() == null) {
                loginUserInfo.setPerms(new HashSet<>());
            }
            //添加默认权限
            loginUserInfo.getPerms().addAll(authProperties.getSys().getDefaultPerms());
            // 权限校验
            if (!AuthUtil.isSuperAdmin() &&
                    !AuthUtil.hasPerms(loginUserInfo.getPerms(), WebUtil.realPath(exchange.getRequest()))) {
                return Mono.error(new AuthException("Insufficient permissions"));
            }

            // 验证通过，将用户登录信息存入exchange属性
            exchange.getAttributes().put(HeaderConstant.LOGIN_INFO, loginInfoStr);
            exchange.getAttributes().put(HeaderConstant.TENANT_ID, loginUserInfo.getTenantId());
        } catch (Exception ignored){
        } finally {
            LoginUserInfoUtil.clearContext();
        }
        return Mono.empty();
    }
}
