package com.github.platform.core.message.application.executor.impl;

import com.github.platform.core.auth.application.executor.SysExecutor;
import com.github.platform.core.message.application.executor.ISysNoticeSendLogExecutor;
import com.github.platform.core.message.domain.context.SysNoticeSendLogContext;
import com.github.platform.core.message.domain.context.SysNoticeSendLogQueryContext;
import com.github.platform.core.message.domain.dto.SysNoticeSendLogDto;
import com.github.platform.core.message.domain.gateway.ISysNoticeSendLogGateway;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.entity.dto.PageBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
/**
 * 通知发送记录执行器
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Service
@Slf4j
public class SysNoticeSendLogExecutorImpl extends SysExecutor implements ISysNoticeSendLogExecutor{
    @Resource
    private ISysNoticeSendLogGateway sysNoticeSendLogGateway;
    /**
    * 查询通知发送记录列表
    * @param context 查询上下文
    * @return 分页结果
    */
    @Override
    public PageBean<SysNoticeSendLogDto> query(SysNoticeSendLogQueryContext context){
        context.setTenantId(getTenantId(context));
        return sysNoticeSendLogGateway.query(context);
    }

    @Override
    public PageBean<SysNoticeSendLogDto> findByEventId(Long eventId) {
        return sysNoticeSendLogGateway.query(SysNoticeSendLogQueryContext.builder().eventId(eventId).pageNum(1).pageSize(100).build());
    }

    /**
    * 新增通知发送记录
    * @param context 新增上下文
    */
    @Override
    public String insert(SysNoticeSendLogContext context){
        context.setTenantId(getMustTenantId(context));
        SysNoticeSendLogDto record = sysNoticeSendLogGateway.insert(context);
        if (Objects.isNull(record.getId())){
            throw exception(ResultStatusEnum.COMMON_INSERT_ERROR);
        }
        return record.getStrId();
    }
    /**
    * 根据id查询通知发送记录明细
    * @param id 主键
    * @return 单条记录
    */
    @Override
    public SysNoticeSendLogDto findById(Long id) {
        return sysNoticeSendLogGateway.findById(id);
    }
    /**
    * 修改通知发送记录
    * @param context 更新上下文
    */
    @Override
    public void update(SysNoticeSendLogContext context) {
        context.setTenantId(getTenantId(context));
        Pair<Boolean, SysNoticeSendLogDto> update = sysNoticeSendLogGateway.update(context);
        if (!update.getKey()){
            throw exception(ResultStatusEnum.COMMON_UPDATE_ERROR);
        }
    }
    /**
    * 根据id删除通知发送记录记录
    * @param id 主键
    */
    @Override
    public void delete(Long id) {
        /**此处是为了再gateway上做多条件缓存，如果有必要，先查，后设置值*/
        SysNoticeSendLogContext context = SysNoticeSendLogContext.builder().id(id).build();
        int d = sysNoticeSendLogGateway.delete(context);
        if (d <=0 ){
            throw exception(ResultStatusEnum.COMMON_DELETE_ERROR);
        }
    }
}
