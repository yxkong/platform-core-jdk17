package com.github.platform.core.sys.application.executor.impl;

import com.github.platform.core.auth.application.executor.SysExecutor;
import com.github.platform.core.auth.util.LoginUserInfoUtil;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.sys.application.executor.ISysDictExecutor;
import com.github.platform.core.sys.domain.constant.DictConstant;
import com.github.platform.core.sys.domain.context.SysDictContext;
import com.github.platform.core.sys.domain.context.SysDictQueryContext;
import com.github.platform.core.sys.domain.dto.SysDictDto;
import com.github.platform.core.sys.domain.dto.SysDictTypeDto;
import com.github.platform.core.sys.domain.gateway.ISysDictGateway;
import com.github.platform.core.sys.domain.gateway.ISysDictTypeGateway;
import com.github.platform.core.sys.infra.constant.SysInfraResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 字典执行器
 *
 * @author: yxkong
 * @date: 2023/2/7 5:32 下午
 * @version: 1.0
 */
@Service
@Slf4j
public class SysDictExecutorImpl extends SysExecutor implements ISysDictExecutor {
    @Resource
    private ISysDictGateway dictGateway;
    @Resource
    private ISysDictTypeGateway dictTypeGateway;
    @Override
    public PageBean<SysDictDto> query(SysDictQueryContext context) {
        context.setTenantId(getTenantId(context));
        return dictGateway.query(context);
    }

    @Override
    public void insert(SysDictContext context) {
        context.setTenantId(getMustTenantId(context));
        SysDictTypeDto dictType = dictTypeGateway.findByType(context.getDictType(),context.getTenantId());
        if (Objects.isNull(dictType)){
            throw exception(SysInfraResultEnum.DICT_TYPE_NOT_FOUND);
        }
        context.setTenantId(dictType.getTenantId());
        dictGateway.insert(context);
    }

    @Override
    public void update(SysDictContext context) {
        context.setTenantId(getTenantId(context));
        dictGateway.update(context);
    }
    @Override
    public void delete(Long id) {
        SysDictDto dto = dictGateway.findById(id);
        if (Objects.isNull(dto)  ) {
            throw exception(SysInfraResultEnum.DICT_EXIST);
        }
        dictGateway.delete(SysDictContext.builder().id(dto.getId()).dictType(dto.getDictType()).build());
    }

    @Override
    public List<SysDictDto> findByType(SysDictQueryContext context) {
        Integer tenantId = getMustTenantId(context);
        if (Objects.equals(DictConstant.ALL_DICT,context.getDictType())){
            return dictTypeGateway.findAllType(tenantId);
        }
        return dictGateway.findByType(context.getDictType(), tenantId);
    }


}
