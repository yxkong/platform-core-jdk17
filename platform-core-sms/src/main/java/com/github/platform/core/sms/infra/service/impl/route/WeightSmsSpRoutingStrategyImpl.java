package com.github.platform.core.sms.infra.service.impl.route;

import com.github.platform.core.sms.domain.constant.SmsRouteEnum;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateDto;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateStatusDto;
import com.github.platform.core.sms.infra.service.ISmsSpRoutingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

/**
 * 基于权重的策略
 * @author: yxkong
 * @date: 2024/3/27
 * @version: 1.0
 */
@Service
@Slf4j
public class WeightSmsSpRoutingStrategyImpl implements ISmsSpRoutingStrategy {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    @Override
    public boolean support(String strategy) {
        return SmsRouteEnum.isWeight(strategy);
    }
    @Override
    public SysSmsTemplateStatusDto route(SysSmsTemplateDto smsRoute, List<SysSmsTemplateStatusDto> list) {
        // 假设每个provider有getWeight()方法
        double totalWeight = list.stream().mapToDouble(SysSmsTemplateStatusDto::getWeight).sum();
        double random = SECURE_RANDOM.nextDouble() * totalWeight;

        double accumulative = 0;
        for (SysSmsTemplateStatusDto provider : list) {
            accumulative += provider.getWeight();
            if (random <= accumulative) {
                return provider;
            }
        }
        // fallback
        return list.get(list.size() - 1);
    }
}
