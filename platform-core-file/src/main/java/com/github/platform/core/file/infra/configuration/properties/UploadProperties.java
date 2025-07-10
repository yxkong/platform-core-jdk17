package com.github.platform.core.file.infra.configuration.properties;

import com.github.platform.core.common.constant.PropertyConstant;
import com.github.platform.core.file.domain.constant.FileUploadEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 文件上传属性
 *
 * @author: yxkong
 * @date: 2022/8/3 11:23 AM
 * @version: 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = PropertyConstant.DATA_FILE)
public class UploadProperties {
    /**
     * 文件上传类型，只支持单一一种，需要配置对应的配置
     */
    private String storage;
    /**
     * 访问路由配置
     */
    private List<String> routers;
    /**
     * 磁盘存储
     */
    private DiskProperties disk = new DiskProperties();
    /**
     * 天翼云存储
     */
    private OssProperties ctyun = new OssProperties();
    /**
     * 阿里云存储
     */
    private OssProperties aliyun = new OssProperties();
    /**
     * ucloud
     */
    private OssProperties uCloud = new OssProperties();

    /**
     * 磁盘上传配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskProperties {
        /**磁盘路径*/
        private String diskPath;
        /**文件访问URL*/
        private String fileUrl;
        /**文件访问根路径*/
        private String fileRoot;
    }
    /**
     * s3配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OssProperties {
        /**云的存储类型*/
        private String storageType ;
        /**云的endpoint*/
        private String endpoint;
        /**云的cname*/
        private String cname;
        /**云的accessId*/
        private String accessId;
        /**云的accessKey*/
        private String accessKey;
        /**云的bucketName*/
        private String bucketName;
        /**云的链接超时时间*/
        @Builder.Default
        private Integer connectionTimeout = 30 * 1000;
        /**云的socketTimeout*/
        @Builder.Default
        private Integer socketTimeout = 30 * 1000;
        @Builder.Default
        private String region = "default";
        /**链接过期时间*/
        private Integer linkExpireMinutes = 60;
        /**缩略图开关*/
        private Boolean thumbSwitch = false;
        /**是否永久链接*/
        private Boolean permanent = false;

    }
}
