package com.github.platform.core.common.configuration.feign;

import com.github.platform.core.common.constant.PropertyConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * feign client 配置
 * @Author: yxkong
 * @Date: 2025/5/30
 * @version: 1.0
 */
@Configuration
@ConfigurationProperties(prefix = PropertyConstant.FEIGN_CLIENT_CONFIG)
@Data
public class FeignClientTimeoutProperties {
    private Map<String, ClientConfig> config = new HashMap<>();
    private ClientConfig defaultConfig = new ClientConfig(2000, 10000);

    public ClientConfig getConfigForClient(String clientName) {
        return config.getOrDefault(clientName, defaultConfig);
    }

    @Data
    public static class ClientConfig {
        private int connectTimeout;
        private int readTimeout;

        public ClientConfig() {}

        public ClientConfig(int connectTimeout, int readTimeout) {
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
        }
    }

}
