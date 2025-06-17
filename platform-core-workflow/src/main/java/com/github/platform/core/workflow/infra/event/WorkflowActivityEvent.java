package com.github.platform.core.workflow.infra.event;

import com.github.platform.core.workflow.domain.entity.WorkflowActivityEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 活动事件
 * @Author: yxkong
 * @Date: 2023/12/6 17:09
 * @version: 1.0
 */
@Getter
public class WorkflowActivityEvent extends ApplicationEvent {
    private WorkflowActivityEntity activityEntity;

    public WorkflowActivityEvent(WorkflowActivityEntity activityEntity) {
        super(activityEntity);
        this.activityEntity = activityEntity;
    }

}
