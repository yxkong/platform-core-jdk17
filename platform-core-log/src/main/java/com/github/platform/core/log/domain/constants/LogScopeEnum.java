package com.github.platform.core.log.domain.constants;

import lombok.Getter;

/**
 * 日志打印范围枚举
 * @author: yxkong
 * @date: 2023/5/4 1:18 PM
 * @version: 1.0
 */
@Getter
public enum LogScopeEnum {
    ALL("all","入参和出参"),
    REQUEST("request","入参"),
    RESPONSE("response","出参");
    private final String scope;
    private final String desc;

    LogScopeEnum(String scope, String desc) {
        this.scope = scope;
        this.desc = desc;
    }
    public static LogScopeEnum of(String scope){
        for (LogScopeEnum logScope:LogScopeEnum.values()){
            if (logScope.getScope().equalsIgnoreCase(scope)){
                return logScope;
            }
        }
        return null;
    }
}
