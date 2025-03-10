package com.github.platform.core.persistence.mapper.message;
import com.github.platform.core.message.domain.common.entity.SysNoticeSendLogBase;
import java.util.List;
import org.apache.ibatis.annotations.Param;
/**
 * 通知发送记录 mapper
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2025-03-10 13:24:06.322
 * @version 1.0
 */
public interface SysNoticeSendLogMapper  {

    /**
    * 插入实体
    * @param record 实体
    * @return 插入结果，1成功，0失败
    */
    int insert(SysNoticeSendLogBase record);
    /**
    * 通过主键id 更新实体
    * @param record 实体
    * @return 1成功  其它失败
    */
    int updateById(SysNoticeSendLogBase record);
    /**
    * 通过主键id 获取实体对象
    * @param id 主键
    * @return 返回结果
    */
    SysNoticeSendLogBase findById(Long id);
    /**
    * 通过主键ids 获取多个实体对象(最多200条)
    * @param ids 主键id
    * @return 返回列表
    */
    List<SysNoticeSendLogBase> findByIds(@Param("ids") Long[] ids);
    /**
    * 通过实体查询
    * @param record 参数实体
    * @return List<SysNoticeSendLogBase>
    */
    List<SysNoticeSendLogBase> findListBy(SysNoticeSendLogBase record);
    /**
    * 通过实体查询总数
    * @param record 参数实体
    * @return 总数
    */
    long findListByCount(SysNoticeSendLogBase record);
    /**
    * 通过主键id 删除
    * @param id 主键
    * @return 删除结果，1成功，0失败
    */
    int deleteById(Long id);
    /**
    * 批量删除
    * @param ids 主键
    * @return 删除的条数
    */
    int deleteByIds(@Param("ids")Long[] ids);
}