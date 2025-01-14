package com.github.platform.core.sys.adapter.api.command.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;

/**
 * 滑块发送验证码
 * @Author: yxkong
 * @Date: 2021/11/15 10:08 AM
 * @version: 1.0
 */
@Schema(name ="滑块发送验证码")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class SmsBySlidingBlockCmd extends SlidingBlockBase{
    @NotNull(message = "短信类型不能为空！")
    @Range(min = 1, max = 3, message = "range在[1,3]之间")
    @Schema(description = "短信类型，1注册、2登录、3重置", required = true)
    private Integer smsType;
}
