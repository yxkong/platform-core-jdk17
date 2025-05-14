package com.github.platform.core.gateway.infra.filter;

import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.gateway.admin.domain.constant.GatewayAuthEnum;
import com.github.platform.core.gateway.admin.domain.constant.GatewayConstant;
import com.github.platform.core.gateway.domain.gateway.IConfigGateway;
import com.github.platform.core.gateway.infra.exception.AuthException;
import com.github.platform.core.gateway.infra.service.AuthValidator;
import com.github.platform.core.gateway.infra.utils.WebUtil;
import com.github.platform.core.standard.constant.HeaderConstant;
import com.github.platform.core.standard.entity.dto.StandardResponse;
import com.github.platform.core.standard.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 权限过滤器
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends GatewayFilterBase implements GlobalFilter, Ordered {
    private final ObjectProvider<AuthValidator> authValidators;
    private final IConfigGateway configGateway;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String contentType = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null && contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            // 对 multipart 请求不做额外包装或读取
            return chain.filter(exchange);
        }
        // 1. 优先处理重定向逻辑（在鉴权前处理）
        Mono<Void> redirectResult = handleRedirect(exchange);
        if (redirectResult != null) {
            return redirectResult;
        }
        // 2. 检查是否在免鉴权列表中
        if (shouldExcludeAuth(exchange)) {
            return getFilter(exchange, chain);
        }
        if (authorizationFilter(exchange)) {
            return getFilter(exchange, chain);
        }
        // 3. 获取路由元数据中的鉴权配置,没有路由配置，直接404
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            ServerHttpRequest request = exchange.getRequest();
            log.warn("URI: {} {} Headers: {} Query: {} IP: {} ", request.getMethod(), request.getURI(), request.getHeaders(), request.getQueryParams(), WebUtil.getIpAddr(request));
            return buildErrorResponse(exchange, AuthErrorType.NOT_FOUND);
        }
        //获取网关配置
        Map<String, Object> metadata = route.getMetadata();
        String authType = (String) metadata.getOrDefault(GatewayConstant.AUTH_TYPE, GatewayAuthEnum.NONE.getType());
        //不鉴权逻辑
        if (GatewayAuthEnum.NONE.getType().equals(authType)) {
            return getFilter(exchange, chain);
        }
        // 获取配置的认证排除
        if (metadata.containsKey(GatewayConstant.AUTH_EXCLUSION)){
            List<String> authExclusions = (List<String>) metadata.get(GatewayConstant.AUTH_EXCLUSION);
            String path = exchange.getRequest().getPath().value();
            if (authExclusions.stream().anyMatch(pattern ->
                    PATH_MATCHER.match(pattern, path))) {
                return getFilter(exchange, chain);
            }
        }
        // 3. 获取并执行对应的验证器
        AuthValidator validator = authValidators.stream()
                .filter(v -> v.support(authType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No validator for auth type: " + authType));

        return validator.validate(exchange)
                .then(Mono.defer(() -> {
                    // 验证通过后处理请求头
                    String token = exchange.getRequest().getHeaders().getFirst(HeaderConstant.TOKEN);
                    //获取用户登录信息
                    String loginInfoStr = (String) exchange.getAttributes().get(HeaderConstant.LOGIN_INFO);
                    Integer tenantId = (Integer) exchange.getAttributes().get(HeaderConstant.TENANT_ID);
                    String requestIp = WebUtil.getIpAddr(exchange.getRequest());
                    // 根据不同的authType设置不同的请求头
                    ServerHttpRequest.Builder requestBuilder = buildForwardRequest(exchange, token,
                            URLEncoder.encode(loginInfoStr,StandardCharsets.UTF_8),
                            requestIp, tenantId);

                    return chain.filter(corsConfig(exchange).mutate()
                            .request(requestBuilder.build())
                            .build());
                }))
                .onErrorResume(e -> {
                    log.error("Auth validation failed: {}", e.getMessage(),e);
                    boolean isTokenError = e instanceof AuthException &&
                            (e.getMessage().contains("token") || e.getMessage().contains("Token"));
                    return authFail(exchange, isTokenError);
                });
    }

    private Mono<Void> getFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(corsConfig(exchange).mutate()
                .request(buildForwardRequest(exchange, null, null, null, null)
                        .build())
                .build());
    }
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, AuthErrorType errorType) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        StandardResponse<?> response = switch (errorType) {
            case INVALID_TOKEN -> StandardResponse.of(401, "Invalid authentication token");
            case ACCESS_DENIED -> StandardResponse.of(403, "Insufficient permissions");
            case NOT_FOUND -> StandardResponse.of(404, "No route configuration found");
            default -> StandardResponse.of(500, "System authentication error");
        };

        exchange.getResponse().setStatusCode(HttpStatus.valueOf(response.getCode()));
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(JsonUtils.toJson(response).getBytes())));
    }

    /**
     * 排除指定域名
     */
    private boolean shouldExcludeAuth(ServerWebExchange exchange) {
        String host = exchange.getRequest().getURI().toString();
        return configGateway.excludeHost(host) ;
    }
    private boolean authorizationFilter(ServerWebExchange exchange) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HeaderConstant.AUTHORIZATION);
        return StringUtils.isNotEmpty(authorization) && (authorization.startsWith(HeaderConstant.AUTH_BEARER) || authorization.startsWith(HeaderConstant.AUTH_BASIC));
    }

    @Override
    public int getOrder() {
        return  100;
    }
    // 错误类型枚举
    private enum AuthErrorType {
        INVALID_TOKEN,
        ACCESS_DENIED,
        SYSTEM_ERROR,
        NOT_FOUND

    }

    /**
     * 处理重定向逻辑
     */
    private Mono<Void> handleRedirect(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String redirectFlag = exchange.getRequest().getQueryParams().getFirst("redirect");
        // 检查是否是需要重定向的路径
        if (!"true".equalsIgnoreCase(redirectFlag) || !configGateway.isRedirectPath(path)) {
            return null;
        }
        try {
            String h5Html = exchange.getRequest().getQueryParams().getFirst("h5Html");
            if (StringUtils.isEmpty(h5Html)) {
                log.warn("Missing h5Html parameter in redirect request");
                return null;
            }
            // 解码URL
            String decodedUrl = new String(
                    Base64.decode(h5Html),
                    StandardCharsets.UTF_8
            );
            String targetUrl = URLDecoder.decode(decodedUrl, StandardCharsets.UTF_8);
            // 验证URL合法性
            if (!isValidRedirectUrl(targetUrl)) {
                log.warn("Invalid redirect URL: {}", targetUrl);
                return Mono.error(new IllegalArgumentException("Invalid redirect URL"));
            }
            // 构建重定向响应
            return buildRedirectResponse(exchange, targetUrl);
        } catch (Exception e) {
            log.error("Redirect processing failed", e);
            return Mono.error(e);
        }
    }
    /**
     * 构建重定向响应
     */
    private Mono<Void> buildRedirectResponse(ServerWebExchange exchange, String targetUrl) {
        ServerHttpResponse response = exchange.getResponse();

        // 使用302临时重定向（更符合常规场景）
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().set(HttpHeaders.LOCATION, targetUrl);

        // 添加安全头
        response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-store");
        response.getHeaders().add("X-Content-Type-Options", "nosniff");

        return response.setComplete();
    }
    /**
     * 验证重定向URL是否合法
     */
    private boolean isValidRedirectUrl(String url) {
        try {
            URI uri = new URI(url);

            // 基础校验
            if (!uri.isAbsolute() || StringUtils.isBlank(uri.getHost())) {
                return false;
            }
            if (!configGateway.isRedirectHost(uri.getHost())){
                return false;
            }
            // 可添加更多安全规则：
            // 1. 协议限制（只允许https）
            // 2. 防止开放重定向漏洞

            return true;

        } catch (URISyntaxException e) {
            return false;
        }
    }

}
