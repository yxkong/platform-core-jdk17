package com.github.platform.core.sys.application.executor.impl;

import com.github.platform.core.auth.application.executor.SysExecutor;
import com.github.platform.core.auth.entity.LoginUserInfo;
import com.github.platform.core.auth.util.AuthUtil;
import com.github.platform.core.auth.util.LoginUserInfoUtil;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.common.utils.SignUtil;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.standard.entity.vue.OptionsDto;
import com.github.platform.core.sys.application.constant.SysAppResultEnum;
import com.github.platform.core.sys.application.executor.ISysUserExecutor;
import com.github.platform.core.sys.domain.context.AccountContext;
import com.github.platform.core.sys.domain.context.ResetPwdContext;
import com.github.platform.core.sys.domain.context.SysUserQueryContext;
import com.github.platform.core.sys.domain.dto.SysDeptDto;
import com.github.platform.core.sys.domain.dto.SysRoleDto;
import com.github.platform.core.sys.domain.dto.SysUserDto;
import com.github.platform.core.sys.domain.dto.resp.PwdResult;
import com.github.platform.core.sys.domain.gateway.ISysDeptGateway;
import com.github.platform.core.sys.domain.gateway.ISysRoleGateway;
import com.github.platform.core.sys.domain.gateway.ISysUserGateway;
import com.github.platform.core.sys.domain.model.user.UserEntity;
import com.github.platform.core.sys.domain.service.SysUserService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户账号管理执行器
 *
 * @author yxkong
 * @create 2023/2/10 下午2:16
 * @desc SysUserExecutorImpl
 */
@Service
public class SysUserExecutorImpl extends SysExecutor implements ISysUserExecutor {
    @Resource
    private ISysUserGateway userGateway;
    @Resource
    private ISysDeptGateway deptGateway;
    @Resource
    private ISysRoleGateway roleGateway;

    @Override
    public PageBean<SysUserDto> query(SysUserQueryContext context) {
        context.setTenantId(getTenantId(context));
        PageBean<SysUserDto> pageBean = userGateway.query(context);
        dealData(pageBean);
        return pageBean;
    }
    private  void dealData(PageBean<SysUserDto> data) {
        if (CollectionUtil.isEmpty( data.getData())){
            return;
        }
        data.getData().stream().forEach(
                s -> {
                    s.setMobile(StringUtils.getMobileVague(s.getMobile()));
                    s.setStrId(SignUtil.getStrId(s.getId()));
                    s.setSecretKey(null);
                    SysDeptDto deptDto = deptGateway.findById(s.getDeptId());
                    if (Objects.nonNull(deptDto)){
                        s.setDeptName(deptDto.getDeptName());
                    }
                }
        );
    }

    @Override
    public PageBean<SysUserDto> queryByDept(SysUserQueryContext context) {
        return userGateway.queryByDept(context);
    }

    /**
     * 新增用户
     *
     * @param context
     * @return
     */
    @Override
    public UserEntity insert(AccountContext context) {
        context.setTenantId(getMustTenantId(context));
        SysUserService userService = new SysUserService(userGateway);
        return userService.addUser(context);
    }
    private Integer getMustTenantId(AccountContext context) {
        if (AuthUtil.isSuperAdmin() && Objects.nonNull(context) && Objects.nonNull(context.getTenantId())){
            return context.getTenantId();
        }
        return LoginUserInfoUtil.getTenantId();
    }

    /**
     * 修改用户信息(只有自己修改的时候才会刷新)
     *
     * @param context
     * @return
     */
    @Override
    public void update(AccountContext context) {
        SysUserService userService = new SysUserService(userGateway);
        //修改用户租户，临时
        context.setTenantId(getMustTenantId(context));
        userService.editUser(context);

        userGateway.reloadToken(context.getUserName(),context.getTenantId());
    }

    @Override
    public void updateUserProfile(AccountContext context) {
        SysUserService userService = new SysUserService(userGateway);
        //修改用户信息
        userService.editUser(context);
        LoginUserInfo userInfo = LoginUserInfoUtil.getLoginUserInfo();
        userInfo.setUserName(context.getUserName());
        userInfo.setEmail(context.getEmail());
        userInfo.setMobile(context.getMobile());
        userInfo.setSecretKey(context.getSecretKey());
        //这里需要重写
        userGateway.reloadToken(LoginUserInfoUtil.getToken(),userInfo);
    }

    /**
     * 重置密码
     *
     * @param context
     * @return
     */
    @Override
    public PwdResult resetPwd(ResetPwdContext context) {
        if (!AuthUtil.isSuperAdmin() && !AuthUtil.isTenantAdmin()){
            if (!LoginUserInfoUtil.getLoginName().equals(context.getLoginName())){
                throw exception(ResultStatusEnum.NO_AUTH);
            }
        }
        UserEntity userEntity = userGateway.findByLoginName(context.getLoginName(),context.getTenantId());
        if (Objects.isNull(userEntity)){
            throw exception(SysAppResultEnum.USER_NOT_FOUND);
        }
        if(AuthUtil.isTenantAdmin()){
            //租户管理员只能重置自己租户的密码
            if (LoginUserInfoUtil.getTenantId().equals(userEntity.getTenantId())){
                throw exception(SysAppResultEnum.ADMIN_TENANT_NO_AUTH);
            }
        }
        context.setId(userEntity.getId());
        return userGateway.resetPwd(context);
    }

    @Override
    public void reloadToken(String token, LoginUserInfo loginUserInfo) {
        userGateway.reloadToken(token,loginUserInfo);
    }

    @Override
    public SysUserDto detail(Long id) {
        SysUserDto userResult = userGateway.findByUserId(id);
        List<SysRoleDto> roles = roleGateway.findRoleByUserId(id);
        if (CollectionUtil.isNotEmpty(roles)){
            userResult.setRoleKeys(roles.stream().map(SysRoleDto::getKey).collect(Collectors.toList()));
        }
        return userResult;
    }

    @Override
    public List<OptionsDto> fuzzySearch(String key) {
        List<OptionsDto> rst = new ArrayList<>();
        List<SysUserDto> list =  userGateway.fuzzySearch(key,LoginUserInfoUtil.getTenantId());
        if (CollectionUtil.isEmpty(list)){
            return rst;
        }
        list.forEach(s->{
            rst.add(new OptionsDto(s.getLoginName(),s.getUserName()));
        });
        return rst;
    }

    @Override
    public List<OptionsDto> queryUsers(SysUserQueryContext query) {
        List<OptionsDto> rst = new ArrayList<>();
        List<SysUserDto> users = null;
        query.setTenantId(getTenantId(query));
        if (StringUtils.isNotEmpty(query.getRoleKey())){
            List<String> roles = new ArrayList<>();
            roles.add(query.getRoleKey());
            users = userGateway.findByRoleKeys(roles,query.getTenantId());
        }else if (Objects.nonNull(query.getDeptId())){
            users = userGateway.findListBy(query);
        }
        if (CollectionUtil.isEmpty(users)){
            return rst;
        }
        users.forEach(s->{
            rst.add(new OptionsDto(s.getLoginName(),s.getDeptName(),s.getUserName()));
        });
        return rst;
    }
}
