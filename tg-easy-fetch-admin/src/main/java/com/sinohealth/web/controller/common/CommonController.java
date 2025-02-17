package com.sinohealth.web.controller.common;

import com.alibaba.fastjson.JSON;
import com.sinohealth.common.config.DataPlatformConfig;
import com.sinohealth.common.constant.Constants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.module.file.BaseStorageService;
import com.sinohealth.common.module.file.constant.FileConst;
import com.sinohealth.common.module.file.dto.BasePath;
import com.sinohealth.common.module.file.dto.UploadFileDTO;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.file.FileUploadUtils;
import com.sinohealth.common.utils.file.FileUtils;
import com.sinohealth.framework.config.ServerConfig;
import com.sinohealth.system.dto.common.FileUploadRespVo;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 通用请求处理
 */
@Api(tags = {"通用接口"})
@RestController
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonApiController.class);

    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    BaseStorageService baseStorageService;
    @Value("${dataset.excel}")
    private String datasetExcelUrl;


    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("common/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtils.checkAllowDownload(fileName)) {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String filePath = DataPlatformConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, fileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("/common/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception {
        try {
            // 上传文件路径
            String filePath = DataPlatformConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/common/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            if (!FileUtils.checkAllowDownload(resource)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = DataPlatformConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }


    /**
     * oss资源下载
     *
     * @param sourcePath      如果不传，返回JSON,传返回文件  |    sourcePath:ARROGANCY/20210826135721-excel-template.xls
     * @param servletResponse
     * @throws IOException
     */
    //@ApiOperation("oss资源下载")
    @GetMapping("/common/download/oss")
    public void ossDownload(@RequestParam(required = false) String sourcePath, HttpServletResponse servletResponse) throws IOException {
        ServletOutputStream writer = servletResponse.getOutputStream();

        if (StringUtils.isBlank(sourcePath)) {
            FileUploadRespVo fileUploadRespVo = new FileUploadRespVo().setAbsolutePath(datasetExcelUrl);
            servletResponse.setHeader("content-type", "application/json;charset=utf-8");
            servletResponse.setCharacterEncoding("UTF-8");
            writer.write(JSON.toJSONString(AjaxResult.success(fileUploadRespVo)).getBytes(StandardCharsets.UTF_8));
        } else {
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
            servletResponse.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

            byte[] bytes = baseStorageService.downloadWithBytes(FileConst.Type.ARROGANCY.getType(), sourcePath);
            writer.write(bytes);
        }
        writer.flush();
        writer.close();

    }

    /**
     * oss资源下载（不用权限）
     *
     * @param sourcePath      如果不传，返回JSON,传返回文件  |    sourcePath:ARROGANCY/20210826135721-excel-template.xls
     * @param servletResponse
     * @throws IOException
     */
    //@ApiOperation("oss资源下载")
    @GetMapping("/common/downloads/oss")
    public void ossDownloads(@RequestParam String sourcePath, HttpServletResponse servletResponse) throws IOException {
        ServletOutputStream writer = servletResponse.getOutputStream();

        String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
        servletResponse.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));

        byte[] bytes = baseStorageService.downloadWithBytes(FileConst.Type.ARROGANCY.getType(), sourcePath);
        writer.write(bytes);
        writer.flush();
        writer.close();
    }

    /**
     * OSS上传
     *
     * @param files 文件列表
     * @return 文件存储信息列表
     */
    //@ApiOperation("OSS上传")
    @PostMapping("/common/upload/oss")
    public AjaxResult<List<BasePath>> ossUpload(@RequestParam("file") MultipartFile[] files) {
        List<UploadFileDTO> fileLies = new ArrayList<>();
        for (MultipartFile file : files) {
            UploadFileDTO uploadFileDTO;
            // 默认文件元信息
            Map<String, String> metaDataMap = new HashMap<>();

            metaDataMap.put(FileConst.MetaData.FILENAME, file.getOriginalFilename());
            try {
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);

                String fileName = UUID.randomUUID() + "." + suffix;
                uploadFileDTO = new UploadFileDTO.Builder()
                        .withInputStream(file.getInputStream())
                        .withFileName(fileName)
                        .withMetaData(metaDataMap)
                        .build();

            } catch (IOException e) {
                log.error("batchCreate >> file getInputStream error", e);
                throw new CustomException("batchCreate >> file getInputStream error");
            }

            fileLies.add(uploadFileDTO);
        }

        return AjaxResult.success(baseStorageService.uploadBatch(FileConst.Type.ARROGANCY.getType(), fileLies));
    }

}
