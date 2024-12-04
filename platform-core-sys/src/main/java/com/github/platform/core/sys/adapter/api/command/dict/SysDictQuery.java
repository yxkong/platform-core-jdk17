package com.github.platform.core.sys.adapter.api.command.dict;

import com.github.platform.core.sys.domain.common.query.SysDictQueryBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
/**
* 字典数据查询
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-15 10:55:05.400
* @version 1.0
*/
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@Schema(description = "字典数据查询")
public class SysDictQuery extends SysDictQueryBase {
}