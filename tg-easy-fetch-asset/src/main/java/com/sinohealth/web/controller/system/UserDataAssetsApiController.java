package com.sinohealth.web.controller.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpUtil;
import com.google.common.base.Strings;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.FtpStatus;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.HttpServletResponseUtil;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.biz.arkbi.service.ArkBiService;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.dao.UserFileAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.domain.UserFileAssets;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetsSyncDTO;
import com.sinohealth.system.biz.dataassets.dto.request.ArkBiEditRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsDataDownloadGetRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsDataDownloadRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsDirRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FileAssetsCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.MyAssetRequest;
import com.sinohealth.system.biz.dataassets.dto.request.UserDataAssetsSyncRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dataassets.service.UserFileAssetsService;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import com.sinohealth.system.biz.dir.util.AssetsTreeUtil;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.ErrorCode;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.dto.application.deliver.request.DeliverPackBaseReq;
import com.sinohealth.system.service.DataDeliverRecordService;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.IMyDataDirService;
import com.sinohealth.system.service.impl.MyDataDirServiceImpl;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-17 10:25
 */
@Api(value = "/api/userAssets", tags = {"用户资产接口"})
@Slf4j
@RestController
@RequestMapping("/api/userAssets")
public class UserDataAssetsApiController {

    @Autowired
    private UserFileAssetsDAO userFileAssetsDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;

    @Autowired
    private IDocService docService;
    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private AssetsUpgradeTriggerService assetsUpgradeTriggerService;
    @Autowired
    private UserFileAssetsService userFileAssetsService;
    @Autowired
    private IMyDataDirService myDataDirService;

    @Autowired
    private OfficeRepository officeRepository;
    @Autowired
    private FileApi fileApi;

    @Autowired
    private FileProperties fileProperties;
    @Autowired
    private DataDeliverRecordService dataDeliverRecordService;
    @Autowired
    private ArkBiService arkBiService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 上传文件资产
     */
    @PostMapping("/uploadAssets")
    public AjaxResult<FileAssetsUploadDTO> uploadFileAssets(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("projectId") Long projectId) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        boolean exist = userFileAssetsService.existsFile(originalFilename, projectId);
        if (exist) {
            return AjaxResult.error(ErrorCode.REPEAT_FILE, "重复文件名：" + originalFilename, null);
        }

