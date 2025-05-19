package com.github.platform.core.persistence.mapper.sms;

import com.github.platform.core.sms.domain.common.entity.SysSmsListBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 短信白名单 mapper
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-14 17:39:30.643
* @version 1.0
*/
public interface SysSmsListMapper {

    /**
    * 插入实体
    * @param record 实体
    * @return 插入结果，1成功，0失败
    */
    int insert(SysSmsListBase record);
    /**
    * 通过主键id 更新实体
    * @param record 实体
    * @return 1成功  其它失败
    */
    int updateById(SysSmsListBase record);
    /**
    * 通过主键id 获取实体对象
    * @param id 主键
    * @return 返回结果
    */
    SysSmsListBase findById(Long id);
    /**
     * 通过手机号查询
     * @param mobile
     * @return
     */
    SysSmsListBase findByMobile(String mobile);
    /**
    * 通过主键ids 获取多个实体对象(最多200条)
    * @param ids 主键id
    * @return 返回列表
    */
    List<SysSmsListBase> findByIds(@Param("ids") Long[] ids);
    /**
    * 通过实体查询
    * @param record 参数实体
    * @return List<SysSmsListBase>
    */
    List<SysSmsListBase> findListBy(SysSmsListBase record);
    /**
    * 通过实体查询总数
    * @param record 参数实体
    * @return 总数
    */
    long findListByCount(SysSmsListBase record);
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