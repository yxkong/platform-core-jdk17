package com.github.platform.core.sys.adapter.api.controller;

import com.github.platform.core.auth.annotation.RequiredLogin;
import com.github.platform.core.common.entity.StrIdReq;
import com.github.platform.core.log.domain.constants.LogOptTypeEnum;
import com.github.platform.core.log.infra.annotation.OptLog;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.standard.entity.vue.OptionsDto;
import com.github.platform.core.standard.validate.Modify;
import com.github.platform.core.sys.adapter.api.command.role.SysRoleCmd;
import com.github.platform.core.sys.adapter.api.command.role.SysRoleQuery;
import com.github.platform.core.sys.adapter.api.convert.SysRoleAdapterConvert;
import com.github.platform.core.sys.application.executor.IRoleExecutor;
import com.github.platform.core.sys.domain.context.SysRoleContext;
import com.github.platform.core.sys.domain.context.SysRoleQueryContext;
import com.github.platform.core.sys.domain.dto.SysRoleDto;
import com.github.platform.core.web.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.util.List;

/**
 * 角色相关接口
 *
 * @author wangxiaozhou
 * @create 2023/2/8 下午3:27
 * @desc SysRoleController
 */
@RestController
@Tag(name = "role",description = "角色相关接口")
@RequestMapping(value = "/sys/role", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class SysRoleController extends BaseController {

    @Resource
    private IRoleExecutor roleExecutor;
    @Resource
    private SysRoleAdapterConvert convert;

    /**
     * 角色列表查询
     *
     * @param query
     * @return
     */
    @RequiredLogin
    @OptLog(module="role",title="角色列表",persistent = false)
    @Operation(summary = "角色列表",tags = {"role"})
    @PostMapping("/query")
    public ResultBean<PageBean<SysRoleDto>> query(@RequestBody SysRoleQuery query) {
        SysRoleQueryContext roleQueryContext = convert.toQuery(query);
        PageBean<SysRoleDto> pageBean = roleExecutor.query(roleQueryContext);
        return buildSucResp(pageBean);
    }

    /**
     * 新增角色
     *
     * @param cmd
     * @return
     */
    @OptLog(module="role",title="新增角色",optType = LogOptTypeEnum.ADD)
    @Operation(summary = "新增角色",tags = {"role"})
    @PostMapping("/add")
    public ResultBean add(@Validated @RequestBody SysRoleCmd cmd) {
        SysRoleContext context = convert.toContext(cmd);
        roleExecutor.addRole(context);
        return buildSucResp();
    }

    /**
     * 修改角色
     *
     * @param cmd
     * @return
     */
    @OptLog(module="role",title="修改角色",optType = LogOptTypeEnum.MODIFY)
    @Operation(summary = "修改角色",tags = {"role"})
    @PostMapping("/modify")
    public ResultBean modify(@Validated({Modify.class, Default.class}) @RequestBody SysRoleCmd cmd) {
        SysRoleContext context = convert.toContext(cmd);
        roleExecutor.update(context);
        return buildSucResp();
    }

    /**
     * 删除角色
     *
     * @param idReq
     * @return
     */
    @OptLog(module="role",title="删除角色",optType = LogOptTypeEnum.DELETE)
    @PostMapping("/delete")
    @Operation(summary = "删除角色",tags = {"role"})
    public ResultBean<Void> delete(@Validated @RequestBody StrIdReq idReq) {
        // 如果非管理员，只能删除本租户的角色
        roleExecutor.deleteById(idReq.getId());
        return buildSucResp();

    }
    /**
     * 角色下拉框查询列表查询
     *
     * @return
     */
    @RequiredLogin
    @OptLog(module="role",title="角色下拉框",persistent = false)
    @Operation(summary = "角色下拉框",tags = {"role"})
    @PostMapping("/select")
    public ResultBean<List<OptionsDto>> select() {
        List<OptionsDto> list = roleExecutor.select(new SysRoleQueryContext());
        return buildSucResp(list);
    }

    /**
     * 查询j角色详情
     * @param idReq
     * @return
     */
    @OptLog(module="role",title="查询角色详情",persistent = false)
    @Operation(summary = "查询角色详情",tags = {"role"})
    @PostMapping("detail")
    public ResultBean<SysRoleDto> detail(@RequestBody @Validated StrIdReq idReq) {
        SysRoleDto roleDto = roleExecutor.findById(idReq.getId());
        return buildSucResp(roleDto);
    }
}
