package com.github.platform.core.sys.domain.constant;

import com.github.platform.core.standard.constant.SymbolConstant;
import lombok.Getter;

/**
 * sys模块缓存key前缀
 * @Author: yxkong
 * @Date: 2024/7/29
 * @version: 1.0
 */
@Getter
public enum SysCacheKeyPrefix {

    DICT(SysCacheConstant.DICT_PREFIX),
    DICT_TYPE(SysCacheConstant.DICT_TYPE_PREFIX),
    DEPT(SysCacheConstant.DEPT_PREFIX),
    CONFIG(SysCacheConstant.CONFIG_PREFIX),
    ROLE(SysCacheConstant.ROLE_PREFIX),
    TOKEN_CACHE(SysCacheConstant.TOKEN_CACHE_PREFIX),
    USER_CONFIG(SysCacheConstant.USER_CONFIG_PREFIX),
    USER(SysCacheConstant.USER_PREFIX),
    ;

    private final String prefix;
    private final String withColon;

    SysCacheKeyPrefix(String prefix) {
        this.prefix = prefix;
        this.withColon = getPrefixWithColon(prefix);
    }

    private static String getPrefix(String key) {
        return key;
    }
    private static String getPrefixWithColon(String key) {
        return key+SymbolConstant.colon;
    }
}