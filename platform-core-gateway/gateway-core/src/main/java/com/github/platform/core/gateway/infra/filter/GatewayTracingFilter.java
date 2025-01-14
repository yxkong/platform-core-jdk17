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

    private static final String IGNORE_CONTENT_TYPE = "multipart/form-data";
    private static final Set<String> EXCLUDE_URI_SET = Set.of(
            "actuator/health", "html", "css", "js", "png", "gif", "configure",
            "v2/api-docs", "swagger", "swagger-resources", "csrf", "webjars");

    @Autowired
    private Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 检查是否需要排除
        if (shouldExclude(request)) {
            return chain.filter(exchange);
        }

        processRequest(exchange, request);

        // 检查是否需要追踪
        if (!isTracing(request)) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst(HeaderConstant.TOKEN);
        String url = request.getPath().toString();

        DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    try {
                        byte[] requestBodyBytes = dataBufferToBytes(dataBuffer); // 转换 DataBuffer
                        String requestBody = newString(requestBodyBytes);
                        putSpan("request.body", requestBody);

                        ServerHttpRequest newRequest = new ServerHttpRequestDecorator(request) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(requestBodyBytes);
                                return Flux.just(buffer);
                            }
                        };

                        ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
                            @Override
                            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                if (body instanceof Flux) {
                                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                                    return super.writeWith(
                                            fluxBody.buffer().map(dataBuffers -> {
                                                DataBuffer joinedBuffer = exchange.getResponse().bufferFactory().join(dataBuffers);
                                                byte[] contentBytes = dataBufferToBytes(joinedBuffer);
                                                String result = newString(contentBytes);
                                                putResult(result);
                                                return exchange.getResponse().bufferFactory().wrap(contentBytes);
                                            })
                                    );
                                }
                                return super.writeWith(body);
                            }

                            @Override
                            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                                return writeWith(Flux.from(body).flatMapSequential(p -> p));
                            }
                        };

                        return chain.filter(exchange.mutate().request(newRequest).response(decoratedResponse).build());
                    } catch (Exception e) {
                        log.error("token:{} url:{} 链路追踪异常", token, url, e);
                        return Mono.error(e); // 返回异常
                    } finally {
                        DataBufferUtils.release(dataBuffer); // 确保释放资源
                    }
                });
        return chain.filter(exchange);
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

    private void processRequest(ServerWebExchange exchange, ServerHttpRequest request) {
        processRequestHeaders(request.getHeaders());
        processQueryParameters( request);
    }

    private void processRequestHeaders( HttpHeaders headers) {
        headers.entrySet().stream()
                .filter(entry -> !SkipHeaderEnum.contains(entry.getKey()))
                .forEach(entry -> putSpan( "request.headers." + entry.getKey(), String.join(",", entry.getValue())));
    }

    private void processQueryParameters( ServerHttpRequest request) {
        String queryStr = request.getURI().getQuery();
        if (StringUtils.isNotEmpty(queryStr)) {
            putSpan("request.queryString", queryStr);
        }
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

    private void putSpan( String key, String value) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag(key, value);
        }
    }
    /**
     * 解析contentBytes
     * @param contentBytes
     * @return
     */
    private String newString(byte[] contentBytes) {
        return new String(contentBytes, StandardCharsets.UTF_8);
    }
    private byte[] dataBufferToBytes(DataBuffer dataBuffer) {
        byte[] content = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(content);
        return content;
    }

    @Override
    public int getOrder() {
        return SpringBeanOrderConstant.GATEWAY_TRACING;
    }
}


