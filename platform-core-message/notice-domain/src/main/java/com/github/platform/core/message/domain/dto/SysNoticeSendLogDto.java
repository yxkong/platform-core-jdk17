package com.github.platform.core.message.domain.dto;
import com.github.platform.core.message.domain.common.entity.SysNoticeSendLogBase;
import lombok.*;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * 通知发送记录传输实体
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper = true)
@Schema(description = "通知发送记录传输实体")
public class SysNoticeSendLogDto extends SysNoticeSendLogBase{
}