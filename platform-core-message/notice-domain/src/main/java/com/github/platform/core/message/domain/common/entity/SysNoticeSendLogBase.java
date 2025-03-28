package com.github.platform.core.message.domain.common.entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.github.platform.core.common.entity.BaseAdminEntity;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 通知发送记录模型实体
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class SysNoticeSendLogBase extends BaseAdminEntity   {
    @Schema(description = "消息事件id")
    protected Long eventId;
    @Schema(description = "发送类型")
    @NotEmpty(message="发送类型不能为空")
    protected String sendType;
    @Schema(description = "标题")
    @NotEmpty(message="标题不能为空")
    protected String title;
    @Schema(description = "接收人")
    @NotEmpty(message="接收人不能为空")
    protected String recipient;
    @Schema(description = "抄送人")
    @NotEmpty(message="抄送人不能为空")
    protected String carbonCopy;
    @Schema(description = "发送内容")
    @NotEmpty(message="发送内容不能为空")
    protected String content;
}
