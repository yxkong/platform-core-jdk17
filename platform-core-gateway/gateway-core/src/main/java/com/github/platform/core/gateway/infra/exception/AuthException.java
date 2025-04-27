package com.github.platform.core.gateway.infra.exception;

import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.exception.BaseResult;
import com.github.platform.core.standard.exception.CommonException;

/**
 * 鉴权
 * @Author: yxkong
 * @Date: 2025/4/23
 * @version: 1.0
 */
public class AuthException extends CommonException {
    public AuthException(BaseResult baseResult) {
        super(baseResult);
    }

    public AuthException(String status,String message) {
        super(status, message);
    }
    public AuthException(String messages){
        super(ResultStatusEnum.NO_AUTH.getStatus(),messages);
    }
}
