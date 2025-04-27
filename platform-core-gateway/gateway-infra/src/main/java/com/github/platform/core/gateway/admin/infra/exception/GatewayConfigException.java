package com.github.platform.core.gateway.admin.infra.exception;

import com.github.platform.core.standard.exception.BaseResult;
import com.github.platform.core.standard.exception.CommonException;
import lombok.Getter;

/**
 * 网关配置异常
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
@Getter
public class GatewayConfigException extends CommonException {
    public GatewayConfigException(BaseResult baseResult) {
        super(baseResult);
    }

    public GatewayConfigException(String status,String message) {
        super(status, message);
    }
}
