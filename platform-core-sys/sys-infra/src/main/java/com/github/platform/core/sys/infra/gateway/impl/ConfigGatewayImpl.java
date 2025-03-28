package com.github.platform.core.sys.infra.gateway.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.platform.core.cache.domain.constant.CacheConstant;
import com.github.platform.core.common.gateway.BaseGatewayImpl;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.persistence.mapper.sys.SysConfigMapper;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.sys.domain.common.entity.SysConfigBase;
import com.github.platform.core.sys.domain.context.SysConfigContext;
import com.github.platform.core.sys.domain.context.SysConfigQueryContext;
import com.github.platform.core.sys.domain.dto.SysConfigDto;
import com.github.platform.core.sys.domain.gateway.ISysConfigGateway;
import com.github.platform.core.sys.infra.constant.SysInfraResultEnum;
import com.github.platform.core.sys.infra.convert.SysConfigInfraConvert;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author: yxkong
 * @date: 2023/4/19 4:00 PM
 * @version: 1.0
 */
@Service
public class ConfigGatewayImpl extends BaseGatewayImpl implements ISysConfigGateway {
    @Resource
    private SysConfigInfraConvert convert;
    @Resource
    private SysConfigMapper sysConfigMapper;
    @Override
    public PageBean<SysConfigDto> query(SysConfigQueryContext context) {
        SysConfigBase configBase = convert.toSysConfigBase(context);
        PageHelper.startPage(context.getPageNum(),context.getPageSize());
        List<SysConfigBase> list = sysConfigMapper.findListBy(configBase);
        return convert.ofPageBean(new PageInfo<>(list));
    }

    @Override
    public List<SysConfigDto> findListBy(SysConfigQueryContext context) {
        SysConfigBase configBase = convert.toSysConfigBase(context);
        List<SysConfigBase> list = sysConfigMapper.findListBy(configBase);
        return convert.toDtos(list);
    }

    @Override
    public void insert(SysConfigContext context) {
        SysConfigBase record = this.findByKey(context.getTenantId(),context.getKey());
        if (Objects.nonNull(record)){
            throw exception(SysInfraResultEnum.CONFIG_ADD_EXIST);
        }
        record = convert.toSysConfigBase(context);
        int insert = sysConfigMapper.insert(record);
        if (insert<=0){
            throw exception(ResultStatusEnum.COMMON_INSERT_ERROR);
        }
    }
    private SysConfigBase findByKey(Integer tenantId, String key) {
        /**暂时不实现租户*/
        SysConfigBase record = SysConfigBase.builder().key(key).build();
        List<SysConfigBase> listBy = sysConfigMapper.findListBy(record);
        if (CollectionUtil.isEmpty(listBy)){
            return null;
        }
        return listBy.get(0);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = CACHE_NAME,key = "#root.target.PREFIX_COLON + #context.id",cacheManager = CacheConstant.cacheManager),
                    @CacheEvict(cacheNames = CACHE_NAME,key = "#root.target.PREFIX_COLON + #context.tenantId+':'+#context.key",cacheManager = CacheConstant.cacheManager)
            }
    )
    public void update(SysConfigContext context) {
        if (Objects.isNull(context.getId())){
            throw exception(ResultStatusEnum.UPDATE_ID_IS_NULL);
        }
        SysConfigBase record = sysConfigMapper.findById(context.getId());
        if (!record.getKey().equals(context.getKey())){
            throw exception(SysInfraResultEnum.CONFIG_NOT_UPDATE_KEY);
        }
        record = convert.toSysConfigBase(context);
        int flag = sysConfigMapper.updateById(record);
        if (flag <= 0){
            throw exception(ResultStatusEnum.COMMON_UPDATE_ERROR);
        }

    }
    @Override
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = CACHE_NAME,key = "#root.target.PREFIX_COLON + #id",cacheManager = CacheConstant.cacheManager),
                    @CacheEvict(cacheNames = CACHE_NAME,key = "#root.target.PREFIX_COLON +#tenantId+':'+#key",cacheManager = CacheConstant.cacheManager)
            }
    )
    public void delete(Long id,Integer tenantId,String key) {
        SysConfigBase record = sysConfigMapper.findById(id);
        if (Objects.isNull(record)){
            throw exception(ResultStatusEnum.NO_DATA);
        }
        //删除字典
        int row = sysConfigMapper.deleteById(id);
        if ( row <= 0) {
            throw exception(ResultStatusEnum.COMMON_UPDATE_ERROR);
        }
    }

    @Override
    public SysConfigDto findById(Long id) {
        SysConfigBase sysConfig = sysConfigMapper.findById(id);
        return convert.toDto(sysConfig);
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#root.target.PREFIX_COLON + #tenantId+':'+ #key", cacheManager = CacheConstant.cacheManager, unless = "#result == null")
    public SysConfigDto getConfig(Integer tenantId,String key) {
        SysConfigBase sysConfig = this.findByKey(null, key);
        return convert.toDto(sysConfig);
    }

    @Override
    @Cacheable(cacheNames = CACHE_NAME, key = "#root.target.PREFIX_COLON + #tenantId+':'+ #key", cacheManager = CacheConstant.cacheManager, unless = "#result == null")
    public void deleteCache(Integer tenantId,String key) {

    }
}
