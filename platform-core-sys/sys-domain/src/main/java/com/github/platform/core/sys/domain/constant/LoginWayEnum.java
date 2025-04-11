package com.github.platform.core.sys.domain.constant;

import lombok.Getter;

/**
 * 登录方式枚举
 * @author: yxkong
 * @date: 2022/4/29 3:03 PM
 * @version: 1.0
 */
@Getter
public enum LoginWayEnum {
    NORMAL("normal","普通登录"),
    LDAP("ldap","ldap登录"),
    SMS("sms","短信登录"),
    THIRD_WX("wx","三方微信登录"),
    THIRD_DING_TALK("dingTalk","三方钉钉登录"),
    THIRD_ALIPAY("alipay","三方支付宝登录"),
    THIRD_QQ("qq","三方qq登录"),
    BEARER("bearer","密钥登录"),
    ;
    private final String type;
    private final String desc;

    LoginWayEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static LoginWayEnum getDefault(){
        return LoginWayEnum.NORMAL;
    }
    public static LoginWayEnum of(String type){
        for (LoginWayEnum wayEnum:LoginWayEnum.values()){
            if (wayEnum.getType().equalsIgnoreCase(type)){
                return wayEnum;
            }
        }
        return null;
    }
    public static Boolean isThird(LoginWayEnum loginWay){
        if (loginWay == LoginWayEnum.THIRD_WX || loginWay == LoginWayEnum.THIRD_ALIPAY || loginWay == LoginWayEnum.THIRD_QQ){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * bean的规则是类型+LoginGateway
     * @return
     */
    public String getBeanName(){
        return this.type+"LoginGateway";
    }
}
