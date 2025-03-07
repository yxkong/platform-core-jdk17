package com.github.platform.core.sys.domain.context;

import com.github.platform.core.sys.domain.common.entity.SysRoleMenuBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
/**
* 角色和菜单关联表增加或修改上下文
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-15 10:55:06.371
* @version 1.0
*/
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class SysRoleMenuContext extends SysRoleMenuBase {
}
