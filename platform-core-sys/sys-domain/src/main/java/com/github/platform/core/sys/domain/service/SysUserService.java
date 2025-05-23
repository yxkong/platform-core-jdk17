package com.github.platform.core.sys.domain.service;

import com.github.platform.core.auth.constant.RoleConstant;
import com.github.platform.core.common.domain.DomainBaseService;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.standard.annotation.DomainService;
import com.github.platform.core.standard.constant.StatusEnum;
import com.github.platform.core.standard.util.LocalDateTimeUtil;
import com.github.platform.core.sys.domain.constant.DeptConstant;
import com.github.platform.core.sys.domain.constant.SysResultEnum;
import com.github.platform.core.sys.domain.constant.UserLogBizTypeEnum;
import com.github.platform.core.sys.domain.context.AccountContext;
import com.github.platform.core.sys.domain.context.SysThirdUserContext;
import com.github.platform.core.sys.domain.dto.SysThirdUserDto;
import com.github.platform.core.sys.domain.dto.SysUserDto;
import com.github.platform.core.sys.domain.gateway.ISysUserGateway;
import com.github.platform.core.sys.domain.gateway.IThirdUserGateway;
import com.github.platform.core.sys.domain.model.user.ThirdUserEntity;
import com.github.platform.core.sys.domain.model.user.UserEntity;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 用户领域服务
 *
 * @author: yxkong
 * @date: 2023/1/4 3:03 PM
 * @version: 1.0
 */
@DomainService
@Builder
@Slf4j
public class SysUserService extends DomainBaseService {

    private ISysUserGateway userGateway;
    private IThirdUserGateway thirdUserGateway;

    public SysUserService(ISysUserGateway userGateway) {
        this.userGateway = userGateway;
    }
    public SysUserService(ISysUserGateway userGateway,IThirdUserGateway thirdUserGateway) {
        this.userGateway = userGateway;
        this.thirdUserGateway = thirdUserGateway;
    }


    /**
     * 后台新增用户
     *
     * @param context
     * @return
     */
    public UserEntity addUser(AccountContext context) {
        UserEntity userEntity = userGateway.findByLoginName(context.getLoginName(),context.getTenantId());
        if (Objects.nonNull(userEntity)) {
            throw exception(SysResultEnum.REGISTERED);
        }
        userEntity = userGateway.findByMobile(context.getMobile(),context.getTenantId());
        if (Objects.nonNull(userEntity)) {
            throw exception(SysResultEnum.MOBILE_REGISTERED);
        }
        return userGateway.addUser(context);
    }

    /**
     * 静默添加用户，如果用户已经存在，直接返回对应的用户信息
     * @param thirdUserEntity
     * @return
     */
    public UserEntity quietAddUserWithLoginName(ThirdUserEntity thirdUserEntity){
        UserEntity userEntity = userGateway.findByLoginName(thirdUserEntity.getLoginName(),thirdUserEntity.getTenantId());
        if (Objects.isNull(userEntity)){
            userEntity =  userGateway.addUser(thirdToRegisterContext(thirdUserEntity));
        }
        if (Objects.nonNull(userEntity)){
            return userEntity;
        }
        SysThirdUserDto thirdUser = findOrSaveThirdUser(thirdUserEntity, userEntity.getId());
        userEntity.setDefaultRoles(thirdUser.isDisabled()?RoleConstant.thirdRole:null);
        return userEntity;
    }

    /**
     * 静默添加以手机号为主的账户,不做账户合并
     * @param thirdUserEntity
     * @return
     */
    public UserEntity quietSysUerWithMobile(ThirdUserEntity thirdUserEntity){
        UserEntity userEntity = userGateway.findByMobile(thirdUserEntity.getMobile(),thirdUserEntity.getTenantId());
        if (Objects.isNull(userEntity)){
            userEntity = userGateway.addUser(thirdToRegisterContext(thirdUserEntity));
        }
        SysThirdUserDto thirdUser = findOrSaveThirdUser(thirdUserEntity, userEntity.getId());
        userEntity.setDefaultRoles(thirdUser.isDisabled()?RoleConstant.thirdRole:null);
        return userEntity;
    }
    private SysThirdUserDto findOrSaveThirdUser(ThirdUserEntity thirdUserEntity,Long sysUserId){
        //查找三方
        SysThirdUserDto thirdUser =  thirdUserGateway.findByChannel(thirdUserEntity.getOpenId(),thirdUserEntity.getChannel(),thirdUserEntity.getTenantId());
        if (Objects.isNull(thirdUser)){
            /**初始化thirduser*/
            thirdUser = thirdUserGateway.insert(thirdUserEntity, sysUserId);
        } else if (Objects.isNull(thirdUser.getUserId())){
            SysThirdUserContext context = SysThirdUserContext.builder().id(thirdUser.getId()).mobile(thirdUserEntity.getMobile()).userId(sysUserId).build();
            thirdUserGateway.update(context);
        }
        return thirdUser;
    }
    private AccountContext thirdToRegisterContext(ThirdUserEntity thirdUser){
        return AccountContext.builder()
                .channel(thirdUser.getChannel())
                .loginName(thirdUser.getLoginName())
                .userName(thirdUser.getUserName())
                .email(thirdUser.getEmail())
                .mobile(thirdUser.getMobile())
                .deptId(DeptConstant.DEFAULT_ID)
                .roleKeys(RoleConstant.thirdRole)
                .logBizTypeEnum(UserLogBizTypeEnum.THIRD)
                .tenantId(thirdUser.getTenantId())
                .status(StatusEnum.OFF.getStatus())
                .createBy(thirdUser.getLoginName())
                .createTime(LocalDateTimeUtil.dateTime())
                .build();
    }

    /**
     * 修改用户信息
     *
     * @param context
     * @return
     */
    public void editUser(AccountContext context) {
        //获取用户信息
        SysUserDto sysUserDto = userGateway.findByUserId(context.getId());
        if (Objects.isNull(sysUserDto)) {
            throw exception(SysResultEnum.NOT_FOUND_USER);
        }
        // 登录名不相同的时候，判断新修改的是否已经存在
        if (!sysUserDto.getLoginName().equals(context.getLoginName())){
            UserEntity userEntity = userGateway.findByLoginName(context.getLoginName(),context.getTenantId());
            if (Objects.nonNull(userEntity)) {
                throw exception(SysResultEnum.EXIST_USER);
            }
        }
        // 如果手机号不相同的时候，判断新修改的是否已经存在
        if (!sysUserDto.getMobile().equals(context.getMobile())){
            UserEntity userEntityByMobile = userGateway.findByMobile(context.getMobile(),context.getTenantId());
            if (Objects.nonNull(userEntityByMobile)) {
                throw exception(SysResultEnum.MOBILE_REGISTERED);
            }
        }

        if (StringUtils.isNotEmpty(context.getSecretKey()) && !context.getSecretKey().equals(sysUserDto.getSecretKey())){
            //修改的时候，需要保持唯一性
            UserEntity secretKeyUser = userGateway.findBySecretKey(context.getSecretKey());
            // 查到以后 如果和修改的用户名不一样，则终止
            if (Objects.nonNull(secretKeyUser) && !secretKeyUser.getLoginName().equals(context.getLoginName())){
                throw exception(SysResultEnum.SECRET_KEY_NOT_UNIQ);
            }
        }
        context.setId(sysUserDto.getId());
        userGateway.deleteCache(sysUserDto.getLoginName(),sysUserDto.getMobile(),sysUserDto.getSecretKey(),sysUserDto.getTenantId());
        userGateway.editUser(context);
    }


}
