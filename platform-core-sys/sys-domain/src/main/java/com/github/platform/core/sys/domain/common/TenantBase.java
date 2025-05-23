package com.github.platform.core.sys.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * 租户操作base（无登录）
 * @author: yxkong
 * @date: 2023/1/9 1:45 PM
 * @version: 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TenantBase implements Serializable {
    @Schema(description ="租户",requiredMode= Schema.RequiredMode.REQUIRED)
    Integer tenantId;

    public Integer getTenantId() {
        if (Objects.isNull(tenantId)){
            return 1001;
        }
        return tenantId;
    }
}
