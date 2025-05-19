package com.github.platform.core.sms.infra.service.impl.route;

import com.github.platform.core.sms.domain.constant.SmsRouteEnum;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateDto;
import com.github.platform.core.sms.domain.dto.SysSmsTemplateStatusDto;
import com.github.platform.core.sms.infra.service.ISmsSpRoutingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 随机策略
 * @author: yxkong
 * @date: 2024/3/27
 * @version: 1.0
 */
@Service
@Slf4j
public class SpecifySmsSpRoutingStrategyImpl implements ISmsSpRoutingStrategy {
    @Override
    public boolean support(String strategy) {
        return SmsRouteEnum.isSpecify(strategy);
    }

    @Override
    public SysSmsTemplateStatusDto route(SysSmsTemplateDto smsRoute, List<SysSmsTemplateStatusDto> list) {
        for (SysSmsTemplateStatusDto provider : list) {
            if (provider != null && smsRoute.getProNo().equals(provider.getProNo())) {
                return provider;
            }
        }
        return null;
    }
}
