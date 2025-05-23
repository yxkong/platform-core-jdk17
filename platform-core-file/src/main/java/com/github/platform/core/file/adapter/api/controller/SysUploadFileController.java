package com.github.platform.core.file.adapter.api.controller;

import com.github.platform.core.auth.annotation.NoLogin;
import com.github.platform.core.auth.annotation.RequiredLogin;
import com.github.platform.core.common.entity.StrIdReq;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.file.adapter.api.command.FileInfoCmd;
import com.github.platform.core.file.adapter.api.command.SysUploadFileCmd;
import com.github.platform.core.file.adapter.api.command.SysUploadFileQuery;
import com.github.platform.core.file.adapter.api.convert.SysUploadFileAdapterConvert;
import com.github.platform.core.file.application.executor.ISysUploadFileExecutor;
import com.github.platform.core.file.application.executor.IUploadFileExecutor;
import com.github.platform.core.file.domain.context.SysUploadFileContext;
import com.github.platform.core.file.domain.context.SysUploadFileQueryContext;
import com.github.platform.core.file.domain.dto.SysUploadFileDto;
import com.github.platform.core.file.domain.entity.UploadEntity;
import com.github.platform.core.log.domain.constants.LogOptTypeEnum;
import com.github.platform.core.log.infra.annotation.OptLog;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.entity.dto.PageBean;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.web.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
* 上传文件表
* @website <a href="https://www.5ycode.com/">5ycode</a>
* @author yxkong
* @datetime 2023-08-14 18:22:38.776
* @version 1.0
*/
@RestController
@Tag(name = "uploadFile",description = "上传文件表管理")
@RequestMapping("sys/file/upload")
@Slf4j
public class SysUploadFileController extends BaseController{
    @Resource
    private ISysUploadFileExecutor executor;
    @Resource
    private IUploadFileExecutor uploadFileExecutor;
    @Resource
    private SysUploadFileAdapterConvert convert;

    /**
    * 查询上传文件表列表
    * @param query 查询实体
    * @return 分页结果
    */
    @Operation(summary = "查询上传文件表列表",tags = {"uploadFile"})
    @PostMapping("/query")
    public ResultBean<PageBean<SysUploadFileDto>> query(@RequestBody SysUploadFileQuery query){
        SysUploadFileQueryContext context = convert.toQuery(query);
        PageBean<SysUploadFileDto> pageBean = executor.query(context);
        return buildSucResp(pageBean);
    }

    /**
    * 新增上传文件表
    * @param files 文件
     *     可用对象替代字段       @ModelAttribute SysUploadFileCmd
    * @return 操作结果
    */
    @RequiredLogin
    @OptLog(module="uploadFile",title="新增上传文件表",optType = LogOptTypeEnum.ADD)
    @Operation(summary = "新增上传文件表",tags = {"sysUploadFile"})
    @PostMapping("/add")
    public ResultBean<List<UploadEntity>> add(@RequestParam("files") List<MultipartFile> files, @RequestParam("module") String module,
                          @RequestParam("bizNo") String bizNo) {
        if (CollectionUtil.isEmpty(files)){
            throw exception(ResultStatusEnum.PARAM_EMPTY);
        }
        List<UploadEntity> rst = new ArrayList<>();
        InputStream fileInputStream = null;
        for (MultipartFile file:files){
            byte[] fileBytes;
            try {
                fileInputStream = file.getInputStream();
                // 使用 try-with-resources 确保流被关闭
                try (InputStream inputStream = fileInputStream) {
                    fileBytes = IOUtils.toByteArray(inputStream);
                } catch (IOException e) {
                    log.error("Failed to read input stream.", e);
                    return null;
                }
            } catch (IOException e) {
                throw exception(ResultStatusEnum.COMMON_UPLOAD_STREAM_EXCEPTION);
            }
            UploadEntity uploadEntity = uploadFileExecutor.uploadAndSave(module, bizNo, null, file.getOriginalFilename(), file.getSize(), fileBytes);
            rst.add(uploadEntity);
        }

        return buildSucResp(rst);
    }
    /**
     * 文件上传
     * @param cmd 文件
     *     可用对象替代字段       @ModelAttribute SysUploadFileCmd
     * @return 操作结果
     */
    @RequiredLogin
    @OptLog(module="uploadFile",title="新增上传文件表",optType = LogOptTypeEnum.ADD)
    @Operation(summary = "新增上传文件表",tags = {"sysUploadFile"})
    @PostMapping("/upload")
    public ResultBean<List<UploadEntity>> upload(@RequestBody SysUploadFileCmd cmd) {
        if (CollectionUtil.isEmpty(cmd.getFiles())){
            throw exception(ResultStatusEnum.PARAM_EMPTY);
        }
        List<UploadEntity> rst = new ArrayList<>();
        for (FileInfoCmd fileInfo:cmd.getFiles()){
            // 创建 ByteArrayInputStream 以将 byte[] 转换为 InputStream
            UploadEntity uploadEntity = uploadFileExecutor.uploadAndSave(cmd.getModule(), cmd.getBizNo(), null, fileInfo.getName(), fileInfo.getSize(), fileInfo.getRaw());
            rst.add(uploadEntity);
        }

        return buildSucResp(rst);
    }

    /**
    * 根据id查询上传文件表明细
    * @param id 主键id
    * @return 单条记录
    */
    @RequiredLogin
    @Operation(summary = "根据id查询上传文件表明细",tags = {"uploadFile"})
    @PostMapping("/detail")
    public ResultBean<SysUploadFileDto> detail(@Validated @RequestBody StrIdReq id) {
        SysUploadFileDto dto = executor.findById(id.getId());
        return buildSucResp(dto);
    }
    /**
     * 直接重定向到文件的临时访问地址（适用于图片、PDF 等可直接浏览的文件）
     *
     * @param fileId 文件唯一标识
     * @return 302 重定向到文件 URL
     */
    @NoLogin
    @GetMapping("/redirect/{fileId}")
    public ResponseEntity<Void> redirectToFile(@PathVariable String fileId) {
        SysUploadFileDto fileDto = executor.findByFileId(fileId);
        if (fileDto == null || fileDto.getUrl() == null) {
            throw new ResourceNotFoundException("File not found with ID: " + fileId);
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                // 缓存 1 小时
                .header("Cache-Control", "max-age=3600")
                .location(URI.create(fileDto.getUrl()))
                .build();
    }

    /**
     * 根据id删除上传文件表记录
     * @param id 主键id
     * @return 操作结果
     */
    @OptLog(module="uploadFile",title="根据id删除上传文件表记录",optType = LogOptTypeEnum.DELETE)
    @Operation(summary = "根据id删除上传文件表记录",tags = {"uploadFile"})
    @PostMapping("/delete")
    public ResultBean delete(@Validated @RequestBody StrIdReq id) {
        executor.delete(id.getId());
        return buildSucResp();
    }
    /**
     * 修改上传文件表
     * @param cmd 修改实体
     * @return 操作结果
     */
    @OptLog(module="uploadFile",title="修改上传文件表",optType = LogOptTypeEnum.MODIFY)
    @Operation(summary = "修改上传文件表",tags = {"uploadFile"})
    @PostMapping("/modify")
    public ResultBean modify(@Validated @RequestBody SysUploadFileCmd cmd) {
        SysUploadFileContext context = convert.toContext(cmd);
        executor.update(context);
        return buildSucResp();
    }
}