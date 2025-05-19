package com.github.platform.core.sms.domain.gateway;

import com.github.platform.core.sms.domain.context.SysSmsListContext;
import com.github.platform.core.sms.domain.context.SysSmsListQueryContext;
import com.github.platform.core.sms.domain.dto.SysSmsListDto;
import com.github.platform.core.standard.entity.dto.PageBean;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
* 短信白名单网关层，隔离模型和实现
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-14 17:39:30.643
* @version 1.0
*/
public interface ISysSmsListGateway {
    /**
    * 查询短信白名单列表
    * @param context 查询上下文
    * @return 分页结果
    */
    PageBean<SysSmsListDto> query(SysSmsListQueryContext context);

    /**
     * 查询名单
     */
    List<SysSmsListDto> findByList(SysSmsListQueryContext context);

    long findListByCount(SysSmsListQueryContext context);

    /**
    * 新增短信白名单
    * @param context 新增上下文
    * @return 返回结果
    */
    SysSmsListDto insert(SysSmsListContext context);
    /**
    * 根据id查询短信白名单明细
    * @param id 主键
    * @return 结果
    */
    SysSmsListDto findById(Long id);
    /**
    * 修改短信白名单
    * @param context 修改上下文
    * @return 更新结果
    */
    Pair<Boolean, SysSmsListDto> update(SysSmsListContext context);
    /**
    * 根据id删除短信白名单记录
    * @param id 主键id
    * @return 删除结果
    */
    int delete(Long id);

    /**
     * 是否存在手机号
     * @param mobile
     * @return
     */
    boolean existMobile(String mobile);
}
