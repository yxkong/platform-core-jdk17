//package com.github.platform.core.gateway.infra.filter;
//
//import com.github.platform.core.auth.configuration.properties.AuthProperties;
//import com.github.platform.core.auth.entity.LoginUserInfo;
//import com.github.platform.core.auth.service.IGatewayTokenService;
//import com.github.platform.core.auth.util.AuthUtil;
//import com.github.platform.core.auth.util.LoginUserInfoUtil;
//import com.github.platform.core.common.utils.JsonUtils;
//import com.github.platform.core.common.utils.StringUtils;
//import com.github.platform.core.gateway.domain.gateway.IConfigGateway;
//import com.github.platform.core.gateway.infra.utils.WebUtil;
//import com.github.platform.core.standard.constant.HeaderConstant;
//import com.github.platform.core.standard.util.Base64;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.HashSet;
//import java.util.Objects;
//
///**
// * 后端管理系统权限校验
// *  要先重写完url再进来，不能添加order排序，由数据库配置控制排序
// *  实现：
// *  1，鉴权
// *  2，信息透传
// * @author: yxkong
// * @date: 2023/4/20 10:35 AM
// * @version: 1.0
// */
//@Slf4j
//public class SysAuthFilter extends GatewayFilterBase implements GatewayFilter {
//    private final IConfigGateway configGateway;
//    private final IGatewayTokenService tokenService;
//    private final AuthProperties authProperties;
//    public SysAuthFilter(IConfigGateway configGateway, IGatewayTokenService tokenService,
//                         AuthProperties authProperties) {
//        this.configGateway = configGateway;
//        this.tokenService = tokenService;
//        this.authProperties = authProperties;
//    }
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        printHeader(exchange.getRequest());
//        String path = exchange.getRequest().getURI().getPath();
//        // 检查是否在免鉴权列表中
//        if (shouldExcludeAuth(exchange)) {
//            return chain.filter(corsConfig(exchange).mutate()
//                    .request(buildForwardRequest(exchange, null, null, null, null)
//                            .build())
//                    .build());
//        }
//        // 检查Token是否存在
//        String token = exchange.getRequest().getHeaders().getFirst(HeaderConstant.TOKEN);
//        if (StringUtils.isBlank(token)) {
//            log.warn("Path: {} - Token is missing", path);
//            return authFail(exchange, true);
//        }
//        // 验证Token有效性
//        String loginInfoStr = tokenService.getLoginInfoStr(token);
//        if (Objects.isNull(loginInfoStr)) {
//            log.error("Path: {} - Token {} has expired", path, token);
//            return authFail(exchange, true);
//        }
//        // 处理权限验证
//        LoginUserInfo loginUserInfo = JsonUtils.fromJson(loginInfoStr, LoginUserInfo.class);
//        processPermissions(loginUserInfo);
//
//        // 权限校验
//        if (!hasPermission(exchange, loginUserInfo)) {
//            log.warn("{} 权限验证失败, 用户已有权限: {}, 需要权限: {}",
//                    loginUserInfo.getLoginName(), loginUserInfo.getPerms(),
//                    WebUtil.realPath(exchange.getRequest()));
//            return authFail(exchange, false);
//        }
//        // 构建转发请求
//        String requestIp = WebUtil.getIpAddr(exchange.getRequest());
//        String encodedLoginInfo = Base64.encodeToString(JsonUtils.toJson(loginUserInfo).getBytes());
//        return chain.filter(corsConfig(exchange).mutate()
//                .request(buildForwardRequest(exchange, token, encodedLoginInfo, requestIp,
//                        String.valueOf(loginUserInfo.getTenantId()))
//                        .build())
//                .build());
//    }
//    /**
//     * 处理用户权限
//     */
//    private void processPermissions(LoginUserInfo loginUserInfo) {
//        if (loginUserInfo.getPerms() == null) {
//            loginUserInfo.setPerms(new HashSet<>());
//        }
//        loginUserInfo.getPerms().addAll(authProperties.getSys().getDefaultPerms());
//        LoginUserInfoUtil.setLoginUserInfo(loginUserInfo);
//    }
//    /**
//     * 检查用户是否有权限
//     */
//    private boolean hasPermission(ServerWebExchange exchange, LoginUserInfo loginUserInfo) {
//        return AuthUtil.isSuperAdmin() ||
//                AuthUtil.hasPerms(loginUserInfo.getPerms(), WebUtil.realPath(exchange.getRequest()));
//    }
//    /**
//     * 判断请求是否应该跳过鉴权
//     */
//    private boolean shouldExcludeAuth(ServerWebExchange exchange) {
//        String host = exchange.getRequest().getURI().toString();
//        String url = exchange.getRequest().getURI().getPath();
//
//        boolean excluded = configGateway.excludeHost(host) || configGateway.excludeUrl(url);
//        if (log.isDebugEnabled()) {
//            log.debug("Host: {}, URL: {} - Excluded from auth: {}", host, url, excluded);
//        }
//        return excluded;
//    }
//}