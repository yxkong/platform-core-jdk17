package com.github.platform.core.kafka.configuration;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.util.Map;

/**
 * 业务kafka
 *
 * @author: yxkong
 * @date: 2021/6/15 2:36 下午
 * @version: 1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.kafka.biz", name = "enabled", havingValue = "true")
public class BizKafkaAutoConfiguration implements Ordered {

    @Resource
    private BizKafkaProperties bizProperties;

    @Bean(name = "bizKafkaTemplate")
    public KafkaTemplate<Object, Object> bizKafkaTemplate(ProducerFactory<Object, Object> bizKafkaProducerFactory,
                                                          ProducerListener<Object, Object> bizKafkaProducerListener,
                                                          ObjectProvider<RecordMessageConverter> messageConverter
                                                         ) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(bizKafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        kafkaTemplate.setProducerListener(bizKafkaProducerListener);
        kafkaTemplate.setDefaultTopic(kafkaProperties().getTemplate().getDefaultTopic());
        return kafkaTemplate;
    }

    @Bean(name = "bizKafkaProducerListener")
    public ProducerListener<Object, Object> bizKafkaProducerListener() {
        return new LoggingProducerListener<>();
    }

    @Bean(name = "bizKafkaProducerFactory")
    public ProducerFactory<Object, Object> bizKafkaProducerFactory(ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers, ObjectProvider<SslBundles> sslBundles) {
        Map<String, Object> producerProperties = kafkaProperties().buildProducerProperties(sslBundles.getIfAvailable());
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(producerProperties);
        String transactionIdPrefix = kafkaProperties().getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Bean(name = "bizKafkaListenerContainerFactoryConfigurer")
    public ConcurrentKafkaListenerContainerFactoryConfigurer bizKafkaListenerContainerFactoryConfigurer(
            KafkaTemplate<Object, Object> bizKafkaTemplate, ObjectProvider<RecordMessageConverter> messageConverter) {

        ConcurrentKafkaListenerContainerFactoryConfigurer configurer = new ConcurrentKafkaListenerContainerFactoryConfigurer();

        configurer.setKafkaProperties(this.kafkaProperties());
        configurer.setMessageConverter(messageConverter.getIfUnique());
        configurer.setReplyTemplate(bizKafkaTemplate);
        return configurer;
    }

    @Bean(name = "bizKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<?, ?> bizKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer bizKafkaListenerContainerFactoryConfigurer,
            ConsumerFactory<Object, Object> bizKafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 启用批量监听
        factory.setBatchListener(true);
        bizKafkaListenerContainerFactoryConfigurer.configure(factory, bizKafkaConsumerFactory);
        return factory;
    }
    /**
     * 消费者的配置
     *
     * @param customizers
     * @return
     */
    @Bean(name = "bizKafkaConsumerFactory")
    public ConsumerFactory<?, ?> bizKafkaConsumerFactory(ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers, ObjectProvider<SslBundles> sslBundles) {
        Map<String, Object> consumerProperties = kafkaProperties().buildConsumerProperties(sslBundles.getIfAvailable());
        DefaultKafkaConsumerFactory<Object, Object> factory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Bean(name = "bizKafkaAdmin")
    public KafkaAdmin bizKafkaAdmin(ObjectProvider<SslBundles> sslBundles) {
        Map<String, Object> adminProperties = kafkaProperties().buildAdminProperties(sslBundles.getIfAvailable());
        return new KafkaAdmin(adminProperties);
    }


    private KafkaProperties kafkaProperties() {
        KafkaProperties kafkaProperties = this.bizProperties.getKafkaProperties();
        kafkaProperties.setBootstrapServers(this.bizProperties.getBootstrapServers());
        return kafkaProperties;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

