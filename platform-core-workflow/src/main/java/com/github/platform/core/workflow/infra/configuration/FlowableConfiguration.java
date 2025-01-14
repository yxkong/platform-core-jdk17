package com.github.platform.core.workflow.infra.configuration;

import com.github.platform.core.common.constant.SpringBeanOrderConstant;
import com.github.platform.core.workflow.infra.filter.FlowableWebFilter;
import com.github.platform.core.workflow.infra.listener.GlobalEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.common.engine.impl.event.FlowableEventDispatcherImpl;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.TaskService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * flowable 配置
 * @author: yxkong
 * @date: 2023/9/21 9:45 AM
 * @version: 1.0
 */
@Configuration
public class FlowableConfiguration implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Resource
    private GlobalEventListener globalEventListener;
    @Bean
    public FilterRegistrationBean<FlowableWebFilter> flowableWebFilter() {
        FilterRegistrationBean<FlowableWebFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FlowableWebFilter());
        registrationBean.setOrder(SpringBeanOrderConstant.FLOWABLE_FILTER);
        return registrationBean;
    }

    @Override
    public void configure(SpringProcessEngineConfiguration configuration) {
        // 禁用异步执行器（根据需要配置）
        configuration.setAsyncExecutorActivate(true);
        FlowableEventDispatcher dispatcher = new FlowableEventDispatcherImpl();
        globalEventListener.setRuntimeService(configuration.getRuntimeService());
        // 添加全局启动事件监听
        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.PROCESS_STARTED);
        //添加全局流程完成监听器
        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.PROCESS_COMPLETED);
        // 添加全局流程取消监听器
        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.PROCESS_CANCELLED);
        // 添加全局活动开始监听器
        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.ACTIVITY_STARTED);
//        // 添加活动全局活动完成监听器
//        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.ACTIVITY_COMPLETED);
//        // 添加多实例活动全局活动完成监听器
//        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.MULTI_INSTANCE_ACTIVITY_COMPLETED);
        // 添加全局任务创建监听器
        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.TASK_CREATED);
        dispatcher.addEventListener(globalEventListener, FlowableEngineEventType.TASK_COMPLETED);
        configuration.setEventDispatcher(dispatcher);

    }
}
