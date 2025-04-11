package com.github.platform.core.tracing.configuration;

import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.github.platform.core.common.configuration.property.PlatformProperties;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 使用zipkin追踪traceId
 * @author: yxkong
 * @date: 2024/6/26 14:43
 * @version: 1.0
 */
@Configuration
public class TracingConfiguration {
    @Resource
    private PlatformProperties properties;
    /**
     * 覆盖默认的 CurrentTraceContext，添加 MDC 支持
     */
    @Bean
    public CurrentTraceContext currentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.get())
                .build();
    }
//    @Bean
//    public Tracing customTracing() {
//        // 使用MDCScopeDecorator
//        return Tracing.newBuilder()
//                // 强制指定服务名
//                .localServiceName(properties.getSystem().getServiceName())
//                // 全量采样
//                .sampler(Sampler.ALWAYS_SAMPLE)
//                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
//                        .addScopeDecorator(MDCScopeDecorator.create())
//                        .build())
//                .clearSpanHandlers()
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        return super.end(context, span, cause);
//                    }
//                })
//                .build();
//    }
}
