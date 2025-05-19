package com.github.platform.core.sms.adapter.api.controller;

import com.github.platform.core.log.domain.constants.LogOptTypeEnum;
import com.github.platform.core.log.infra.annotation.OptLog;
import com.github.platform.core.sms.adapter.api.command.SendSmsCmd;
import com.github.platform.core.sms.application.executor.ISmsExecutor;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.web.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信发送
 * @Author: yxkong
 * @Date: 2025/5/12
 * @version: 1.0
 */
@RestController
@Tag(name = "sendSms",description = "短信发送")
@RequestMapping("/sys/sms")
@Slf4j
public class SysSmsController extends BaseController {
    @Resource
    private ISmsExecutor smsExecutor;
    /**
     * 新增服务商
     * @param cmd 新增实体
     * @return 操作结果
     */
    @OptLog(module="sendSms",title="短信发送",optType = LogOptTypeEnum.TRIGGER)
    @Operation(summary = "短信发送",tags = {"sendSms"})
    @PostMapping("/send")
    public ResultBean send(@Validated @RequestBody SendSmsCmd cmd) {
        return smsExecutor.sendSms(cmd);
    }

}
