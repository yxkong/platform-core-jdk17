package com.github.platform.core.message.application.executor.impl;

import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.message.application.executor.IMessageNoticeExecutor;
import com.github.platform.core.message.application.executor.IMessageNoticeSender;
import com.github.platform.core.message.domain.context.SysNoticeEventLogContext;
import com.github.platform.core.message.domain.dto.SysNoticeEventLogDto;
import com.github.platform.core.message.domain.dto.SysNoticeTemplateDto;
import com.github.platform.core.message.domain.gateway.ISysNoticeEventLogGateway;
import com.github.platform.core.message.domain.gateway.ISysNoticeSendLogGateway;
import com.github.platform.core.message.domain.gateway.ISysNoticeTemplateGateway;
import com.github.platform.core.message.infra.configuration.properties.NoticeProperties;
import com.github.platform.core.standard.constant.StatusEnum;
import com.github.platform.core.standard.constant.SymbolConstant;
import com.github.platform.core.standard.entity.context.MessageNoticeContext;
import com.github.platform.core.standard.entity.event.DomainEvent;
import com.github.platform.core.standard.util.LocalDateTimeUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * 消息通知执行器实现
 * @Author: yxkong
 * @Date: 2024/12/16
 * @version: 1.0
 */
@Service
@Slf4j
public class MessageNoticeExecutorImpl implements IMessageNoticeExecutor {

    @Resource
    private ISysNoticeTemplateGateway sysNoticeTemplateGateway;
    @Resource
    private ISysNoticeEventLogGateway sysNoticeEventLogGateway;
    @Autowired
    private Map<String, IMessageNoticeSender> senderMap;
    @Resource
    private NoticeProperties noticeProperties;
    @Resource
    private ISysNoticeSendLogGateway noticeSendLogGateway;
    @Override
    public boolean execute(DomainEvent domainEvent,boolean validate) {
        MessageNoticeContext noticeContext = JsonUtils.convertValue(domainEvent.getData(),MessageNoticeContext.class) ;
        if (StringUtils.isEmpty(noticeContext.getNoticeChannelInfo().getChannelType())){
            noticeContext.getNoticeChannelInfo().setChannelType(noticeProperties.getChannelType());
        }
        Long eventId =  null;
        //幂等处理，并插入日志记录
        SysNoticeEventLogDto dto = sysNoticeEventLogGateway.findByMsgId(domainEvent.getMsgId());
        if(Objects.isNull(dto)){
            eventId = logRecord(domainEvent, noticeContext);
        } else {
            eventId = dto.getId();
        }
        //TODO 这里需要加一个配置，是否需要发送的配置
        SysNoticeTemplateDto templateDto = getSysNoticeTemplateDto(noticeContext, domainEvent);
        noticeContext.setEventId(eventId);
        if (Objects.isNull(templateDto)){
            updateLog(eventId,null, "没有配置可发送的模板！");
            log.warn("eventType:{} 租户：{} 对应的模板不存在",noticeContext.getEventType(),domainEvent.getTenantId());
            return false;
        }
        String channelType = Objects.nonNull(noticeContext.getNoticeChannelInfo().getChannelType()) ? noticeContext.getNoticeChannelInfo().getChannelType(): templateDto.getChannelType();
        noticeContext.setChannelType(channelType);
        // 查到通道
        IMessageNoticeSender messageNoticeSender = senderMap.get(channelType+"MessageNoticeSender");
        if (Objects.isNull(messageNoticeSender)){
            updateLog(eventId,null,  "无发送通道！");
            log.error("channelType:{} 租户：{} 对应的通道实现不存在",channelType,domainEvent.getTenantId());
            return false;
        }
        return messageNoticeSender.send(noticeSendLogGateway,domainEvent, noticeContext, templateDto);
    }

    private SysNoticeTemplateDto getSysNoticeTemplateDto(MessageNoticeContext noticeContext, DomainEvent domainEvent) {
        // 提前获取必要变量
        String eventType = noticeContext.getEventType();
        Integer tenantId = domainEvent.getTenantId();
        String tempNo = noticeContext.getTempNo();
        SysNoticeTemplateDto templateDto = null;
        // 判断事件类型是否包含冒号
        boolean containsColon = StringUtils.contains(eventType, SymbolConstant.colon);
        // 如果包含冒号，优先进行全匹配
        if (containsColon) {
            templateDto = sysNoticeTemplateGateway.findEventType(eventType, tenantId);
        }

        // 如果未匹配到且模板编号不为空，则通过模板编号查找
        if (templateDto == null && StringUtils.isNotEmpty(tempNo)) {
            templateDto = sysNoticeTemplateGateway.findByTempNo(tempNo);
        }

        // 如果还未找到，并且事件类型不包含冒号，则进行全匹配
        if (templateDto == null && !containsColon) {
            templateDto = sysNoticeTemplateGateway.findEventType(eventType, tenantId);
        }

        // 如果仍未找到，且事件类型包含冒号，则尝试半匹配（取冒号前的部分）
        if (templateDto == null && containsColon) {
            templateDto = sysNoticeTemplateGateway.findEventType(eventType.split(SymbolConstant.colon)[0], tenantId);
        }

        return templateDto;
    }

    private void updateLog(Long id,String msgId,String remark){
        SysNoticeEventLogContext sysNoticeEventLogContext = SysNoticeEventLogContext.builder()
                .id(id)
                .msgId(msgId)
                .remark(remark)
                .build();
        sysNoticeEventLogGateway.update(sysNoticeEventLogContext);
    }

    private Long logRecord(DomainEvent domainEvent,MessageNoticeContext noticeContext){
        SysNoticeEventLogContext sysNoticeEventLogContext = SysNoticeEventLogContext.builder()
                .eventType(noticeContext.getEventType())
                .module(domainEvent.getAppInfo().getModule())
                .node(domainEvent.getAppInfo().getNode())
                .msgId(domainEvent.getMsgId())
                .payload(JsonUtils.toJson(domainEvent))
                .createBy(domainEvent.getUserInfo().getLoginName())
                .createTime(LocalDateTimeUtil.dateTime())
                .status(StatusEnum.OFF.getStatus())
                .tenantId(domainEvent.getTenantId())
                .build();
        return sysNoticeEventLogGateway.insert(sysNoticeEventLogContext).getId();
    }

}
