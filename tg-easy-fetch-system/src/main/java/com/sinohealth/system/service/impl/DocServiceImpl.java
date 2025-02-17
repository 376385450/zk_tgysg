package com.sinohealth.system.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.pdf.BizType;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.saas.file.model.dto.request.CreateRequestDTO;
import com.sinohealth.saas.file.model.dto.response.CreateResponseDTO;
import com.sinohealth.sca.base.model.dto.response.PlatformResponse;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.dir.service.AssetsSortService;
import com.sinohealth.system.biz.dir.vo.DataDirListVO;
import com.sinohealth.system.biz.doc.dto.DocInfoPageDto;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.dao.DataDirDAO;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgUploadedFileDim;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgDocInfoMapper;
import com.sinohealth.system.mapper.TgUploadedFileDimMapper;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author Rudpolh
 */
@Service
@Slf4j
public class DocServiceImpl implements IDocService, InitializingBean {

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private OfficeRepository officeRepository;
    @Autowired
    private FileApi fileApi;
    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private IDataDirService dataDirService;
    @Autowired
    private AssetsSortService assetsSortService;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private TgUploadedFileDimMapper uploadedFileDimMapper;

    @Autowired
    private TgDocInfoMapper tgDocInfoMapper;
    @Autowired
    private DataDirDAO dataDirDAO;

    // 单实例一个id
    private String lockOwner = UUID.randomUUID().toString();

    private final BlockingQueue<Long> taskQueue = new LinkedBlockingQueue<>();


    @Override
    public AjaxResult<?> query(Map<String, Object> params) {

        if (params == null) {
            return selectList(null);
        }

        AjaxResult<TgDocInfo> one = selectOne(params);
        if (one != null) {
            return one;
        }

        QueryWrapper<TgDocInfo> qw = prepareQueryWrapper(params, new QueryWrapper<>());
        AjaxResult<Page<DocInfoPageDto>> page = selectPage(params, qw);
        if (page != null) {
            return page;
        }

        return selectList(qw);
    }

    /**
     * @see com.sinohealth.web.controller.system.TemplateApiController#download 下载
     */
    @Override
    public AjaxResult<FileAssetsUploadDTO> uploadGetPath(@RequestParam("file") MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String[] splitFileName = originalFilename.split("\\.");
        if (splitFileName.length < CommonConstants.FILENAME_NORMAL_PART_SIZE) {
            return AjaxResult.error("文件名异常");
        }

        // 文件上传
        CompletableFuture<PlatformResponse<CreateResponseDTO>> uploadFuture = CompletableFuture.supplyAsync(() -> {
            CreateRequestDTO request = new CreateRequestDTO();
            request.setUploadPath(fileProperties.getObsPrefix());
            request.setPolicyRead(true);
            request.setEncodeFileName(true);
            request.setStorageConfigCode(fileProperties.getFileStorageCode());
            PlatformResponse<CreateResponseDTO> uploadResult = fileApi.createFile(file, JSON.toJSONString(request));
            log.info("uploadResult={}", uploadResult);
            return uploadResult;
        });
        try {
            PlatformResponse<CreateResponseDTO> uploadResult = uploadFuture.get();
            CreateResponseDTO rsp = uploadResult.getResult();

            return AjaxResult.success("", new FileAssetsUploadDTO().setPath(rsp.getSourcePath()).setName(originalFilename));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    private AjaxResult<List<TgDocInfo>> selectList(QueryWrapper<TgDocInfo> qw) {
        List<TgDocInfo> tgDocInfos = TgDocInfo.newInstance().selectList(qw);
        List<Long> userIds = tgDocInfos.stream().map(TgDocInfo::getOwnerId).distinct().collect(Collectors.toList());
        List<SysUser> userList = sysUserService.selectUserByIds(userIds);
        Map<Long, String> nameMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId,
                v -> sysUserService.getUserViewName(Collections.singleton(v)), (front, current) -> current));
        for (TgDocInfo tgDocInfo : tgDocInfos) {
            tgDocInfo.setOwnerName(nameMap.get(tgDocInfo.getOwnerId()));
            JsonBeanConverter.convert2Obj(tgDocInfo);
        }
        return AjaxResult.success(tgDocInfos);
    }

