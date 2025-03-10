package com.github.platform.core.message.application.executor;

import com.github.platform.core.message.domain.context.SysNoticeSendLogContext;
import com.github.platform.core.message.domain.context.SysNoticeSendLogQueryContext;
import com.github.platform.core.message.domain.dto.SysNoticeSendLogDto;
import com.github.platform.core.standard.entity.dto.PageBean;

import java.util.List;

/**
 * 通知发送记录执行器接口
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
public interface ISysNoticeSendLogExecutor {
    /**
    * 查询通知发送记录列表
    * @param context 查询上下文
    * @return 分页结果
    */
    PageBean<SysNoticeSendLogDto> query(SysNoticeSendLogQueryContext context);

    /**
     * 根据logId查询
     * @param logId
     * @return
     */
    List<SysNoticeSendLogDto> findListByLogId(Long logId);
    /**
    * 新增通知发送记录
    * @param context 新增上下文
    */
    String insert(SysNoticeSendLogContext context);
    /**
    * 根据id查询通知发送记录明细
    * @param id 主键
    * @return 单条记录
    */
    SysNoticeSendLogDto findById(Long id);
    /**
    * 修改通知发送记录
    * @param context 更新上下文
    */
    void update(SysNoticeSendLogContext context);
    /**
    * 根据id删除通知发送记录记录
    * @param id 主键
    */
    void delete(Long id);


}