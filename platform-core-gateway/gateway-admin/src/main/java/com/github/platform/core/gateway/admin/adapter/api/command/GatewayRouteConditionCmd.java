package com.github.platform.core.gateway.admin.adapter.api.command;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.gateway.admin.domain.common.entity.GatewayRouteConditionBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

/**
 * 网关路由条件增加或修改
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2024-08-13 14:58:55.453
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "网关路由条件增加或修改")
public class GatewayRouteConditionCmd extends GatewayRouteConditionBase {
    protected List<String> typeAndName;

    @Override
    public String getType() {
        if (CollectionUtil.isNotEmpty(typeAndName)){
            return typeAndName.get(0);
        }
        return this.type;
    }

    @Override
    public String getName() {
        if (CollectionUtil.isNotEmpty(typeAndName)){
            return typeAndName.get(1);
        }
        return this.name;
    }
}
