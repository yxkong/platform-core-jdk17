package com.github.platform.core.file.domain.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 文件上传方式，如果添加自己扩展
 * @author yxkong
 */
@Getter
public enum FileUploadEnum {
    DISK("disk","本地存储"),
    U_CLOUD("ucloud","ucloud"),
    CT_YUN("ctyun","天翼云存储"),
    ALI_YUN("aliyun","阿里云存储");
    private final String type;
    private final String desc;
    FileUploadEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
    public static FileUploadEnum getName(String code){
        if (StringUtils.isBlank(code)){
            return null;
        }
        for (FileUploadEnum item:FileUploadEnum.values()){
            if (item.name().equals(code)){
                return item;
            }
        }
        return null;
    }
    public static boolean isDisk(String type){
        return DISK.getType().equalsIgnoreCase(type);
    }
    public static boolean isUcloud(String type){
        return U_CLOUD.getType().equalsIgnoreCase(type);
    }
    public static boolean isCtYun(String type){
        return CT_YUN.getType().equalsIgnoreCase(type);
    }
    public static boolean isAliYun(String type){
        return ALI_YUN.getType().equalsIgnoreCase(type);
    }

}
