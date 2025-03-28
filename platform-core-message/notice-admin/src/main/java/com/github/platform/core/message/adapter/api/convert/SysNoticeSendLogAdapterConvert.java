package com.github.platform.core.message.adapter.api.convert;

import com.github.platform.core.message.adapter.api.command.SysNoticeSendLogCmd;
import com.github.platform.core.message.adapter.api.command.SysNoticeSendLogQuery;
import com.github.platform.core.message.domain.context.SysNoticeSendLogContext;
import com.github.platform.core.message.domain.context.SysNoticeSendLogQueryContext;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
/**
 * 通知发送记录Controller到Application层的适配
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysNoticeSendLogAdapterConvert {
    /**
    * 查询实体转查询上下文
    * @param query 查询实体
    * @return 查询上下文
    */
    SysNoticeSendLogQueryContext toQuery(SysNoticeSendLogQuery query);
    /**
    * 操作实体转操作上下文
    * @param cmd 操作实体
    * @return 操作上下文
    */
    SysNoticeSendLogContext toContext(SysNoticeSendLogCmd cmd);
}