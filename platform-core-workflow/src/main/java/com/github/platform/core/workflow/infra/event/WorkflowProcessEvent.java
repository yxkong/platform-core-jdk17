package com.github.platform.core.workflow.infra.event;

import com.github.platform.core.workflow.domain.constant.InstanceStatusEnum;
import com.github.platform.core.workflow.domain.constant.ProcessTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.Objects;

/**
 * 流程完成事件
 * @Author: yxkong
 * @Date: 2023/12/15 14:45
 * @version: 1.0
 */
@Getter
public class WorkflowProcessEvent extends ApplicationEvent {
    private String instanceId;
    private String bizNo;
    private String processType;
    @Setter
    private String processUsers ;
    @Setter
    private Boolean createGroup;
    @Setter
    private String createUser ;
    @Setter
    Map<String, Object> variables;
    private InstanceStatusEnum status;

    public WorkflowProcessEvent(String instanceId, String bizNo, String processType, InstanceStatusEnum status) {
        super(new Object());
        this.instanceId = instanceId;
        this.bizNo = bizNo;
        this.processType = processType;
        this.status = status;
    }
    public Boolean isCreateGroup() {
        return Objects.nonNull(this.createGroup) && Objects.equals(this.createGroup,Boolean.TRUE);
    }

    public boolean isPm(){
        return Objects.equals(ProcessTypeEnum.PM.getType(),this.processType);
    }

}
