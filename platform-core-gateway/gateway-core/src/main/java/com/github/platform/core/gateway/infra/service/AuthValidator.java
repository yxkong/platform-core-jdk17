package com.github.platform.core.gateway.infra.service;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 权限校验器，不同的系统，需要自己实现
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
public interface AuthValidator {

    /**
     * 支持类型
     * @param authType 鉴权类型
     */
    boolean support(String authType);

    /**
     * 校验
     */
    Mono<Void> validate(ServerWebExchange exchange);
}
