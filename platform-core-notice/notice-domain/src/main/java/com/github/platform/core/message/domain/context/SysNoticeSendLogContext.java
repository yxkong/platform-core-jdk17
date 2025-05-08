package com.github.platform.core.message.domain.context;
import com.github.platform.core.message.domain.common.entity.SysNoticeSendLogBase;
import lombok.experimental.SuperBuilder;
import lombok.*;
/**
 * 通知发送记录增加或修改上下文
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SysNoticeSendLogContext extends SysNoticeSendLogBase {
}
