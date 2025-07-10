package com.github.platform.core.file.infra.service.impl;

import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.file.domain.constant.FileUploadEnum;
import com.github.platform.core.file.domain.dto.SysUploadFileDto;
import com.github.platform.core.file.infra.configuration.properties.UploadProperties;
import com.github.platform.core.file.infra.convert.SysUploadFileInfraConvert;
import com.github.platform.core.persistence.mapper.file.SysUploadFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author yxkong
 */
@Slf4j
public class DiskUploadFileService extends AbstractUploadFileService {
    public DiskUploadFileService(SysUploadFileMapper uploadFileMapper, UploadProperties properties, SysUploadFileInfraConvert convert) {
        this.uploadFileMapper = uploadFileMapper;
        this.uploadProperties = properties;
        this.convert = convert;
    }
    @Override
    public boolean support(String storage) {
        return FileUploadEnum.isDisk(storage);
    }

    /**
     * 文件上传
     *   上传后的绝对路径 ：diskPath/module/path/fileName
     * @param module   模块名，以模块区分
     * @param bizNo    业务号
     * @param uploadFileName 上传文件名称 带后缀
     * @param is       文件流
     */
    @Override
    public String upload(String module, String bizNo, String uploadFileName, InputStream is) {
        DataOutputStream out = null;
        try {
            UploadProperties.DiskProperties disk = uploadProperties.getDisk();
            StringBuilder relativePath = new StringBuilder(module);
            relativePath.append(module).append(File.separator).append(getDatePath());
            if (StringUtils.isNotEmpty(bizNo)){
                relativePath.append(File.separator).append(bizNo);
            }
            StringBuilder absolutePath = new StringBuilder(disk.getDiskPath());
            File tmp = new File(absolutePath.toString());
            if (!tmp.exists()) {
                tmp.mkdirs();
            }
            absolutePath.append(relativePath).append(File.separator).append(uploadFileName);
            out = new DataOutputStream(new FileOutputStream(absolutePath.toString()));
            byte[] buffer = new byte[4096];
            int r = 0;
            while ((r = is.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
            out.flush();
            out.close();
            return relativePath.append(File.separator).append(uploadFileName).toString();
        } catch (FileNotFoundException e) {
            log.error("未找到对应的文件", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("文件io异常", e);
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    protected UploadProperties.OssProperties getProperties() {
        return null;
    }

    @Override
    public String getUrl(SysUploadFileDto dto) {
        //需要nginx去做转发
        return uploadProperties.getDisk().getFileUrl() + File.separator +dto.getFilePath() ;
    }
}
