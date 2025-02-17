package com.sinohealth.system.biz.dataassets.service.impl;

import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dataassets.AssetsCompareTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.sca.base.basic.util.DateUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.common.FileAdapter;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareFileDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareSelectDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompareFile;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompareSelect;
import com.sinohealth.system.biz.dataassets.domain.CompareFile;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.AssetsCompareFilePageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsCompareLastSelectDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsComparePageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsComparePlanPageDTO;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareDownloadRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareFilePageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareFileRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareInvokeRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsComparePageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsComparePlanRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsPlanSaveRequest;
import com.sinohealth.system.biz.dataassets.helper.AssetsCompareInvoker;
import com.sinohealth.system.biz.dataassets.listener.AssetsCompareResultListener;
import com.sinohealth.system.biz.dataassets.listener.AssetsFileCompareListener;
import com.sinohealth.system.biz.dataassets.mapper.AssetsCompareMapper;
import com.sinohealth.system.biz.dataassets.service.AssetsCompareService;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.dao.ProjectHelperDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.mapper.ProjectDataAssetsRelateMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.util.EasyExcelUtil;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-21 11:37
 */
@Slf4j
@Service
public class AssetsCompareServiceImpl implements AssetsCompareService {

    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Autowired
    private AssetsCompareMapper assetsCompareMapper;
    @Autowired
    private AssetsCompareDAO assetsCompareDAO;
    @Autowired
    private TableInfoSnapshotDAO tableInfoSnapshotDAO;
    @Autowired
    private ProjectHelperDAO projectHelperDAO;
    @Autowired
    private ProjectDataAssetsRelateMapper projectAssetsMapper;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private AssetsCompareSelectDAO assetsCompareSelectDAO;
    @Autowired
    private AssetsCompareFileDAO assetsCompareFileDAO;

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private FileAdapter fileAdapter;

    @Autowired
    private ISysUserService userService;
    @Autowired
    private AssetsCompareInvoker assetsCompareInvoker;
    @Autowired
    private CKClusterAdapter ckClusterAdapter;
    @Autowired
    private TgCkProviderMapper ckProviderMapper;

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;

    private final Set<String> supportSuffix = Sets.newHashSet("csv", "xls", "xlsx");

    @Override
    public AjaxResult<List<String>> queryAllProdCode() {
        List<LinkedHashMap<String, Object>> data = ckProviderMapper
                .selectAllDataFromCk("SELECT DISTINCT prodcode FROM " + appProperties.getCascadeTable() + " ORDER BY prodcode");

        return AjaxResult.success(data.stream()
                .map(v -> v.get("prodcode")).filter(Objects::nonNull).map(Object::toString)
                .collect(Collectors.toList()));
    }

    private Map<String, String> queryAllProdCodeSort() {
        Map<String, String> result = new HashMap<>();
        List<LinkedHashMap<String, Object>> data = ckProviderMapper
                .selectAllDataFromCk("SELECT DISTINCT prodcode,sort1 FROM " + appProperties.getCascadeTable() + " ORDER BY prodcode");

        data.forEach(v -> {
            Object prodcode = v.get("prodcode");
            Object sort1 = v.get("sort1");

            result.put(prodcode.toString(), sort1.toString());
        });
        return result;
    }