    private AjaxResult<Page<DocInfoPageDto>> selectPage(Map<String, Object> params, QueryWrapper<TgDocInfo> qw) {
        if (params.containsKey(CommonConstants.PAGENUM) && params.containsKey(CommonConstants.PAGESIZE)) {
            Page<TgDocInfo> page = TgDocInfo.newInstance().selectPage(new Page<>(TypeUtils.castToLong(params.get("pageNum")),
                    TypeUtils.castToLong(params.get("pageSize"))), qw);

            List<TgDocInfo> handleList = page.getRecords().stream().peek(d -> {
                if (d.getDirId() != null) {
                    DataDir dataDir = DataDir.newInstance().selectById(d.getDirId());
                    if (dataDir != null) {
                        d.setDirName(StringUtils.isNotBlank(dataDir.getDirName()) ? dataDir.getDirName() : "");
                    }
                }
                if (d.getOwnerId() != null) {
                    d.setOwnerName(sysUserService.getUserViewName(d.getOwnerId()));
                }
                JsonBeanConverter.convert2Obj(d);
            }).collect(Collectors.toList());
            page.setRecords(handleList);
            List<Long> dirIds = Lambda.buildList(handleList, TgDocInfo::getDirId);
            Map<Long, String> nameMap = dataDirDAO.queryParentMap(dirIds);
            Page<DocInfoPageDto> newPage = (Page<DocInfoPageDto>) PageUtil.convertMap(page, v -> {
                DocInfoPageDto dto = new DocInfoPageDto();
                BeanUtils.copyProperties(v, dto);
                dto.setBusinessType(nameMap.get(v.getDirId()));
                return dto;
            });
            return AjaxResult.success(newPage);
        }
        return null;
    }

    private AjaxResult<TgDocInfo> selectOne(Map<String, Object> params) {
        if (params.containsKey(CommonConstants.ID)) {
            Long id = TypeUtils.castToLong(params.get(CommonConstants.ID));
            TgDocInfo tgDocInfo = JsonBeanConverter.convert2Obj(TgDocInfo.newInstance().selectById(id));
            tgDocInfo.setOwnerName(sysUserService.getUserViewName(tgDocInfo.getOwnerId()));
            return AjaxResult.success(tgDocInfo);
        }
        return null;
    }


