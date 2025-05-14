package com.github.platform.core.gateway.infra.filter;

import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.standard.constant.HeaderConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

/**
 * gateway filter 基础类
 *
 * @author: yxkong
 * @date: 2023/4/20 11:12 AM
 * @version: 1.0
 */
@Slf4j
public abstract class GatewayFilterBase {
    protected static final byte[] TOKEN_NULL = "{\"status\":\"1008\",\"message\":\"网关拦截，token无效,请重新登录！\"}"
            .getBytes(StandardCharsets.UTF_8);
    protected static final byte[] TOKEN_ERROR = "{\"status\":\"1004\",\"message\":\"网关拦截，无数据权限，请联系开发人员!\"}"
            .getBytes(StandardCharsets.UTF_8);
    protected static final String ALLOWED_HEADERS = "*";
    protected static final String ALLOWED_ORIGIN = "*";
    protected static final String ALLOWED_EXPOSE = "*";
    protected static final Long MAX_AGE = 18000L;
    /**
     * 打印请求头信息
     */
    protected void printHeader(ServerHttpRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("请求地址:{}, header:{}", request.getURI().getPath(), request.getHeaders());
        }
    }
    /**
     * 鉴权失败响应
     */
    protected Mono<Void> authFail(ServerWebExchange exchange, boolean isTokenNull) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] responseBody = isTokenNull ? TOKEN_NULL : TOKEN_ERROR;
        DataBuffer buffer = response.bufferFactory().wrap(responseBody);
        return response.writeWith(Flux.just(buffer));
    }
    /**
     * 配置CORS响应头
     */
    protected ServerWebExchange corsConfig(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.setAccessControlAllowOrigin(ALLOWED_ORIGIN);
        headers.setAccessControlAllowMethods(Arrays.asList(HttpMethod.POST,HttpMethod.PUT,HttpMethod.GET,HttpMethod.DELETE));
        headers.setAccessControlMaxAge(MAX_AGE);
        headers.setAccessControlAllowHeaders(Collections.singletonList(ALLOWED_HEADERS));
        headers.setAccessControlExposeHeaders(Collections.singletonList(ALLOWED_EXPOSE));
        headers.setAccessControlAllowCredentials(true);
        return exchange;
    }
    /**
     * 构建转发请求
     */
    protected ServerHttpRequest.Builder buildForwardRequest(ServerWebExchange exchange, String token,
                                                            String loginInfo, String requestIp,
                                                            Integer tenantId) {
        // 获取原始请求的mutate构建器
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
        builder.headers(headers -> {
            // 保留原始Header
            headers.addAll(exchange.getRequest().getHeaders());
        });
        // 标记从网关过去
        builder.header(HeaderConstant.REQUEST_FROM, HeaderConstant.REQUEST_FROM_SOURCE);
        // 条件性添加头信息（你的实际实现应该已经有非空判断）
        if (StringUtils.isNotEmpty(loginInfo) ) {
            builder.header(HeaderConstant.LOGIN_INFO, loginInfo);
        }
        if (StringUtils.isNotEmpty(token)) {
            builder.header(HeaderConstant.TOKEN, token);
        }
        if (StringUtils.isNotEmpty(requestIp)) {
            builder.header(HeaderConstant.IP_HEADER_X_FORWARDED_FOR, requestIp);
        }

        if (tenantId != null) {
            builder.header(HeaderConstant.TENANT_ID, String.valueOf(tenantId));
        }

        return builder;
    }

}
