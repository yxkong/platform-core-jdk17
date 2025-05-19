package com.github.platform.core.sms.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 供应商枚举
 * @Author: yxkong
 * @Date: 2025/5/12
 * @version: 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SmsProviderEnum {
    ALI_YUN("aliyun","阿里云"),
    CT_YUN("ctyun","天翼云"),
    ;

    private String provider;
    private String desc;

    public static boolean isAliYun(String provider){
        return Objects.equals(ALI_YUN.getProvider(),provider);
    }
    public static boolean isCtYun(String provider){
        return Objects.equals(CT_YUN.getProvider(),provider);
    }
}
