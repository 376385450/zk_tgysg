package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.BeanUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.common.utils.uuid.IdUtils;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDetailDto;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
import com.sinohealth.system.biz.application.dto.TemplatePageDto;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.dto.request.FieldListRequest;
import com.sinohealth.system.biz.dict.service.FieldDictService;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dir.service.AssetsSortService;
import com.sinohealth.system.biz.dir.vo.DataDirListVO;
import com.sinohealth.system.biz.project.dao.ProjectHelperDAO;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.biz.template.dto.PowerPushBiTemplateVO;
import com.sinohealth.system.dao.DataDirDAO;
import com.sinohealth.system.domain.CustomFieldInfo;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ColsInfoDto;
import com.sinohealth.system.dto.application.MetricsInfoDto;
import com.sinohealth.system.dto.application.RealName;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.IAuditProcessService;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITemplatePackTailSettingService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-05-13 14:09
 * @Desc
 */
@Log4j2
@Service
public class TemplateServiceImpl implements ITemplateService {

    @Autowired
    TgTemplateInfoMapper mapper;
    @Autowired
    TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    CustomFieldInfoMapper customFieldInfoMapper;
    @Autowired
    TgCkProviderMapper tgCkProviderMapper;
    @Autowired
    IAuditProcessService auditProcessService;
    @Autowired
    ITableInfoService tableInfoService;
    @Autowired
    ITableFieldInfoService tableFieldInfoService;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private TgAssetInfoMapper assetInfoMapper;
    @Autowired
    private TgAuditProcessInfoMapper processInfoMapper;
    //    @Autowired
//    private TemplateSnapshotDAO templateSnapshotDAO;
    @Autowired
    private DataDirDAO dataDirDAO;
    @Autowired
    private ProjectHelperDAO projectHelperDAO;
    @Autowired
    private ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;

    @Autowired
    private Validator validator;
    @Autowired
    private IDataDirService dataDirService;
    @Autowired
    private AssetsSortService assetsSortService;
    @Autowired
    private ITemplatePackTailSettingService templatePackTailSettingService;

    @Autowired
    private FieldDictService fieldDictService;

    @Autowired
    private AppProperties appProperties;
//    @Autowired
//    private IAssetService assetService;

    private static AjaxResult<Void> checkTemplate(TgTemplateInfo param) {
        List<TemplateGranularityDto> granularity = param.getGranularity();
        if (CollectionUtils.isNotEmpty(granularity)) {
            for (TemplateGranularityDto dto : granularity) {
                if (CollectionUtils.isEmpty(dto.getDetails())) {
                    continue;
                }
                Optional<String> existEmpty = dto.getDetails().stream()
                        .filter(v -> CollectionUtils.isEmpty(v.getOptions()) && CollectionUtils.isEmpty(v.getRequired()))
                        .map(v -> FieldGranularityEnum.getDesc(dto.getGranularity())
                                + "信息下的【" + v.getName() + "】粒度，请至少选择一个粒度字段或标签字段 ")
                        .findFirst();
                if (existEmpty.isPresent()) {
                    return AjaxResult.error(existEmpty.get());
                }
            }
        }

        return null;
    }