    @Override
    public AjaxResult<?> createOrUpdate(TgDocInfo docInfo) {
        if (Objects.isNull(docInfo.getDisSort()) || docInfo.getDisSort() == 0) {
            assetsSortService.fillDefaultDisSort(docInfo);
        }
        Pair<AjaxResult, TgUploadedFileDim> pair = SpringUtils.getBean(DocServiceImpl.class).doCreateOrUpdate(docInfo);


        AjaxResult<?> result = pair.getKey();
        if (result.isSuccess()) {
            officeRepository.transformPdfAsync(pair.getValue().getFilePath(), BizType.DOC_CREATE, Long.toString(pair.getValue().getId()));
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Pair<AjaxResult, TgUploadedFileDim> doCreateOrUpdate(TgDocInfo docInfo) {
        AjaxResult<Object> error = isParamsAllowed(docInfo);
        if (error != null) {
            return new Pair<>(error, null);
        }

//        // 更新操作需要做权限校验
//        error = isOperationAllowed(docInfo, "非管理员或文档所属人无法更新文档");
//        if (error != null) {
//            return new Pair<>(error, null);
//        }

        SysUser user = ThreadContextHolder.getSysUser();
        String userViewName = sysUserService.getUserViewName(user.getUserId());

        createInfoInit(docInfo, user, userViewName);
        updateInfoInit(docInfo, userViewName);

        JsonBeanConverter.convert2Json(docInfo);
        AjaxResult<?> result = insertOrUpdate(docInfo);

        setParentDir(docInfo);

        if (!result.isSuccess()) {
            return new Pair<>(result, null);
        }
        TgUploadedFileDim fileDim = insertVersion(docInfo);
        return new Pair<>(result, fileDim);
    }

    private void setParentDir(TgDocInfo docInfo) {
        if (null != docInfo.getDirId()) {
            DataDir dataDir = DataDir.newInstance().selectOne(new QueryWrapper<DataDir>()
                    .eq("icon", CommonConstants.ICON_DOC)
                    .eq("node_id", docInfo.getId()));
            if (null != dataDir) {
                dataDir.setParentId(docInfo.getDirId());
                dataDir.updateById();
            }
        }
    }

    private AjaxResult<Object> isOperationAllowed(TgDocInfo docInfo, String alterMessage) {
        if (StringUtils.isNotBlank(docInfo.getCreateTime())) {
            TgDocInfo oldDocInfo = docInfo.selectById();
            if (!oldDocInfo.getOwnerId().equals(ThreadContextHolder.getSysUser().getUserId())
                    && ThreadContextHolder.getSysUser().getRoles().stream().noneMatch(r -> r.getRoleName().contains(CommonConstants.ADMIN))) {
                return AjaxResult.error(alterMessage);
            }
        }
        return null;
    }

    private TgUploadedFileDim insertVersion(TgDocInfo docInfo) {
        TgUploadedFileDim tgUploadedFileDim = TgUploadedFileDim.newInstance();
        tgUploadedFileDim.setFileName(docInfo.getName());
        tgUploadedFileDim.setFileType(docInfo.getType());
        tgUploadedFileDim.setFilePath(docInfo.getPath());
        tgUploadedFileDim.setPdfPath(docInfo.getPdfPath());
        tgUploadedFileDim.setUploadTime(DateUtils.getTime());
        tgUploadedFileDim.setMappingId(docInfo.getId());
        tgUploadedFileDim.setMappingType(ApplicationConst.ApplicationType.DOC_APPLICATION);
        tgUploadedFileDim.insert();
        return tgUploadedFileDim;
    }

    @Override
    public AjaxResult<?> delete(Long id) {
        TgDocInfo tgDocInfo = TgDocInfo.newInstance().selectById(id);

        AjaxResult error = isOperationAllowed(tgDocInfo, "非管理员或文档所属人无法删除文档");
        if (error != null) {
            return error;
        }

        boolean delete = TgDocInfo.newInstance().deleteById(id);
        if (!delete) {
            log.warn("该文档或已被删除，未找到对应文档进行删除: {}", id);
            return AjaxResult.error("该文档或已被删除，未找到对应文档进行删除", id);
        }

        // 删除对应的提数申请
//        applicationInfoMapper.delete(new QueryWrapper<TgApplicationInfo>().lambda().eq(TgApplicationInfo::getDocId, id));

        // 删除对应的目录节点
        DataDir dataDir = dataDirService.getByNodeId(id);
        if (Objects.nonNull(dataDir)) {
            dataDirService.delete(dataDir.getId());
        }

        log.info("文档删除成功: {}", tgDocInfo);
        return AjaxResult.success("文档删除成功", tgDocInfo);
    }

    @Override
    public List<TgDocInfo> queryByDocIds(List<Long> docIds) {
        QueryWrapper qw = new QueryWrapper() {{
            in("id", docIds);
        }};
        return TgDocInfo.newInstance().selectList(qw);
    }

    @Override
    public TgDocInfo getById(Long relatedId) {
        return TgDocInfo.newInstance().selectById(relatedId);
    }

    @Override
    public List<TgDocInfo> getUnLinkedData(List<Long> fileAssetIds) {
        return TgDocInfo.newInstance().selectList(new QueryWrapper<TgDocInfo>() {{
            if (!fileAssetIds.isEmpty()) {
                notIn("id", fileAssetIds);
            }
        }});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePdfPath(String bizId, String pdfPath) {

        Wrapper<TgUploadedFileDim> updateWrapper = Wrappers.<TgUploadedFileDim>lambdaUpdate()
                .set(TgUploadedFileDim::getLockOwner_, null)
                .set(TgUploadedFileDim::getLockExpTime_, null)
                .set(StringUtils.isNotEmpty(pdfPath), TgUploadedFileDim::getPdfPath, pdfPath)
                .eq(TgUploadedFileDim::getId, bizId);

        uploadedFileDimMapper.update(null, updateWrapper);

        final TgUploadedFileDim tgUploadedFileDim = uploadedFileDimMapper.selectById(bizId);
        final Long mappingId = tgUploadedFileDim.getMappingId();
        final TgDocInfo tgDocInfo = tgDocInfoMapper.selectById(mappingId);
        tgDocInfo.setPdfPath(pdfPath);
        tgDocInfoMapper.updateById(tgDocInfo);

    }

    private <T> QueryWrapper<T> prepareQueryWrapper(Map<String, Object> params, QueryWrapper<T> qw) {
        Long dirId = Optional.ofNullable(params.get("dirId")).map(Object::toString).filter(StringUtils::isNotBlank).map(Long::parseLong).orElse(null);
        List<Long> dirIds = null;
        if (Objects.nonNull(dirId) && dirId != 0L) {
            DataDirListVO dirs = dataDirService.selectSonOfParentDir(dirId, DataDirConst.Status.ENABLE);
            dirIds = dirs.getDirs().stream().map(DataDir::getId).collect(Collectors.toList());
            dirIds.add(dirId);
            dirId = null;
        }

        if (params.containsKey(CommonConstants.SEARCH_CONTENT) && StringUtils.isNotBlank(params.get(CommonConstants.SEARCH_CONTENT).toString())) {
            String searchContent = TypeUtils.castToString(params.get(CommonConstants.SEARCH_CONTENT));
            qw.and(v -> v.like("name", searchContent).or().like("type", searchContent));
        }
        qw.eq(Objects.nonNull(dirId), "dir_id", dirId)
                .in(CollectionUtils.isNotEmpty(dirIds), "dir_id", dirIds)
                .isNotNull("path");
        if (!SecurityUtils.isAdmin()) {
            qw.eq("owner_id", SecurityUtils.getUserId());
        }
        String sorting = (String) params.get("orderSort");
        String orderField = (String) params.get("orderField");
        if (!Objects.equals(orderField, "update_time") && !Objects.equals(orderField, "dis_sort")) {
            orderField = "update_time";
        }
        if (com.sinohealth.common.utils.StringUtils.isBlank(sorting)) {
            params.put("orderSort", "desc");
        }

        boolean asc = Objects.equals(params.get("orderSort"), "ASC");
        qw.orderBy(true, asc, orderField);
        return qw;
    }

    private AjaxResult<?> insertOrUpdate(TgDocInfo docInfo) {
        if (!docInfo.insertOrUpdate()) {
            log.error("文档对象新增或修改失败: {}", docInfo);
            return AjaxResult.error("文档对象新增或修改失败");
        }

        log.info("文档对象新增或修改成功: {}", docInfo);
        return AjaxResult.success("文档对象新增或修改成功", docInfo);
    }

    private AjaxResult<Object> isParamsAllowed(TgDocInfo docInfo) {
        if (!docInfo.getIsInit() && StringUtils.isAnyBlank(docInfo.getName(), docInfo.getPath(), docInfo.getType())) {
            log.error("参数错误：缺少文件参数：{}", docInfo);
            return AjaxResult.error("请先上传再保存");
        }

        if (docInfo.getNeed2Audit() && !docInfo.getIsInit() &&
                ObjectUtils.isNull(docInfo.getProcessId())) {
            log.error("未绑定审核流程信息: {}", docInfo);
            return AjaxResult.error("未绑定审核流程信息");
        }

        return null;
    }

    private void updateInfoInit(TgDocInfo docInfo, String userViewName) {
        docInfo.setUpdater(userViewName);
        docInfo.setUpdateTime(DateUtils.getTime());
    }

    private void createInfoInit(TgDocInfo docInfo, SysUser user, String userViewName) {
        if (ObjectUtils.isNull(docInfo.getOwnerId())) {
            docInfo.setOwnerId(user.getUserId());
            docInfo.setCreator(userViewName);
        }
        docInfo.setCreateTime(DateUtils.getTime());
    }

    /**
     * 异步转换为PDF文件
     *
     * @param dim
     */
    public void asyncConvert2PDF(TgUploadedFileDim dim) {
        if (dim == null || StringUtils.isBlank(dim.getFilePath()) || StringUtils.isNotBlank(dim.getPdfPath())) {
            return;
        }
        try {
            log.info("异步转换为PDF文件>>, dim: {}", JSON.toJSONString(dim));
            File file = new File(dim.getFilePath());
            // 先下载文件
            byte[] fileBytes = fileApi.get(fileProperties.getFileStorageCode(), Objects.requireNonNull(dim).getFilePath());
            FileItem fileItem = new DiskFileItem("file", Files.probeContentType(file.toPath()), false, FileUtil.getName(dim.getFilePath()), fileBytes.length, file.getParentFile());
            OutputStream outputStream = fileItem.getOutputStream();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            IoUtil.copy(inputStream, outputStream);
            MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
            String pdfPath = officeRepository.transformPdf(multipartFile);
            dim.setPdfPath(pdfPath);
        } catch (Exception e) {
            dim.setPdfExcepts(e.toString());
            log.error("异步转换为PDF文件异常: ", e);
        }
        dim.updateById();
    }

    /**
     * 检测pdfPath为空的文档，进行重试
     */
    //@Scheduled(fixedRate = 15 * 60 * 1000, initialDelay = 1000)
    public void retry() {
        List<TgUploadedFileDim> tasks = uploadedFileDimMapper.listTasks(3);
        for (TgUploadedFileDim task : tasks) {
            task.setRetryTimes(task.getRetryTimes() == null ? 1 : task.getRetryTimes() + 1);
            task.setLockOwner_(lockOwner);
            // 锁超时时间10分钟
            task.setLockExpTime_(Date.from(LocalDateTime.now().plusMinutes(10L).atZone(ZoneId.systemDefault()).toInstant()));
            task.insertOrUpdate();
            taskQueue.offer(task.getId());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Long taskId = taskQueue.poll(1, TimeUnit.SECONDS);
                    if (taskId == null) {
                        continue;
                    }
                    TgUploadedFileDim tgUploadedFileDim = uploadedFileDimMapper.selectById(taskId);
                    boolean exclusive = (StringUtils.isNotBlank(tgUploadedFileDim.getLockOwner_()) && !StringUtils.equals(tgUploadedFileDim.getLockOwner_(), lockOwner))
                            || (tgUploadedFileDim.getLockExpTime_() != null && new java.util.Date().compareTo(tgUploadedFileDim.getLockExpTime_()) < 0);
                    if (exclusive) {
                        continue;
                    }
                    asyncConvert2PDF(tgUploadedFileDim);
                    boolean success = StringUtils.isNotBlank(tgUploadedFileDim.getPdfPath());
                    tgUploadedFileDim.setLockExpTime_(null);
                    tgUploadedFileDim.setLockOwner_(null);
                    Wrapper<TgUploadedFileDim> updateWrapper = Wrappers.<TgUploadedFileDim>lambdaUpdate()
                            .set(TgUploadedFileDim::getLockOwner_, null)
                            .set(TgUploadedFileDim::getLockExpTime_, null)
                            .set(success, TgUploadedFileDim::getPdfExcepts, null)
                            .eq(TgUploadedFileDim::getId, taskId);
                    uploadedFileDimMapper.update(null, updateWrapper);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, "convertPdfJobThread");
        thread.start();*/
    }
}