        return docService.uploadGetPath(file);
    }

    /**
     * BI 数据同步状态列表
     */
    @GetMapping("/syncList")
    public AjaxResult<List<UserDataAssetsSyncDTO>> syncList() {
        return userDataAssetsService.querySyncList();
    }

    /**
     * 批量同步，忽略已同步数据
     *
     * @see MyDataDirServiceImpl#getPushTableSelectSql(Long)
     */
    @PostMapping("/sync")
    public AjaxResult<Void> syncAssetsToBiView(@RequestBody UserDataAssetsSyncRequest request) {
        return myDataDirService.syncAssetsToBiView(request);
    }

    /**
     * 保存文件资产
     */
    @PostMapping("/createFileAssets")
    public AjaxResult<Void> createFileAssets(@Validated @RequestBody FileAssetsCreateRequest request) {
        return userFileAssetsService.createFileAssets(request);
    }

    /**
     * 我的数据 目录和数据（数据，BI）
     */
    @ApiOperation(value = "我的资产-V2")
    @PostMapping("listAll")
    public AjaxResult<Object> listAllApi(@RequestBody AssetsDirRequest request) {
        try {
            return AjaxResult.success(InfoConstants.REQUEST_OK, Collections.emptyList());
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD);
        }
    }

    @PostMapping("/listMyAsset")
    @ApiOperation("获取项目可关联资产")
    public AjaxResult<PageInfo<UserDataAssetResp>> listMyAsset(@RequestBody MyAssetRequest request) {
        return userDataAssetsService.listMyAsset(request);
    }


    /**
     * 我的数据 目录和数据（数据，BI）
     */
    @ApiOperation(value = "我的资产-V3")
    @PostMapping("assetsTree")
    public AjaxResult<Object> assetsTreeApi(@RequestBody AssetsDirRequest request) {
        try {
            if (StringUtils.isNotBlank(request.getIcon())) {
                List<AssetsNode> data = userDataAssetsService.queryUserAssets(request);
                return AjaxResult.success(AssetsTreeUtil.reSort(data));
            }
            List<AssetsNode> data = userDataAssetsService.queryUserAssetsTree(request);
            return AjaxResult.success(AssetsTreeUtil.reSort(data));
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD);
        }
    }

    @PostMapping("assetsTimeGra")
    public AjaxResult<List<String>> assetsTimeGra(@RequestBody AssetsDirRequest request) {
        return userDataAssetsService.assetsTimeGra(request);
    }


    @ApiOperation(value = "编辑仪表板")
    @PostMapping("/editArkBi")
    public AjaxResult editArkBi(@RequestBody ArkBiEditRequest request) {
        arkBiService.editArkBi(request);
        return AjaxResult.success("编辑成功");
    }

    /**
     * @see DocApiController#preview(Long, Long, HttpServletResponse)
     */
    @ApiOperation("文档在线预览")
    @GetMapping(value = {"/preview/{id}"})
    public void preview(@PathVariable("id") Long fileAssetsId, HttpServletResponse response) throws Exception {
        ServletOutputStream outputStream = response.getOutputStream();
        UserFileAssets file = userFileAssetsDAO.getById(fileAssetsId);
        String username = SecurityUtils.getUsername();
        String pdfPath = file.getPdfPath();
        if (StringUtils.isBlank(pdfPath)) {
            return;
        }

        // 预览添加水印
        String watermarkPdfUrl = officeRepository.watermark(pdfPath, username);
        log.info("预览水印文件url: {}", watermarkPdfUrl);
        response.setHeader("Content-Type", "application/pdf");
        byte[] fileBytes = HttpUtil.downloadBytes(watermarkPdfUrl);
        outputStream.write(fileBytes);
        outputStream.flush();
    }

    @ApiOperation("文档下载")
    @GetMapping(value = {"/download/{id}"})
    public void download(@PathVariable("id") Long fileAssetsId, HttpServletResponse response) throws Exception {
        try {
            UserFileAssets file = userFileAssetsDAO.getById(fileAssetsId);
            // 保存下载记录
            DeliverPackBaseReq req = new DeliverPackBaseReq();
            req.setAssetsId(fileAssetsId);
            req.setPack(false);
            DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(req, DeliverResourceType.FILE);
            dataDeliverRecordService.saveDownloadRecords(requestContextHolder);

            ServletOutputStream outputStream = response.getOutputStream();
            String filePath = Objects.requireNonNull(file).getPath();
            URLEncoder.encode(filePath, StandardCharsets.UTF_8.name());
            byte[] fileBts = fileApi.get(fileProperties.getFileStorageCode(), filePath);
            outputStream.write(fileBts);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @ApiOperation("文档删除")
    @GetMapping(value = {"/delete/{id}"})
    public AjaxResult<Void> deleteFile(@PathVariable("id") Long fileAssetsId) {
        return userFileAssetsService.deleteById(fileAssetsId);
    }

    @ApiOperation("删除资产")
    @GetMapping(value = {"/deleteData/{id}"})
    public AjaxResult<Boolean> deleteData(@PathVariable("id") Long id) {
        return AjaxResult.success(userDataAssetsService.deleteSaveAs(id));
    }

    @ApiOperation("删除资产")
    @GetMapping(value = {"/deleteChart/{id}"})
    public AjaxResult<Boolean> deleteChart(@PathVariable("id") Long id) {
        return AjaxResult.success(arkBiService.deleteChart(id));
    }

    @ApiOperation("数据资产下载")
    @GetMapping("/download/dataAssets/{id}")
    public void downloadDataAssets(@PathVariable("id") Long assetsId,
                                   @RequestParam(value = "version", required = false) Integer version,
                                   HttpServletResponse response) {
        FtpClient ftpClient = null;
        try {
            UserDataAssets dataAssets = userDataAssetsDAO.getById(assetsId);
            if (Objects.nonNull(version) && !Objects.equals(dataAssets.getVersion(), version)) {
                UserDataAssetsSnapshot snap = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, version);
                if (Objects.isNull(snap)) {
                    log.error("", new CustomException("下载的资产版本不存在 " + assetsId + "_" + version));
                    return;
                }
                dataAssets = snap;
            }
            if (!Objects.equals(dataAssets.getFtpStatus(), FtpStatus.SUCCESS.name())) {
                throw new CustomException("Excel文件生成中或生成失败: " + assetsId + " " + dataAssets.getFtpStatus());
            }

            Assert.isTrue(!Strings.isNullOrEmpty(dataAssets.getFtpPath()), "ftp路径为空");
            // 保存下载记录
            DeliverResourceType deliverResourceType = "csv".equals(FileUtil.getSuffix(dataAssets.getFtpPath()))
                    ? DeliverResourceType.CSV : DeliverResourceType.EXCEL;
//            log.info("path={}", dataAssets.getFtpPath());
            DeliverPackBaseReq req = new DeliverPackBaseReq();
            req.setAssetsId(assetsId);
            req.setPack(false);
            DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(req, deliverResourceType);
            dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
            // 输出流设置
            HttpServletResponseUtil.setting(dataAssets.getFtpFileName(), response);
            //
            ftpClient = FtpClientFactory.getInstance();
            ftpClient.open();
            ftpClient.downloadFile(dataAssets.getFtpPath(), response.getOutputStream());
        } catch (Exception e) {
            log.error("{}#{} 数据资产下载失败:", assetsId, version, e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception ignored) {

                }
            }
        }
    }

    @ApiOperation("数据资产批量下载")
    @PostMapping("/download/dataAssets")
    public void downloadDataAssets(@RequestBody @Validated AssetsDataDownloadRequest request) {
        Boolean userLock = null;
        Boolean downLock = null;
        ServletOutputStream outputStream = null;
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        File file = null;
        try {
            if (Objects.isNull(response)) {
                return;
            }
            //  防止同一用户连续提交任务
            userLock =
                    redisTemplate.opsForValue().setIfAbsent(RedisKeys.ASSETS_DOWNLOAD_LOCK + "_" + SecurityUtils.getUserId(), 0, Duration.ofHours(1));
            if (BooleanUtils.isTrue(userLock)) {
                // 防止同时有三个批量导出任务
                downLock = increaseAssetsDownLock(RedisKeys.ASSETS_DOWNLOAD_LOCK);
                List<Long> assetsIds = request.getAssetsIds();

                // 这里要先过滤是否有异常信息
                List<UserDataAssets> dataAssetsList = userDataAssetsDAO.listByIds(assetsIds);
                if (CollectionUtils.isEmpty(dataAssetsList)) {
                    throw new CustomException("下载的资产不存在");
                } else {
                    List<String> errMsg = new ArrayList<>();
                    for (UserDataAssets dataAssets : dataAssetsList) {
                        if (!Objects.equals(dataAssets.getFtpStatus(), FtpStatus.SUCCESS.name())) {
                            errMsg.add("Excel文件生成中或生成失败: " + dataAssets.getId() + " " + dataAssets.getProjectName() + "" + dataAssets.getFtpStatus());
                        } else if (Strings.isNullOrEmpty(dataAssets.getFtpPath())) {
                            errMsg.add("ftp路径为空: " + dataAssets.getId() + " " + dataAssets.getProjectName() + "" + dataAssets.getFtpStatus());
                        }
                    }
                    if (!CollectionUtils.isEmpty(errMsg)) {
                        throw new CustomException(String.join(",", errMsg));
                    }
                }

                // 获取目录树
                AssetsDirRequest dirRequest = new AssetsDirRequest();
                dirRequest.setExpireType("normal");
                List<AssetsNode> data = userDataAssetsService.queryUserAssetsTree(dirRequest);
                Map<Long, String> pathMap = new HashMap<>();
                buildAssetsPathMap(data, "\\", pathMap);

                String fileName = UUID.randomUUID() + ".zip";
                String responseFileName = "资产批量下载-" + DateUtils.compactDate() + ".zip";
                file = new File(fileName);
                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(file.toPath()))) {
                    for (UserDataAssets dataAssets : dataAssetsList) {
                        // 保存下载记录
                        DeliverResourceType deliverResourceType = "csv".equals(FileUtil.getSuffix(dataAssets.getFtpPath()))
                                ? DeliverResourceType.CSV : DeliverResourceType.EXCEL;
                        DeliverPackBaseReq req = new DeliverPackBaseReq();
                        req.setAssetsId(dataAssets.getId());
                        req.setPack(false);
                        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(req, deliverResourceType);
                        dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
                        FtpClient ftpClient = FtpClientFactory.getInstance();
                        ftpClient.open();
                        try (InputStream inputStream = ftpClient.downloadFile(dataAssets.getFtpPath());
                             BufferedInputStream bis = new BufferedInputStream(inputStream)) {
//                        try (InputStream inputStream = ftpClient.downloadFile(dataAssets.getFtpPath())) {
                            // 组装路径
                            String name = pathMap.get(dataAssets.getId());
                            String ftpFileName = dataAssets.getFtpFileName();
                            String replace = ftpFileName.replaceAll("\\\\", "").replaceAll("/", "");
                            String path = name.replace(dataAssets.getProjectName(), replace);
                            path = path.replaceFirst("\\\\", "");

                            zos.putNextEntry(new ZipEntry(path));
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = bis.read(buffer)) != -1) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();
                        } catch (Exception e) {
                            log.error("ftp下载异常", e);
                        } finally {
                            try {
                                ftpClient.close();
                            } catch (Exception ignored) {

                            }
                        }
                    }
                }
                HttpServletResponseUtil.settingZip(responseFileName, (int) file.length(), response);
                outputStream = response.getOutputStream();
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                }
            } else {
                throw new CustomException("每位用户不能同时执行多个批量下载任务");
            }
        } catch (Exception e) {
            log.error("数据资产下载失败:", e);
        } finally {
            if (Objects.nonNull(userLock)) {
                redisTemplate.delete(RedisKeys.ASSETS_DOWNLOAD_LOCK + "_" + SecurityUtils.getUserId());
            }
            if (Objects.nonNull(downLock)) {
                decreaseAssetsDownLock(RedisKeys.ASSETS_DOWNLOAD_LOCK);
            }
            if (Objects.nonNull(outputStream)) {
                IoUtil.close(outputStream);
            }
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    @ApiOperation("数据资产批量下载")
    @GetMapping("/download/dataAssets")
    public void downloadDataAssetsGet(AssetsDataDownloadGetRequest request) {
        Boolean userLock = null;
        Boolean downLock = null;
        ServletOutputStream outputStream = null;
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        File file = null;
        try {
            if (Objects.isNull(response)) {
                return;
            }
            //  防止同一用户连续提交任务
            userLock =
                    redisTemplate.opsForValue().setIfAbsent(RedisKeys.ASSETS_DOWNLOAD_LOCK + "_" + SecurityUtils.getUserId(), 0, Duration.ofHours(1));
            if (BooleanUtils.isTrue(userLock)) {
                // 防止同时有三个批量导出任务
                downLock = increaseAssetsDownLock(RedisKeys.ASSETS_DOWNLOAD_LOCK);
                List<Long> assetsIds = Arrays.stream(request.getAssetsIds().split(",")).map(Long::new).collect(Collectors.toList());

                // 这里要先过滤是否有异常信息
                List<UserDataAssets> dataAssetsList = userDataAssetsDAO.listByIds(assetsIds);
                if (CollectionUtils.isEmpty(dataAssetsList)) {
                    throw new CustomException("下载的资产不存在");
                } else {
                    List<String> errMsg = new ArrayList<>();
                    for (UserDataAssets dataAssets : dataAssetsList) {
                        if (!Objects.equals(dataAssets.getFtpStatus(), FtpStatus.SUCCESS.name())) {
                            errMsg.add("Excel文件生成中或生成失败: " + dataAssets.getId() + " " + dataAssets.getProjectName() + "" + dataAssets.getFtpStatus());
                        } else if (Strings.isNullOrEmpty(dataAssets.getFtpPath())) {
                            errMsg.add("ftp路径为空: " + dataAssets.getId() + " " + dataAssets.getProjectName() + "" + dataAssets.getFtpStatus());
                        }
                    }
                    if (!CollectionUtils.isEmpty(errMsg)) {
                        throw new CustomException(String.join(",", errMsg));
                    }
                }

                // 获取目录树
                AssetsDirRequest dirRequest = new AssetsDirRequest();
                dirRequest.setExpireType("normal");
                List<AssetsNode> data = userDataAssetsService.queryUserAssetsTree(dirRequest);
                Map<Long, String> pathMap = new HashMap<>();
                buildAssetsPathMap(data, "\\", pathMap);

                String fileName = UUID.randomUUID() + ".zip";
                String responseFileName = "资产批量下载-" + DateUtils.compactDate() + ".zip";
                file = new File(fileName);
                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(file.toPath()))) {
                    for (UserDataAssets dataAssets : dataAssetsList) {
                        // 保存下载记录
                        DeliverResourceType deliverResourceType = "csv".equals(FileUtil.getSuffix(dataAssets.getFtpPath()))
                                ? DeliverResourceType.CSV : DeliverResourceType.EXCEL;
                        DeliverPackBaseReq req = new DeliverPackBaseReq();
                        req.setAssetsId(dataAssets.getId());
                        req.setPack(false);
                        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(req, deliverResourceType);
                        dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
                        FtpClient ftpClient = FtpClientFactory.getInstance();
                        ftpClient.open();
                        try (InputStream inputStream = ftpClient.downloadFile(dataAssets.getFtpPath());
                             BufferedInputStream bis = new BufferedInputStream(inputStream)) {
//                        try (InputStream inputStream = ftpClient.downloadFile(dataAssets.getFtpPath())) {
                            // 组装路径
                            String name = pathMap.get(dataAssets.getId());
                            String path = name.replace(dataAssets.getProjectName(), dataAssets.getFtpFileName());
                            path = path.replaceFirst("\\\\", "");

                            zos.putNextEntry(new ZipEntry(path));
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = bis.read(buffer)) != -1) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();
                        } catch (Exception e) {
                            log.error("ftp下载异常", e);
                        } finally {
                            try {
                                ftpClient.close();
                            } catch (Exception ignored) {

                            }
                        }
                    }
                }
                HttpServletResponseUtil.settingZip(responseFileName, (int) file.length(), response);
                outputStream = response.getOutputStream();
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                }
            } else {
                throw new CustomException("每位用户不能同时执行多个批量下载任务");
            }
        } catch (Exception e) {
            log.error("数据资产下载失败:", e);
        } finally {
            if (Objects.nonNull(userLock)) {
                redisTemplate.delete(RedisKeys.ASSETS_DOWNLOAD_LOCK + "_" + SecurityUtils.getUserId());
            }
            if (Objects.nonNull(downLock)) {
                decreaseAssetsDownLock(RedisKeys.ASSETS_DOWNLOAD_LOCK);
            }
            if (Objects.nonNull(outputStream)) {
                IoUtil.close(outputStream);
            }
            if (Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 申请人手动触发 资产更新
     */
    @GetMapping("/manualUpgrade/{id}")
    public AjaxResult<Void> manualUpgrade(@PathVariable("id") Long dataAssetsId) {
        return assetsUpgradeTriggerService.manualUpgrade(dataAssetsId);
    }

    @GetMapping("/manualDeprecated/{id}")
    public AjaxResult<Void> manualDeprecated(@PathVariable("id") Long dataAssetsId) {
        return userDataAssetsService.manualDeprecated(dataAssetsId, null);
    }

    @GetMapping("/manualDeprecatedByApply/{id}")
    public AjaxResult<Void> manualDeprecatedByApply(@PathVariable("id") Long applyId) {
        return userDataAssetsService.manualDeprecatedByApply(applyId);
    }

    /**
     * 递增资产下载锁
     *
     * @param key redis key
     * @return 是否成功
     */
    private synchronized boolean increaseAssetsDownLock(String key) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, 0L, 1, TimeUnit.DAYS);
        }
        Object o = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(o)) {
            Number number = (Number) o;
            if (number.longValue() >= fileProperties.getLockNum()) {
                throw new CustomException("任务队列已有3个批量下载任务，不可提交！请稍后再试。");
            }
            long l = number.longValue() + 1;
            redisTemplate.opsForValue().set(key, l, 1, TimeUnit.DAYS);
            return true;
        }
        return true;
    }

    /**
     * 获取对应key的数值
     *
     * @param key redis key
     * @return 对应数值
     */
    private synchronized void decreaseAssetsDownLock(String key) {
        if (redisTemplate.hasKey(key)) {
            Object o = redisTemplate.opsForValue().get(key);
            if (Objects.nonNull(o)) {
                Number number = (Number) o;
                long l = number.longValue() - 1;
                if (Objects.equals(l, 0L)) {
                    redisTemplate.delete(key);
                } else {
                    redisTemplate.opsForValue().set(key, l, 1, TimeUnit.DAYS);
                }
            }
        }
    }

    /**
     * 获取文件输入流对应字节码
     *
     * @param inputStream 输入流
     * @return 字节码数组
     * @throws IOException
     */
    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
//        int availableSize = inputStream.available();
//        byte[] byteArray = new byte[availableSize];
//        int bytesRead = inputStream.read(byteArray);
//        if (bytesRead != availableSize) {
//            throw new IOException("InputStream size and available size do not match.");
//        }
//        return byteArray;

        byte[] buffer = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            byte[] b = new byte[4096];
            int n;
            while ((n = inputStream.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            inputStream.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    private void buildAssetsPathMap(List<AssetsNode> data, String parentPath, Map<Long, String> pathMap) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        for (AssetsNode node : data) {
            if (Objects.equals(node.getIcon(), ApplicationConst.AssetsIcon.DATA) || Objects.equals(node.getIcon(),
                    ApplicationConst.AssetsIcon.FILE)) {
                pathMap.put(node.getBizId(), parentPath + node.getName());
            }
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                buildAssetsPathMap(node.getChildren(), parentPath + node.getName() + "\\", pathMap);
            }
        }
    }
}
