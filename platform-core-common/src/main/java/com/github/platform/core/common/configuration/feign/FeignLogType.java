package com.github.platform.core.common.configuration.feign;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yxkong
 * @date 2019-07-22 13:57
 */
@Data
public class FeignLogType {

    Set<String> excludeHeaders;
    /**是否记录日志*/
    private boolean recordFlag;
    private String keyWord;
    /**是否记录请求头*/
    private boolean headersFlag;
    /**是否记录请求体*/
    private boolean bodyFlag;

    FeignLogType(FeignLog.FeignLogType feignLogType, String keyWord) {
        this.recordFlag = feignLogType.value();
        this.keyWord = keyWord;
        this.headersFlag = feignLogType.headers();
        this.bodyFlag = feignLogType.body();
        this.excludeHeaders = Arrays.stream(feignLogType.excludeHeaders()).map(StringUtils::lowerCase).collect(Collectors.toSet());
    }
}
