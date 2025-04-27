package com.github.platform.core.gateway.admin.domain.constant;

import com.github.platform.core.standard.constant.SymbolConstant;
import lombok.Getter;

import java.util.Objects;

/**
 * 条件枚举，有些配置没有验证过，需要后续w完善验证
 * @Author: yxkong
 * @Date: 2024/8/21
 * @version: 1.0
 */
@Getter
public enum ConditionEnum {
    // 过滤器
    RewritePath("RewritePath",GatewayConstant.FILTER,2, SymbolConstant.COMMA,"regexp","replacement"),
    AddRequestHeader("AddRequestHeader", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"name","value"),
    SetPath("SetPath", GatewayConstant.FILTER,1, null,"template",null),
    RemoveRequestHeader("RemoveRequestHeader", GatewayConstant.FILTER,1, null,"name",null),
    AddRequestParameter("AddRequestParameter", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"name","value"),
    StripPrefix("StripPrefix", GatewayConstant.FILTER,1, null,"parts",null),
    Retry("Retry", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"retries","backoff"),
    AddResponseHeader("AddResponseHeader", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"name","value"),
    PrefixPath("PrefixPath", GatewayConstant.FILTER,1, null,"prefix",null),
    RemoveRequestParameter("RemoveRequestParameter", GatewayConstant.FILTER,1, null,"name",null),
    RemoveResponseHeader("RemoveResponseHeader", GatewayConstant.FILTER,1, null,"name",null),
    RedirectTo("RedirectTo", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"status","url"),
    RewriteResponseHeader("RewriteResponseHeader", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"header","regexp"),
    SetResponseStatusCode("SetResponseStatusCode", GatewayConstant.FILTER,1, null,"status","null"),
    //自定义filter
    CUSTOM_FILTER("CustomFilter", GatewayConstant.FILTER,2, SymbolConstant.COMMA,"name","args"),

    //自定义分类
    AUTH_EXCLUSION(GatewayConstant.AUTH_EXCLUSION,GatewayConstant.CUSTOM_TYPE,0, SymbolConstant.COMMA,"name","args"),
    // 断言
    Path("Path",GatewayConstant.PREDICATE,1, null,"pattern",""),
    Header("Header", GatewayConstant.PREDICATE,2, SymbolConstant.COMMA,"header","regexp"),
    Method("Method", GatewayConstant.PREDICATE,1, null,"method",""),
    Query("Query", GatewayConstant.PREDICATE,2, SymbolConstant.COMMA,"param","regexp"),
    Host("Host", GatewayConstant.PREDICATE,1, null,"host",""),
    Cookie("Cookie", GatewayConstant.PREDICATE,2, SymbolConstant.COMMA,"name","value"),
    RemoteAddr("RemoteAddr", GatewayConstant.PREDICATE,1, null,"remoteAddr",""),
    After("After", GatewayConstant.PREDICATE,1, null,"datetime",""),
    Before("Before", GatewayConstant.PREDICATE,1, null,"datetime",""),
    Between("Between", GatewayConstant.PREDICATE,2, SymbolConstant.COMMA,"datetime1","datetime2"),
    Weight("Weight", GatewayConstant.PREDICATE,2, SymbolConstant.COMMA,"group","weight");
    ;
    ConditionEnum(String name,String type, Integer length, String split, String split0, String split1) {
        this.name = name;
        this.type = type;
        this.length = length;
        this.split = split;
        this.split0 = split0;
        this.split1 = split1;
    }
    private final String name;
    private final String type;
    private final Integer length;
    private final String split;
    private final String split0;
    private final String split1;

    public static ConditionEnum getByName(String name) {
        for (ConditionEnum conditionEnum : ConditionEnum.values()) {
            if (conditionEnum.name.equals(name)) {
                return conditionEnum;
            }
        }
        return null;
    }
    public boolean isFilter() {
        return Objects.equals(GatewayConstant.FILTER,this.type) ;
    }
    public boolean isPredicate() {
        return Objects.equals(GatewayConstant.PREDICATE,this.type) ;
    }
    public boolean isCustomType() {
        return Objects.equals(GatewayConstant.CUSTOM_TYPE,this.type) ;
    }
}
