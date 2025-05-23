package com.github.platform.core.sys.infra.gateway.impl;

import com.github.platform.core.auth.configuration.properties.AuthProperties;
import com.github.platform.core.auth.entity.LoginUserInfo;
import com.github.platform.core.auth.entity.TokenCacheEntity;
import com.github.platform.core.auth.gateway.ITokenCacheGateway;
import com.github.platform.core.cache.infra.service.ICacheService;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.standard.constant.StatusEnum;
import com.github.platform.core.standard.util.LocalDateTimeUtil;
import com.github.platform.core.sys.domain.context.SysTokenCacheContext;
import com.github.platform.core.sys.domain.context.SysTokenCacheQueryContext;
import com.github.platform.core.sys.domain.dto.SysTokenCacheDto;
import com.github.platform.core.sys.domain.gateway.ISysTokenCacheGateway;
import com.github.platform.core.sys.infra.convert.SysTokenCacheInfraConvert;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * token映射操作
 * @author: yxkong
 * @date: 2024/4/29 22:19
 * @version: 1.0
 */
@Service
@Slf4j
public class TokenCacheGatewayImpl implements ITokenCacheGateway {
    @Resource
    private ISysTokenCacheGateway sysTokenCacheGateway;
    @Resource
    private SysTokenCacheInfraConvert sysTokenCacheConvert;
    @Resource
    private AuthProperties authProperties;
    private  long getSeconds(){
        return TimeoutUtils.toSeconds(authProperties.getSys().getExpire(), TimeUnit.MINUTES);
    }

    @Resource
    private ICacheService cacheService;
    @Override
    public TokenCacheEntity findByToken(String token) {
        //查询缓存或数据库
        SysTokenCacheDto tokenCacheDto = sysTokenCacheGateway.findByToken(token);
        if (Objects.isNull(tokenCacheDto) || tokenCacheDto.isExpired()){
            return null;
        }
        return sysTokenCacheConvert.toEntity(tokenCacheDto);
    }

    @Override
    public TokenCacheEntity findByLoginName(Integer tenantId, String loginName) {

        SysTokenCacheDto record = sysTokenCacheGateway.findByLoginName(tenantId, loginName);
        return sysTokenCacheConvert.toEntity(record);
    }

    @Override
    public TokenCacheEntity saveOrUpdate(Integer tenantId, String token, String loginName,String optUser, String loginInfo ,boolean isLogin) {
        /**
         * 使用场景：
         * 1，AuthorizeAspect切面续租  tenantId = null ，isLogin = false  不管状态，直接更新expireTime，续租
         * 2, 登录成功，保持token isLogin = true   直接插入 sysTokenCacheGateway.insert
         * 3，刷新token isLogin = false // 更新，但是不续租expireTime 不变
         */

        SysTokenCacheDto cacheDto =  sysTokenCacheGateway.findByToken(token);
        LoginUserInfo userInfo = JsonUtils.fromJson(loginInfo,LoginUserInfo.class);
        SysTokenCacheContext context = getSysTokenCacheContext(userInfo,tenantId, token, loginName, optUser, loginInfo, isLogin);
        if (Objects.nonNull(cacheDto) ){
            context.setId(cacheDto.getId());
            if (Objects.isNull(tenantId)){
                context.setRemark("last:"+cacheDto.getExpireTime());
            }
        }
        if (Objects.isNull(cacheDto)){
            cacheDto = sysTokenCacheGateway.insert(context);
        } else {
            cacheDto  = sysTokenCacheGateway.update(context);
        }
        return sysTokenCacheConvert.toEntity(cacheDto);
    }
    private SysTokenCacheContext getSysTokenCacheContext(LoginUserInfo userInfo,Integer tenantId,String token, String loginName,String optUser, String loginInfo ,boolean isLogin){
        //构建上下文
        LocalDateTime localDateTime = LocalDateTimeUtil.dateTime();
        SysTokenCacheContext context = SysTokenCacheContext.builder()
                .token(token)
                .loginName(loginName)
                .loginInfo(loginInfo)
                .build();
        if (Objects.isNull(tenantId) || isLogin){
            context.setExpireTime(localDateTime.plusSeconds( getSeconds()));
        }
        if (isLogin){
            context.setCreateBy(optUser);
            context.setCreateTime(localDateTime);
            context.setLoginWay(userInfo.getLoginWay());
            context.setTenantId(userInfo.getTenantId());
        } else {
            context.setLastLoginTime(LocalDateTimeUtil.parseDefault(userInfo.getLoginTime()));
            context.setUpdateBy(optUser);
            context.setUpdateTime(localDateTime);
        }
        return context;
    }

    @Override
    public void expireByToken( String token) {
        SysTokenCacheDto cacheDto =  sysTokenCacheGateway.findByToken(token);
        if (Objects.isNull(cacheDto) || cacheDto.isExpired()){
            return;
        }
        sysTokenCacheGateway.expire(toExpireContext(cacheDto));
    }

    @Override
    public void expireById(Long id){
        SysTokenCacheDto cacheDto = sysTokenCacheGateway.findById(id);
        if (Objects.isNull(cacheDto)  || cacheDto.isExpired()){
            return;
        }
        sysTokenCacheGateway.expire(toExpireContext(cacheDto));

    }

    private SysTokenCacheContext toExpireContext(SysTokenCacheDto dto){
        return SysTokenCacheContext.builder()
                .id(dto.getId())
                .token(dto.getToken())
                .loginName(dto.getLoginName())
                .tenantId(dto.getTenantId())
                .status(StatusEnum.OFF.getStatus())
                .build();
    }

    @Override
    public void expireByLoginName(Integer tenantId, String loginName) {
        //查询所有有效的token，并过期，这里不能使用批量更新，因为要操作缓存
        SysTokenCacheQueryContext queryContext = SysTokenCacheQueryContext.builder().tenantId(tenantId).loginName(loginName).searchStartTime(LocalDateTimeUtil.dateTimeDefaultShort()).build();
        List<SysTokenCacheDto> list = sysTokenCacheGateway.findListBy(queryContext);

        if (CollectionUtil.isEmpty(list)){
            return;
        }
        list.forEach(s->{
            sysTokenCacheGateway.expire(toExpireContext(s));
        });
    }
}
