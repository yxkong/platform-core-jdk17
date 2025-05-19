package com.github.platform.core.sms.application.executor.impl;

import com.github.platform.core.auth.application.executor.SysExecutor;
import com.github.platform.core.sms.application.executor.ISysSmsListExecutor;
import com.github.platform.core.sms.domain.context.SysSmsListContext;
import com.github.platform.core.sms.domain.context.SysSmsListQueryContext;
import com.github.platform.core.sms.domain.dto.SysSmsListDto;
import com.github.platform.core.sms.domain.gateway.ISysSmsListGateway;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.entity.dto.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Objects;

/**
* 短信白名单执行器
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-14 17:39:30.643
* @version 1.0
*/
@Service
@Slf4j
public class SysSmsListExecutorImpl extends SysExecutor implements ISysSmsListExecutor {
    @Resource
    private ISysSmsListGateway gateway;
    @Override
    public PageBean<SysSmsListDto> query(SysSmsListQueryContext context){
        context.setTenantId(getTenantId(context));
        return gateway.query(context);
    };
    @Override
    public void insert(SysSmsListContext context){
        context.setTenantId(getTenantId(context));
        SysSmsListDto record = gateway.insert(context);
        if (Objects.isNull(record.getId())){
            throw exception(ResultStatusEnum.COMMON_INSERT_ERROR);
        }
    }
    @Override
    public SysSmsListDto findById(Long id) {
        return gateway.findById(id);
    }
    @Override
    public void update(SysSmsListContext context) {
        context.setTenantId(getTenantId(context));
        Pair<Boolean, SysSmsListDto> update = gateway.update(context);
        if (!update.getKey()){
            throw exception(ResultStatusEnum.COMMON_UPDATE_ERROR);
        }
    }
    @Override
    public void delete(Long id) {
        int delete = gateway.delete(id);
        if (delete <=0 ){
            throw exception(ResultStatusEnum.COMMON_DELETE_ERROR);
        }
    }
}
