package com.github.platform.core.sys.domain.gateway;

import com.github.platform.core.cache.domain.constant.CacheConstant;
import com.github.platform.core.sys.domain.constant.SysCacheKeyPrefix;
import com.github.platform.core.sys.domain.context.SysDeptContext;
import com.github.platform.core.sys.domain.context.SysDeptQueryContext;
import com.github.platform.core.sys.domain.dto.SysDeptDto;
import com.github.platform.core.sys.domain.dto.resp.TreeSelectDto;

import java.util.List;

/**
 * 部门网关
 * @author: yxkong
 * @date: 2023/2/9 5:32 下午
 * @version: 1.0
 */
public interface ISysDeptGateway {
    /**缓存前缀*/
    String PREFIX =  SysCacheKeyPrefix.DEPT.getPrefix();
    /**缓存前缀加冒号*/
    String PREFIX_COLON = SysCacheKeyPrefix.DEPT.getWithColon();
    /**缓存名称*/
    String CACHE_NAME = CacheConstant.c1h;
    /**
     * 查询部门列表
     * @param context
     * @return
     */
    List<SysDeptDto> queryList(SysDeptQueryContext context);

    /**
     * 新增部门
     * @param deptContext
     * @return
     */

    void insert(SysDeptContext deptContext);

    /**
     * 修改部门
     * @param context
     * @return
     */
    void update(SysDeptContext context);

    /**
     * 删除部门
     * @param id
     * @return
     */
    void delete(Long id);

    /**
     * 获取当前用户部门树信息
     * @return
     */
    List<TreeSelectDto> deptTree();


//    /**
//     * 新建租户部门
//     * @param deptName
//     * @param leader
//     * @param phone
//     * @param tenant
//     * @return
//     */
//    Long insertTenantDept(String deptName,String leader,String phone,Integer tenantId);

    /**
     * 根据部门id查询当前部门及其所有子部门
     * @param deptId
     * @return
     */
    List<SysDeptDto> findAllDept(Long deptId);

    /**
     * 根据部门id查询当前部门信息
     * @param id
     * @return
     */
    SysDeptDto findById(Long id);
}
