package com.github.platform.core.gateway.domain.gateway;

import java.util.List;

/**
 * 配置网关
 *
 * @author: yxkong
 * @date: 2021/12/6 11:47 AM
 * @version: 1.0
 */
public interface IConfigGateway {
    /**
     * 判断host是否在放行列表里，用于后端请求时，只是添加白名单放行
     * @param hosts
     * @param host
     * @return
     */
    default boolean hostEquals(List<String> hosts, String host){
        if(hosts!= null
                && hosts.stream().anyMatch(host::equals)){
            return Boolean.TRUE;
        }
        return  Boolean.FALSE;
    }
    /**
     * 哪些host不能走网关
     * @param host
     * @return 走网关返回true，不走，返回false
     */
    boolean excludeHost(String host);

    /**
     * 请求路径是否需要重定向
     * @param path
     * @return
     */
    boolean isRedirectPath(String path);

    /**
     * 转发url是否白名单url
     */
    boolean isRedirectHost(String host);
}
