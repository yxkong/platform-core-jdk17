package com.github.platform.core.message.infra.gateway.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.platform.core.message.domain.common.entity.SysNoticeSendLogBase;
import com.github.platform.core.message.domain.context.SysNoticeSendLogContext;
import com.github.platform.core.message.domain.context.SysNoticeSendLogQueryContext;
import com.github.platform.core.message.domain.dto.SysNoticeSendLogDto;
import com.github.platform.core.message.domain.gateway.ISysNoticeSendLogGateway;
import com.github.platform.core.persistence.mapper.message.SysNoticeSendLogMapper;
import com.github.platform.core.message.infra.convert.SysNoticeSendLogInfraConvert;
import com.github.platform.core.common.gateway.BaseGatewayImpl;
import com.github.platform.core.standard.constant.StatusEnum;
import com.github.platform.core.standard.entity.dto.PageBean;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
/**
 * 通知发送记录网关层实现
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Service
public class SysNoticeSendLogGatewayImpl extends BaseGatewayImpl implements ISysNoticeSendLogGateway {

    @Resource
    private SysNoticeSendLogMapper sysNoticeSendLogMapper;
    @Resource
    private SysNoticeSendLogInfraConvert sysNoticeSendLogConvert;
    @Override
    public PageBean<SysNoticeSendLogDto> query(SysNoticeSendLogQueryContext context) {
        SysNoticeSendLogBase record = sysNoticeSendLogConvert.toSysNoticeSendLogBase(context);
        PageHelper.startPage( context.getPageNum(), context.getPageSize());
        List<SysNoticeSendLogBase> list = sysNoticeSendLogMapper.findListBy(record);
        return sysNoticeSendLogConvert.ofPageBean(new PageInfo<>(list));
    }

    @Override
    public List<SysNoticeSendLogDto> findListByLogId(Long logId) {
        SysNoticeSendLogBase record = SysNoticeSendLogBase.builder().logId(logId).build();
        List<SysNoticeSendLogBase> list = sysNoticeSendLogMapper.findListBy(record);
        return sysNoticeSendLogConvert.toDtos(list);
    }

    @Override
    public SysNoticeSendLogDto insert(SysNoticeSendLogContext context) {
        SysNoticeSendLogBase record = sysNoticeSendLogConvert.toSysNoticeSendLogBase(context);
        sysNoticeSendLogMapper.insert(record);
        return sysNoticeSendLogConvert.toDto(record);
    }
    @Override
    public long findByCount(Long logId){
        return  sysNoticeSendLogMapper.findListByCount(SysNoticeSendLogBase.builder().logId(logId).status(StatusEnum.ON.getStatus()).build());
    }

    @Override
    public SysNoticeSendLogDto findById(Long id) {
        if (Objects.isNull(id)){
            return null;
        }
        SysNoticeSendLogBase record = sysNoticeSendLogMapper.findById(id);
        return sysNoticeSendLogConvert.toDto(record);
    }

    @Override
    public Pair<Boolean, SysNoticeSendLogDto> update(SysNoticeSendLogContext context) {
        SysNoticeSendLogBase record = sysNoticeSendLogConvert.toSysNoticeSendLogBase(context);
        int flag = sysNoticeSendLogMapper.updateById(record);
        return Pair.of( flag>0 , sysNoticeSendLogConvert.toDto(record)) ;
    }

    @Override
    public int delete(SysNoticeSendLogContext context) {
        return sysNoticeSendLogMapper.deleteById(context.getId());
    }
}
