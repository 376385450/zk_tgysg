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
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.saas.file.model.dto.request.CreateRequestDTO;
import com.sinohealth.saas.file.model.dto.response.CreateResponseDTO;
import com.sinohealth.sca.base.model.dto.response.PlatformResponse;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.converter.AssetBeanConverter;
import com.sinohealth.system.domain.vo.TgDocVersionInfoVo;
import com.sinohealth.system.dto.api.cataloguemanageapi.CatalogueDetailDTO;
import com.sinohealth.system.event.EventPublisher;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgAssetRelateMapper;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.impl.DocServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
 * @Author Rudolph
 * @Date 2023-08-08 14:22
 * @Desc
 */
@Api(value = "/api/doc_management", tags = {"提数文档接口"})
@Slf4j
@RestController
@RequestMapping("/api/doc_management")
public class DocApiController extends BaseController {

    @Autowired
    IDocService docService;

    @Autowired
    IAssetService assetService;

    @Autowired
    DataAssetsCatalogueService assetCatalogueService;

    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private FileApi fileApi;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private IAssetService iAssetService;

    @Autowired
    private TgAssetRelateMapper tgAssetRelateMapper;


    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public AjaxResult<?> createOrUpdate(@RequestBody TgAssetDocBindingInfo tgAssetDocBindingInfo) {
        TgDocInfo tgDocInfo = tgAssetDocBindingInfo.getBindingData();
        TgAssetInfo tgAssetInfo = tgAssetDocBindingInfo.getTgAssetInfo();

        if (assetService.checkSameAssetName(tgAssetInfo.getId(), tgAssetInfo.getAssetName()) > 0) {
            return AjaxResult.error(InfoConstants.DUPLICATED_ASSET_NAME);
        }
        AjaxResult<?> ajaxResult = assetService.fillProcessId4FollowMenuDirItem(tgAssetInfo);
        if (!ajaxResult.isSuccess()) {
            return ajaxResult;
        }

        CatalogueDetailDTO tgCatalogueBaseInfo = assetCatalogueService.getCatalogueBaseInfo(tgAssetInfo.getAssetMenuId());
        AssetBeanConverter.asset2DocInfo(tgAssetInfo, tgCatalogueBaseInfo, tgDocInfo);

        tgDocInfo.setProcessId(tgAssetInfo.getProcessId());
        AjaxResult<?> docAjaxResult = docService.createOrUpdate(tgDocInfo);
        if (docAjaxResult.getCode().equals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)) {
            return docAjaxResult;
        }
        tgDocInfo = (TgDocInfo) docAjaxResult.getData();
        tgAssetDocBindingInfo.setBindingData(tgDocInfo);
        tgAssetInfo.setType(AssetType.FILE);
        tgAssetInfo.setRelatedId(tgDocInfo.getId());
        tgAssetInfo.setAssetBindingDataType(tgDocInfo.getType());
        tgAssetInfo.setAssetBindingDataName(tgDocInfo.getName());
        assetService.addAsset(tgAssetInfo);
        return AjaxResult.success(tgAssetDocBindingInfo);
    }


    @GetMapping("/query")
    public AjaxResult<?> query(@RequestParam Map<String, Object> params) {
        return docService.query(params);
    }


    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/delete")
    public AjaxResult<?> delete(@RequestParam Long id) {
        assetService.delete(id, AssetType.FILE);
        return docService.delete(id);
    }


    /*****************************************************/


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
     * Ajax, 异步上传富文本图片文件
     */

    @PostMapping(value = { "/uploadCommonPicture"})
    public AjaxResult<TgDocInfo> uploadCommonPicture(@RequestParam("file") MultipartFile file)
            throws ExecutionException, InterruptedException {

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
            request.setUploadPath("tg-easy-fetch/common");
            request.setPolicyRead(true);
            request.setEncodeFileName(true);
            request.setStorageConfigCode(fileProperties.getFileStorageCode());
            PlatformResponse<CreateResponseDTO> uploadResult = fileApi.createFile(file, JSON.toJSONString(request));
            log.info("uploadResult={}", uploadResult);
            return uploadResult;
        });

        PlatformResponse<CreateResponseDTO> uploadResult = uploadFuture.get();

        CreateResponseDTO rsp = uploadResult.getResult();

        TgDocInfo result = TgDocInfo.newInstance();
        result.setName(name);
        result.setType(type);
        result.setPath(rsp.getSourcePath());
        result.setRealPath(rsp.getUrl());

        return AjaxResult.success(result);
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
        AtomicBoolean canDownloadPdf = new AtomicBoolean(false);
        AtomicBoolean canDownloadSrc = new AtomicBoolean(false);
        AtomicBoolean canDownloadPdfOffLine = new AtomicBoolean(false);
        AtomicBoolean canDownloadSrcOffLine = new AtomicBoolean(false);


        final TgAssetInfo tgAssetInfo = assetService.queryOne(id, AssetType.FILE);
        // 下载权限校验
        authCheck(tgAssetInfo.getId(), canDownloadPdf, canDownloadSrc, canDownloadPdfOffLine, canDownloadSrcOffLine);

        if (StringUtils.isBlank(type)) {
            // 默认下载PDF水印文件
            type = CommonConstants.PDF_VERSION;
        }

        try {
            TgUploadedFileDim downloadInfo = getDownloadInfo(id, versionId);
            ServletOutputStream outputStream = response.getOutputStream();
            if (type.equals(CommonConstants.PDF_VERSION) && canDownloadPdf.get()) {
                if (canDownloadPdfOffLine.get()) {
                    // 资产下架服务
                    handleCustomException(response, outputStream, CommonConstants.SERVICE_OFFLINE);
                    return;
                }
            }

            if (type.equals(CommonConstants.SRC_VERSION) && canDownloadSrc.get()) {
                if (canDownloadSrcOffLine.get()) {
                    // 资产下架服务
                    handleCustomException(response, outputStream, CommonConstants.SERVICE_OFFLINE);
                    return;
                }
            }

            if (null == downloadInfo) {
                handleCustomException(response, outputStream, CommonConstants.CAN_NOT_LOAD_FILE);
                return;
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            if (type.equals(CommonConstants.PDF_VERSION)) {
                if (StringUtils.isBlank(downloadInfo.getPdfPath())) {
                    handleCustomException(response, outputStream, CommonConstants.WAIT_LOADING);
                } else {
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
        AtomicBoolean canViewPdf = new AtomicBoolean(true);
        AtomicBoolean canDownloadPdf = new AtomicBoolean(false);
        AtomicBoolean canDownloadSrc = new AtomicBoolean(false);
        // 权限校验
        //authCheck(tgDocInfo, whitelistUsers, canViewPdf, canDownloadPdf, canDownloadSrc);

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
        if (errorCode == CommonConstants.SERVICE_OFFLINE) {
            log.error(InfoConstants.SERVICE_OFFLINE);
            response.setStatus(CommonConstants.SERVICE_OFFLINE);
            writeErrorMessage(response, outputStream, InfoConstants.SERVICE_OFFLINE);
        } else if (errorCode == CommonConstants.CAN_NOT_LOAD_FILE) {
            log.error(InfoConstants.CAN_NOT_LOAD_FILE);
            response.setStatus(CommonConstants.CAN_NOT_LOAD_FILE);
            writeErrorMessage(response, outputStream, InfoConstants.CAN_NOT_LOAD_FILE);
        } else if (errorCode == CommonConstants.NO_DOC_AUTH) {
            log.error(InfoConstants.NO_DOC_AUTH);
            response.setStatus(CommonConstants.NO_DOC_AUTH);
            writeErrorMessage(response, outputStream, InfoConstants.NO_DOC_AUTH);
        } else if (errorCode == CommonConstants.WAIT_LOADING) {
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

    private void authCheck(Long assetId, AtomicBoolean canDownloadPdf, AtomicBoolean canDownloadSrc, AtomicBoolean canDownloadPdfOffLine, AtomicBoolean canDownloadSrcOffLine) {

        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        String deptId = o.getMainOrganizationId();
        final TgAssetInfo tgAssetInfo = assetService.queryOne(assetId);
        final List<AssetPermissionType> assetPermissionTypes = iAssetService.computePermissions(tgAssetInfo, userId, deptId, true);

        if (assetPermissionTypes.contains(AssetPermissionType.DOWNLOAD_PDF)) {
            canDownloadPdf.set(true);
        }

        if (assetPermissionTypes.contains(AssetPermissionType.DOWNLOAD_PDF_OFFLINE)) {
            canDownloadPdfOffLine.set(true);
        }

        if (assetPermissionTypes.contains(AssetPermissionType.DOWNLOAD_SRC)) {
            canDownloadSrc.set(true);
        }

        if (assetPermissionTypes.contains(AssetPermissionType.DOWNLOAD_SRC_OFFLINE)) {
            canDownloadSrcOffLine.set(true);
        }

    }

}
