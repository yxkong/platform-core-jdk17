package com.github.platform.core.kafka.configuration;

import com.github.platform.core.common.utils.CollectionUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: yxkong
 * @date: 2021/6/15 2:36 下午
 * @version: 1.0
 */
@Data
@Component("bizKafkaProperties")
@ConfigurationProperties(prefix = "spring.kafka.biz")
public class BizKafkaProperties {
    private String enabled;
    private List<String> bizBootstrapServers = new ArrayList<>(Collections.singletonList("localhost:9092"));
    private List<String> bootstrapServers = new ArrayList<>();
    @Resource
    private KafkaProperties kafkaProperties;

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }


    public void setBizBootstrapServers(List<String> bizBootstrapServers) {
        this.bizBootstrapServers = bizBootstrapServers;
    }

    public List<String> getBootstrapServers() {
        if (CollectionUtil.isNotEmpty(bootstrapServers)){
            return bootstrapServers;
        }
        return bizBootstrapServers;
    }

}