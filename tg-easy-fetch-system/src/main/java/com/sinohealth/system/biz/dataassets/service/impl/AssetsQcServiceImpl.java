package com.sinohealth.system.biz.dataassets.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.DsConstants;
import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.dataassets.FlowType;
import com.sinohealth.common.enums.process.FlowProcessCategory;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.biz.dataassets.constant.AssetsQcTypeEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsQcBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsQcDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcDetail;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.AssetsQcPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsQcPageRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.facade.TgFlowProcessAlertFacade;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-17 16:24
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AssetsQcServiceImpl implements AssetsQcService {

    private final TemplateInfoDAO templateInfoDAO;
    private final ApplicationDAO applicationDAO;
    private final AssetsQcBatchDAO qcBatchDAO;
    private final AssetsQcDetailDAO qcDetailDAO;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final TgAssetInfoMapper assetInfoMapper;
    private final TgFlowProcessManagementDAO tgFlowProcessManagementDAO;

    private final ISysUserService sysUserService;
    private final AlertService alertService;
    private final AppProperties appProperties;
    private final RedisTemplate redisTemplate;
    private final RedisLock redisLock;
    private final CKClusterAdapter clusterAdapter;

    private final TgFlowProcessAlertFacade tgFlowProcessAlertFacade;

    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;

    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AjaxResult<IPage<AssetsQcPageDTO>> pageQuery(AssetsQcPageRequest request) {
        LambdaQueryWrapper<AssetsQcBatch> wrapper = new QueryWrapper<AssetsQcBatch>().lambda()
                .like(StringUtils.isNotBlank(request.getSearchName()), AssetsQcBatch::getBatchNo, request.getSearchName());
        if (StringUtils.isNotBlank(request.getState())) {
            if (Objects.equals(request.getState(), AssetsUpgradeStateEnum.running.name())) {
                wrapper.in(AssetsQcBatch::getState, AssetsUpgradeStateEnum.actions);
            } else {
                wrapper.eq(AssetsQcBatch::getState, request.getState());
            }
        }
        wrapper.orderByDesc(AssetsQcBatch::getCreateTime);
        IPage<AssetsQcBatch> pageResult = qcBatchDAO.getBaseMapper().selectPage(request.buildPage(), wrapper);

        List<AssetsQcBatch> records = pageResult.getRecords();
        List<Long> batchIds = Lambda.buildList(records, AssetsQcBatch::getId);

        Set<Long> userIds = Lambda.buildSet(records, AssetsQcBatch::getCreator);
        Map<Long, String> userNameMap = Lambda.queryMapIfExist(userIds, sysUserService::selectUserNameMapByIds);

        List<AssetsQcDetail> details = Lambda.queryListIfExist(batchIds, v -> qcDetailDAO.lambdaQuery()
                .select(AssetsQcDetail::getId, AssetsQcDetail::getBatchId, AssetsQcDetail::getTemplateId,
                        AssetsQcDetail::getAssetsQcType, AssetsQcDetail::getStartTime, AssetsQcDetail::getFinishTime)
                .in(AssetsQcDetail::getBatchId, v)
                .list());
        Map<Long, Map<String, Long>> batchMap = details.stream()
                .collect(Collectors.groupingBy(AssetsQcDetail::getBatchId,
                        Collectors.groupingBy(AssetsQcDetail::getAssetsQcType, Collectors.counting())));
        Map<Long, List<AssetsQcDetail>> batchDetailMap = details.stream()
                .collect(Collectors.groupingBy(AssetsQcDetail::getBatchId));
        Map<Long, String> tempNameMap = this.buildTempNameMap(records, details);

        List<Long> bizIds = Lambda.buildNonNullList(records, AssetsQcBatch::getBizId);
        Map<Long, TgFlowProcessManagement> flowMap = tgFlowProcessManagementDAO.queryForPageList(bizIds);
        return AjaxResult.success(PageUtil.convertMap(pageResult, x -> {
            Map<String, Long> detailMap = batchMap.get(x.getId());
            List<AssetsQcDetail> detailList = batchDetailMap.get(x.getId());

            String templateName;
            if (Objects.isNull(x.getTemplateId())) {
                templateName = detailList.stream().map(AssetsQcDetail::getTemplateId)
                        .map(tempNameMap::get).distinct().collect(Collectors.joining("、"));
            } else {
                templateName = x.getTemplateName();
            }

            LocalDateTime first = detailList.stream().filter(Objects::nonNull).map(AssetsQcDetail::getStartTime)
                    .filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);

            Optional<TgFlowProcessManagement> flowOpt = Optional.ofNullable(flowMap.get(x.getBizId()));

            boolean endState = AssetsUpgradeStateEnum.end.contains(x.getState());
            LocalDateTime endTimeVal = endState ? x.getFinishTime() : null;
            return AssetsQcPageDTO.builder()
                    .id(x.getId())
                    .batchNo(x.getBatchNo())
                    .templateName(templateName)
                    .brandCnt(Optional.ofNullable(detailMap).map(v -> v.get(AssetsQcTypeEnum.brand.name())).map(Long::intValue).orElse(0))
                    .skuCnt(Optional.ofNullable(detailMap).map(v -> v.get(AssetsQcTypeEnum.sku.name())).map(Long::intValue).orElse(0))
                    .state(x.getState())
                    .createTime(x.getCreateTime())
                    .creator(Optional.ofNullable(userNameMap.get(x.getCreator())).orElse("系统"))
                    .flowProcessCategory(flowOpt.map(v -> FlowProcessCategory.AUTO.getCode()).orElse(FlowProcessCategory.MANUAL_OPERATION.getCode()))
                    .flowProcessName(flowOpt.map(TgFlowProcessManagement::getName).orElse(""))
                    .startTime(first)
                    .finishTime(endTimeVal)
                    .costTime(DateUtil.caluLocalDateTimeDiff(first, endTimeVal))
                    .build();
        }));
    }

    private Map<Long, String> buildTempNameMap(List<AssetsQcBatch> records, List<AssetsQcDetail> details) {
        Set<Long> needFill = records.stream().filter(v -> Objects.isNull(v.getTemplateId())).map(AssetsQcBatch::getId)
                .collect(Collectors.toSet());
        Map<Long, String> tempNameMap;
        if (CollectionUtils.isNotEmpty(needFill)) {
            Set<Long> allTemp = details.stream()
                    .filter(v -> needFill.contains(v.getBatchId()))
                    .map(AssetsQcDetail::getTemplateId).collect(Collectors.toSet());

            tempNameMap = templateInfoDAO.queryNameMap(allTemp);
        } else {
            tempNameMap = Collections.emptyMap();
        }
        return tempNameMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> createAllQc(Long bizId) {
        Integer running = qcBatchDAO.lambdaQuery()
                .in(AssetsQcBatch::getState, AssetsUpgradeStateEnum.actions)
                .count();
        if (Objects.nonNull(running) && running > 0) {
            return AjaxResult.error("存在一条执行中的QC任务，请勿重复执行");
        }

        List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getTemplateType)
                .eq(TgTemplateInfo::getAssetsQc, true)
                .list();
        List<TgTemplateInfo> skuTempList = tempList.stream()
                .filter(v -> Objects.equals(v.getTemplateType(), TemplateTypeEnum.wide_table.name()))
                .collect(Collectors.toList());
        List<AssetsQcDetail> skuDetails = this.buildDetailByTemplate(AssetsQcTypeEnum.sku, skuTempList);
        if (CollectionUtils.isEmpty(skuDetails)) {
            return AjaxResult.error("任务创建失败 单品类型 无资产可QC");
        }

        List<TgTemplateInfo> brandTempList = tempList.stream()
                .filter(v -> !Objects.equals(v.getTemplateType(), TemplateTypeEnum.wide_table.name()))
                .collect(Collectors.toList());
        List<AssetsQcDetail> brandDetails = this.buildDetailByTemplate(AssetsQcTypeEnum.brand, brandTempList);
        if (CollectionUtils.isEmpty(brandDetails)) {
            return AjaxResult.error("任务创建失败 品牌类型 无资产可QC");
        }

        Optional<Long> batchOpt = this.createOrFillBatch(AssetsQcTypeEnum.sku,
                skuDetails, null);
        if (!batchOpt.isPresent()) {
            return AjaxResult.error("任务创建失败");
        }

        this.createOrFillBatch(AssetsQcTypeEnum.brand, brandDetails, null);

        Set<Long> needTemp = new HashSet<>();
        brandDetails.forEach(v -> needTemp.add(v.getTemplateId()));
        skuDetails.forEach(v -> needTemp.add(v.getTemplateId()));

        String ids = tempList.stream()
                .map(TgTemplateInfo::getId).filter(needTemp::contains).map(Object::toString)
                .collect(Collectors.joining(","));
        String names = tempList.stream().filter(v -> needTemp.contains(v.getId())).map(TgTemplateInfo::getTemplateName)
                .collect(Collectors.joining("、"));
        qcBatchDAO.lambdaUpdate()
                .set(AssetsQcBatch::getTemplateId, ids)
                .set(AssetsQcBatch::getTemplateName, names)
                .set(AssetsQcBatch::getBizId, bizId)
                .set(AssetsQcBatch::getBrandState, AssetsUpgradeStateEnum.success.name())
                .eq(AssetsQcBatch::getId, batchOpt.get())
                .update();

        ttl.execute(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                log.info("start sku merge");
                this.finishWideUpgrade(batchOpt.get());
            } catch (Throwable e) {
                log.error("", e);
            }
        });
        return AjaxResult.succeed();
    }

    @Override
    public Optional<Long> queryRunBatch() {
        return qcBatchDAO.lambdaQuery().select(AssetsQcBatch::getId)
                .in(AssetsQcBatch::getState, AssetsUpgradeStateEnum.actions).oneOpt()
                .map(AssetsQcBatch::getId);
    }

    @Override
    public Optional<Long> createOrFillBatch(AssetsQcTypeEnum actType, List<AssetsQcDetail> details, Long bizId) {
        // 为了防止 SKU和品牌两种方式出现并发
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.Assets.QC_BATCH_LOCK, 0, Duration.ofMinutes(1));
        if (BooleanUtils.isNotTrue(lock)) {
            try {
                log.warn("wait create batch");
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error("", e);
            }
            Optional<Long> batchOpt = this.queryRunBatch();
            log.warn("other create batch {}", batchOpt);
            batchOpt.ifPresent(v -> saveDetail(v, details));
            return batchOpt;
        }

        try {
            if (Objects.equals(actType, AssetsQcTypeEnum.brand)) {
                LocalDateTime now = LocalDateTime.now();
                details.forEach(v -> {
                    v.setStartTime(now);
                    v.setFinishTime(now);
                });
            }

            List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                    .eq(TgTemplateInfo::getAssetsQc, true)
                    .list();
            if (CollectionUtils.isEmpty(tempList)) {
                return Optional.empty();
            }

            Optional<Long> batchOpt = this.queryRunBatch();
            if (!batchOpt.isPresent()) {
                Integer today = qcBatchDAO.lambdaQuery().ge(AssetsQcBatch::getCreateTime, LocalDate.now()).count();
                AssetsQcBatch batch = new AssetsQcBatch();
                batch.setCreator(SecurityUtils.getUserIdOrSystem())
                        .setBatchNo(DateUtils.dateTimeNow("yyyyMMdd") + String.format("%02d", today + 1))
                        .setState(AssetsUpgradeStateEnum.wait.name())
                        .setSkuState(AssetsUpgradeStateEnum.wait.name())
                        .setBrandState(AssetsUpgradeStateEnum.wait.name())
                        .setBizId(bizId)
                        .setActType(actType.name())
                ;
                qcBatchDAO.save(batch);
                batchOpt = Optional.ofNullable(batch.getId());
            }

            batchOpt.ifPresent(v -> this.saveDetail(v, details));
            return batchOpt;
        } catch (Exception e) {
            log.error("", e);
        } finally {
            redisTemplate.delete(RedisKeys.Assets.QC_BATCH_LOCK);
        }
        return Optional.empty();
    }

    private void saveDetail(Long batchId, List<AssetsQcDetail> details) {
        if (Objects.isNull(batchId)) {
            log.warn("no id");
            return;
        }

        if (CollectionUtils.isEmpty(details)) {
            log.warn("no detail");
            return;
        }

        // 为了规避 一个批次中出现多个重复的申请明细
        Set<Long> exceptIds = Lambda.buildSet(details, AssetsQcDetail::getApplicationId);
        List<AssetsQcDetail> existList = qcDetailDAO.lambdaQuery().select(AssetsQcDetail::getApplicationId)
                .in(AssetsQcDetail::getApplicationId, exceptIds)
                .eq(AssetsQcDetail::getBatchId, batchId)
                .list();
        Set<Long> existIds = Lambda.buildSet(existList, AssetsQcDetail::getApplicationId);
        if (CollectionUtils.isNotEmpty(existIds)) {
            log.warn("repeat: ids={}", existIds);
        }
        List<AssetsQcDetail> saveDetails = details.stream()
                .filter(v -> !existIds.contains(v.getApplicationId()))
                .peek(v -> v.setBatchId(batchId))
                .collect(Collectors.toList());

        log.info("save qc detail: batchId={} reqSize={} saveSize={}", batchId, details.size(), saveDetails.size());
        qcDetailDAO.saveBatch(saveDetails);
    }

    @Override
    public List<AssetsQcDetail> buildWideDetail(Long tableId) {
        List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId)
                .eq(TgTemplateInfo::getBaseTableId, tableId)
                .eq(TgTemplateInfo::getAssetsQc, true)
                .list();
        if (CollectionUtils.isEmpty(tempList)) {
            return Collections.emptyList();
        }

        return this.buildDetailByTemplate(AssetsQcTypeEnum.sku, tempList);
    }

    @Override
    public List<AssetsQcDetail> buildDetailsByApply(Collection<Long> applyIds, AssetsQcTypeEnum qcType) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }

        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getAssetsId)
                .in(TgApplicationInfo::getId, applyIds)
                .eq(TgApplicationInfo::getAssetsQc, true)
                .list();
        if (CollectionUtils.isEmpty(applyList)) {
            return Collections.emptyList();
        }

        Set<Long> assetsIds = Lambda.buildSet(applyList, TgApplicationInfo::getAssetsId, Objects::nonNull);
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyList();
        }

        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getSrcApplicationId,
                        UserDataAssets::getTemplateId, UserDataAssets::getVersion, UserDataAssets::getAssetTableName)
                .in(UserDataAssets::getId, assetsIds)
                .gt(UserDataAssets::getDataExpire, LocalDateTime.now())
                .list();

        Set<Long> needAssets = Lambda.buildSet(applyList, TgApplicationInfo::getAssetsId);
        return assets.stream().filter(v -> needAssets.contains(v.getId())).map(v -> {
            AssetsQcDetail detail = new AssetsQcDetail()
                    .setTemplateId(v.getTemplateId())
                    .setApplicationId(v.getSrcApplicationId())
                    .setAssetsId(v.getId())
                    .setAssetsVer(v.getVersion())
                    .setTableName(v.getAssetTableName())
                    .setAssetsQcType(qcType.name())
                    .setState(AssetsUpgradeStateEnum.wait.name());
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * @param assetsIds 需有效资产id 调用方保证有效未作废非另存
     */
    public List<AssetsQcDetail> buildDetailsByAssets(Collection<Long> assetsIds, AssetsQcTypeEnum qcType, Long bizId) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyList();
        }

        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getSrcApplicationId,
                        UserDataAssets::getTemplateId, UserDataAssets::getVersion, UserDataAssets::getAssetTableName)
                .in(UserDataAssets::getId, assetsIds)
                .list();
        Set<Long> applyIds = Lambda.buildSet(assets, UserDataAssets::getSrcApplicationId);
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }

        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getAssetsId)
                .in(TgApplicationInfo::getId, applyIds)
                .eq(TgApplicationInfo::getAssetsQc, true)
                .list();
        if (CollectionUtils.isEmpty(applyList)) {
            return Collections.emptyList();
        }

        Set<Long> needAssets = Lambda.buildSet(applyList, TgApplicationInfo::getAssetsId);
        return assets.stream().filter(v -> needAssets.contains(v.getId())).map(v -> {
            AssetsQcDetail detail = new AssetsQcDetail()
                    .setBizId(bizId)
                    .setTemplateId(v.getTemplateId())
                    .setApplicationId(v.getSrcApplicationId()).setAssetsId(v.getId())
                    .setAssetsVer(v.getVersion()).setTableName(v.getAssetTableName())
                    .setAssetsQcType(qcType.name())
                    .setState(AssetsUpgradeStateEnum.wait.name());
            return detail;
        }).collect(Collectors.toList());
    }

    /**
     * 手动方式
     */
    private List<AssetsQcDetail> buildDetailByTemplate(AssetsQcTypeEnum qcType, List<TgTemplateInfo> tempList) {
        if (CollectionUtils.isEmpty(tempList)) {
            return Collections.emptyList();
        }

        Set<Long> tempIds = Lambda.buildSet(tempList, TgTemplateInfo::getId);
//        LambdaQueryChainWrapper<UserDataAssets> wrapper = userDataAssetsDAO.lambdaQuery()
//                .select(UserDataAssets::getId, UserDataAssets::getSrcApplicationId,
//                        UserDataAssets::getTemplateId, UserDataAssets::getVersion, UserDataAssets::getAssetTableName)
//                .in(UserDataAssets::getTemplateId, tempIds);
//        userDataAssetsDAO.fillValid(wrapper);
//        List<UserDataAssets> assets = wrapper.list();

        List<UserDataAssets> assets = userDataAssetsDAO.queryRelateAssets(tempIds);
        Set<Long> applyIds = Lambda.buildSet(assets, UserDataAssets::getSrcApplicationId);
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }

        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getAssetsId)
                .in(TgApplicationInfo::getId, applyIds)
                .eq(TgApplicationInfo::getAssetsQc, true)
                .and(v -> v.isNull(TgApplicationInfo::getConfigType).or().ne(TgApplicationInfo::getConfigType, ApplicationConfigTypeConstant.FILE_TYPE))
                .list();
        if (CollectionUtils.isEmpty(applyList)) {
            return Collections.emptyList();
        }

        Set<Long> needAssets = Lambda.buildSet(applyList, TgApplicationInfo::getAssetsId);
        return assets.stream().filter(v -> needAssets.contains(v.getId())).map(v -> {
            AssetsQcDetail detail = new AssetsQcDetail()
                    .setTemplateId(v.getTemplateId())
                    .setApplicationId(v.getSrcApplicationId()).setAssetsId(v.getId())
                    .setAssetsVer(v.getVersion()).setTableName(v.getAssetTableName())
                    .setAssetsQcType(qcType.name());
            // 品牌的明细由 工作流内合并实现，默认认为是成功的
            if (Objects.equals(qcType, AssetsQcTypeEnum.brand)) {
                detail.setState(AssetsUpgradeStateEnum.success.name());
            } else {
                detail.setState(AssetsUpgradeStateEnum.wait.name());
            }
            return detail;
        }).collect(Collectors.toList());
    }

    @Override
    public void finishWideUpgrade(Long batchId) {
        log.info("finish wide: batchId={}", batchId);
        if (Objects.isNull(batchId)) {
            return;
        }
        Optional<AssetsQcBatch> batchOpt = qcBatchDAO.lambdaQuery()
                .eq(AssetsQcBatch::getId, batchId)
                .in(AssetsQcBatch::getState, AssetsUpgradeStateEnum.actions).oneOpt();
        if (!batchOpt.isPresent()) {
            log.warn("invalid wide state: batchId={}", batchId);
            return;
        }
        AssetsQcBatch batch = batchOpt.get();
        if (Objects.equals(batch.getSkuState(), AssetsUpgradeStateEnum.success.name())) {
            log.warn("repeat invoke: batchId={}", batchId);
            return;
        }

        List<AssetsQcDetail> details = qcDetailDAO.lambdaQuery()
                .eq(AssetsQcDetail::getBatchId, batchId)
                .eq(AssetsQcDetail::getAssetsQcType, AssetsQcTypeEnum.sku.name())
                .list();
        if (CollectionUtils.isEmpty(details)) {
            log.warn("no sku detail: batchId={}", batchId);
            return;
        }

        // 按模板信息选择不同的合并SQL
//        Set<Long> tempIds = Lambda.buildSet(details, AssetsQcDetail::getTemplateId);
//        List<TgAssetInfo> weightedList = assetInfoMapper.selectList(new QueryWrapper<TgAssetInfo>().lambda()
//                .select(TgAssetInfo::getRelatedId)
//                .in(TgAssetInfo::getRelatedId, tempIds)
//                .eq(TgAssetInfo::getType, AssetType.MODEL)
//                .like(TgAssetInfo::getAssetDescription, "加权单价")
//        );
//        Set<Long> weightedIds = Lambda.buildSet(weightedList, TgAssetInfo::getRelatedId);

        List<Long> assetsIds = Lambda.buildList(details, AssetsQcDetail::getAssetsId);
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getProjectName,
                        UserDataAssets::getAssetTableName, UserDataAssets::getDataTotal)
                .in(UserDataAssets::getId, assetsIds)
                .list();

        Map<Long, String> assetsNameMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getProjectName);
        Map<Long, String> assetsTableMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getAssetTableName);
        Map<Long, Long> assetsTotalMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getDataTotal);

        // 清理并合并资产表数据
        String skuTable = appProperties.getQcSkuTable();
        log.info("clean sku merge table: {}", skuTable);
        clusterAdapter.execute(AssetsQcTypeEnum.sku.buildClean(skuTable));
        for (AssetsQcDetail detail : details) {
            Long assetsId = detail.getAssetsId();
            String name = assetsNameMap.get(assetsId);
            Long total = assetsTotalMap.get(assetsId);
            String assetsTable = assetsTableMap.get(assetsId);

            this.mergeAssetsTableForSKU(AssetsQcTypeEnum.sku, detail, name, assetsTable, total);
        }

        qcBatchDAO.lambdaUpdate().eq(AssetsQcBatch::getId, batchId)
                .set(AssetsQcBatch::getSkuState, AssetsUpgradeStateEnum.success.name())
                .update();

        this.tryRunBatch(batchId);
    }

    /**
     * 资产表合并为大表，执行工作流（同步数据，执行sku对应函数）
     */
    public void mergeAssetsTableForSKU(AssetsQcTypeEnum type,
                                       AssetsQcDetail detail,
                                       String assetsName,
                                       String assetsTable,
                                       Long assetsTotal) {
        Long assetsId = detail.getAssetsId();
        String logPrefix = DateUtils.getTime() + " " + MDC.get(LogConstant.TRACE_ID) +
                " 资产ID：" + assetsId + " 资产名：" + assetsName;
        try {
            qcDetailDAO.updateStartState(detail.getId());
            if (StringUtils.isBlank(assetsTable)) {
                log.warn("no table: assetsId={}", assetsId);
                return;
            }

            String skuTable = appProperties.getQcSkuTable();
            log.info("merge: table={} total={}", assetsTable, assetsTotal);
            clusterAdapter.execute(assetsTable, type.buildInsert(skuTable, assetsName, assetsTable));
            qcDetailDAO.updateFinishState(detail.getId(), AssetsUpgradeStateEnum.success,
                    logPrefix + " finish");
        } catch (Exception e) {
            log.error("", e);
            qcDetailDAO.updateFinishState(detail.getId(), AssetsUpgradeStateEnum.failed,
                    logPrefix + " error:" + e.getMessage());
        }
    }

    @Override
    public void finishFlowUpgrade(Long batchId) {
        log.info("finish flow: batchId={}", batchId);

        qcBatchDAO.lambdaUpdate().eq(AssetsQcBatch::getId, batchId)
                .set(AssetsQcBatch::getBrandState, AssetsUpgradeStateEnum.success.name())
                .update();

        this.tryRunBatch(batchId);
    }

    /**
     * 检查两种类型的数据是否都准备好了
     */
    private void tryRunBatch(Long batchId) {
        Optional<AssetsQcBatch> batchOpt = qcBatchDAO.lambdaQuery()
                .eq(AssetsQcBatch::getId, batchId)
                .eq(AssetsQcBatch::getState, AssetsUpgradeStateEnum.wait.name()).oneOpt();
        if (!batchOpt.isPresent()) {
            log.warn("invalid batchId: {}", batchId);
            return;
        }

        AssetsQcBatch batch = batchOpt.get();
        boolean allFinish = Objects.equals(batch.getSkuState(), AssetsUpgradeStateEnum.success.name())
                && Objects.equals(batch.getBrandState(), AssetsUpgradeStateEnum.success.name());
        if (!allFinish) {
            log.info("wait other side: sku={} brand={}", batch.getSkuState(), batch.getBrandState());
            return;
        }

        // 执行工作流
        AjaxResult exeResult = this.executeWorkFlow(batchId);
        log.warn("exeResult={}", exeResult);
        if (!exeResult.isSuccess()) {
            log.info("发起尚书台qc工作流失败：{},{}", batch, exeResult.getMsg());
            qcBatchDAO.lambdaUpdate().eq(AssetsQcBatch::getId, batchId)
                    .set(AssetsQcBatch::getState, AssetsUpgradeStateEnum.failed.name())
                    .update();
            // 告警
            tgFlowProcessAlertFacade.sendAssetsQcBatchAlert(qcBatchDAO.getById(batchId));
        }
    }

    @Override
    public void dolphinCallBack(String instanceUid, Integer state) {
        List<AssetsQcBatch> list = qcBatchDAO.lambdaQuery()
                .eq(AssetsQcBatch::getFlowInstanceId, instanceUid)
                .eq(AssetsQcBatch::getState, AssetsUpgradeStateEnum.running.name())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            log.warn("no match batch: instanceUid={} state={}", instanceUid, state);
            return;
        }
        if (list.size() > 1) {
            List<Long> ids = Lambda.buildList(list, AssetsQcBatch::getId);
            log.warn("over match: ids={}", ids);
        }
        AssetsQcBatch batch = list.get(0);
        AssetsUpgradeStateEnum stateEnum = AssetsUpgradeStateEnum.ofFlowState(state);
        if (Objects.equals(stateEnum, AssetsUpgradeStateEnum.success)) {
            Integer failed = qcDetailDAO.lambdaQuery().eq(AssetsQcDetail::getBatchId, batch.getId())
                    .eq(AssetsQcDetail::getState, AssetsUpgradeStateEnum.failed.name())
                    .count();
            if (Objects.nonNull(failed) && failed > 0) {
                stateEnum = AssetsUpgradeStateEnum.failed;
            }
        }
        if (Objects.equals(stateEnum, AssetsUpgradeStateEnum.failed)) {
            alertService.sendDevNormalMsg("项目QC" + batch.getId() + ":" + batch.getBatchNo() + " 执行失败");
        } else {
            alertService.sendDevNormalMsg("项目QC" + batch.getId() + ":" + batch.getBatchNo() + " 执行成功");
        }

        qcBatchDAO.updateFinishState(batch.getId(), stateEnum);

        // 这里要有告警
        AssetsQcBatch qcBatch = qcBatchDAO.getById(batch.getId());
        tgFlowProcessAlertFacade.sendAssetsQcBatchAlert(qcBatch);
    }

    @Override
    public List<AssetsQcDetail> queryByBizIds(List<Long> bizIds) {
        return qcDetailDAO.lambdaQuery().in(AssetsQcDetail::getBizId, bizIds).list();
    }

    @Override
    public List<AssetsQcBatch> queryBatchByBizIds(List<Long> bizIds) {
        return qcBatchDAO.lambdaQuery().in(AssetsQcBatch::getBizId, bizIds).list();
    }

    /**
     * 执行工作流同步数据和QC函数
     *
     * @see UserDataAssetsServiceImpl#executeWorkFlow(Long, Long)
     */
    public AjaxResult executeWorkFlow(Long batchId) {
        return redisLock.wrapperLock(RedisKeys.Assets.QC_BATCH_FLOW_LOCK, () -> {
            log.info("executeWorkFlow batchId={}", batchId);
            Integer flowId = appProperties.getQcFlowId();
            if (Objects.isNull(flowId)) {
                alertService.sendDevNormalMsg("项目QC：未配置QC工作流！");
                return AjaxResult.error("未配置QC工作流！");
            }

            AssetsQcBatch batch = qcBatchDAO.getById(batchId);
            if (Objects.equals(batch.getState(), AssetsUpgradeStateEnum.running.name())) {
                return AjaxResult.error("QC工作流已启动");
            }

            MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
            postParameters.add("processDefinitionId", flowId);
            postParameters.add("failureStrategy", "CONTINUE");
            postParameters.add("taskDependType", null);
            postParameters.add("execType", "");
            postParameters.add("warningType", "NONE");
            postParameters.add("warningGroupId", 0);
            postParameters.add("runMode", "RUN_MODE_SERIAL");
            postParameters.add("processInstancePriority", "MEDIUM");
            postParameters.add("workerGroup", "default");
            //任务串行化执行
            postParameters.add("inSerialMode", true);
            postParameters.add("timeout", DsConstants.MAX_TASK_TIMEOUT);
            postParameters.add("callbackUrl", Optional.ofNullable(appProperties.getDolphinCallHost())
                    .map(v -> v + "/tg-easy-fetch/openapi/dolphin/callback?bizId=" + FlowType.assets_qc.name())
                    .orElse(""));

            // 工作流启动参数
            Map<String, Object> params = new HashMap<>();

            List<AssetsQcDetail> brandDetails = qcDetailDAO.lambdaQuery()
                    .select(AssetsQcDetail::getApplicationId)
                    .eq(AssetsQcDetail::getBatchId, batchId)
                    .eq(AssetsQcDetail::getAssetsQcType, AssetsQcTypeEnum.brand.name())
                    .list();
            String applyIds = Lambda.buildIdList(brandDetails, AssetsQcDetail::getApplicationId);
            params.put("apply_ids", applyIds);

            params.put("ysg_host", Optional.ofNullable(appProperties.getDolphinCallHost()).orElse(""));
            postParameters.add("startParams", JSON.toJSONString(params));

            AjaxResult ajaxResult = intergrateProcessDefService.execProcessInstance(postParameters);
            if (ajaxResult.isDolphinSuccess()) {
                Object data = ajaxResult.getData();
                if (Objects.isNull(data)) {
                    log.warn("未返回实例id: ajaxResult={}", ajaxResult);
                } else {
                    List<AssetsQcDetail> details = qcDetailDAO.lambdaQuery().eq(AssetsQcDetail::getBatchId, batchId).list();
                    Set<Long> assetsIds = Lambda.buildSet(details, AssetsQcDetail::getAssetsId);
                    List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                            .select(UserDataAssets::getTemplateId)
                            .in(UserDataAssets::getId, assetsIds)
                            .list();
                    Set<Long> tempIds = Lambda.buildSet(assets, UserDataAssets::getTemplateId);

                    List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                            .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                            .in(TgTemplateInfo::getId, tempIds)
                            .list();

                    String ids = tempList.stream().map(TgTemplateInfo::getId).map(Object::toString)
                            .collect(Collectors.joining(","));
                    String names = tempList.stream().map(TgTemplateInfo::getTemplateName)
                            .collect(Collectors.joining("、"));

                    String uid = data.toString();
                    qcBatchDAO.lambdaUpdate()
                            .eq(AssetsQcBatch::getId, batchId)
                            .set(AssetsQcBatch::getFlowInstanceId, uid)
                            .set(AssetsQcBatch::getState, AssetsUpgradeStateEnum.running.name())
                            .set(AssetsQcBatch::getTemplateId, ids)
                            .set(AssetsQcBatch::getTemplateName, names)
                            .update();
                }
            } else if (Objects.equals(ajaxResult.getCode(), 50004)) {
                // PROCESS_DEFINE_NOT_RELEASE 工作流下线情况
                AjaxResult processResult = intergrateProcessDefService.queryProcessById(flowId);

                String name;
                if (processResult.isDolphinSuccess()) {
                    Map<String, Object> resultMap = (Map<String, Object>) processResult.getData();
                    name = Optional.ofNullable(resultMap.get("name")).map(Object::toString).orElse(null);
                } else {
                    name = "[" + flowId + "]";
                }
                return AjaxResult.error("当前工作流【" + name + "】已下线无法执行，请到尚书台上线后执行");
            }
            if (!ajaxResult.isDolphinSuccess()) {
                log.error("exe result={}", ajaxResult);
                return AjaxResult.error(ajaxResult.getMsg());
            }

            return AjaxResult.succeed();
        }).orElse(AjaxResult.error("获取锁失败"));
    }
}
