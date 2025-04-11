package com.github.platform.core.workflow.adapter.api.command;

import com.github.platform.core.common.utils.SignUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 流程设计
 * @author: yxkong
 * @date: 2023/9/26 5:45 PM
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name ="流程设计")
public class ProcessDefinitionDesignCmd {
    @Schema(description = "流程id",requiredMode= Schema.RequiredMode.REQUIRED)
    @NotNull(message = "流程编号为空")
    private String id;
    /**
     * 审批流编号
     */
    @Schema(description = "流程编号",requiredMode= Schema.RequiredMode.REQUIRED)
    private String processNo;

    /**
     * 审批流文件
     */
    @Schema(description = "流程文件",requiredMode= Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "流程文件为空")
    private String processFile;

    public Long getId() {
        return SignUtil.getLongId(this.id);
    }
}
