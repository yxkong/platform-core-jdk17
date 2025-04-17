package com.github.platform.core.schedule.infra.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.github.platform.core.common.constant.PropertyConstant.DATA_SCHEDULE;

/**
 * 定时任务配置
 * @Author: yxkong
 * @Date: 2025/4/15
 * @version: 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = DATA_SCHEDULE)
public class ScheduleProperties {
    private boolean enabled = false;
    private String type;
}
