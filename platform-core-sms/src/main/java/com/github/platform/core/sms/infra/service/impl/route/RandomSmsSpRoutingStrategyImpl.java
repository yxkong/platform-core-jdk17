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
 * 随机策略
 * @author: yxkong
 * @date: 2024/3/27
 * @version: 1.0
 */
@Service
@Slf4j
public class RandomSmsSpRoutingStrategyImpl implements ISmsSpRoutingStrategy {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    @Override
    public boolean support(String strategy) {
        return SmsRouteEnum.isRandom(strategy);
    }
    @Override
    public SysSmsTemplateStatusDto route(SysSmsTemplateDto smsRoute, List<SysSmsTemplateStatusDto> list) {
        // 使用更安全的随机数生成器
        int index = SECURE_RANDOM.nextInt(list.size());
        return list.get(index);
    }
}
