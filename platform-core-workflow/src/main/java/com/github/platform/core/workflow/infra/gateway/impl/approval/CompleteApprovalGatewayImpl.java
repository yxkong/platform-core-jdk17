package com.github.platform.core.workflow.infra.gateway.impl.approval;

import com.github.platform.core.common.gateway.BaseGatewayImpl;
import com.github.platform.core.workflow.domain.constant.ProcessOptTypeEnum;
import com.github.platform.core.workflow.domain.context.ApprovalContext;
import com.github.platform.core.workflow.domain.gateway.IProcessApprovalGateway;
import com.github.platform.core.workflow.infra.service.IProcessTaskService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

/**
 * 完成任务实现
 * @author: yxkong
 * @date: 2023/11/10 15:23
 * @version: 1.0
 */
@Service("completeApprovalGateway")
@Slf4j
public class CompleteApprovalGatewayImpl extends BaseGatewayImpl implements IProcessApprovalGateway {
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 200;
    @Override
    public void execute(IProcessTaskService processTaskService,Task task, ApprovalContext context) {
        String comment = getComment(context);
        /** 任务处于委派挂起状态，表示任务已经被委派，但尚未被受让人处理。*/
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            handleDelegatedTask(processTaskService, context, comment);
        } else {
            /**正常的任务提交 任务已经被解决*/
            handleNormalTask(processTaskService, context, comment);
        }
    }
    private void handleDelegatedTask(IProcessTaskService service, ApprovalContext context, String comment) {
        service.addComment(context.getTaskId(), context.getInstanceId(),
                ProcessOptTypeEnum.DELEGATE.getType(), comment);
        executeWithRetry(() -> service.resolveTask(context.getTaskId()));
    }

    private void handleNormalTask(IProcessTaskService service, ApprovalContext context, String comment) {
        service.addComment(context.getTaskId(), context.getInstanceId(),
                ProcessOptTypeEnum.NORMAL.getType(), comment);
        service.setAssignee(context.getTaskId(), context.getAssignee());
        executeWithRetry(() -> service.complete(context.getTaskId(), context.getVariables()));
    }
    private void executeWithRetry(Runnable operation) {
        int attempt = 0;
        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                operation.run();
                return;
            } catch (FlowableOptimisticLockingException e) {
                if (++attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
