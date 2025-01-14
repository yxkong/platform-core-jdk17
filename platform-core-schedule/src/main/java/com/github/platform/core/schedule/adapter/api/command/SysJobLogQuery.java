package com.github.platform.core.schedule.adapter.api.command;

import com.github.platform.core.common.utils.SignUtil;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.schedule.domain.common.query.SysJobLogQueryBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotEmpty;
/**
* 任务执行日志查询
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-09-06 18:53:10.711
* @version 1.0
*/
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "任务执行日志查询")
public class SysJobLogQuery extends SysJobLogQueryBase {
    @Getter
    @Schema(description = "任务id")
    @NotEmpty(message = "任务id不能为空！")
    private String strJobId;
    @Override
    public Long getJobId() {
        if (StringUtils.isNotEmpty(this.strJobId)){
            return SignUtil.getLongId(this.strJobId);
        }
        return this.jobId;
    }
}