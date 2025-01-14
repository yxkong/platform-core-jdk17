package com.github.platform.core.kafka.configuration;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.util.Map;

/**
 * 默认kafka配置
 * @Author: yxkong
 * @Date: 2024/12/30
 * @version: 1.0
 */
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaProperties.class)
@Slf4j
public class DefaultKafkaConfiguration {
    @Resource
    private KafkaProperties properties;

    @Bean(name = "defaultKafkaTemplate")
    @Primary
    public KafkaTemplate<Object, Object> defaultKafkaTemplate(
            @Qualifier("defaultKafkaProducerFactory") ProducerFactory<Object, Object> defaultKafkaProducerFactory,
            @Qualifier("defaultKafkaProducerListener") ProducerListener<Object, Object> defaultKafkaProducerListener,
            ObjectProvider<RecordMessageConverter> messageConverter) {
        log.info("Initializing defaultKafkaTemplate Bean");
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(defaultKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(defaultKafkaProducerListener);
        kafkaTemplate.setDefaultTopic(this.properties.getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }
    @Bean(name = "defaultKafkaProducerFactory")
    public ProducerFactory<Object, Object> defaultKafkaProducerFactory(ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers, ObjectProvider<SslBundles> sslBundles) {
        Map<String, Object> producerProperties = this.properties.buildProducerProperties(sslBundles.getIfAvailable());
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(producerProperties);
        String transactionIdPrefix = this.properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }
    @Bean(name = "defaultKafkaProducerListener")
    public ProducerListener<Object, Object> defaultKafkaProducerListener() {
        return new LoggingProducerListener<>();
    }

}
