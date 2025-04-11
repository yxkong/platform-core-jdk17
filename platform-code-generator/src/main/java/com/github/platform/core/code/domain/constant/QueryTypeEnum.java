package com.github.platform.core.code.domain.constant;

import lombok.Getter;

/**
 * 查询类型枚举
 * @Author: yxkong
 * @Date: 2023/4/27 3:47 PM
 * @version: 1.0
 */
@Getter
public enum QueryTypeEnum {
    EQ("=","等于"),
    LT("<","小于"),
    LEQ("<=","小于等于"),
    GT(">","大于"),
    GEQ(">=","大于等于"),
    NE("!=","不等于"),
    LIKE("like","模糊"),
    IS_NULL("isNull","为null"),
    NOT_NULL("notnull","不为null"),
    between("between","在什么之间");
    private final String type ;
    private final String desc ;
    QueryTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }


}
