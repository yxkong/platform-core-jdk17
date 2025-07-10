package com.github.platform.core.file.infra.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.file.domain.constant.FileUploadEnum;
import com.github.platform.core.file.domain.dto.SysUploadFileDto;
import com.github.platform.core.file.infra.configuration.properties.UploadProperties;
import com.github.platform.core.file.infra.convert.SysUploadFileInfraConvert;
import com.github.platform.core.persistence.mapper.file.SysUploadFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Objects;

/**
 * 阿里云文件上传
 * @author: yxkong
 * @date: 2024/3/22 15:12
 * @version: 1.0
 */
@Slf4j
public class AliYunFileUploadFileService extends AbstractUploadFileService{
    private final OSS ossClient;
    private final UploadProperties.OssProperties ossProperties;
    public AliYunFileUploadFileService(OSS ossClient, SysUploadFileMapper uploadFileMapper
            , UploadProperties properties, UploadProperties.OssProperties ossProperties, SysUploadFileInfraConvert convert) {
        this.uploadFileMapper = uploadFileMapper;
        this.uploadProperties = properties;
        this.ossProperties = ossProperties;
        this.ossClient = ossClient;
        this.convert = convert;
    }

    @Override
    public boolean support(String storage) {
        return FileUploadEnum.isAliYun(storage);
    }

    @Override
    protected UploadProperties.OssProperties getProperties() {
        return this.ossProperties;
    }
    @Override
    public String upload(String module, String bizNo, String uploadFileName, InputStream is) {
        try {
            String objectName = getObjectName(module, getDatePath(), bizNo,uploadFileName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(getProperties().getBucketName(),objectName, is);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            log.info("上传阿里云oss结果{}", JsonUtils.toJson(result));
            return objectName;
        }catch (OSSException oe) {
            log.error("上传阿里云oss服务端异常，code:{},message:{},requestId:{},hostId:{}",oe.getErrorCode(),oe.getMessage(),oe.getRequestId(),oe.getHostId());
            throw oe;
        } catch (ClientException ce) {
            log.error("上传阿里云oss客户端异常,code:{},message:{},requestId:{}",ce.getErrorCode(),ce.getMessage(),ce.getRequestId());
            throw ce;
        }
    }

    @Override
    public String getUrl(SysUploadFileDto dto) {
        //这里，oss必须设置cname访问
        String cnameUrl = getCnameUrl(dto);
        if (StringUtils.isNotEmpty(cnameUrl)){
            return cnameUrl;
        }
        return getUrlStr(dto,null);
    }

    /**
     *
     * @param dto
     * @param style style = "image/resize,m_fixed,w_100,h_100/rotate,90";
     *  可以实时处理图片 将图片缩放为固定宽高100 px后，再旋转90°。
     * @return
     */
    private String getUrlStr(SysUploadFileDto dto,String style) {
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(getProperties().getBucketName(), dto.getFilePath());
        // 设置失效时间
        int activeMinutes = Objects.equals(dto.getPermanent() ,Boolean.TRUE) ? 10080  :
                Objects.isNull(getProperties().getLinkExpireMinutes()) ? 60 : getProperties().getLinkExpireMinutes();
        req.setExpiration(DateUtils.addDays(new Date(), activeMinutes));
        if (StringUtils.isNotEmpty(style)){
            req.setProcess(style);
        }
        URL url  = ossClient.generatePresignedUrl(req);
        return url.toString();
    }

    @Override
    public String getThumbUrl(SysUploadFileDto dto) {
        if (!dto.isImage()){
            return null;
        }
        //如果不开启压缩直接返回
        String url = getUrl(dto);
        if (!this.getProperties().getThumbSwitch()){
            return url;
        }
        /**
         *  m_lfit 等比例缩放
         */
        String style = "image/resize,m_lfit,w_100,h_100";
        String thumbUrl = getUrlStr(dto, style);
        return getThumbCnameUrl(thumbUrl,url);
    }

}
