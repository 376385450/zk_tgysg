package com.sinohealth.web.controller.system;

import cn.hutool.core.lang.Pair;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.saas.file.model.dto.request.CreateRequestDTO;
import com.sinohealth.saas.file.model.dto.response.CreateResponseDTO;
import com.sinohealth.sca.base.model.dto.response.PlatformResponse;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgUploadedFileDim;
import com.sinohealth.system.domain.WhiteListUser;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.vo.TgDocVersionInfoVo;
import com.sinohealth.system.event.EventPublisher;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.impl.DocServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Rudolph
 */
@Api(value = "/doc_management", tags = {"提数文档接口"})
@Slf4j
@RestController
@RequestMapping("/doc_management")
public class DocController extends BaseController {

    @Autowired
    IDocService docService;

    @Autowired
    private FileApi fileApi;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private FileProperties fileProperties;
    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private EventPublisher eventPublisher;

    /**
     * 创建更新文档项，序列化名称
     */
    @PostMapping("/add")
    public AjaxResult<?> createOrUpdate(@RequestBody TgDocInfo docInfo) {
        if (Objects.isNull(docInfo.getDirId())) {
            return AjaxResult.error("请选择目录");
        }
        return docService.createOrUpdate(docInfo);
    }

    @GetMapping("/query")
    public AjaxResult<?> query(@RequestParam Map<String, Object> params) {
        return docService.query(params);
    }

    @DeleteMapping("/delete")
    public AjaxResult<?> delete(@RequestParam Long id) {
        return docService.delete(id);
    }

    @GetMapping("/uploadedVersion/{id}")
    public AjaxResult<TgDocVersionInfoVo> listUploadedVersion(@PathVariable("id") Long id) {

        TgDocVersionInfoVo result = new TgDocVersionInfoVo();

        TgDocInfo currentDoc = queryOne(id);

        Long pageNum = TypeUtils.castToLong(ThreadContextHolder.getParams().get(CommonConstants.PAGENUM).toString());
        Long pageSize = TypeUtils.castToLong(ThreadContextHolder.getParams().get(CommonConstants.PAGESIZE).toString());
        QueryWrapper<TgUploadedFileDim> qw = new QueryWrapper<>();
        qw.eq("mapping_type", ApplicationConst.ApplicationType.DOC_APPLICATION);
        qw.eq("mapping_id", id);
        qw.isNotNull("file_path");
        qw.orderByDesc("upload_time");
        Page<TgUploadedFileDim> tgUploadedFileDimPage = TgUploadedFileDim.newInstance().selectPage(new Page<>(pageNum, pageSize), qw);

        result.setDocId(id);
        result.setCurrentDocInfo(currentDoc);
        result.setVersionInfo(tgUploadedFileDimPage);

        return AjaxResult.success(result);
    }

    private TgDocInfo queryOne(Long id) {
        // 这里返回的查询结果只会是 TgDocInfo的包装, 强转是没有问题的,所以消除检查
        @SuppressWarnings("unchecked")
        AjaxResult<TgDocInfo> currentDocAjxResult = (AjaxResult<TgDocInfo>) docService.query(
                new HashMap<String, Object>(CommonConstants.INIT_MAP_SIZE) {{
                    put("id", id);
                }});
        return currentDocAjxResult.getData();
    }

    /**
     * Ajax, 异步删除已上传文件
     */
    @DeleteMapping("/deleteUploadedFile/{docGroupId}/{docName}")
    public AjaxResult<Object> deleteUploadedFile(@PathVariable("docGroupId") Long docGroupId, @PathVariable String docName) {

        return AjaxResult.success();
    }

