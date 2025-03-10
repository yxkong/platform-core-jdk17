package com.github.platform.core.message.adapter.api.command;
import com.github.platform.core.message.domain.common.entity.SysNoticeSendLogBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
/**
 * 通知发送记录增加或修改
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
@Schema(description = "通知发送记录增加或修改")
public class SysNoticeSendLogCmd extends SysNoticeSendLogBase{
}
