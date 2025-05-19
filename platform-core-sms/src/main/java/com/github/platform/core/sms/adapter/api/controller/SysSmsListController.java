package com.github.platform.core.sms.adapter.api.controller;

import com.github.platform.core.log.domain.constants.LogOptTypeEnum;
import com.github.platform.core.log.infra.annotation.OptLog;
import com.github.platform.core.sms.adapter.api.command.SysSmsListCmd;
import com.github.platform.core.sms.adapter.api.command.SysSmsListQuery;
import com.github.platform.core.sms.adapter.api.convert.SysSmsListAdapterConvert;
import com.github.platform.core.sms.application.executor.ISysSmsListExecutor;
import com.github.platform.core.sms.domain.context.SysSmsListContext;
import com.github.platform.core.sms.domain.context.SysSmsListQueryContext;
import com.github.platform.core.sms.domain.dto.SysSmsListDto;
import com.github.platform.core.standard.entity.IdReq;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.web.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
/**
* 短信单服务
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-14 17:39:30.643
* @version 1.0
*/
@RestController
@Tag(name = "smsListBase",description = "短信名单管理")
@RequestMapping("sys/sms/list")
@Slf4j
public class SysSmsListController extends BaseController{
    @Resource
    private ISysSmsListExecutor executor;
    @Resource
    private SysSmsListAdapterConvert convert;

    /**
    * 查询短信名单列表
    * @param query 查询实体
    * @return 分页结果
    */
    @Operation(summary = "查询短信名单列表",tags = {"sysSmsList"})
    @PostMapping("/query")
    public ResultBean<PageBean<SysSmsListDto>> query(@RequestBody SysSmsListQuery query){
        SysSmsListQueryContext context = convert.toQuery(query);
        PageBean<SysSmsListDto> pageBean = executor.query(context);
        return buildSucResp(pageBean);
    }

    /**
    * 新增短信名单
    * @param cmd 新增实体
    * @return 操作结果
    */
    @OptLog(module="sysSmsList",title="新增短信名单",optType = LogOptTypeEnum.ADD)
    @Operation(summary = "新增短信名单",tags = {"sysSmsList"})
    @PostMapping("/add")
    public ResultBean add(@Validated @RequestBody SysSmsListCmd cmd) {
        SysSmsListContext context = convert.toContext(cmd);
        executor.insert(context);
        return buildSucResp();
    }

    /**
    * 根据id查询短信名单明细
    * @param id 主键id
    * @return 单条记录
    */
    @Operation(summary = "根据id查询短信名单明细",tags = {"sysSmsList"})
    @PostMapping("/detail")
    public ResultBean<SysSmsListDto> detail(@Validated @RequestBody IdReq id) {
        SysSmsListDto dto = executor.findById(id.getId());
        return buildSucResp(dto);
    }

    /**
     * 根据id删除短信名单记录
     * @param id 主键id
     * @return 操作结果
     */
    @OptLog(module="sysSmsList",title="根据id删除短信名单记录",optType = LogOptTypeEnum.DELETE)
    @Operation(summary = "根据id删除短信名单记录",tags = {"sysSmsList"})
    @PostMapping("/delete")
    public ResultBean<?> delete(@Validated @RequestBody IdReq id) {
        executor.delete(id.getId());
        return buildSucResp();
    }

    /**
     * 修改短信名单
     * @param cmd 修改实体
     * @return 操作结果
     */
    @OptLog(module="sysSmsList",title="修改短信名单",optType = LogOptTypeEnum.MODIFY)
    @Operation(summary = "修改短信名单",tags = {"sysSmsList"})
    @PostMapping("/modify")
    public ResultBean<?> modify(@Validated @RequestBody SysSmsListCmd cmd) {
        SysSmsListContext context = convert.toContext(cmd);
        executor.update(context);
        return buildSucResp();
    }
}