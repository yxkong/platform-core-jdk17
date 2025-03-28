package com.github.platform.core.sys.application.executor.impl;

import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.sys.application.executor.ISysDictTypeExecutor;
import com.github.platform.core.sys.domain.context.SysDictTypeContext;
import com.github.platform.core.sys.domain.context.SysDictTypeQueryContext;
import com.github.platform.core.sys.domain.dto.SysDictTypeDto;
import com.github.platform.core.sys.domain.gateway.ISysDictGateway;
import com.github.platform.core.sys.domain.gateway.ISysDictTypeGateway;
import com.github.platform.core.auth.application.executor.SysExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 字典类型执行器
 * @Author: yxkong
 * @Date: 2024/1/28 12:42
 * @version: 1.0
 */
@Service
@Slf4j
public class SysDictTypeExecutorImpl extends SysExecutor implements ISysDictTypeExecutor {
    @Resource
    private ISysDictTypeGateway dictTypeGateway;
    @Resource
    private ISysDictGateway dictGateway;

    @Override
    public PageBean<SysDictTypeDto> query(SysDictTypeQueryContext context) {
        context.setTenantId(getTenantId(context));
        return dictTypeGateway.query(context);
    }
    @Override
    public void insert(SysDictTypeContext context) {
        context.setTenantId(getMustTenantId(context));
        dictTypeGateway.insert(context);
    }

    @Override
    public void update(SysDictTypeContext context) {
        context.setTenantId(getTenantId(context));
        dictTypeGateway.update(context);
    }
    @Override
    public void delete(Long id) {
        SysDictTypeDto dto = dictTypeGateway.findById(id);
        if (Objects.isNull(dto)){
            return;
        }
        dictTypeGateway.delete(SysDictTypeContext.builder().id(id).type(dto.getType()).build());
    }
    @Override
    public void reload(SysDictTypeQueryContext context) {
        String type = context.getType();
        Integer tenantId = getMustTenantId(context);
        if (StringUtils.isNotEmpty(type)){
            dictTypeGateway.deleteCache(type,tenantId);
            dictGateway.findByType(type,tenantId);
            return;
        }
        List<SysDictTypeDto> list = dictTypeGateway.findListBy(SysDictTypeQueryContext.builder().tenantId(tenantId).build());
        if (CollectionUtil.isEmpty(list)){
            return;
        }
        dictTypeGateway.deleteAllCache(tenantId);
        list.forEach(s->{
            dictTypeGateway.deleteCache(s.getType(),s.getTenantId());
            dictGateway.findByType(s.getType(),s.getTenantId());
        });

    }
}
