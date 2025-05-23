package com.github.platform.core.schedule.infra.handler;

import com.github.platform.core.common.service.IEventHandlerService;
import com.github.platform.core.schedule.domain.dto.SysJobDto;
import com.github.platform.core.schedule.infra.configuration.ScheduleManager;
import com.github.platform.core.standard.entity.dto.CommonPublishDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.SchedulerException;

/**
 * 更新job任务处理器
 * @author: yxkong
 * @date: 2023/9/11 11:32 AM
 * @version: 1.0
 */
@Slf4j
public class TriggerJobService implements IEventHandlerService<SysJobDto> {

    private final ScheduleManager scheduleManager;

    public TriggerJobService(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    @Override
    public Pair<Boolean, String> handler(CommonPublishDto<SysJobDto> dto) {
        try {
            scheduleManager.addOrUpdateJob(dto.getData());
        } catch (SchedulerException e) {
            return Pair.of(false,"更新失败！");
        }
        return Pair.of(true,"更新成功！");
    }
}
