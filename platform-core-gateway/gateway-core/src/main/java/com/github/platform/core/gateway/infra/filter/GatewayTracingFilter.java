package com.github.platform.core.gateway.infra.filter;

import brave.Span;
import brave.Tracer;
import com.alibaba.fastjson2.JSONObject;
import com.github.platform.core.common.constant.SpringBeanOrderConstant;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.gateway.infra.constants.SkipHeaderEnum;
import com.github.platform.core.standard.constant.HeaderConstant;
import com.github.platform.core.standard.entity.dto.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;
/**
 * 链路追踪
 * @author: yxkong
 * @date: 2023/8/30 4:07 PM
 * @version: 1.0
 */
@Component
@Slf4j
public class GatewayTracingFilter implements GlobalFilter, Ordered {
    private static final int MAX_RESPONSE_BODY_SIZE = 1024 * 10;
    private static final String IGNORE_CONTENT_TYPE = "multipart/form-data";
    private static final Set<String> EXCLUDE_URI_SET = Set.of(
            "actuator/health", "html", "css", "js", "png", "gif", "configure",
            "v2/api-docs", "swagger", "swagger-resources", "csrf", "webjars");

    @Autowired
    private Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. 检查是否需要排除
        if (shouldExclude(request)) {
            return chain.filter(exchange);
        }

        // 2. 处理请求元数据（头部和查询参数）
        processRequestMetadata(exchange);

        // 3. 检查是否需要追踪响应体
        boolean shouldTraceBody = isTracing(request);
        if (!shouldTraceBody) {
            return chain.filter(exchange);
        }

        // 4. 创建响应装饰器来捕获完整响应体
        ServerHttpResponse originalResponse = exchange.getResponse();
        ResponseBodyRecorder responseRecorder = new ResponseBodyRecorder(originalResponse, tracer);

        // 5. 处理请求体
        if (exchange.getAttributeOrDefault("cachedRequestBody", null) != null) {
            return processWithCachedBody(exchange, chain, responseRecorder);
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    try {
                        byte[] requestBodyBytes = dataBufferToBytes(dataBuffer);
                        putSpan("request.body", newString(requestBodyBytes));
                        return processWithCachedBody(exchange, chain, responseRecorder, requestBodyBytes);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .switchIfEmpty(chain.filter(
                        exchange.mutate().response(responseRecorder).build()
                ));
    }
    private void processRequestMetadata(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        processRequestHeaders(request.getHeaders());
        processQueryParameters(request);
    }

    private Mono<Void> processWithCachedBody(ServerWebExchange exchange,
                                             GatewayFilterChain chain,
                                             ResponseBodyRecorder responseRecorder) {
        byte[] requestBodyBytes = exchange.getAttribute("cachedRequestBody");
        return processWithCachedBody(exchange, chain, responseRecorder, requestBodyBytes);
    }

    private Mono<Void> processWithCachedBody(ServerWebExchange exchange,
                                             GatewayFilterChain chain,
                                             ResponseBodyRecorder responseRecorder,
                                             byte[] requestBodyBytes) {
        try {
            // 1. 记录请求体
            if (requestBodyBytes != null) {
                putSpan("request.body", newString(requestBodyBytes));
            }

            // 2. 创建请求装饰器
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public Flux<DataBuffer> getBody() {
                    if (requestBodyBytes != null) {
                        return Flux.just(exchange.getResponse().bufferFactory().wrap(requestBodyBytes));
                    }
                    return super.getBody();
                }
            };

            // 3. 使用响应记录器
            return chain.filter(exchange.mutate()
                    .request(newRequest)
                    .response(responseRecorder)
                    .build());
        } catch (Exception e) {
            log.error("Tracing processing failed for URI: {}", exchange.getRequest().getURI(), e);
            return chain.filter(exchange); // 出错时继续处理请求
        }
    }

    private boolean shouldExclude(ServerHttpRequest request) {
        String url = request.getURI().toString();
        String contentType = request.getHeaders().getFirst("Content-Type");
        return EXCLUDE_URI_SET.stream().anyMatch(url::contains)
                || IGNORE_CONTENT_TYPE.equals(contentType);
    }

    private boolean isTracing(ServerHttpRequest request) {
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return (HttpMethod.POST.equals(request.getMethod()) || HttpMethod.PUT.equals(request.getMethod()))
                && contentType != null && contentType.contains(HeaderConstant.CONTENT_TYPE_JSON);
    }

    private void processRequestHeaders(HttpHeaders headers) {
        headers.entrySet().stream()
                .filter(entry -> !SkipHeaderEnum.contains(entry.getKey()))
                .forEach(entry -> putSpan("request.headers." + entry.getKey(), String.join(",", entry.getValue())));
    }
    private void processQueryParameters(ServerHttpRequest request) {
        String queryStr = request.getURI().getQuery();
        if (StringUtils.isNotEmpty(queryStr)) {
            putSpan("request.queryString", queryStr);
        }
    }
    private void putSpan(String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag(key, value);
        }
    }

    private byte[] dataBufferToBytes(DataBuffer dataBuffer) {
        byte[] content = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(content);
        return content;
    }
    /**
     * 解析contentBytes
     * @param contentBytes
     * @return
     */
    private String newString(byte[] contentBytes) {
        return new String(contentBytes, StandardCharsets.UTF_8);
    }



    private void putResult( String result) {
        try {
            JSONObject jsonObject = JSONObject.parseObject(result);
            jsonObject.forEach((key, value) -> {
                if (ResultBean.isValidKey(key)) {
                    putSpan("response." + key, value.toString());
                }
            });
        } catch (Exception ignored) {
            log.debug("Failed to parse response for tracing.");
        }
    }


    @Override
    public int getOrder() {
        return SpringBeanOrderConstant.GATEWAY_TRACING;
    }

    // 内部类用于记录完整响应体
    private static class ResponseBodyRecorder extends ServerHttpResponseDecorator {
        private final Tracer tracer;
        private final StringBuilder responseBody = new StringBuilder();
        private final int maxSize;

        public ResponseBodyRecorder(ServerHttpResponse delegate, Tracer tracer) {
            super(delegate);
            this.tracer = tracer;
            this.maxSize = MAX_RESPONSE_BODY_SIZE;
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return super.writeWith(Flux.from(body).map(dataBuffer -> {
                // 记录响应体，但限制最大大小
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                String contentStr = new String(content, StandardCharsets.UTF_8);

                if (responseBody.length() + contentStr.length() <= maxSize) {
                    responseBody.append(contentStr);
                } else if (responseBody.length() < maxSize) {
                    // 只记录最大允许的部分
                    int remaining = maxSize - responseBody.length();
                    responseBody.append(contentStr, 0, remaining);
                }

                return bufferFactory().wrap(content);
            }));
        }

        @Override
        public Mono<Void> setComplete() {
            recordResponse();
            return super.setComplete();
        }

        private void recordResponse() {
            if (!responseBody.isEmpty()) {
                putSpan("response.body", responseBody.toString());
            }
        }

        private void putSpan(String key, String value) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                currentSpan.tag(key, value);
            }
        }
    }
}