    /**
     * Ajax, 异步上传文件
     */
    @PostMapping(value = {"/upload/{id}", "/upload"})
    public AjaxResult<TgDocInfo> upload(@PathVariable(value = "id", required = false) Long id,
                                        @RequestParam("file") MultipartFile file)
            throws ExecutionException, InterruptedException {

        AjaxResult<TgDocInfo> result;
        // tgDocInfo 不存在，先初始化
        if (ObjectUtils.isNull(id)) {
            TgDocInfo tgDocInfo = TgDocInfo.newInstance();
            tgDocInfo.setOwnerId(ThreadContextHolder.getSysUser().getUserId());
            tgDocInfo.setIsInit(true);

            Pair<AjaxResult, TgUploadedFileDim> pair = SpringUtils.getBean(DocServiceImpl.class).doCreateOrUpdate(tgDocInfo);
            result = pair.getKey();
        } else {
            result = (AjaxResult<TgDocInfo>) docService.query(new HashMap<String, Object>(CommonConstants.INIT_MAP_SIZE) {{
                put("id", id);
            }});
        }
        if (!result.isSuccess()) {
            return result;
        }

//        if (StringUtils.isNotBlank(result.getData().getCreateTime())
//                && !result.getData().getOwnerId().equals(ThreadContextHolder.getSysUser().getUserId())
//                && ThreadContextHolder.getSysUser().getRoles().stream().noneMatch(r -> r.getRoleName().contains(CommonConstants.ADMIN))) {
//            return AjaxResult.error("非管理员或文档所属人无法更新文档");
//        }

        String name, type;
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String[] splitFileName = originalFilename.split("\\.");
        if (splitFileName.length >= CommonConstants.FILENAME_NORMAL_PART_SIZE) {
            List<String> buff = new ArrayList<>(Arrays.asList(splitFileName));
            buff.remove(splitFileName.length - 1);
            name = String.join(".", buff);
            type = splitFileName[splitFileName.length - 1];
        } else {
            return AjaxResult.error("文件名异常");
        }

        // 文件上传
        CompletableFuture<PlatformResponse<CreateResponseDTO>> uploadFuture = CompletableFuture.supplyAsync(() -> {
            CreateRequestDTO request = new CreateRequestDTO();
            request.setUploadPath("tg-easy-fetch");
            request.setPolicyRead(true);
            request.setEncodeFileName(true);
            request.setStorageConfigCode(fileProperties.getFileStorageCode());
            PlatformResponse<CreateResponseDTO> uploadResult = fileApi.createFile(file, JSON.toJSONString(request));
            log.info("uploadResult={}", uploadResult);
            return uploadResult;
        });

        PlatformResponse<CreateResponseDTO> uploadResult = uploadFuture.get();

        CreateResponseDTO rsp = uploadResult.getResult();
        TgDocInfo data = result.getData();
        data.setName(name);
        data.setType(type);
        data.setPath(rsp.getSourcePath());

        return result;
    }

