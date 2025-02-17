package com.sinohealth.api.common;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.module.file.dto.BasePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Api(tags = {"通用接口"})
@RequestMapping("/api")
public interface CommonApi {

    @GetMapping("common/download")
    void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request);

    @PostMapping("/common/upload")
    AjaxResult uploadFile(MultipartFile file) throws Exception;

    @GetMapping("/common/download/resource")
    void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception;

    @ApiOperation("oss资源下载")
    @GetMapping("/common/download/oss")
    void ossDownload(@RequestParam(required = false) String sourcePath, HttpServletResponse servletResponse) throws IOException;

    @ApiOperation("oss资源下载")
    @GetMapping("/common/downloads/oss")
    void ossDownloads(@RequestParam String sourcePath, HttpServletResponse servletResponse) throws IOException;

    @ApiOperation("OSS上传")
    @PostMapping("/common/upload/oss")
    AjaxResult<List<BasePath>> ossUpload(@RequestParam("file") MultipartFile[] files);
}
