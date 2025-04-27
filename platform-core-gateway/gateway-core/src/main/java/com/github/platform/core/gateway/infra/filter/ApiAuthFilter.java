//package com.github.platform.core.gateway.infra.filter;
//
//import com.github.platform.core.auth.service.IGatewayTokenService;
//import com.github.platform.core.common.utils.JsonUtils;
//import com.github.platform.core.common.utils.StringUtils;
//import com.github.platform.core.gateway.domain.gateway.IConfigGateway;
//import com.github.platform.core.gateway.infra.utils.WebUtil;
//import com.github.platform.core.standard.constant.HeaderConstant;
//import com.github.platform.core.standard.entity.common.LoginInfo;
//import com.github.platform.core.standard.util.Base64;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.Objects;
//
///**
// * c端api权限校验
// *  要先重写完url再进来，不能添加order排序，由数据库配置控制排序
// *  实现：
// *  1，鉴权
// *  2，将用户的登录信息后传
// * @author: yxkong
// * @date: 2023/4/20 10:35 AM
// * @version: 1.0
// */
//@Slf4j
//public class ApiAuthFilter extends GatewayFilterBase implements GatewayFilter {
//    private final IConfigGateway configGateway;
//    private final IGatewayTokenService tokenService;
//    public ApiAuthFilter(IConfigGateway configGateway, IGatewayTokenService tokenService) {
//        this.configGateway = configGateway;
//        this.tokenService = tokenService;
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
//        // 构建转发请求
//        LoginInfo loginInfo = JsonUtils.fromJson(loginInfoStr, LoginInfo.class);
//        String requestIp = WebUtil.getIpAddr(exchange.getRequest());
//        String encodedLoginInfo = Base64.encodeToString(loginInfoStr.getBytes());
//        return chain.filter(corsConfig(exchange).mutate()
//                .request(buildForwardRequest(exchange, token, encodedLoginInfo, requestIp,
//                        String.valueOf(loginInfo.getTenantId()))
//                        .build())
//                .build());
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
//package com.github.platform.core.gateway.infra.filter;
//
//import com.github.platform.core.auth.service.IGatewayTokenService;
//import com.github.platform.core.common.utils.JsonUtils;
//import com.github.platform.core.common.utils.StringUtils;
//import com.github.platform.core.gateway.domain.gateway.IConfigGateway;
//import com.github.platform.core.gateway.infra.utils.WebUtil;
//import com.github.platform.core.standard.constant.HeaderConstant;
//import com.github.platform.core.standard.entity.common.LoginInfo;
//import com.github.platform.core.standard.util.Base64;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.Objects;
//
///**
// * c端api权限校验
// *  要先重写完url再进来，不能添加order排序，由数据库配置控制排序
// *  实现：
// *  1，鉴权
// *  2，将用户的登录信息后传
// * @author: yxkong
// * @date: 2023/4/20 10:35 AM
// * @version: 1.0
// */
//@Slf4j
//public class ApiAuthFilter extends GatewayFilterBase implements GatewayFilter {
//    private final IConfigGateway configGateway;
//    private final IGatewayTokenService tokenService;
//    public ApiAuthFilter(IConfigGateway configGateway, IGatewayTokenService tokenService) {
//        this.configGateway = configGateway;
//        this.tokenService = tokenService;
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
//        // 构建转发请求
//        LoginInfo loginInfo = JsonUtils.fromJson(loginInfoStr, LoginInfo.class);
//        String requestIp = WebUtil.getIpAddr(exchange.getRequest());
//        String encodedLoginInfo = Base64.encodeToString(loginInfoStr.getBytes());
//        return chain.filter(corsConfig(exchange).mutate()
//                .request(buildForwardRequest(exchange, token, encodedLoginInfo, requestIp,
//                        String.valueOf(loginInfo.getTenantId()))
//                        .build())
//                .build());
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