    /**
     * Ajax, 异步下载已上传文件
     */
    @GetMapping(value = {"/download/{id}/{versionId}/{type}",
            "/download/{id}/{type}", "/download/{id}"})
    public void download(@PathVariable("id") Long id,
                         @PathVariable(value = "versionId", required = false) Long versionId,
                         @PathVariable(value = "type", required = false) String type,
                         HttpServletResponse response) {
        if (null == id) {
            return;
        }

        TgDocInfo tgDocInfo = queryOne(id);
        List<WhiteListUser> whitelistUsers = tgDocInfo.getWhitelistUsers();
        AtomicBoolean canViewPdf = new AtomicBoolean(false);
        AtomicBoolean canDownloadPdf = new AtomicBoolean(false);
        AtomicBoolean canDownloadSrc = new AtomicBoolean(false);

        // 下载权限校验
        authCheck(tgDocInfo, whitelistUsers, canViewPdf, canDownloadPdf, canDownloadSrc);

        if (StringUtils.isBlank(type)) {
            // 默认下载PDF水印文件
            type = CommonConstants.PDF_VERSION;
        }

        try {
            TgUploadedFileDim downloadInfo = getDownloadInfo(id, versionId);
            ServletOutputStream outputStream = response.getOutputStream();
            if (null == downloadInfo) {
                handleCustomException(response, outputStream, CommonConstants.CAN_NOT_LOAD_FILE);
                return;
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            if (type.equals(CommonConstants.PDF_VERSION)) {
                if (canDownloadPdf.get()) {
                    // 下载PDF，添加水印
                    String username = SecurityUtils.getUsername();
                    String watermarkPdfUrl = officeRepository.watermark(downloadInfo.getPdfPath(), username);
                    byte[] fileBytes = HttpUtil.downloadBytes(watermarkPdfUrl);
                    outputStream.write(fileBytes);
                    eventPublisher.registerDocEvent(tgDocInfo.getId(), CommonConstants.PDF_DOWNLOAD_TIMES, InfoConstants.DOC_PDF_DOWNLOAD);
                    return;
                } else {
                    handleCustomException(response, outputStream, CommonConstants.NO_DOC_AUTH);
                }
            }

            if (type.equals(CommonConstants.SRC_VERSION)) {
                if (canDownloadSrc.get()) {
                    // 下载源文件
                    String filePath = Objects.requireNonNull(downloadInfo).getFilePath();
                    URLEncoder.encode(filePath, StandardCharsets.UTF_8.name());
                    byte[] fileBts = fileApi.get(fileProperties.getFileStorageCode(), filePath);
                    outputStream.write(fileBts);
                    eventPublisher.registerDocEvent(tgDocInfo.getId(), CommonConstants.SOURCE_FILE_DOWNLOAD_TIMES, InfoConstants.DOC_SRC_DOWNLOAD);
                } else {
                    handleCustomException(response, outputStream, CommonConstants.NO_DOC_AUTH);
                }
            }

        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void writeErrorMessage(HttpServletResponse response, ServletOutputStream outputStream, String s) {
        log.error("{}", s);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = new PrintWriter(outputStream);
        out.println(s);
        out.flush();
    }

    @ApiOperation("文档在线预览权限校验")
    @GetMapping(value = {"/preview/authCheck/{id}/{versionId}", "/preview/authCheck/{id}"})
    public AjaxResult<Object> previewAuthCheck(@PathVariable("id") Long id, @PathVariable(value = "versionId", required = false) Long versionId) {
        // 权限校验
        TgDocInfo tgDocInfo = queryOne(id);
        List<WhiteListUser> whitelistUsers = tgDocInfo.getWhitelistUsers();
        boolean docAuth = isOwner(tgDocInfo);
        if (!docAuth) {
            Optional<WhiteListUser> first = whitelistUsers.stream().filter(u -> u.getUserId().longValue() == ThreadContextHolder.getSysUser().getUserId()).findFirst();
            if (first.isPresent() && first.get().getAuthorization().contains(DataDirConst.DocPermission.CAN_VIEW_PDF)) {
                docAuth = true;
            }
        }
        if (!docAuth) {
            return AjaxResult.error("权限不足");
        }
        TgUploadedFileDim fileDim = getDownloadInfo(id, versionId);
        if (null == fileDim) {
            return AjaxResult.error("没有找到对应的文件");
        } else if (StringUtils.isBlank(fileDim.getPdfPath())) {
            return AjaxResult.error("预览文件加载中，请稍后重试");
        } else {
            return AjaxResult.success();
        }
    }

    @ApiOperation("文档在线预览")
    @GetMapping(value = {"/preview/{id}/{versionId}", "/preview/{id}"})
    public void preview(@PathVariable("id") Long id, @PathVariable(value = "versionId", required = false) Long versionId, HttpServletResponse response) throws Exception {

        TgDocInfo tgDocInfo = queryOne(id);
        ServletOutputStream outputStream = response.getOutputStream();
        List<WhiteListUser> whitelistUsers = tgDocInfo.getWhitelistUsers();
        AtomicBoolean canViewPdf = new AtomicBoolean(false);
        AtomicBoolean canDownloadPdf = new AtomicBoolean(false);
        AtomicBoolean canDownloadSrc = new AtomicBoolean(false);
        // 权限校验
        authCheck(tgDocInfo, whitelistUsers, canViewPdf, canDownloadPdf, canDownloadSrc);

        if (canViewPdf.get()) {
            TgUploadedFileDim fileDim = getDownloadInfo(id, versionId);
            if (null == fileDim) {
                handleCustomException(response, outputStream, CommonConstants.CAN_NOT_LOAD_FILE);
            } else if (StringUtils.isBlank(fileDim.getPdfPath())) {
                handleCustomException(response, outputStream, CommonConstants.WAIT_LOADING);
            } else {
                String username = SecurityUtils.getUsername();
                String pdfPath = fileDim.getPdfPath();
                // 预览添加水印
                String watermarkPdfUrl = officeRepository.watermark(pdfPath, username);
                log.info("预览水印文件url: {}", watermarkPdfUrl);
                // 埋点: 文档预览数 表 tg_doc_info
                eventPublisher.registerDocEvent(tgDocInfo.getId(), CommonConstants.READ_TIMES, InfoConstants.PREVIEW_DOC);
                response.setHeader("Content-Type", "application/pdf");
                byte[] fileBytes = HttpUtil.downloadBytes(watermarkPdfUrl);
                outputStream.write(fileBytes);
                outputStream.flush();
            }
        } else {
            handleCustomException(response, outputStream, CommonConstants.NO_DOC_AUTH);
        }
    }

    private void handleCustomException(HttpServletResponse response, ServletOutputStream outputStream, int errorCode) {
        if (errorCode == CommonConstants.CAN_NOT_LOAD_FILE) {
            log.error(InfoConstants.CAN_NOT_LOAD_FILE);
            response.setStatus(CommonConstants.CAN_NOT_LOAD_FILE);
            writeErrorMessage(response, outputStream, InfoConstants.CAN_NOT_LOAD_FILE);
        }
        if (errorCode == CommonConstants.NO_DOC_AUTH) {
            log.error(InfoConstants.NO_DOC_AUTH);
            response.setStatus(CommonConstants.NO_DOC_AUTH);
            writeErrorMessage(response, outputStream, InfoConstants.NO_DOC_AUTH);
        }
        if (errorCode == CommonConstants.WAIT_LOADING) {
            log.warn(InfoConstants.WAIT_LOADING);
            response.setStatus(CommonConstants.WAIT_LOADING);
            writeErrorMessage(response, outputStream, InfoConstants.WAIT_LOADING);
        }

    }


    private boolean isOwner(TgDocInfo tgDocInfo) {
        return tgDocInfo.getOwnerId().longValue() == (ThreadContextHolder.getSysUser().getUserId());
    }

    private TgUploadedFileDim getDownloadInfo(Long id, Long versionId) {
        QueryWrapper<TgUploadedFileDim> qw = new QueryWrapper<>();
        qw.eq("mapping_id", id);
        if (null != versionId) {
            qw.eq("id", versionId);
        }
        qw.orderByDesc("upload_time");
        List<TgUploadedFileDim> downloadInfoList = TgUploadedFileDim.newInstance().selectList(qw);
        TgUploadedFileDim downloadInfo;
        if (downloadInfoList.size() > 0) {
            downloadInfo = downloadInfoList.get(0);
            return downloadInfo;
        } else {
            log.error("参数错误,无法找到对应的文件进行下载");
            return null;
        }
    }

    private void authCheck(TgDocInfo tgDocInfo, List<WhiteListUser> whitelistUsers,
                           AtomicBoolean canViewPdf, AtomicBoolean canDownloadPdf, AtomicBoolean canDownloadSrc) {

        if (isOwner(tgDocInfo)) {
            canViewPdf.set(true);
            canDownloadPdf.set(true);
            canDownloadSrc.set(true);
        }
        if (!tgDocInfo.getNeed2Audit()) {
            canViewPdf.set(true);
            if (tgDocInfo.getCanDownloadPdf()) {
                canDownloadPdf.set(true);
            }
            if (tgDocInfo.getCanDownloadSourceFile()) {
                canDownloadSrc.set(true);
            }
        }
        Optional<WhiteListUser> first = whitelistUsers.stream().filter(u -> u.getUserId().longValue() == ThreadContextHolder.getSysUser().getUserId()).findFirst();

        if (first.isPresent() && first.get().getAuthorization().contains(DataDirConst.DocPermission.CAN_VIEW_PDF)) {
            canViewPdf.set(true);
        }
        if (first.isPresent() && first.get().getAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF)) {
            canDownloadPdf.set(true);
        }
        if (first.isPresent() && first.get().getAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC)) {
            canDownloadSrc.set(true);
        }

        // 检测当前用户是否对该文档进行过申请
        List<TgApplicationInfo> docApplication = TgApplicationInfo.newInstance().selectList(new QueryWrapper<TgApplicationInfo>() {{
            eq("doc_id", tgDocInfo.getId());
            eq("applicant_id", ThreadContextHolder.getSysUser().getUserId());
            eq("current_audit_process_status", ApplicationConst.AuditStatus.AUDIT_PASS);
            eq("status", CommonConstants.NORMAL);
        }});
        for (TgApplicationInfo d : docApplication) {
            JsonBeanConverter.convert2Obj(d);
            if (d.getDocAuthorization().contains(DataDirConst.DocPermission.CAN_VIEW_PDF)) {
                canViewPdf.set(true);
            }
            if (d.getDocAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF)) {
                canDownloadPdf.set(true);
            }
            if (d.getDocAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC)) {
                canDownloadSrc.set(true);
            }
        }
    }

}
