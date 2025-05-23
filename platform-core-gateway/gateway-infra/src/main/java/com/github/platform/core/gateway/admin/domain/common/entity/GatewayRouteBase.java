package com.github.platform.core.gateway.admin.domain.common.entity;

import com.github.platform.core.common.entity.BaseAdminEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotEmpty;

/**
 * 网关路由模型实体
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2024-08-13 14:58:53.823
 * @version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class GatewayRouteBase extends BaseAdminEntity   {
    /** 路由id,对应gateway的route的id */
    @Schema(description = "路由id,对应gateway的route的id")
    protected String routeId;
    /** 目标服务的URI地址 */
    @Schema(description = "目标服务的URI地址")
    @NotEmpty(message="目标服务的URI地址（uri）不能为空")
    protected String uri;
    @Schema(description = "鉴权类型：NONE-无需鉴权，JWT-JWT鉴权，OAUTH2-OAuth2鉴权，CUSTOM-自定义鉴权")
    protected String authType;
    @Schema(description = "鉴权配置(JSON格式)")
    protected String authConfig;
    /** 标签 */
    @Schema(description = "标签")
    protected String tags;
    /** 归属网关 */
    @Schema(description = "归属网关")
    @NotEmpty(message="归属网关（gateway）不能为空")
    protected String gateway;
    /** 模块 */
    @Schema(description = "模块")
    protected String module;
    /** 路由顺序，数值越小优先级越高 */
    @Schema(description = "路由顺序，数值越小优先级越高")
    protected Integer sort;
}
