package com.github.platform.core.sys.domain.constant;

import lombok.Getter;

/**
 * @author: yxkong
 * @date: 2023/1/4 2:42 PM
 * @version: 1.0
 */
@Getter
public enum VerifyResultEnum {

    PASS("pass","验证通过！"),
    REFUSE("refuse","验证未通过！"),
    NOT_FOUND("notfound","未找到对应的验证码！")
    ;

    private final String type;
    private final String desc;

    VerifyResultEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
