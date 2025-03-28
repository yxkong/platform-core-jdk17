package com.github.platform.core.web.configuration.properties;

import com.github.platform.core.common.constant.PropertyConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * swagger属性配置
 *
 * @author: yxkong
 * @date: 2021/6/7 4:06 下午
 * @version: 1.0
 */
@Component
@ConfigurationProperties(PropertyConstant.DATA_SWAGGER)
@Data
public class SwaggerProperties {
    private boolean show;
    /**
     * swagger标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 接口版本
     */
    private String version;
    /**
     * 联系姓名
     */
    private String contactName;
    /**
     * 联系url
     */
    private String contactUrl;
    /**
     * 联系邮箱
     */
    private String contactEmail;
    /**
     * 支持url
     */
    private String serviceUrl;
    /**
     * 扫描包
     */
    private String scanPackage;


}