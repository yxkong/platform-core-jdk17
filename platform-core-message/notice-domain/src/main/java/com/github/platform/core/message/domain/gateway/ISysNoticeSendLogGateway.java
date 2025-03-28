package com.github.platform.core.message.domain.gateway;

import com.github.platform.core.message.domain.context.SysNoticeSendLogContext;
import com.github.platform.core.message.domain.context.SysNoticeSendLogQueryContext;
import com.github.platform.core.message.domain.dto.SysNoticeSendLogDto;
import com.github.platform.core.standard.entity.dto.PageBean;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 通知发送记录网关层，隔离模型和实现
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
public interface ISysNoticeSendLogGateway {
    /**
    * 查询通知发送记录列表
    * @param context 查询上下文
    * @return 分页结果
    */
    PageBean<SysNoticeSendLogDto> query(SysNoticeSendLogQueryContext context);

    /**
     * 根据logId查询
     * @param eventId
     * @return
     */
    List<SysNoticeSendLogDto> findByEventId(Long eventId);
    /**
    * 新增通知发送记录
    * @param context 新增上下文
    * @return 返回结果
    */
    SysNoticeSendLogDto insert(SysNoticeSendLogContext context);

    long findByCount(Long eventId);

    /**
    * 根据id查询通知发送记录明细
    * @param id 主键
    * @return 结果
    */
    SysNoticeSendLogDto findById(Long id);
    /**
    * 修改通知发送记录
    * @param context 修改上下文
    * @return 更新结果
    */
    Pair<Boolean, SysNoticeSendLogDto> update(SysNoticeSendLogContext context);
    /**
    * 根据id删除通知发送记录记录
    * @param context 删除上下文（由id改为上下文了，是为了兼容cache的多处理）
    * @return 删除结果
    */
    int delete(SysNoticeSendLogContext context);


}
