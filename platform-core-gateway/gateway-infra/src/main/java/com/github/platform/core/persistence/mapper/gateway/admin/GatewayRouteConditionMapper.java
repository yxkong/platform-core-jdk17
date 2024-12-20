package com.github.platform.core.persistence.mapper.gateway.admin;

import com.github.platform.core.gateway.admin.domain.common.entity.GatewayRouteConditionBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 网关路由条件 mappper
 * @website <a href="https://www.5ycode.com/">5ycode</a>
 * @author yxkong
 * @date 2024-08-13 14:58:55.453
 * @version 1.0
 */
public interface GatewayRouteConditionMapper {

    /**
    * 插入实体
    * @param record 实体
    * @return 插入结果，1成功，0失败
    */
    int insert(GatewayRouteConditionBase record);
    /**
    * 通过主键id 更新实体
    * @param record 实体
    * @return 1成功  其它失败
    */
    int updateById(GatewayRouteConditionBase record);
    /**
    * 通过主键id 获取实体对象
    * @param id 主键
    * @return 返回结果
    */
    GatewayRouteConditionBase findById(Long id);
    /**
    * 通过主键ids 获取多个实体对象(最多200条)
    * @param ids 主键id
    * @return 返回列表
    */
    List<GatewayRouteConditionBase> findByIds(@Param("ids") Long[] ids);
    /**
    * 通过实体查询
    * @param record 参数实体
    * @return List<GatewayRouteConditionsBase>
    */
    List<GatewayRouteConditionBase> findListBy(GatewayRouteConditionBase record);
    /**
    * 通过实体查询总数
    * @param record 参数实体
    * @return 总数
    */
    long findListByCount(GatewayRouteConditionBase record);
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

    /**
     * 批量插入
     * @param list
     */
    void insertList(List<GatewayRouteConditionBase> list);
    /**
     * 批量更新
     * @param list
     */
    void updateList(List<GatewayRouteConditionBase> list);
}