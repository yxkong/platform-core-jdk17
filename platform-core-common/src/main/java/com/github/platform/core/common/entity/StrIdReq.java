package com.github.platform.core.common.entity;

import com.github.platform.core.common.utils.SignUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;


/**
 * 加密主键id
 * @author: yxkong
 * @version: 1.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StrIdReq {
    @NotNull(message = "主键id不能为空")
    @Schema(description = "加密后的主键id")
    protected String id;

    public Long getId() {
        return SignUtil.getLongId(this.id);
    }
}
