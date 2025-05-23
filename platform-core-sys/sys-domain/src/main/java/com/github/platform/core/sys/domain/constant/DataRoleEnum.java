package com.github.platform.core.sys.domain.constant;


/**
 * 数据角色
 * @author yxkong
 */
public enum DataRoleEnum {
    ALL("all","所有数据"),
    USER("user","人员数据"),
    DEPT("dept","部门数据"),
    ;

    ;
    private final String roleKey;
    private final String desc;


    DataRoleEnum(String roleKey, String desc) {
        this.roleKey = roleKey;
        this.desc = desc;
    }
    public static DataRoleEnum of(String role){
        for (DataRoleEnum value : DataRoleEnum.values()) {
            if(value.roleKey.equals(role)){
                return value;
            }
        }
        return DataRoleEnum.USER;
    }
    public  Boolean equals(String roleKey){
        return this.roleKey.equals(roleKey);
    }

    public String getRoleKey() {
        return roleKey;
    }

    public String getDesc() {
        return desc;
    }
}
