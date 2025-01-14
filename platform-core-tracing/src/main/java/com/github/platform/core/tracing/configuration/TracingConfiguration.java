package com.github.platform.core.tracing.configuration;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
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
/**
 * 在 BraveBaggageConfiguration 中
     @Bean
     @ConditionalOnMissingBean
     @ConditionalOnClass({MDC.class})
     CorrelationScopeDecorator.Builder correlationScopeDecoratorBuilder() {
         return MDCScopeDecorator.newBuilder();
     }

     CurrentTraceContext.ScopeDecorator
 */
    @Bean
    public Tracing customTracing() {
        // 使用MDCScopeDecorator
        return Tracing.newBuilder()
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                        .addScopeDecorator(MDCScopeDecorator.create())
                        .build())
                .build();
    }
}
