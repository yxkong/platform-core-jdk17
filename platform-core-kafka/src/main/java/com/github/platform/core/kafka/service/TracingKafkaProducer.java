package com.github.platform.core.kafka.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 链路数据推送
 * @Author: yxkong
 * @Date: 2024/12/30
 * @version: 1.0
 */
@Service
@Slf4j
public class TracingKafkaProducer {

    @Resource(name = "defaultKafkaTemplate")
    private KafkaTemplate<Object, Object> kafkaTemplate;

    public void sendTraceData(String topic,String data) {
        if (Objects.isNull(kafkaTemplate)){
            return;
        }
        CompletableFuture<SendResult<Object, Object>> future = kafkaTemplate.send(topic, data);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                if (log.isTraceEnabled()){
                    log.trace("tracing 推送成功 topic={}, msg={}, offset={}", topic, data, result.getRecordMetadata().offset());
                }
            } else {
                log.error("tracing 推送失败 topic={}, msg={}", topic, data, ex);
            }
        });
    }

}
