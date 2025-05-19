package com.github.platform.core.sms.adapter.api.command;

import com.github.platform.core.sms.domain.context.SendSmsContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 发送短信
 *
 * @Author: yxkong
 * @Date: 2025/5/12
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(description = "发送短信")
public class SendSmsCmd extends SendSmsContext {
}
