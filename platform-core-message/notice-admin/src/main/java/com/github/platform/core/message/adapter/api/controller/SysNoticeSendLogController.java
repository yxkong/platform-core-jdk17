package com.github.platform.core.message.adapter.api.controller;

import com.github.platform.core.auth.annotation.RequiredLogin;
import com.github.platform.core.common.entity.StrIdReq;
import com.github.platform.core.log.infra.annotation.OptLog;
import com.github.platform.core.message.application.executor.ISysNoticeSendLogExecutor;
import com.github.platform.core.message.domain.dto.SysNoticeSendLogDto;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.web.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知发送记录
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
@RestController
@Tag(name = "sysNoticeSendLog",description = "通知发送记录管理")
@RequestMapping("api/message/notice/send")
@Slf4j
public class SysNoticeSendLogController extends BaseController{
    @Resource
    private ISysNoticeSendLogExecutor sysNoticeSendLogExecutor;

    /**
    * 查询通知发送记录列表
    * @param query 查询实体
    * @return 分页结果
    */
    @RequiredLogin
    @OptLog(module="sysNoticeSendLog",title="查询通知发送记录列表",persistent = false)
    @Operation(summary = "查询通知发送记录列表",tags = {"sysNoticeSendLog"})
    @PostMapping("/findByEventId")
    public ResultBean<PageBean<SysNoticeSendLogDto>> findByEventId(@RequestBody StrIdReq query){
        PageBean<SysNoticeSendLogDto> list = sysNoticeSendLogExecutor.findByEventId(query.getId());
        return buildSucResp(list);
    }

}