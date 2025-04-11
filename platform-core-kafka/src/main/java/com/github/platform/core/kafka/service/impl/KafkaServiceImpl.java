package com.github.platform.core.kafka.service.impl;

import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.kafka.service.IKafkaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * kafka发送实现
 * @author: yxkong
 * @date: 2024/5/19 13:35
 * @version: 1.0
 */
@Service
@Slf4j
public class KafkaServiceImpl implements IKafkaService {
    @Resource(name = "bizKafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;
    @Override
    public void asyncSend(String topic, String key, String data) {
        CompletableFuture<SendResult<String, String>> future ;
        if (StringUtils.isEmpty(key)) {
            future = kafkaTemplate.send(topic, data);
        } else {
            future = kafkaTemplate.send(topic, key, data);
        }
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("推送成功数据到 topic={}, msg={}, offset={}", topic, data, result.getRecordMetadata().offset());
            } else {
                log.error("推送失败数据到 topic={}, msg={}", topic, data, ex);
            }
        });
    }

    @Override
    public Pair<Boolean,String> syncSend(String topic, String key, String data) {
        CompletableFuture<SendResult<String, String>> future;
        if (StringUtils.isEmpty(key)) {
            future = kafkaTemplate.send(topic, data);
        } else {
            future = kafkaTemplate.send(topic, key, data);
        }
        try {
            SendResult<String, String> result = future.get();
            log.info("推送数据到 topic={} 成功. msg={}", topic, data);
            return Pair.of(true, "推送成功！");
        } catch (Exception e) {
            log.error("推送数据到 topic={} 失败. msg={}", topic, data, e);
            return Pair.of(false, e.getMessage());
        }
    }
}
