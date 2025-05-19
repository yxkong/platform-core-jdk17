package com.github.platform.core.sms.infra.service.impl.route;

import com.github.platform.core.sms.domain.constant.SmsRouteEnum;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateDto;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateStatusDto;
import com.github.platform.core.sms.infra.service.ISmsSpRoutingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 随机策略
 * @author: yxkong
 * @date: 2024/3/27 
 * @version: 1.0
 */
@Service
@Slf4j
public class RoundRobinSmsSpRoutingStrategyImpl implements ISmsSpRoutingStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);
    // 安全阈值
    private static final int RESET_THRESHOLD = Integer.MAX_VALUE - 1000;
    @Override
    public boolean support(String strategy) {
        return SmsRouteEnum.isRoundRobin(strategy);
    }
    @Override
    public SysSmsTemplateStatusDto route(SysSmsTemplateDto smsRoute, List<SysSmsTemplateStatusDto> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("SMS provider list cannot be empty");
        }
        // 获取并递增计数器，自动处理整数溢出
        int nextIndex = counter.getAndUpdate(prev ->
                (prev >= RESET_THRESHOLD) ? 0 : prev + 1
        );
        return list.get(Math.abs(nextIndex % list.size()));
    }
}
