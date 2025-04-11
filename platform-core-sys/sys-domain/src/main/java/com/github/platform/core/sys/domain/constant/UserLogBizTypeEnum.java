package com.github.platform.core.sys.domain.constant;

import lombok.Getter;

/**
 * 用户日志业务类型
 * @author: yxkong
 * @date: 2023/1/5 10:53 AM
 * @version: 1.0
 */
@Getter
public enum UserLogBizTypeEnum {
    REGISTER(0,"注册"),
    LOGIN(1,"登录"),
    MODIFY_PWD(2,"修改密码"),
    ADD_USER(3,"添加用户"),
    BIND(4,"绑定账户"),
    THIRD(5,"三方登录"),
    PROFILE(6,"用户修改")
    ;

    private final Integer type;
    private final String desc;

    UserLogBizTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    public static UserLogBizTypeEnum getDefault(){
        return UserLogBizTypeEnum.REGISTER;
    }
    public static UserLogBizTypeEnum of(Integer type){
        for (UserLogBizTypeEnum typeEnum:UserLogBizTypeEnum.values()){
            if (typeEnum.getType().intValue() == type.intValue()){
                return typeEnum;
            }
        }
        return null;
    }
}
