package com.github.platform.core.tracing.configuration.filter;

import brave.Span;
import brave.Tracing;
import brave.propagation.TraceContext;
import com.alibaba.fastjson2.JSONObject;
import com.github.platform.core.common.constant.SpringBeanOrderConstant;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.kafka.service.TracingKafkaProducer;
import com.github.platform.core.web.filter.PlatformOncePerRequestFilter;
import com.github.platform.core.web.util.RequestHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.env.Environment;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采集输入输出信息到zipkin 兼容以前的sleuth，并推送到 Kafka
 */
@Slf4j
public class TracingFilter extends PlatformOncePerRequestFilter {
    private final TracingKafkaProducer kafkaProducer;
    private final Environment environment;

    public TracingFilter(TracingKafkaProducer kafkaProducer,Environment environment) {
        this.kafkaProducer = kafkaProducer;
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return SpringBeanOrderConstant.FILTER_SLEUTH;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Pair<Boolean, ContentCachingRequestWrapper> p1 = getRequestWrapper(request);
        ContentCachingRequestWrapper requestWrapper = p1.getRight();
        Pair<Boolean, ContentCachingResponseWrapper> p2 = getResponseWrapper(response);
        ContentCachingResponseWrapper responseWrapper = p2.getRight();

        long startTime = System.nanoTime();
        TraceContext traceContext = getSpan().context();
        String traceId = traceContext.traceIdString();
        String spanId = traceContext.spanIdString();

        Map<String, Object> zipkinData = new HashMap<>();
        zipkinData.put("traceId", traceId);
        zipkinData.put("id", spanId);
        zipkinData.put("kind", "SERVER");
        zipkinData.put("name", request.getMethod().toLowerCase() + " " + request.getRequestURI());
        zipkinData.put("timestamp", startTime);
        String serviceName = environment.getProperty("spring.application.name");
        zipkinData.put("localEndpoint", Map.of(
                "serviceName", serviceName ,
                "ipv4", request.getLocalAddr()
        ));
        zipkinData.put("remoteEndpoint", Map.of(
                "ipv4", request.getRemoteAddr(),
                "port", request.getRemotePort()
        ));
        Map<String, Object> tags = new HashMap<>();

        try {
            // 跳过指定资源
            String uri = request.getRequestURI();
            if (SkipUrlSuffixEnum.contains(uri)) {
                filterChain.doFilter(requestWrapper, responseWrapper);
                return;
            }

            // 设置请求信息
            before(requestWrapper, tags);
            filterChain.doFilter(requestWrapper, responseWrapper);

            // 设置响应信息
            long duration = System.nanoTime() - startTime;
            zipkinData.put("duration", duration);

            after(requestWrapper, responseWrapper, tags);
            zipkinData.put("tags", tags);

            // 发送到 Kafka
            kafkaProducer.sendTraceData("zipkin", JsonUtils.toJson(List.of(zipkinData)));
        } finally {
            if (p2.getLeft()) {
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    private void before(ContentCachingRequestWrapper requestWrapper, Map<String, Object> tags) {
        try {
            tags.put("http.method", requestWrapper.getMethod());
            tags.put("http.path", requestWrapper.getRequestURI());
            Map<String, String> headers = RequestHolder.getHeaders(requestWrapper);
            headers.forEach((k, v) -> {
                if (!SkipHeaderEnum.contains(k)) {
                    tags.put("request.headers." + k, v);
                }
            });
            String queryString = requestWrapper.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                tags.put("http.queryString", queryString);
            }
        } catch (Exception e) {
            log.warn("Error in before filter", e);
        }
    }

    private void after(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, Map<String, Object> tags) {
        try {
            String requestPayload = RequestHolder.getRequestBody(request);
            String responsePayload = RequestHolder.getResponseBody(response);

            tags.put("request.body", requestPayload != null ? requestPayload : "[unknown]");
//            tags.put("response.content", responsePayload != null ? responsePayload : "[unknown]");
            tags.put("response.status", response.getStatus());

            String contentType = response.getContentType();
            if (contentType != null) {
                tags.put("response.contentType", contentType);
            }

            String characterEncoding = response.getCharacterEncoding();
            if (characterEncoding != null) {
                tags.put("response.characterEncoding", characterEncoding);
            }
            JSONObject jsonObject = JSONObject.parseObject(responsePayload);
            if (jsonObject != null) {
                jsonObject.forEach((key, value) -> tags.put("response.content." + key, value));
            }

        } catch (Exception e) {
            log.warn("Error in after filter", e);
        }
    }

    private Span getSpan() {
        try {
            return Tracing.currentTracer().currentSpan();
        } catch (Exception e) {
            log.warn("Failed to get current span", e);
            return null;
        }
    }
}

