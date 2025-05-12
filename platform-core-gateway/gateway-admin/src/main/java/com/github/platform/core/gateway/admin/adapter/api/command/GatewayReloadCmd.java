package com.github.platform.core.gateway.admin.adapter.api.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 网关重载
 * @Author: yxkong
 * @Date: 2025/5/12
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@Schema(description = "网关路由重载")
public class GatewayReloadCmd {
    private String gateway;
}
