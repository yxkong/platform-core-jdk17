package com.github.platform.core.file.domain.constant;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 文件上传方式，如果添加自己扩展
 * @author yxkong
 */
@Getter
public enum FileUploadEnum {
    /**
     * 本地存储
     */
    DISK,
    /**
     * 天翼云存储
     */
    CT_YUN,
    /**
     * 阿里云存储
     */
    ALI_YUN;

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
}
