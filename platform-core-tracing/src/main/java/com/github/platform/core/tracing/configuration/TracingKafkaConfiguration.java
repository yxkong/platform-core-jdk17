package com.github.platform.core.tracing.configuration;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import com.github.platform.core.common.configuration.property.PlatformProperties;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.kafka.KafkaSender;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yxkong
 */

@Configuration
@ConditionalOnClass(KafkaSender.class)
@ConditionalOnProperty(name = "management.zipkin.enabled", havingValue = "true")
@EnableAutoConfiguration(exclude = ZipkinAutoConfiguration.class)
public class TracingKafkaConfiguration {
    private final PlatformProperties platformProperties;
    private final KafkaProperties kafkaProperties;
    public TracingKafkaConfiguration(PlatformProperties platformProperties, KafkaProperties kafkaProperties) {
        this.platformProperties = platformProperties;
        this.kafkaProperties = kafkaProperties;
    }
    // ==================== 基础配置 ====================
    @Bean
    public brave.propagation.CurrentTraceContext braveCurrentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(MDCScopeDecorator.get())
                .build();
    }
    @Bean
    public CurrentTraceContext micrometerCurrentTraceContext() {
        return new BraveCurrentTraceContext(braveCurrentTraceContext());
    }
    // ==================== Kafka Sender 配置 ====================
//    @Bean
//    public KafkaSender zipkinKafkaSender() {
//        return KafkaSender.newBuilder()
//                .bootstrapServers(String.join(",", kafkaProperties.getBootstrapServers()))
//                .topic("zipkin")
//                .overrides(Map.of(
//                        "compression.type", "gzip",
//                        "batch.size", "10",
//                        "linger.ms", "5"
//                ))
//                .build();
//    }

    @Bean
    public BytesMessageSender zipkinKafkaSender() {
        List<String> servers = kafkaProperties.getBootstrapServers();
        return KafkaSender.newBuilder()
                // 正确拼接多个地址
                .bootstrapServers(String.join(",", servers))
                .topic("zipkin")
                .overrides(Map.of(
                        "compression.type", "gzip",
                        "batch.size", "5",
                        "linger.ms", "5"
                ))
                .build();
    }
    // ==================== Zipkin Reporter 配置 ====================
    @Bean
    public AsyncReporter<zipkin2.Span> zipkinReporter(BytesMessageSender zipkinKafkaSender) {
        return AsyncReporter.builder(zipkinKafkaSender)
                .closeTimeout(5000, TimeUnit.MILLISECONDS)
                .queuedMaxSpans(1000)
                .build();
    }
    // ==================== Span Handler ====================
//    @Bean
//    @ConditionalOnMissingBean(SpanHandler.class)
//    public SpanHandler zipkinSpanHandler(AsyncReporter<zipkin2.Span> reporter) {
//        return ZipkinSpanHandler.newBuilder(reporter)
//                .build();
//    }
    // ==================== Tracer 核心配置 ====================
    @Bean
    public Tracing braveTracing(SpanHandler spanHandler) {
        return Tracing.newBuilder()
                // 从配置获取服务名
                .localServiceName(platformProperties.getSystem().getServiceName())
                // 注入MDC支持
                .currentTraceContext(braveCurrentTraceContext())
                .propagationFactory(B3Propagation.newFactoryBuilder()
                        .injectFormat(B3Propagation.Format.MULTI)
                        .build())
                .addSpanHandler(spanHandler)
                .build();
    }
    // ==================== Tracer 配置 ====================
    @Bean
    public Tracer tracer(Tracing tracing) {
        // 配置需要传播的字段（示例）
        List<String> tagFields = List.of("token");
        // 需要跨服务传播的字段
        List<String> remoteFields = List.of("userId");
        return new BraveTracer(
                tracing.tracer(),
                // 使用 Micrometer 兼容类型
                micrometerCurrentTraceContext(),
                // 使用新版构造函数
                new BraveBaggageManager(tagFields, remoteFields)
        );
    }
}
