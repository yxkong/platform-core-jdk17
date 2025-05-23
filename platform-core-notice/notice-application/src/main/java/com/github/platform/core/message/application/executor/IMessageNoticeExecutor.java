package com.github.platform.core.message.application.executor;

import com.github.platform.core.standard.entity.event.DomainEvent;

/**
 * 消息通知处理器
 * @Author: yxkong
 * @Date: 2024/12/16
 * @version: 1.0
 */
public interface IMessageNoticeExecutor {

    /**
     * 消息通知处理器
     * @param domainEvent
     * @param validate
     * @return
     */
    boolean execute(DomainEvent domainEvent,boolean validate);
}
