package com.github.platform.core.web.filter;

import com.github.platform.core.common.configuration.property.PlatformProperties;
import com.github.platform.core.common.constant.SpringBeanOrderConstant;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.web.constant.WebConstant;
import com.github.platform.core.web.util.RequestHolder;
import com.github.platform.core.web.util.WebUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.StopWatch;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author: yxkong
 * @date: 2021/5/19 9:26 上午
 * @version: 1.0
 */
@Slf4j
public class HttpTraceLogFilter extends PlatformOncePerRequestFilter {
    private PlatformProperties platformProperties;
    public HttpTraceLogFilter(PlatformProperties platformProperties) {
        this.platformProperties = platformProperties;
    }

    @Override
    public int getOrder() {
        return SpringBeanOrderConstant.FILTER_HTTP_TRACE_LOG;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Pair<Boolean, ContentCachingRequestWrapper> p1 = getRequestWrapper(request);
        ContentCachingRequestWrapper requestWrapper = p1.getRight();
        Pair<Boolean, ContentCachingResponseWrapper> p2 = getResponseWrapper(response);
        ContentCachingResponseWrapper responseWrapper = p2.getRight();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Boolean intercept = false;
        try {
            // 如果不需要日志记录，接着往下走
            intercept = isIntercept(requestWrapper);
            if (intercept){
                filterChain.doFilter(requestWrapper, responseWrapper);
                return;
            }
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            stopWatch.stop();
            if (!intercept){
                HttpTraceLog traceLog = getHttpTraceLog(request, response, stopWatch, requestWrapper, responseWrapper);
                // 记录日志
                String jsonLog = JsonUtils.toJson(traceLog);
                if (log.isWarnEnabled() && platformProperties.isWarn()){
                    log.warn("Http trace log: {}", jsonLog);
                } else if (platformProperties.isInfo() && log.isInfoEnabled()){
                    log.info("Http trace log: {}", jsonLog);
                } else {
                    log.debug("Http trace log: {}", jsonLog);
                }
            }

            if (p2.getLeft()){
                responseWrapper.copyBodyToResponse();
            }
        }

    }

    private static HttpTraceLog getHttpTraceLog(HttpServletRequest request, HttpServletResponse response, StopWatch stopWatch, ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper) {
       return HttpTraceLog.builder()
                .ip(WebUtil.getIpHost(request))
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timeTaken(stopWatch.getTotalTimeMillis())
                .time(LocalDateTime.now().toString())
                .headersMap(RequestHolder.getIncludeHeaders(requestWrapper,WebConstant.INCLUDE_HEADERS))
                .parameterMap(requestWrapper.getParameterMap())
                .status(response.getStatus())
                .requestBody(RequestHolder.getRequestBody(requestWrapper))
                .responseBody(RequestHolder.getResponseBody(responseWrapper))
                .build();
    }

    @Data
    @Builder
    private static class HttpTraceLog {
        private String ip;
        private String path;
        private Map<String, String> headersMap;
        private Map<String, String[]> parameterMap;
        private String method;
        private Long timeTaken;
        private String time;
        private Integer status;
        private String requestBody;
        private String responseBody;
    }
}