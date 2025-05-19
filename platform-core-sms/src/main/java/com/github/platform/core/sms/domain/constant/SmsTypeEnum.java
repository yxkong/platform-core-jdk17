package com.github.platform.core.sms.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 短信类型
 * @author: yxkong
 * @date: 2023/3/1 3:17 PM
 * @version: 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SmsTypeEnum {
    VERIFY("verify","验证码"),
    NOTICE("notice","通知"),
    MARKET("market","营销"),
    LOAN("loan","催收");
    private String type;
    private String desc;
}
