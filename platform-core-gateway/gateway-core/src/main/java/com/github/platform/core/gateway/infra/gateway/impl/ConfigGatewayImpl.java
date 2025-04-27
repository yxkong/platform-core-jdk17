package com.github.platform.core.gateway.infra.gateway.impl;

import com.github.platform.core.auth.configuration.properties.AuthProperties;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.gateway.domain.gateway.IConfigGateway;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

/**
 * 配置中心实现url过滤
 * @author: yxkong
 * @date: 2021/12/6 4:08 PM
 * @version: 1.0
 */
@Service("configGateway")
public class ConfigGatewayImpl implements IConfigGateway {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Resource
    private AuthProperties authProperties;
    @Override
    public boolean excludeHost(String host) {
        return hostEquals(authProperties.getForwardHosts(),host);
    }

    @Override
    public boolean isRedirectPath(String path) {
        if (CollectionUtil.isEmpty(authProperties.getRedirectPaths())){
            return false;
        }
        return authProperties.getRedirectPaths().stream().noneMatch(s-> PATH_MATCHER.match(s,path));
    }

    @Override
    public boolean isRedirectHost(String host) {
        if (CollectionUtil.isEmpty(authProperties.getRedirectHosts())){
            return false;
        }
        return authProperties.getRedirectPaths().stream().noneMatch(s-> PATH_MATCHER.match(s,host));
    }
}
