package com.github.platform.core.sms.application.executor;

import com.github.platform.core.sms.domain.context.SysSmsListContext;
import com.github.platform.core.sms.domain.context.SysSmsListQueryContext;
import com.github.platform.core.sms.domain.dto.SysSmsListDto;
import com.github.platform.core.standard.entity.dto.PageBean;

/**
 * 短信白名单执行器
 * @author: yxkong
 * @date: 2023/12/27 12:00
 * @version: 1.0
 */
public interface ISysSmsListExecutor {
    /**
     * 查询短信白名单列表
     *
     * @param context 查询上下文
     * @return 分页结果
     */
    PageBean<SysSmsListDto> query(SysSmsListQueryContext context);

    /**
     * 新增短信白名单
     *
     * @param context 新增上下文
     * @return 操作结果
     */
    void insert(SysSmsListContext context);

    /**
     * 根据id查询短信白名单明细
     *
     * @param id 主键
     * @return 单条记录
     */
    SysSmsListDto findById(Long id);

    /**
     * 修改短信白名单
     *
     * @param context 更新上下文
     * @return 更新结果
     */
    void update(SysSmsListContext context);

    /**
     * 根据id删除短信白名单记录
     *
     * @param id 主键
     * @return 删除结果
     */
    void delete(Long id);
}