    private static AjaxResult<Void> checkTemplateForWideTable(TgTemplateInfo param) {
        if (!Objects.equals(param.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            return null;
        }

        List<TemplateGranularityDto> granularity = param.getGranularity();
        if (CollectionUtils.isNotEmpty(granularity)) {
            Map<String, TemplateGranularityDto> map = granularity.stream()
                    .collect(Collectors.toMap(TemplateGranularityDto::getGranularity, v -> v, (front, current) -> current));
            TemplateGranularityDto dto = map.get(FieldGranularityEnum.time.name());
            boolean limitField = CollectionUtils.isNotEmpty(param.getApplicationPeriodField());
            if (limitField && (Objects.isNull(dto) || CollectionUtils.isEmpty(dto.getDetails()))) {
                return AjaxResult.error("设置了时间分组的情况下，需要输入至少一个时间粒度");
            }

            if (Objects.nonNull(dto) && CollectionUtils.isNotEmpty(dto.getDetails())) {
                for (TemplateGranularityDetailDto detail : dto.getDetails()) {
                    if (!CommonConstants.TIME_GRANULARITY.contains(detail.getName())) {
                        return AjaxResult.error("请输入有效的时间粒度名称：" +
                                String.join(",", CommonConstants.TIME_GRANULARITY) + " 存在无效的：" + detail.getName());
                    }
                }
            }
        }

        List<ColsInfoDto> colsInfo = param.getColsInfo();
        if (CollectionUtils.isNotEmpty(colsInfo)) {
            List<List<Long>> idList = colsInfo.stream()
                    .map(ColsInfoDto::getRealName).filter(CollectionUtils::isNotEmpty).map(v -> v.stream()
                            .map(RealName::getRelationColId).filter(Objects::nonNull).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            int totalSize = idList.stream().map(List::size).mapToInt(Integer::intValue).sum();
            Set<Long> diffIds = idList.stream().flatMap(Collection::stream).collect(Collectors.toSet());
            if (diffIds.size() != totalSize) {
                return AjaxResult.error("关联表字段与主表字段存在重复的字段库映射，请修正后提交，谢谢。");
            }
        }
        return null;
    }

    private static AjaxResult<Void> checkTemplateForFlow(TgTemplateInfo param) {
//        if (Objects.equals(param.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
//            return null;
//        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<TgTemplateInfo> upsertTemplate(TgTemplateInfo param, Boolean confirmUpgrade) {
        TgTemplateInfo templateInfo = JsonBeanConverter.convert2Json(param);
        boolean isSingleLayer = Objects.equals(ApplicationConst.SQL_MODE_SINGLE, templateInfo.getSqlBuildMode());
//        if (isSingleLayer) {
//            if (Objects.equals(templateInfo.getColAttr(), 1L)) {
//                return AjaxResult.error("自定义维度不支持单层SQL");
//            }
//        }

        // 公共校验
        AjaxResult<Void> checkResult = checkTemplate(param);
        if (checkResult != null) {
            return AjaxResult.error(checkResult.getMsg());
        }
        // 宽表模式 校验
        AjaxResult<Void> wideResult = checkTemplateForWideTable(param);
        if (wideResult != null) {
            return AjaxResult.error(wideResult.getMsg());
        }
        // 常规/通用模式 校验
        AjaxResult<Void> flowResult = checkTemplateForFlow(param);
        if (flowResult != null) {
            return AjaxResult.error(flowResult.getMsg());
        }

        // 指标校验
        List<MetricsInfoDto> metricsInfo = templateInfo.getMetricsInfo();
        if (CollectionUtils.isNotEmpty(metricsInfo)) {
            long count = metricsInfo.stream().map(MetricsInfoDto::getAliasName).distinct().count();
            if (count != metricsInfo.size()) {
                return AjaxResult.error("聚合指标 存在重名");
            }
            for (MetricsInfoDto metricsInfoDto : metricsInfo) {
                if (Objects.nonNull(metricsInfoDto.getConditions())) {
                    if (isSingleLayer) {
                        return AjaxResult.error("单层SQL不支持指标条件配置");
                    }
                }
                if (Objects.isNull(metricsInfoDto.getColName())
                        && metricsInfoDto.getComputeWay() < CommonConstants.ComputeWay.CUSTOM_START) {
                    return AjaxResult.error("聚合指标 未选择字段");
                }
                if (Objects.nonNull(metricsInfoDto.getConditions())
                        ^ StringUtils.isNoneBlank(metricsInfoDto.getContent())) {
                    return AjaxResult.error("聚合指标 条件配置不全");
                }
            }
        }

//        boolean needSaveSnapshot;
        if (Objects.nonNull(templateInfo.getId())) {
            TgTemplateInfo current = JsonBeanConverter.convert2Obj(mapper
                    .selectOne(new QueryWrapper<TgTemplateInfo>().lambda().eq(TgTemplateInfo::getId, param.getId())));

            // 不允许 有申请的模板 切换模板类型
            if (!Objects.equals(current.getTemplateType(), param.getTemplateType())) {
                Integer refCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>()
                        .lambda().eq(TgApplicationInfo::getTemplateId, templateInfo.getId()));
                if (Objects.nonNull(refCount) && refCount > 0) {
                    return AjaxResult.error("不允许更改已被申请使用的提数模板的类型");
                }
            }

//            List<String> diff = TemplateVersionUtil.needSaveSnapshot(current, templateInfo);
//            needSaveSnapshot = CollectionUtils.isNotEmpty(diff);
//            if (needSaveSnapshot && BooleanUtils.isNotTrue(confirmUpgrade)) {
//                String merge = diff.stream().map(v -> "【" + v + "】").collect(Collectors.joining("、"));
//                return AjaxResult.errorMsg(ErrorCode.TEMPLATE_UPGRADE_CONFIRM,
//                        "当前改动影响模板版本+1，影响的字段有：" + merge + "，是否确定提交？");
//            }
//            if (needSaveSnapshot) {
//                templateInfo.setVersion(current.getVersion() + 1);
//            }
//            return AjaxResult.error("diff: " + needSaveSnapshot);
        } else {
//            templateInfo.setVersion(1);
//            needSaveSnapshot = true;
        }

        // 预防同名模板
        if (this.alertSameName(templateInfo)) {
            return AjaxResult.error("该模板名称已被占用, 请重新命名");
        }

        // 规避同表多次join引起别名及字段映射错误
        boolean checkedRepeat = ApplicationSqlUtil.checkRepeatJoin(templateInfo.getJoinInfo());
        if (checkedRepeat) {
            return AjaxResult.error("暂不支持同名表多次关联");
        }

        this.rebuildTemplateInfo(templateInfo);

        try {
            this.fillSortIndex(param);

            if (Objects.isNull(templateInfo.getId())) {
                assetsSortService.fillDefaultDisSort(templateInfo);
            }
            log.info("upsert: id={} push={}", templateInfo.getId(), templateInfo.getPushFieldsJson());
            templateInfo.insertOrUpdate();
//            if (needSaveSnapshot) {
//                templateSnapshotDAO.saveNewVersion(templateInfo);
//            }
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(ApplicationConst.ErrorMsg.buildCkMsg(e));
        }

        return AjaxResult.success(templateInfo);
    }

    private void fillSortIndex(TgTemplateInfo param) {
        List<TemplateAuditProcessEasyDto> existInfos = this.queryByBaseTableId(param.getBaseTableId());
        Optional<Integer> maxSortIndexOpt = existInfos.stream().map(TemplateAuditProcessEasyDto::getSortIndex).max(Comparator.naturalOrder());
        Integer curIndex = maxSortIndexOpt.map(v -> v + 1).orElse(0);
        param.setSortIndex(curIndex);
    }

    /**
     * 一个字段不能同时出现在筛选范围和自定义指标中
     */
    private boolean alertConflict(TgTemplateInfo templateInfo) {
        FilterDTO dataRangeInfo = templateInfo.getDataRangeInfo();
        List<MetricsInfoDto> metricsInfo = templateInfo.getMetricsInfo();
        Set<Long> metricIds = metricsInfo.stream().map(MetricsInfoDto::getColName).collect(Collectors.toSet());

        return this.findAnySameField(dataRangeInfo, metricIds);
    }

    private boolean findAnySameField(FilterDTO dataRangeInfo, Set<Long> metricIds) {
        if (Objects.isNull(dataRangeInfo)) {
            return false;
        }
        Boolean current = Optional.of(dataRangeInfo)
                .map(FilterDTO::getFilterItem)
                .map(FilterDTO.FilterItemDTO::getFieldId).map(metricIds::contains).orElse(false);

        if (CollectionUtils.isNotEmpty(dataRangeInfo.getFilters())) {
            return current || dataRangeInfo.getFilters().stream().anyMatch(v -> this.findAnySameField(v, metricIds));
        }
        return false;
    }

    /**
     * 历史数据处理
     */
    private void rebuildTemplateInfo(TgTemplateInfo result) {
        SinoPassUserDTO orgUserInfo = (SinoPassUserDTO) ThreadContextHolder.getParams()
                .get(CommonConstants.ORG_USER_INFO);
        if (ObjectUtils.isNull(result.getId())) {
            result.setCreateTime(DateUtils.getTime());
            result.setCreator(orgUserInfo.getViewName());
        }
        result.setUpdater(orgUserInfo.getViewName());
        result.setUpdateTime(DateUtils.getTime());
        if (Objects.nonNull(result.getJoinInfo())) {
            result.getJoinInfo().forEach(j -> {
                TableInfoManageDto detail1 = tableInfoService.getDetail(j.getTableId1());
                j.setTableName1(detail1.getTableName());
                j.setTableDistributeName1(detail1.getTableNameDistributed());
                TableInfoManageDto detail2 = tableInfoService.getDetail(j.getTableId2());
                j.setTableName2(detail2.getTableName());
                j.setTableDistributeName2(detail2.getTableNameDistributed());
            });
        }
    }


    private boolean alertSameName(TgTemplateInfo templateInfo) {
        TgTemplateInfo template = templateInfo.selectOne(new QueryWrapper<TgTemplateInfo>() {{
            eq("template_name", templateInfo.getTemplateName());
        }});

        // 如果是新增, 并且存在同名记录
        if (ObjectUtils.isNull(templateInfo.getId()) && ObjectUtils.isNotNull(template)) {
            return true;
        }

        // 如果是修改, 存在同名记录, 且非自身
        return ObjectUtils.isNotNull(templateInfo.getId(), template) && !templateInfo.getId().equals(template.getId());
    }

    @Override
    public Object query(Map<String, Object> params) {
        TgTemplateInfo tgTemplateInfo = new TgTemplateInfo();

        Long dirId = Optional.ofNullable(params.get("dirId")).map(Object::toString).filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .map(Long::parseLong).orElse(null);
        if (Objects.nonNull(dirId) && dirId != 0L) {
            DataDirListVO dirs = dataDirService.selectSonOfParentDir(dirId, DataDirConst.Status.ENABLE);
            List<Long> dirIds = dirs.getDirs().stream().map(DataDir::getId).collect(Collectors.toList());
            dirIds.add(dirId);
            params.remove("dirId");
            params.put("dirIds", dirIds);
        }

        try {
            if (ObjectUtils.isNotNull(params.get(CommonConstants.ID))) {
                Long id = Long.parseLong(params.get(CommonConstants.ID).toString());
                // 查询历史版本
//                Object versionObj = params.get("version");

                TgTemplateInfo tempResult = JsonBeanConverter.convert2Obj(tgTemplateInfo.selectById(id));
                tempResult.setTailSettings(Optional.ofNullable(templatePackTailSettingService.findByTemplateId(id))
                        .orElse(new ArrayList<>()).stream().map(JsonBeanConverter::convert2Obj).collect(Collectors.toList()));
//                return tempResult;

                tempResult.setDistributedFields(distributedFieldList());
                TemplatePageDto dto = new TemplatePageDto();
                BeanUtils.copyProperties(tempResult, dto);

                Map<Long, TgAssetInfo> tgAssetInfoMap = Lambda.queryMapIfExist(Collections.singleton(id),
                        v -> TgAssetInfo.newInstance().selectList(new QueryWrapper<TgAssetInfo>() {{
                            select("id", "related_id");
                            eq("type", AssetType.MODEL);
                            in("related_id", v);
                        }}), TgAssetInfo::getRelatedId);

                Optional.ofNullable(tgAssetInfoMap.get(id)).map(TgAssetInfo::getId).ifPresent(dto::setNewAssetId);
                return dto;

//                Integer version = Optional.ofNullable(versionObj).map(Object::toString).map(Integer::parseInt).orElse(null);
//                if (Objects.nonNull(versionObj) && !Objects.equals(version, tempResult.getVersion())) {
//                    Optional<TemplateSnapshot> snapshot = templateSnapshotDAO.queryByVersion(id, version);
//                    if (!snapshot.isPresent()) {
//                        log.warn("id={} version={}", id, version);
//                        return AjaxResult.error("该版本的模板不存在");
//                    }
//                    TemplateSnapshot templateSnapshot = snapshot.get();
//                    return JsonBeanConverter.convert2Obj(templateSnapshot);
//                }
            }

            // Page 查询
            Integer pagenum = Integer.valueOf(ThreadContextHolder.getParams().get(CommonConstants.PAGENUM).toString());
            Integer pagesize = Integer.valueOf(ThreadContextHolder.getParams().get(CommonConstants.PAGESIZE).toString());
            if (ObjectUtils.isNotNull(pagenum, pagesize)) {
                PageHelper.startPage(pagenum, pagesize);

                String sorting = (String) params.get("orderSort");
                String orderField = (String) params.get("orderField");
                if (!Objects.equals(orderField, "update_time") && !Objects.equals(orderField, "dis_sort")) {
                    params.put("orderField", "update_time");
                }
                if (StringUtils.isBlank(sorting)) {
                    params.put("orderSort", "desc");
                }
                // 逻辑调整，其他入口已重构，只保留id和分页查询
                List<TgTemplateInfo> infos = mapper.queryTemplatePage(params);
                PageInfo<TgTemplateInfo> pageInfo = new PageInfo<>(infos);

                Map<Long, List<TgApplicationInfo>> appMap = Collections.emptyMap();
                List<Long> tempIds = infos.stream().map(TgTemplateInfo::getId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(tempIds)) {
                    List<TgApplicationInfo> applicationInfos = applicationInfoMapper.selectList(
                            new QueryWrapper<TgApplicationInfo>().lambda()
                                    .select(TgApplicationInfo::getTemplateId, TgApplicationInfo::getId)
                                    .in(TgApplicationInfo::getTemplateId, tempIds));
                    appMap = applicationInfos.stream().collect(Collectors.groupingBy(TgApplicationInfo::getTemplateId));
                }

                Set<Long> dirIds = new HashSet<>();
                for (TgTemplateInfo t : pageInfo.getList()) {
                    JsonBeanConverter.convert2Obj(t);
                    t.setJoinTables(t.getTableInfo());
                    t.setUsedTimes((long) CollectionUtils.size(appMap.get(t.getId())));
                    t.setTemplateType(TemplateTypeEnum.of(t.getTemplateType())
                            .map(TemplateTypeEnum::getDesc).orElse("模板"));
                    dirIds.add(t.getDirId());
                }

                Set<Long> relatedIds = pageInfo.getList().stream().map(TgTemplateInfo::getId).collect(Collectors.toSet());
                Map<Long, TgAssetInfo> tgAssetInfoMap = Lambda.queryMapIfExist(relatedIds,
                        v -> TgAssetInfo.newInstance().selectList(new QueryWrapper<TgAssetInfo>() {{
                            eq("type", AssetType.MODEL);
                            in("related_id", v);
                        }}), TgAssetInfo::getRelatedId);
//                Map<Long, TgAssetInfo> tgAssetInfoMap = TgAssetInfo.newInstance().selectList(new QueryWrapper<TgAssetInfo>() {{
//                    eq("type", AssetType.MODEL);
//                    in("related_id", relatedIds);
//                }}).stream().collect(Collectors.toMap(TgAssetInfo::getRelatedId, tgAssetInfo -> tgAssetInfo));

                Map<Long, String> busNameMap = dataDirDAO.queryParentMap(dirIds);
                return PageUtil.convert(pageInfo, v -> {
                    TemplatePageDto dto = new TemplatePageDto();
                    BeanUtils.copyProperties(v, dto);
                    dto.setBusinessType(busNameMap.get(v.getDirId()));
                    TgAssetInfo tgAssetInfo = tgAssetInfoMap.get(dto.getId());
                    if (Objects.nonNull(tgAssetInfo)) {
                        // 设置资产id
                        dto.setNewAssetId(tgAssetInfo.getId());
                        // 设置流程id
                        dto.setProcessId(tgAssetInfo.getProcessId());
                    }
                    return dto;
                });
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return "error usage";

    }

    @Override
    public List<TgTemplateInfo> queryNameByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<TgTemplateInfo> wrapper = new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                .in(TgTemplateInfo::getId, ids);
        return this.mapper.selectList(wrapper);
    }

    @Override
    public Object delete(Map<String, Object> params) {
        TgTemplateInfo tgTemplateInfo = new TgTemplateInfo();
        tgTemplateInfo.setId(Long.valueOf(String.valueOf(params.get(CommonConstants.ID))));
        tgTemplateInfo.deleteById();
        return "ok";
    }

    @Override
    public void updateProcessInfoById(Long templateId, Long processId, Integer sortIndex) {
        TgTemplateInfo tgTemplateInfo = new TgTemplateInfo();
        tgTemplateInfo.setId(templateId);
        tgTemplateInfo = tgTemplateInfo.selectById();
        tgTemplateInfo.setProcessId(processId);
        tgTemplateInfo.setSortIndex(sortIndex);
        tgTemplateInfo.updateById();
    }

    @Override
    public List<TemplateAuditProcessEasyDto> queryByBaseTableId(Long baseTableId) {
        if (Objects.isNull(baseTableId)) {
            return Collections.emptyList();
        }
        List<TgTemplateInfo> infos = mapper.selectList(new QueryWrapper<TgTemplateInfo>()
                .lambda().eq(TgTemplateInfo::getBaseTableId, baseTableId));

        List<Long> processIds = infos.stream().map(TgTemplateInfo::getProcessId).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(processIds)) {
            return Collections.emptyList();
        }
        List<TgAuditProcessInfo> processList = processInfoMapper.selectBatchIds(processIds);
        Map<Long, String> processMap = processList.stream().collect(Collectors.toMap(TgAuditProcessInfo::getProcessId,
                TgAuditProcessInfo::getProcessName, (front, current) -> current));

        return infos.stream().map(v -> {
            TemplateAuditProcessEasyDto dto = new TemplateAuditProcessEasyDto();
            dto.setBaseTableId(v.getBaseTableId());
            dto.setTemplateId(v.getId());
            dto.setTemplateName(v.getTemplateName());
            dto.setProcessId(v.getProcessId());
            dto.setProcessName(processMap.get(v.getProcessId()));
            dto.setSortIndex(v.getSortIndex());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TemplateAuditProcessEasyDto> queryProcessesByBaseTableId(Long baseTableId) {
        return mapper.queryProcessesByBaseTableId(baseTableId);
    }

    @Override
    public List<TemplateAuditProcessEasyDto> queryProcessesByBaseTableIds(List<Long> baseTableIds) {
        return mapper.queryProcessesByBaseTableIds(baseTableIds);
    }

    @Override
    public Object getTemplateFieldMeta(String page, List<Long> tableIds, Long templateId) {
        TgTemplateInfo tgTemplateInfo = new TgTemplateInfo();
        tgTemplateInfo = JsonBeanConverter.convert2Obj(tgTemplateInfo.selectById(templateId));
        if (tgTemplateInfo == null) {
            return ListUtil.empty();
        }
        long templateUuid = IdUtils.getUUID(CommonConstants.TEMPLATE + tgTemplateInfo.getCreateTime());
        List<TableFieldInfo> tableFields = tableFieldInfoService.findListByIds(tableIds);
        List<CustomFieldInfo> customFieldInfos = customFieldInfoMapper.selectCustomFields(templateUuid, CommonConstants.TEMPLATE);
        List<Object> result = new LinkedList<>();
        if (!"audit".equalsIgnoreCase(page)) {
            // 作为指标存在的列不能出现在维度里
            List<Long> metricsIds = tgTemplateInfo.getMetricsInfo().stream().map(MetricsInfoDto::getColName).collect(Collectors.toList());
            tableFields = tableFields.stream().filter(x -> !metricsIds.contains(x.getId())).collect(Collectors.toList());
        }

        result.addAll(tableFields);
        result.addAll(customFieldInfos);
        return result;
    }

    @Override
    public Object updateStatus(Long templateId) {
        // #TODO 废弃
        // 启用前提: 关联表单启用

        // 禁用前提: 无有效模板/无有效申请

        return AjaxResult.success();
    }

    @Override
    public List<TgTemplateInfo> queryByModelIds(List<Long> modelIds) {
        QueryWrapper qw = new QueryWrapper() {{
            in("id", modelIds);
        }};
        return TgTemplateInfo.newInstance().selectList(qw);
    }

    @Override
    public List<TgTemplateInfo> getUnLinkedData(List<Long> modelAssetIds) {
        return mapper.selectList(new QueryWrapper<TgTemplateInfo>() {{
            if (!modelAssetIds.isEmpty()) {
                notIn("id", modelAssetIds);
            }
        }});
    }

    @Override
    public List<PowerPushBiTemplateVO> queryForPushBi(String bizType) {
        List<TgTemplateInfo> templaList = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                .eq(TgTemplateInfo::getPushPowerBi, true)
                .eq(StringUtils.isNotBlank(bizType), TgTemplateInfo::getBizType, bizType)
                .list();
        if (CollectionUtils.isEmpty(templaList)) {
            return Collections.emptyList();
        }

        Set<Long> tempIds = Lambda.buildSet(templaList, TgTemplateInfo::getId);
        Map<Long, TgAssetInfo> usableMap = Lambda.queryMapIfExist(tempIds,
                v -> assetInfoMapper.selectList(new QueryWrapper<TgAssetInfo>().lambda()
                        .in(TgAssetInfo::getRelatedId, v).eq(TgAssetInfo::getShelfState, "已上架"))
                , TgAssetInfo::getRelatedId);

        return templaList.stream().filter(v -> usableMap.containsKey(v.getId())).map(v -> {
            PowerPushBiTemplateVO vo = new PowerPushBiTemplateVO();
            vo.setId(v.getId());
            vo.setTemplateName(v.getTemplateName());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PowerPushBiTemplateVO> queryNameList(String bizType) {
        Long userId = SecurityUtils.getUserId();
        Set<Long> projectIds = projectHelperDAO.queryProjects(userId);
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        List<ProjectDataAssetsRelate> relates = projectDataAssetsRelateMapper
                .selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                        .in(ProjectDataAssetsRelate::getProjectId, projectIds)
                        .isNotNull(ProjectDataAssetsRelate::getUserAssetId)
                );
        Map<Long, List<ProjectDataAssetsRelate>> assetsProjectMap = relates.stream()
                .collect(Collectors.groupingBy(ProjectDataAssetsRelate::getUserAssetId));
        if (MapUtils.isEmpty(assetsProjectMap)) {
            return Collections.emptyList();
        }
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getTemplateId)
                .in(UserDataAssets::getId, assetsProjectMap.keySet())
                .list();
        if (CollectionUtils.isEmpty(assets)) {
            return Collections.emptyList();
        }

        Set<Long> ids = Lambda.buildSet(assets, UserDataAssets::getTemplateId);
        List<TgTemplateInfo> templaList = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                .eq(StringUtils.isNotBlank(bizType), TgTemplateInfo::getBizType, bizType)
                .in(TgTemplateInfo::getId, ids)
                .list();
        if (CollectionUtils.isEmpty(templaList)) {
            return Collections.emptyList();
        }

        return templaList.stream().map(v -> {
            PowerPushBiTemplateVO vo = new PowerPushBiTemplateVO();
            vo.setId(v.getId());
            vo.setTemplateName(v.getTemplateName());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TgTemplateInfo> listAllAssetsTable(String bizType) {
        return templateInfoDAO.lambdaQuery().eq(TgTemplateInfo::getBizType, bizType).list();
    }

    @Override
    public List<TgTemplateInfo> listByFlowId(Long flowId) {
        return templateInfoDAO.lambdaQuery().eq(TgTemplateInfo::getSchedulerId, flowId).list();
    }

    @Override
    public List<TgTemplateInfo> listByIds(List<Long> ids) {
        return templateInfoDAO.listByIds(ids);
    }

    /**
     * 获取分布信息字段
     *
     * @return 分布信息字段
     */
    @Override
    public List<FieldDictDTO> distributedFieldList() {
        FieldListRequest request = new FieldListRequest();
        request.setIds(Arrays.asList(appProperties.getDistributedTypeFieldId(), appProperties.getDistributedQjFieldId()));
        AjaxResult<List<FieldDictDTO>> res = fieldDictService.listQuery(request);
        if (res.isSuccess() && CollectionUtils.isNotEmpty(res.getData())) {
            return res.getData();
        }
        return null;
    }
    
    @Override
    public AjaxResult<String> queryBizType(Long templateId) {
        if (Objects.isNull(templateId)) {
            return AjaxResult.error("模板参数为空");
        }
        return templateInfoDAO.lambdaQuery().select(TgTemplateInfo::getBizType)
                .eq(TgTemplateInfo::getId, templateId).oneOpt()
                .map(TgTemplateInfo::getBizType)
                .map(v -> AjaxResult.success("", v))
                .orElse(AjaxResult.error("模板不存在"));
    }
}
