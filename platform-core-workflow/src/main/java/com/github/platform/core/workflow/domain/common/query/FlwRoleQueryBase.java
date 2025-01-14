package com.github.platform.core.workflow.domain.common.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程角色查询实体
 *
 * @author: yxkong
 * @date: 2023/10/30 13:37
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Schema(description = "流程角色查询")
public class FlwRoleQueryBase {
    /**
     *流程类型
     */
    @Schema(description = "流程类型")
    @NotNull(message = "流程类型不能为空")
    private String processType;
    /**
     *
     */
    @Schema(description = "角色名称")
    private String name;
    @Schema(description = "租户id")
    private Integer tenantId;
}
