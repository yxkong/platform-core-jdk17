package com.github.platform.core.sms.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 短信路由类型
 * @Author: yxkong
 * @Date: 2025/5/12
 * @version: 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SmsRouteEnum {
    RANDOM("random","随机"),
    WEIGHT("weight","权重"),
    ROUND_ROBIN("roundRobin","轮训"),
    SPECIFY("specify","指定"),
    ;


    private String type;
    private String desc;

    public static boolean isRandom(String type){
        return Objects.equals(RANDOM.getType(),type);
    }
    public static boolean isWeight(String type){
        return Objects.equals(WEIGHT.getType(),type);
    }
    public static boolean isRoundRobin(String type){
        return Objects.equals(ROUND_ROBIN.getType(),type);
    }
    public static boolean isSpecify(String type){
        return Objects.equals(SPECIFY.getType(),type);
    }
}
