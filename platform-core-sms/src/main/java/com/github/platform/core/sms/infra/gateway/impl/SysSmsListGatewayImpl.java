package com.github.platform.core.sms.infra.gateway.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.platform.core.persistence.mapper.sms.SysSmsListMapper;
import com.github.platform.core.sms.domain.common.entity.SysSmsListBase;
import com.github.platform.core.sms.domain.context.SysSmsListContext;
import com.github.platform.core.sms.domain.context.SysSmsListQueryContext;
import com.github.platform.core.sms.domain.dto.SysSmsListDto;
import com.github.platform.core.sms.domain.gateway.ISysSmsListGateway;
import com.github.platform.core.sms.infra.convert.SysSmsListInfraConvert;
import com.github.platform.core.standard.entity.dto.PageBean;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
* 短信白名单网关层实现
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-14 17:39:30.643
* @version 1.0
*/
@Service
public class SysSmsListGatewayImpl implements ISysSmsListGateway {

    @Resource
    private SysSmsListMapper smsListMapper;
    @Resource
    private SysSmsListInfraConvert convert;
    @Override
    public PageBean<SysSmsListDto> query(SysSmsListQueryContext context) {
        SysSmsListBase smsListBase = convert.toSmsListBase(context);
        PageHelper.startPage( context.getPageNum(), context.getPageSize());
        List<SysSmsListBase> list = smsListMapper.findListBy(smsListBase);
        return convert.ofPageBean(new PageInfo<>(list));
    }

    @Override
    public List<SysSmsListDto> findByList(SysSmsListQueryContext context) {
        SysSmsListBase smsListBase = convert.toSmsListBase(context);
        List<SysSmsListBase> list = smsListMapper.findListBy(smsListBase);
        return convert.toDtos(list);
    }
    @Override
    public long findListByCount(SysSmsListQueryContext context) {
        SysSmsListBase smsListBase = convert.toSmsListBase(context);
        return smsListMapper.findListByCount(smsListBase);
    }

    @Override
    public SysSmsListDto insert(SysSmsListContext context) {
        SysSmsListBase smsListBase = convert.toSmsListBase(context);
        smsListMapper.insert(smsListBase);
        return convert.toDto(smsListBase);
    }

    @Override
    public SysSmsListDto findById(Long id) {
        SysSmsListBase smsListBase = smsListMapper.findById(id);
        return convert.toDto(smsListBase);
    }

    @Override
    public Pair<Boolean, SysSmsListDto> update(SysSmsListContext context) {
        SysSmsListBase smsListBase = convert.toSmsListBase(context);
        int flag = smsListMapper.updateById(smsListBase);
        return Pair.of( flag>0 , convert.toDto(smsListBase)) ;
    }

    @Override
    public int delete(Long id) {
        return smsListMapper.deleteById(id);
    }
    @Override
    public boolean existMobile(String mobile){
        SysSmsListBase record = smsListMapper.findByMobile(mobile);
        if (Objects.nonNull(record) && record.isOn()){
            return true;
        }
        return false;
    }
}
