package com.github.platform.core.message.infra.convert;

import com.github.pagehelper.PageInfo;
import com.github.platform.core.message.domain.common.entity.SysNoticeSendLogBase;
import com.github.platform.core.message.domain.context.SysNoticeSendLogContext;
import com.github.platform.core.message.domain.context.SysNoticeSendLogQueryContext;
import com.github.platform.core.message.domain.dto.SysNoticeSendLogDto;
import com.github.platform.core.standard.entity.dto.PageBean;
import org.mapstruct.*;
import java.util.List;
/**
 * 通知发送记录基础设施层转换器
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysNoticeSendLogInfraConvert {
    /**
    * 数据库实体列表转dto列表
    * @param list 数据库实体列表
    * @return dto列表
    */
    List<SysNoticeSendLogDto> toDtos(List<SysNoticeSendLogBase> list);
    /**
    * 数据库实体转dto
    * @param entity 数据库实体
    * @return dto
    */
    @Mappings({
        @Mapping(target = "strId", expression = "java(com.github.platform.core.common.utils.SignUtil.getStrId(entity.getId()))"),
    })
    SysNoticeSendLogDto toDto(SysNoticeSendLogBase entity);
    /**
    * 数据库分页转业务分页
    * @param pageInfo 数据库分页
    * @return 业务分页
    */
    @Mappings({
        @Mapping(target = "totalSize", source = "total"),
        @Mapping(target = "data", source = "list"),
    })
    PageBean<SysNoticeSendLogDto> ofPageBean(PageInfo<SysNoticeSendLogBase> pageInfo);

    /**
    * 查询上下文转数据库实体
    * @param context 数据库分页
    * @return 数据库实体
    */
    SysNoticeSendLogBase toSysNoticeSendLogBase(SysNoticeSendLogQueryContext context);
    /**
    * 实体上下文转数据库实体
    * @param context 实体上下文
    * @return 数据库实体
    */
    SysNoticeSendLogBase toSysNoticeSendLogBase(SysNoticeSendLogContext context);
    /**
    * dto转数据库实体
    * @param dto 传输实体
    * @return 数据库实体
    */
    SysNoticeSendLogBase toSysNoticeSendLogBase(SysNoticeSendLogDto dto);
}