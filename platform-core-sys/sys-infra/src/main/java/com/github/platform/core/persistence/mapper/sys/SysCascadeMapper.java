package com.github.platform.core.persistence.mapper.sys;
import com.github.platform.core.sys.domain.common.entity.SysCascadeBase;
import java.util.List;

import com.github.platform.core.sys.domain.dto.SysCascadeDto;
import org.apache.ibatis.annotations.Param;
/**
 * 级联表 mapper
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2024-12-01 20:57:44.667
 * @version 1.0
 */
public interface SysCascadeMapper  {

    /**
    * 插入实体
    * @param record 实体
    * @return 插入结果，1成功，0失败
    */
    int insert(SysCascadeBase record);
    /**
    * 通过主键id 更新实体
    * @param record 实体
    * @return 1成功  其它失败
    */
    int updateById(SysCascadeBase record);
    /**
    * 通过主键id 获取实体对象
    * @param id 主键
    * @return 返回结果
    */
    SysCascadeBase findById(Long id);

    /**
     * 根据编码查询
     * @param code
     * @param tenantId
     * @return
     */
    SysCascadeBase findByCode(@Param("code")String code,@Param("tenantId") Integer tenantId);

    /**
    * 通过主键ids 获取多个实体对象(最多200条)
    * @param ids 主键id
    * @return 返回列表
    */
    List<SysCascadeBase> findByIds(@Param("ids") Long[] ids);
    /**
    * 通过实体查询
    * @param record 参数实体
    * @return List<SysCascadeBase>
    */
    List<SysCascadeBase> findListBy(SysCascadeBase record);
    /**
    * 通过实体查询总数
    * @param record 参数实体
    * @return 总数
    */
    long findListByCount(SysCascadeBase record);
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