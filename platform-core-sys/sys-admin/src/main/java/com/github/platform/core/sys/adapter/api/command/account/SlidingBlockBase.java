package com.github.platform.core.sys.adapter.api.command.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

/**
 * 滑块信息
 *
 * @Author: yxkong
 * @Date: 2021/11/15 10:26 AM
 * @version: 1.0
 */
@Data
public class SlidingBlockBase  {

    @NotEmpty(message = "滑块id不能为空")
    @Schema(description = "滑块id",requiredMode= Schema.RequiredMode.REQUIRED)
    String slidingBlockId;
    /**
     * 滑块来源（不同的厂商，yidun,shumei）
     */
    @Schema(description = "滑块厂商来源",requiredMode= Schema.RequiredMode.REQUIRED)
    String slidingBlockSupplier;
}
