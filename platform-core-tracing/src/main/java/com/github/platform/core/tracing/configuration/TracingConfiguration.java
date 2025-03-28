package com.github.platform.core.tracing.configuration;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    public Tracing customTracing(@Value("${spring.application.name}") String serviceName) {
        // 使用MDCScopeDecorator
        return Tracing.newBuilder()
                // 强制指定服务名
                .localServiceName(serviceName)
                // 全量采样
                .sampler(Sampler.ALWAYS_SAMPLE)
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                        .addScopeDecorator(MDCScopeDecorator.create())
                        .build())
                .build();
    }
}
