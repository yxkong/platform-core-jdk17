package com.github.platform.core.schedule.domain.context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.platform.core.schedule.domain.common.entity.SysJobBase;
import lombok.experimental.SuperBuilder;
import lombok.*;

import java.util.Objects;

/**
* 任务管理增加或修改上下文
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-09-05 11:37:59.627
* @version 1.0
*/
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class SysJobContext extends SysJobBase {

}
