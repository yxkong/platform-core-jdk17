package com.github.platform.core.file.infra.configuration;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.github.platform.core.common.annotation.ConditionalOnPropertyInList;
import com.github.platform.core.common.constant.PropertyConstant;
import com.github.platform.core.file.infra.configuration.properties.UploadProperties;
import com.github.platform.core.file.infra.convert.SysUploadFileInfraConvert;
import com.github.platform.core.file.infra.service.IUploadFileService;
import com.github.platform.core.file.infra.service.impl.AliYunFileUploadFileService;
import com.github.platform.core.file.infra.service.impl.DiskUploadFileService;
import com.github.platform.core.file.infra.service.impl.S3CompatibleUploadService;
import com.github.platform.core.persistence.mapper.file.SysUploadFileMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;

/**
 * 文件上传配置
 * @author: yxkong
 * @date: 2023/2/17 10:44 AM
 * @version: 1.0
 */
@Configuration
@Slf4j
public class FileUploadConfiguration {

    @Resource
    private SysUploadFileMapper uploadFileMapper;
    @Resource
    private UploadProperties properties;
    @Resource
    private SysUploadFileInfraConvert convert;

    /**
     * 阿里云实现
     * @return
     */
    @Bean
    @ConditionalOnClass(OSS.class)
    @ConditionalOnPropertyInList(name = PropertyConstant.CON_FILE_ROUTERS, havingValue = "aliyun")
    public IUploadFileService aliyunUploadService(){
        UploadProperties.OssProperties oss = properties.getAliyun();
        if (Objects.isNull(oss) || Objects.isNull(oss.getAccessId())){
            log.error("file.aliyun 参数未配置,请检查相关配置");
            throw new RuntimeException("请配置file.aliyun操作文件的相关的参数");
        }
        OSS ossClient =  new OSSClientBuilder().build(oss.getEndpoint(), oss.getAccessId(), oss.getAccessKey());
        return new AliYunFileUploadFileService(ossClient,uploadFileMapper,properties,oss,convert);
    }

    /**
     * 磁盘实现
     * @return
     */
    @Bean
    @ConditionalOnPropertyInList(name = PropertyConstant.CON_FILE_ROUTERS, havingValue = "disk")
    public IUploadFileService diskUploadService(){
        UploadProperties.DiskProperties disk = properties.getDisk();
        if (Objects.isNull(disk) || Objects.isNull(disk.getDiskPath())){
            log.error("file.disk 相关参数未配置,请检查相关配置");
            throw new RuntimeException("请配置file.disk操作文件的相关的参数");
        }
        return new DiskUploadFileService(uploadFileMapper,properties,convert);
    }
    @Bean
    @ConditionalOnClass(S3Client.class)
    @ConditionalOnPropertyInList(name = PropertyConstant.CON_FILE_ROUTERS, havingValue = "ctyun")
    public IUploadFileService ctyunUploadService(){
        UploadProperties.OssProperties oss = properties.getCtyun();
        if (Objects.isNull(oss) || Objects.isNull(oss.getAccessId())){
            log.error("file.ctyun 参数未配置,请检查相关配置");
            throw new RuntimeException("请配置file.ctyun操作文件的相关的参数");
        }
        return new S3CompatibleUploadService(createS3Client(oss), createS3Presigner(oss), uploadFileMapper, properties,oss, convert);
    }
    @Bean
    @ConditionalOnClass(S3Client.class)
    @ConditionalOnPropertyInList(name = PropertyConstant.CON_FILE_ROUTERS, havingValue = "ucloud")
    public IUploadFileService uCloudUploadService(){
        UploadProperties.OssProperties oss = properties.getUCloud();
        if (Objects.isNull(oss) || Objects.isNull(oss.getAccessId())){
            log.error("file.ucloud 参数未配置,请检查相关配置");
            throw new RuntimeException("请配置file.ucloud操作文件的相关的参数");
        }
        return new S3CompatibleUploadService(createS3Client(oss), createS3Presigner(oss), uploadFileMapper, properties,oss, convert );
    }

    private S3Client createS3Client(UploadProperties.OssProperties oss) {
        try {
            String endpoint = ensureHttpsEndpoint(oss.getEndpoint());

            return S3Client.builder()
                    .endpointOverride(new URI(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(oss.getAccessId(), oss.getAccessKey())))
                    .region(Region.of(oss.getRegion()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .overrideConfiguration(b -> b
                            .apiCallAttemptTimeout(Duration.ofSeconds(30))
                            .retryStrategy(RetryMode.STANDARD)
                            )
                    .build();

        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid endpoint URL", e);
        }
    }
    private String ensureHttpsEndpoint(String endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoint cannot be null");
        }
        // 本地开发环境允许HTTP
        if (endpoint.contains("localhost") || endpoint.contains("127.0.0.1")) {
            return endpoint;
        }
        // 其他情况强制HTTPS
        if (!endpoint.startsWith("http")) {
            return "https://" + endpoint;
        }
        return endpoint.replace("http://", "https://");
    }

    private S3Presigner createS3Presigner(UploadProperties.OssProperties oss) {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(oss.getAccessId(), oss.getAccessKey());

            return S3Presigner.builder()
                    .endpointOverride(new URI(ensureHttpsEndpoint(oss.getEndpoint())))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(oss.getRegion()))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid endpoint URL", e);
        }
    }
//    private S3Client createS3Client(UploadProperties.OssProperties oss) {
//        try {
//            AwsBasicCredentials credentials = AwsBasicCredentials.create(oss.getAccessId(), oss.getAccessKey());
//            // 大多数兼容S3的服务需要启用路径样式访问
//            S3Configuration s3Configuration = S3Configuration.builder()
//                    .pathStyleAccessEnabled(true)
//                    .build();
//
//            return S3Client.builder()
//                    .endpointOverride(new URI(oss.getEndpoint()))
//                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
//                    .region(Region.of(oss.getRegion()))
//                    .serviceConfiguration(s3Configuration)
//                    .build();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Invalid endpoint URL", e);
//        }
//    }
//
//    private S3Presigner createS3Presigner(UploadProperties.OssProperties oss) {
//        try {
//            AwsBasicCredentials credentials = AwsBasicCredentials.create(oss.getAccessId(), oss.getAccessKey());
//
//            S3Configuration s3Configuration = S3Configuration.builder()
//                    .pathStyleAccessEnabled(true)
//                    .build();
//
//            return S3Presigner.builder()
//                    .endpointOverride(new URI(oss.getEndpoint()))
//                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
//                    .region(Region.of(oss.getRegion()))
//                    .serviceConfiguration(s3Configuration)
//                    .build();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Invalid endpoint URL", e);
//        }
//    }
}