    @Override
    public AjaxResult<IPage<AssetsComparePageDTO>> pageQueryCompare(AssetsComparePageRequest request) {
        Long userId = SecurityUtils.getUserId();

        if (Objects.equals(request.getCreateType(), AssetsCompareTypeEnum.manual.name())) {
            Set<Long> projectIds = projectHelperDAO.queryProjects(userId);
            if (CollectionUtils.isEmpty(projectIds)) {
                return AjaxResult.success(new Page<>());
            }
            request.setProjectIds(projectIds);
        } else {
            request.setProjectIds(Collections.emptySet());
        }
        if (Objects.nonNull(request.getEndTime())) {
            request.setEndTime(DateUtils.addDay(request.getEndTime(), 1));
        }
        IPage<AssetsComparePageDTO> resultPage = assetsCompareMapper.pageQueryCompare(request.buildPage(), request);
        List<AssetsComparePageDTO> records = resultPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            Set<Long> userIds = Lambda.buildSet(records, v -> {
                try {
                    String creator = v.getCreator();
                    return Long.parseLong(creator);
                } catch (Exception e) {
                    log.error("", e);
                    return null;
                }
            }, Objects::nonNull);

            Map<Long, String> userMap = userService.selectUserNameMapByIds(userIds);
            for (AssetsComparePageDTO record : records) {
                String creator = record.getCreator();
                if (Objects.equals(creator, "0")) {
                    record.setCreator("系统");
                    continue;
                }

                try {
                    String userName = userMap.get(Long.parseLong(creator));
                    record.setCreator(userName);
                } catch (Exception e) {
                    record.setCreator("系统");
                }
            }
        }
        return AjaxResult.success(resultPage);
    }

    @Override
    public AjaxResult<IPage<AssetsCompareFilePageDTO>> pageQueryCompareFile(AssetsCompareFilePageRequest request) {
        LambdaQueryChainWrapper<AssetsCompareFile> wrapper = assetsCompareFileDAO.lambdaQuery()
                .and(StringUtils.isNotBlank(request.getSearchName()),
                        v -> v.like(AssetsCompareFile::getNewFileName, request.getSearchName())
                                .or().like(AssetsCompareFile::getOldFileName, request.getSearchName())
                )
                .like(StringUtils.isNotBlank(request.getPeriod()), AssetsCompareFile::getDataPeriod, request.getPeriod())
                .eq(StringUtils.isNotBlank(request.getState()), AssetsCompareFile::getState, request.getState())
                .ge(Objects.nonNull(request.getStartTime()), AssetsCompareFile::getCreateTime, request.getStartTime())
                .eq(AssetsCompareFile::getDeleted, false)
                .le(Objects.nonNull(request.getEndTime()), AssetsCompareFile::getCreateTime,
                        Optional.ofNullable(request.getEndTime()).map(v -> DateUtils.addDay(v, 1)).orElse(null));
        if (CollectionUtils.isNotEmpty(request.getProdCode())) {
            wrapper.and(w -> {
                w.apply(" 1 = 2");
                List<String> codes = request.getProdCode();
                for (String code : codes) {
                    w.or().like(AssetsCompareFile::getProdCode, code);
                }
            });
        }

        wrapper.orderByDesc(AssetsCompareFile::getCreateTime);
        IPage<AssetsCompareFile> pageResult = wrapper.page(request.buildPage());

        List<AssetsCompareFile> records = pageResult.getRecords();
        Set<Long> userIds = Lambda.buildSet(records, AssetsCompareFile::getCreator);
        Map<Long, String> userMap = userService.selectUserNameMapByIds(userIds);

        return AjaxResult.success(PageUtil.convertMap(pageResult, v -> {
            AssetsCompareFilePageDTO dto = new AssetsCompareFilePageDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setCreator(userMap.get(v.getCreator()));
            return dto;
        }));
    }

    @Override
    public AjaxResult<IPage<AssetsComparePlanPageDTO>> pageQueryPlan(AssetsComparePlanRequest request) {
        if (StringUtils.isNotBlank(request.getProjectName())) {
            request.setProjectName(request.getProjectName().trim());
        }
        Set<Long> bizTempIds;
        if (StringUtils.isNotBlank(request.getBizType())) {
            List<TgTemplateInfo> info = templateInfoDAO.lambdaQuery().eq(TgTemplateInfo::getBizType, request.getBizType())
                    .select(TgTemplateInfo::getId)
                    .list();
            bizTempIds = Lambda.buildSet(info);
        } else {
            bizTempIds = Collections.emptySet();
        }

        List<Project> searchPros = projectDAO.lambdaQuery()
                .select(Project::getId)
                .like(StringUtils.isNotBlank(request.getProjectName()), Project::getName, request.getProjectName())
                .list();

        Set<Long> searchProIds = Lambda.buildSet(searchPros);
        List<ProjectDataAssetsRelate> relateList = Lambda.queryListIfExist(searchProIds, v -> projectAssetsMapper.selectList(
                new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                        .in(ProjectDataAssetsRelate::getProjectId, v)));
        Set<Long> proAssetsIds = Lambda.buildSet(relateList, ProjectDataAssetsRelate::getUserAssetId, Objects::nonNull);

        LambdaQueryChainWrapper<UserDataAssets> wrapper = userDataAssetsDAO.lambdaQuery()
                .and(StringUtils.isNotBlank(request.getProjectName()), v ->
                        v.like(StringUtils.isNotBlank(request.getProjectName()), UserDataAssets::getProjectName, request.getProjectName())
                                .or().in(CollectionUtils.isNotEmpty(proAssetsIds), UserDataAssets::getId, proAssetsIds))
                .eq(Objects.nonNull(request.getTemplateId()), UserDataAssets::getTemplateId, request.getTemplateId())
                .in(CollectionUtils.isNotEmpty(bizTempIds), UserDataAssets::getTemplateId, bizTempIds)
                .eq(UserDataAssets::getApplicantId, SecurityUtils.getUserId());
        userDataAssetsDAO.fillValid(wrapper);

        IPage<UserDataAssets> pageResult = wrapper.page(request.buildPage());
        List<UserDataAssets> records = pageResult.getRecords();
        Set<Long> assetsIds = Lambda.buildSet(records);
        Map<Long, Long> projectMap = Lambda.queryMapIfExist(assetsIds, v ->
                        projectAssetsMapper.selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                                .in(ProjectDataAssetsRelate::getUserAssetId, v)),
                ProjectDataAssetsRelate::getUserAssetId, ProjectDataAssetsRelate::getProjectId);
        Map<Long, String> proNameMap = Lambda.queryMapIfExist(projectMap.values(), projectDAO::listByIds,
                Project::getId, Project::getName);

        List<Long> tempIds = Lambda.buildList(records, UserDataAssets::getTemplateId);
        Map<Long, TgTemplateInfo> tempMap = Lambda.queryMapIfExist(tempIds, templateInfoDAO.getBaseMapper()::selectBatchIds, TgTemplateInfo::getId);

        return AjaxResult.success(PageUtil.convertMap(pageResult, v -> {
            AssetsComparePlanPageDTO dto = new AssetsComparePlanPageDTO();
            TgTemplateInfo template = tempMap.get(v.getTemplateId());
            dto.setId(v.getId());
            dto.setProjectName(v.getProjectName());
            dto.setNewProjectName(proNameMap.get(projectMap.get(v.getId())));
            dto.setPlanCompare(v.getPlanCompare());

            if (Objects.nonNull(template)) {
                dto.setBizType(template.getBizType());
                dto.setTemplateName(template.getTemplateName());
            }
            return dto;
        }));
    }

    @Override
    public AjaxResult<Void> savePlan(AssetsPlanSaveRequest request) {
        log.info("plan: ids={}", request.getIds());
        userDataAssetsDAO.lambdaUpdate()
                .set(UserDataAssets::getPlanCompare, false)
                .notIn(CollectionUtils.isNotEmpty(request.getIds()), UserDataAssets::getId, request.getIds())
                .update();

        if (CollectionUtils.isNotEmpty(request.getIds())) {
            userDataAssetsDAO.lambdaUpdate()
                    .set(UserDataAssets::getPlanCompare, true)
                    .in(UserDataAssets::getId, request.getIds())
                    .update();
        }

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteFileCompare(Long id) {
        AssetsCompareFile file = assetsCompareFileDAO.getById(id);
        if (Objects.isNull(file)) {
            return AjaxResult.error("不存在的数据对比");
        }
        if (!Objects.equals(file.getState(), AssetsUpgradeStateEnum.failed.name())) {
            return AjaxResult.error("仅支持删除失败的数据对比");
        }
        if (BooleanUtils.isTrue(file.getDeleted())) {
            return AjaxResult.error("数据对比已删除");
        }

        List<String> files = Stream.of(file.getOldPath(), file.getNewPath(), file.getResultPath())
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(files)) {
            try (FtpClient ftp = FtpClientFactory.getInstance()) {
                ftp.open();
                for (String path : files) {
                    ftp.delete(path);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }

        assetsCompareFileDAO.lambdaUpdate()
                .set(AssetsCompareFile::getDeleted, true)
                .eq(AssetsCompareFile::getId, id)
                .update();
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteCompare(Long id) {
        AssetsCompare assetsCompare = assetsCompareMapper.selectById(id);
        if (Objects.isNull(assetsCompare)) {
            return AjaxResult.error("不存在的版本对比");
        }
        if (!Objects.equals(assetsCompare.getCreateType(), AssetsCompareTypeEnum.manual.name())) {
            return AjaxResult.error("仅支持删除自定义版本对比");
        }

        if (StringUtils.isNotBlank(assetsCompare.getResultPath())) {
            try (FtpClient ftp = FtpClientFactory.getInstance()) {
                ftp.open();
                ftp.delete(assetsCompare.getResultPath());
            } catch (Exception e) {
                log.error("", e);
            }
        }

        assetsCompareMapper.update(null, new UpdateWrapper<AssetsCompare>()
                .lambda()
                .set(AssetsCompare::getDeleted, true)
                .eq(AssetsCompare::getId, id)
        );
        return AjaxResult.succeed();
    }

    @Override
    public void download(String ids, String type, HttpServletResponse response) {
        if (StringUtils.isBlank(ids)) {
            return;
        }
        FtpClient ftpClient = null;
        try {
            List<Long> idList = Stream.of(StringUtils.split(ids, ","))
                    .map(Long::parseLong).collect(Collectors.toList());

            List<? extends CompareFile> list;
            if (Objects.equals(type, AssetsCompareTypeEnum.file.name())) {
                list = assetsCompareFileDAO.lambdaQuery()
                        .in(AssetsCompareFile::getId, idList)
                        .eq(AssetsCompareFile::getState, AssetsUpgradeStateEnum.success.name())
                        .list();
            } else {
                list = assetsCompareMapper.selectList(new QueryWrapper<AssetsCompare>().lambda()
                        .in(AssetsCompare::getId, idList)
                        .and(v -> v.eq(AssetsCompare::getDeleted, false).or().isNull(AssetsCompare::getDeleted))
                        .eq(AssetsCompare::getState, AssetsUpgradeStateEnum.success.name())
                );
            }

            if (CollectionUtils.isEmpty(list)) {
                log.warn("no file: ids={}", ids);
                return;
            }

            List<String> pathList = null;
            String path = null;
            if (CollectionUtils.size(list) == 1) {
                path = list.get(0).getResultPath();
            } else {
                pathList = Lambda.buildList(list, CompareFile::getResultPath);
            }

            ftpClient = FtpClientFactory.getInstance();
            ftpClient.open();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + System.currentTimeMillis() + "-sop.xlsx\"");
            if (Objects.nonNull(path)) {
                ftpClient.downloadFile(path, response.getOutputStream());
            } else {
                DiskFile mergeFile = this.mergeFile(ftpClient, Collections.emptyList(), Collections.emptyMap(), pathList);
                sendFileToResponse(response, mergeFile);
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void mergeDownloadFile(List<String> pathList, List<String> prodCodeList, HttpServletResponse response) {
        FtpClient ftpClient = null;
        try {
            ftpClient = FtpClientFactory.getInstance();
            ftpClient.open();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + System.currentTimeMillis() + "-sop.xlsx\"");

            Map<String, String> prodMap = this.queryAllProdCodeSort();
            DiskFile mergeFile = this.mergeFile(ftpClient, prodCodeList, prodMap, pathList);

            sendFileToResponse(response, mergeFile);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    @Override
    public AjaxResult<Long> preview(AssetsCompareDownloadRequest request) {
        if (CollectionUtils.isEmpty(request.getAutoIds()) && CollectionUtils.isEmpty(request.getHandleIds())) {
            return AjaxResult.success(0L);
        }

        List<Long> idList = Stream.of(request.getAutoIds(), request.getHandleIds())
                .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return AjaxResult.success(0L);
        }

        List<AssetsCompare> list = assetsCompareDAO.lambdaQuery()
                .select(AssetsCompare::getAssetsId)
                .in(AssetsCompare::getId, idList)
                .list();

        return AjaxResult.success(list.stream().map(AssetsCompare::getAssetsId).distinct().count());
    }

    @Override
    public AjaxResult<AssetsCompareLastSelectDTO> lastSelect(Long userId) {
        Optional<AssetsCompareSelect> confOpt = assetsCompareSelectDAO.lambdaQuery()
                .eq(AssetsCompareSelect::getCreator, userId)
                .oneOpt();
        if (!confOpt.isPresent()) {
            return AjaxResult.success(AssetsCompareLastSelectDTO.empty);
        }

        AssetsCompareSelect select = confOpt.get();
        String manualAssetsId = select.getManualAssetsId();
        String autoAssetsId = select.getAutoAssetsId();

        List<Long> autoIds = this.parseCompareId(autoAssetsId, AssetsCompareTypeEnum.auto);
        List<Long> handIds = this.parseCompareId(manualAssetsId, AssetsCompareTypeEnum.manual);
        return AjaxResult.success(new AssetsCompareLastSelectDTO(autoIds, handIds));
    }

    private List<Long> parseCompareId(String assetsIds, AssetsCompareTypeEnum type) {
        List<Long> assetsList = Arrays.stream(assetsIds.split(","))
                .filter(StringUtils::isNoneBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(assetsList)) {
            return Collections.emptyList();
        }

        List<AssetsCompare> list = assetsCompareDAO.lambdaQuery()
                .select(AssetsCompare::getId, AssetsCompare::getAssetsId, AssetsCompare::getCurVersion,
                        AssetsCompare::getCreateTime)
                .eq(AssetsCompare::getCreateType, type.name())
                .in(AssetsCompare::getAssetsId, assetsList)
                .eq(AssetsCompare::getState, AssetsUpgradeStateEnum.success.name())
                .list();

        Map<Long, List<AssetsCompare>> assetsMap = list.stream().collect(Collectors.groupingBy(AssetsCompare::getAssetsId));
        List<Long> result = new ArrayList<>();
        List<Long> noMap = new ArrayList<>();

        for (Map.Entry<Long, List<AssetsCompare>> entry : assetsMap.entrySet()) {
            List<AssetsCompare> value = entry.getValue();
            // 最新版本 最新时间的对比记录
            Optional<AssetsCompare> maxCompare = value.stream()
                    .max(Comparator.comparing(AssetsCompare::getCurVersion).thenComparing(AssetsCompare::getCreateTime));
            if (maxCompare.isPresent()) {
                result.add(maxCompare.get().getId());
            } else {
                noMap.add(entry.getKey());
            }
        }
        if (CollectionUtils.isNotEmpty(noMap)) {
            log.warn("noMapCompare: assetsIds={}", noMap);
        }
        return result;
    }

    /**
     * @see AssetsCompareServiceImpl#download
     */
    @Override
    public void mergeDownload(AssetsCompareDownloadRequest request, HttpServletResponse response) {
        Long userId = SecurityUtils.getUserId();
        if (BooleanUtils.isTrue(request.getSaveSelect())) {
            String ids = this.compareIdToAssetsId(request.getAutoIds());
            String hanIds = this.compareIdToAssetsId(request.getHandleIds());

            Optional<AssetsCompareSelect> confOpt = assetsCompareSelectDAO.lambdaQuery()
                    .eq(AssetsCompareSelect::getCreator, userId)
                    .oneOpt();
            if (confOpt.isPresent()) {
                assetsCompareSelectDAO.lambdaUpdate()
                        .set(AssetsCompareSelect::getAutoAssetsId, ids)
                        .set(AssetsCompareSelect::getManualAssetsId, hanIds)
                        .eq(AssetsCompareSelect::getId, confOpt.get().getId())
                        .update();
            } else {
                AssetsCompareSelect select = new AssetsCompareSelect()
                        .setCreator(userId).setAutoAssetsId(ids).setManualAssetsId(hanIds);
                assetsCompareSelectDAO.save(select);
            }
        }

        List<String> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getFileIds())) {
            List<AssetsCompareFile> fileList = assetsCompareFileDAO.lambdaQuery()
                    .in(AssetsCompareFile::getId, request.getFileIds())
                    .eq(AssetsCompareFile::getState, AssetsUpgradeStateEnum.success.name())
                    .list();
            fileList.stream().map(AssetsCompareFile::getResultPath).forEach(list::add);
        }

        List<Long> mergeIds = Stream.of(request.getHandleIds(), request.getAutoIds())
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(mergeIds)) {
            List<AssetsCompare> assetsList = assetsCompareMapper.selectList(new QueryWrapper<AssetsCompare>().lambda()
                    .in(AssetsCompare::getId, mergeIds)
                    .and(v -> v.eq(AssetsCompare::getDeleted, false).or().isNull(AssetsCompare::getDeleted))
                    .eq(AssetsCompare::getState, AssetsUpgradeStateEnum.success.name())
            );
            assetsList.stream().map(AssetsCompare::getResultPath).forEach(list::add);
        }

        if (CollectionUtils.isEmpty(list)) {
            log.warn("no result file: request={}", request);
            return;
        }
        this.mergeDownloadFile(list, request.getProdCodeList(), response);
    }

    private String compareIdToAssetsId(List<Long> idList) {
        List<AssetsCompare> list = Lambda.queryListIfExist(idList, v -> assetsCompareDAO.lambdaQuery()
                .select(AssetsCompare::getAssetsId)
                .in(AssetsCompare::getId, v)
                .list());
        return list.stream().map(AssetsCompare::getAssetsId).distinct()
                .map(v -> v + "").collect(Collectors.joining(","));
    }

    private DiskFile mergeFile(FtpClient ftpClient, List<String> prodCodeList,
                               Map<String, String> prodMap, List<String> list) {
        DiskFile mergeFile = null;
        ExcelWriter excelWriter = null;
        List<DiskFile> allFiles = new ArrayList<>(list.size());
        try {
            int cnt = 0;
            // 下载所有Excel
            for (String path : list) {
                try {
                    cnt++;
                    String fileName = cnt + "-" + StrUtil.randomAlpha(3) + ".xlsx";
                    DiskFile partFile = DiskFile.createTmpFile(fileName);
                    allFiles.add(partFile);
                    FileOutputStream partOut = new FileOutputStream(partFile.getFile());
                    ftpClient.downloadFile(path, partOut);
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            mergeFile = DiskFile.createTmpFile("merge-" + StrUtil.randomAlpha(6) + ".xlsx");
            FileOutputStream fileOutputStream = new FileOutputStream(mergeFile.getFile());
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(fileOutputStream);
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

            Set<String> sort1List = Optional.ofNullable(prodCodeList).map(v -> v.stream().map(prodMap::get)
                    .collect(Collectors.toSet())).orElse(Collections.emptySet());
            // 合并逻辑
            for (DiskFile file : allFiles) {
                EasyExcel.read(file.getFile(), new AssetsCompareResultListener(excelWriter, sort1List)).doReadAll();
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(excelWriter)) {
                excelWriter.finish();
            }
            if (CollectionUtils.isNotEmpty(allFiles)) {
                for (DiskFile allFile : allFiles) {
                    allFile.destroy();
                }
            }
        }
        return mergeFile;
    }

    private static void sendFileToResponse(HttpServletResponse response, DiskFile diskFile) throws IOException {
        if (Objects.isNull(diskFile)) {
            return;
        }
        OutputStream outputStream = null;
        FileInputStream inputStream = null;
        try {
            outputStream = response.getOutputStream();
            inputStream = new FileInputStream(diskFile.getFile());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(inputStream)) {
                inputStream.close();
            }
            if (Objects.nonNull(outputStream)) {
                outputStream.close();
            }
            diskFile.destroy();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> createCompare(Long assetsId, Integer preVersion, Integer curVersion, boolean invoke) {
        List<AssetsCompare> exist = assetsCompareMapper.selectList(new QueryWrapper<AssetsCompare>().lambda()
                .eq(AssetsCompare::getAssetsId, assetsId)
                .eq(AssetsCompare::getPreVersion, preVersion)
                .eq(AssetsCompare::getCurVersion, curVersion)
        );
        if (CollectionUtils.isNotEmpty(exist)) {
            return AjaxResult.error("当前对比任务已存在，请勿重复新建");
        }

        UserDataAssets assets = userDataAssetsDAO.getById(assetsId);
        if (Objects.isNull(assets)) {
            return AjaxResult.error("资产不存在");
        }
        if (Objects.nonNull(assets.getCopyFromId())) {
            return AjaxResult.error("另存类型资产不允许创建对比任务");
        }

        UserDataAssets preAssets = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, preVersion);
        UserDataAssets curAssets;
        if (Objects.equals(curVersion, assets.getVersion())) {
            curAssets = assets;
        } else {
            curAssets = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, curVersion);
        }
        if (!preAssets.hasValidFtp()) {
            return AjaxResult.error("历史版本Excel尚未上传完，请稍后再试");
        }
        if (invoke && !curAssets.hasValidFtp()) {
            return AjaxResult.error("当前版本Excel尚未上传完，请稍后再试");
        }

        if (ApplicationConfigTypeConstant.isFile(preAssets.getConfigType())
                || ApplicationConfigTypeConstant.isFile(curAssets.getConfigType())) {
            return AjaxResult.error("该资产存在文件交付版本，不支持数据比对");
        }

        AssetsCompare compare = new AssetsCompare()
                .setBaseTableId(assets.getBaseTableId())
                .setAssetsId(assetsId)
                .setCurVersion(curVersion)
                .setPreVersion(preVersion)
                .setCreateType(AssetsCompareTypeEnum.manual.name())
                .setState(AssetsUpgradeStateEnum.wait.name())
                .setCreator(assets.getCreator())
                .setUpdater(assets.getCreator());
        List<TableInfoSnapshot> snapTablePair = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, assets.getBaseTableId())
                .in(TableInfoSnapshot::getVersion, Arrays.asList(curAssets.getBaseVersion(), preAssets.getBaseVersion()))
                .list();
        snapTablePair.stream().filter(v -> Objects.isNull(v.getVersionPeriod())).forEach(v -> v.setVersionPeriod(""));
        Map<Integer, String> periodMap = Lambda.buildMap(snapTablePair, TableInfoSnapshot::getVersion, TableInfoSnapshot::getVersionPeriod);
        compare.setPreVersionPeriod(AssetsCompare.buildVersionPeriod(preVersion, periodMap.get(preAssets.getBaseVersion())));
        compare.setCurVersionPeriod(AssetsCompare.buildVersionPeriod(curVersion, periodMap.get(curAssets.getBaseVersion())));
        assetsCompareMapper.insert(compare);

        if (invoke) {
            // 立刻触发对比， 因为选中的资产版本肯定都已经转换成了Excel
            AssetsCompareInvokeRequest req = AssetsCompareInvokeRequest.builder()
                    .compareId(compare.getId())
                    .assetsId(assetsId)
                    .projectName(assets.getProjectName())
                    .oldPath(preAssets.getFtpPath())
                    .newPath(curAssets.getFtpPath())
                    .callbackUrl(appProperties.getAssetsCompareSelfUrl())
                    .build();
            assetsCompareInvoker.invokeCompareReq(req);
        }
        return AjaxResult.succeed();
    }


    private String rename(FtpClient ftpClient, String path, Long userId) {
        String targetPath = path.replace("/compf/tmp/", "/compf/" + userId + "/");

        ftpClient.rename(path, targetPath);
        return targetPath;
    }


    @Override
    public AjaxResult<FileAssetsUploadDTO> uploadTmpFile(MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (StringUtils.isBlank(originalFilename)) {
            return AjaxResult.error("文件名为空");
        }

        String suffix = FileUtil.getSuffix(originalFilename);
        if (StringUtils.isBlank(suffix) || !supportSuffix.contains(suffix)) {
            return AjaxResult.error("仅支持Excel和CSV文件");
        }
        String remote = fileAdapter.uploadTempCompareFile(file, suffix);
        FileAssetsUploadDTO dto = new FileAssetsUploadDTO();
        dto.setName(originalFilename);
        dto.setPath(remote);
        return AjaxResult.success(dto);
    }

    /**
     * 性能优化成本大，直接废弃，交由下游调用的Python去解析和校验
     */
    @Deprecated
    private Optional<String> fillFieldAndCheck(AssetsCompareFile file, String name, String path, FtpClient ftpClient) {
        DiskFile partFile = null;
        try {
            String fileName = "up-" + StrUtil.randomAlpha(6) + ".xlsx";
            partFile = DiskFile.createTmpFile(fileName);
            FileOutputStream partOut = new FileOutputStream(partFile.getFile());
            ftpClient.downloadFile(path, partOut);

//            String srcName = file.getNewFileName().replace("." + FileUtil.getSuffix(file.getNewFileName()), "");
//            file.setProjectName(srcName);

            EasyExcel.read(partFile.getFile(), new AssetsFileCompareListener(file, name)).doReadAll();
        } catch (Exception e) {
            log.error("", e);
            return Optional.ofNullable(e.getMessage());
        } finally {
            Optional.ofNullable(partFile).ifPresent(DiskFile::destroy);
        }
        return Optional.empty();
    }

    /**
     * 用户选择文件进行对比
     */
    @Override
    public AjaxResult<Void> createFileCompare(AssetsCompareFileRequest request) {
        List<FileAssetsUploadDTO> newFiles = request.getNewFiles();
        List<FileAssetsUploadDTO> oldFiles = request.getOldFiles();
        if (CollectionUtils.isEmpty(newFiles) || CollectionUtils.isEmpty(oldFiles)) {
            return AjaxResult.error("新版或旧版 文件未选择");
        }
        if (newFiles.size() != oldFiles.size()) {
            return AjaxResult.error("请上传对应的新版和旧版文件");
        }

        FtpClient ftpClient = null;
        try {
            Long userId = SecurityUtils.getUserId();
            ftpClient = FtpClientFactory.getInstance();

            ftpClient.open();

            List<AssetsCompareFile> files = new ArrayList<>();
//            List<String> msg = new ArrayList<>();
            for (int i = 0; i < newFiles.size(); i++) {
                FileAssetsUploadDTO newDTO = newFiles.get(i);
                FileAssetsUploadDTO oldDTO = oldFiles.get(i);

                AssetsCompareFile file = new AssetsCompareFile();
                file.setNewFileName(newDTO.getName());
                file.setOldFileName(oldDTO.getName());

                // Java做数据校验和提取 很慢，Excel会有200-300M
//                Optional<String> newMsgOpt = this.fillFieldAndCheck(file, newDTO.getName(), newDTO.getPath(), ftpClient);
//                Optional<String> oldMsgOpt = this.fillFieldAndCheck(null, oldDTO.getName(), oldDTO.getPath(), ftpClient);

//                if (!newMsgOpt.isPresent()) {
//                    String newPath = this.rename(ftpClient, newDTO.getPath(), userId);
//                    file.setNewPath(newPath);
//                } else {
//                    msg.add(newMsgOpt.get());
//                }
//                if (!oldMsgOpt.isPresent()) {
//                    String oldPath = this.rename(ftpClient, oldDTO.getPath(), userId);
//                    file.setOldPath(oldPath);
//                } else {
//                    msg.add(oldMsgOpt.get());
//                }

                // 搬目录，正式创建对比
                String newPath = this.rename(ftpClient, newDTO.getPath(), userId);
                file.setNewPath(newPath);
                String oldPath = this.rename(ftpClient, oldDTO.getPath(), userId);
                file.setOldPath(oldPath);

                file.setCreator(userId);
                file.setState(AssetsUpgradeStateEnum.wait.name());

                files.add(file);
            }
//            if (CollectionUtils.isNotEmpty(msg)) {
//                return AjaxResult.error(String.join("; ", msg));
//            }

            assetsCompareFileDAO.saveBatch(files);
            this.asyncInvokeCompare(files);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            if (ftpClient != null) {
                try {
                    ftpClient.close();
                } catch (Exception ignored) {
                }
            }
        }

        return AjaxResult.succeed();
    }

    @Override
    public String queryFileRunLog(Long id) {
        return Optional.ofNullable(id)
                .flatMap(v -> assetsCompareFileDAO.lambdaQuery().eq(AssetsCompareFile::getId, v).oneOpt())
                .map(AssetsCompareFile::getRunLog).orElse("");
    }

    @Override
    public List<AssetsCompare> queryByBizIds(List<Long> bizIds) {
        if (CollectionUtils.isEmpty(bizIds)) {
            return Collections.emptyList();
        }
        return assetsCompareDAO.lambdaQuery().in(AssetsCompare::getBizId, bizIds).list();
    }

    private void asyncInvokeCompare(List<AssetsCompareFile> files) {
        // 等待是为了等保存的事务提交 避免Py端执行完对比后，回调时找不到数据
        scheduler.schedule(() -> assetsCompareInvoker.invokeFileCompare(files),
                10, TimeUnit.SECONDS);
    }

}
