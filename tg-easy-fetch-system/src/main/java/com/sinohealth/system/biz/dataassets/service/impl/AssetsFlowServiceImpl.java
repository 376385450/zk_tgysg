package com.sinohealth.system.biz.dataassets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.process.FlowProcessCategory;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.CronExpParserUtil;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.quartz.domain.SysJob;
import com.sinohealth.quartz.service.ISysJobService;
import com.sinohealth.quartz.util.CronUtils;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.biz.dataassets.constant.AutoFlowTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowAutoBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowAutoBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowAutoBatchDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowAutoBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowBatchInfoDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.*;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsMapper;
import com.sinohealth.system.biz.dataassets.service.AssetsFlowService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dataassets.util.AssetsFlowAutoBatchUtil;
import com.sinohealth.system.biz.dataassets.util.DataAssetsUtil;
import com.sinohealth.system.biz.dataassets.vo.AssetsFlowBatchDetailVO;
import com.sinohealth.system.biz.dataassets.vo.AssetsFlowBatchVO;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.facade.TgFlowProcessAlertFacade;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.ckpg.PostgresqlProperties;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.mapper.ProjectDataAssetsRelateMapper;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kuangcp
 * 2024-07-16 14:49
 */
@Slf4j
@Service("assetsFlowService")
@RequiredArgsConstructor
public class AssetsFlowServiceImpl implements AssetsFlowService {


    private final RedisLock redisLock;
    private final ProjectDAO projectDAO;
    private final TemplateInfoDAO templateInfoDAO;
    private final ApplicationDAO applicationDAO;
    private final UserDataAssetsMapper userDataAssetsMapper;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final ProjectDataAssetsRelateMapper projectAssetsMapper;
    private final AssetsFlowBatchDAO assetsFlowBatchDAO;
    private final AssetsFlowBatchDetailDAO assetsFlowBatchDetailDAO;
    private final AssetsFlowAutoBatchDAO flowAutoBatchDAO;
    private final TgFlowProcessManagementDAO tgFlowProcessManagementDAO;
    private final ApplicationFormDAO applicationFormDAO;

    private final DataPlanService dataPlanService;
    private final ISysJobService jobService;
    private final ISysUserService sysUserService;
    private final UserDataAssetsService userDataAssetsService;
    private final IApplicationService applicationService;
    private final AlertService alertService;
    private final IntergrateProcessDefService processDefService;
    private final TgFlowProcessAlertFacade tgFlowProcessAlertFacade;
    private final PostgresqlProperties postgresqlProperties;


    @Override
    public AjaxResult<IPage<AssetsFlowBatchPageDTO>> pageQueryBatch(AssetsFlowBatchPageRequest request) {
        Set<Long> batchIds;
        if (StringUtils.isNotBlank(request.getTemplateName()) || StringUtils.isNotBlank(request.getBizType())) {
            List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                    .like(StringUtils.isNotBlank(request.getTemplateName()), TgTemplateInfo::getTemplateName, request.getTemplateName())
                    .eq(StringUtils.isNotBlank(request.getBizType()), TgTemplateInfo::getBizType, request.getBizType())
                    .list();
            Set<Long> tempIds = Lambda.buildSet(tempList);

            if (CollectionUtils.isEmpty(tempIds)) {
                return AjaxResult.success(new Page<>());
            }
            List<AssetsFlowBatchDetail> batchList = assetsFlowBatchDetailDAO.lambdaQuery()
                    .select(AssetsFlowBatchDetail::getBatchId)
                    .in(AssetsFlowBatchDetail::getTemplateId, tempIds)
                    .list();
            batchIds = Lambda.buildSet(batchList, AssetsFlowBatchDetail::getBatchId);
            if (CollectionUtils.isEmpty(batchIds)) {
                return AjaxResult.success(new Page<>());
            }
        } else {
            batchIds = Collections.emptySet();
        }


        IPage<AssetsFlowBatch> pageResult = assetsFlowBatchDAO.lambdaQuery()
                .like(StringUtils.isNotBlank(request.getName()), AssetsFlowBatch::getName, request.getName())
                .in(CollectionUtils.isNotEmpty(batchIds), AssetsFlowBatch::getId, batchIds)
                .eq(StringUtils.isNotBlank(request.getState()), AssetsFlowBatch::getState, request.getState())
                .eq(StringUtils.isNotBlank(request.getBizType()), AssetsFlowBatch::getBizType, request.getBizType())
                .orderByDesc(AssetsFlowBatch::getCreateTime)
                .page(request.buildPage());

        List<AssetsFlowBatch> records = pageResult.getRecords();
        List<Long> bizIds = Lambda.buildNonNullList(records, AssetsFlowBatch::getBizId);
        Map<Long, TgFlowProcessManagement> flowMap = tgFlowProcessManagementDAO.queryForPageList(bizIds);

        Set<Long> batchIdSet = Lambda.buildSet(records);
        Set<Long> userIds = Lambda.buildSet(records, AssetsFlowBatch::getCreator);
        Map<Long, String> userNameMap = Lambda.queryMapIfExist(userIds, sysUserService::selectUserNameMapByIds);

        List<Long> templateIds = records.stream().map(AssetsFlowBatch::getTemplateIds)
                .flatMap(v -> Stream.of(v.split(",")))
                .map(Long::parseLong)
                .distinct().collect(Collectors.toList());
        Map<Long, String> tempNameMap = templateInfoDAO.queryNameMap(templateIds);

        List<AssetsFlowBatchDetail> allDetails = assetsFlowBatchDetailDAO.queryByBatchId(batchIdSet);
        Map<Long, List<AssetsFlowBatchDetail>> batchMap = allDetails.stream()
                .collect(Collectors.groupingBy(AssetsFlowBatchDetail::getBatchId));

        return AjaxResult.success(PageUtil.convertMap(pageResult, v -> {
            AssetsFlowBatchPageDTO dto = new AssetsFlowBatchPageDTO();
            BeanUtils.copyProperties(v, dto);
            String name = Arrays.stream(v.getTemplateIds().split(",")).map(Long::parseLong).distinct()
                    .map(tempNameMap::get).distinct().collect(Collectors.joining("、"));
            dto.setTemplateName(name);
            List<AssetsFlowBatchDetail> detailList = batchMap.get(v.getId());

            if (CollectionUtils.isNotEmpty(detailList)) {
                long finish = detailList.stream().filter(d -> Objects.equals(d.getState(), AssetsUpgradeStateEnum.success.name())).count();
                dto.setDetailCnt(detailList.size());
                dto.setFinishCnt((int) finish);

                LocalDateTime first = detailList.stream().filter(Objects::nonNull).map(AssetsFlowBatchDetail::getStartTime)
                        .filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
                dto.setStartTime(first);

                if (AssetsUpgradeStateEnum.end.contains(dto.getState())) {
                    LocalDateTime last = detailList.stream().filter(Objects::nonNull).map(AssetsFlowBatchDetail::getFinishTime)
                            .filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
                    dto.setFinishTime(last);
                }
                dto.setCostTime(DateUtil.caluLocalDateTimeDiff(dto.getStartTime(), dto.getFinishTime()));
            } else {
                dto.setDetailCnt(0);
                dto.setFinishCnt(0);

            }

            Optional<TgFlowProcessManagement> flowOpt = Optional.ofNullable(flowMap.get(v.getBizId()));
            dto.setFlowProcessCategory(flowOpt.map(x -> FlowProcessCategory.AUTO.getCode()).orElse(FlowProcessCategory.MANUAL_OPERATION.getCode()));
            dto.setFlowProcessName(flowOpt.map(TgFlowProcessManagement::getName).orElse(""));


            dto.setCreator(Optional.ofNullable(userNameMap.get(v.getCreator())).orElse("系统"));
            return dto;
        }));
    }

    @Override
    public AjaxResult<List<FlowAssetsPageDTO>> listAssets(FlowAssetsAutoPageRequest request) {
        boolean containSearch = AssetsFlowAutoBatchUtil.containSearch(request.getProjectName());
        List<String> search;
        if (containSearch) {
            search = AssetsFlowAutoBatchUtil.parseSearch(request.getProjectName());
            request.setProjectName(null);
        } else {
            search = null;
        }

        if (CollectionUtils.isEmpty(request.getTemplateIds())
                && Objects.isNull(request.getBatchId())
                && Objects.isNull(request.getAutoBatchId())) {
            return AjaxResult.error("参数缺失");
        }

        if (CollectionUtils.isNotEmpty(request.getTemplateIds())) {
            // 未创建
            List<FlowAssetsPageDTO> data = userDataAssetsService.listForCreate(request);
            data = this.filterByFlowNameForAuto(request, data);
            data = this.filterByProjectName(search, data);
            return AjaxResult.success(data);
        } else if (Objects.nonNull(request.getBatchId())) {
            // 已创建的历史批次 下 明细展示
            List<AssetsFlowBatchDetail> details = assetsFlowBatchDetailDAO.queryByBatchId(request.getBatchId());
            List<Long> applyIds = Lambda.buildList(details, AssetsFlowBatchDetail::getApplicationId);

            Map<Long, String> stateMap = Lambda.buildMap(details, AssetsFlowBatchDetail::getApplicationId, AssetsFlowBatchDetail::getState);
            AjaxResult<List<FlowAssetsPageDTO>> result = applicationService.pageQueryRelateApply(request, applyIds);
            if (result.isSuccess() && CollectionUtils.isNotEmpty(result.getData())) {
                List<FlowAssetsPageDTO> list = this.filterByFlowNameForAuto(request, result.getData());
                for (FlowAssetsPageDTO dto : list) {
                    dto.setState(stateMap.get(dto.getApplyId()));
                }
                if (CollectionUtils.isNotEmpty(request.getState())) {
                    list = list.stream().filter(v -> request.getState().contains(v.getState())).collect(Collectors.toList());
                }
                list = this.filterByProjectName(search, list);
                return AjaxResult.success(list);
            }
            return result;
        } else if (Objects.nonNull(request.getAutoBatchId())) {
            // 自动任务 预计明细
            AssetsFlowAutoBatch auto = flowAutoBatchDAO.getById(request.getAutoBatchId());
            if (Objects.isNull(auto)) {
                return AjaxResult.error("批次不存在");
            }
            FlowAssetsAutoPageRequest nowReq = new FlowAssetsAutoPageRequest();
            nowReq.setTemplateIds(Lambda.splitId(auto.getTemplateIds()));
            if (Objects.equals(auto.getAutoType(), AutoFlowTypeEnum.apply_id)) {
                nowReq.setApplyIds(Lambda.splitId(auto.getApplyIds()));
            } else {
                nowReq.setProjectName(auto.getProjectName());
                nowReq.setRequireTimeType(AssetsFlowAutoBatchUtil.parseInt(auto.getRequireTimeType()));
                nowReq.setDeliverTimeType(AssetsFlowAutoBatchUtil.parse(auto.getDeliverTimeType()));
                nowReq.setFlowName(AssetsFlowAutoBatchUtil.parse(auto.getFlowName()));
            }

            List<FlowAssetsPageDTO> list = userDataAssetsService.listForCreate(nowReq);
            list = this.filterByFlowNameForAuto(nowReq, list);
            List<Long> maxRange = Lambda.buildList(list, FlowAssetsPageDTO::getApplyId);
            request.setApplyIds(maxRange);

            // 自动批次
            List<FlowAssetsPageDTO> data = userDataAssetsService.listForCreate(request);
            data = this.filterByFlowNameForAuto(request, data);
            data = this.filterByProjectName(search, data);
            return AjaxResult.success(data);
        } else {
            return AjaxResult.error("参数缺失");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> createBatch(AssetsFlowBatchCreateRequest request) {
        Integer count = assetsFlowBatchDAO.lambdaQuery()
                .eq(AssetsFlowBatch::getName, request.getName())
                .count();
        if (count > 0) {
            return AjaxResult.error("任务名重复");
        }

//        List<AssetsFlowBatch> runBatch = assetsFlowBatchDAO.lambdaQuery()
//                .select(AssetsFlowBatch::getTemplateIds)
//                .in(AssetsFlowBatch::getState, AssetsUpgradeStateEnum.actions)
//                .list();
//        List<Long> runTemplateIds = runBatch.stream().map(AssetsFlowBatch::getTemplateIds)
//                .flatMap(v -> Stream.of(v.split(",")))
//                .map(Long::parseLong).collect(Collectors.toList());
//        boolean containRun = request.getTemplateIds().stream().anyMatch(runTemplateIds::contains);
//        if (containRun) {
//            log.info("run={} except={}", runTemplateIds, request.getTemplateIds());
//            return AjaxResult.error("当前模板存在待执行/执行中的出数任务，请勿重复创建");
//        }

        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getProjectName, TgApplicationInfo::getProjectId,
                        TgApplicationInfo::getApplicantId, TgApplicationInfo::getApplicantName, TgApplicationInfo::getApplicationNo,
                        TgApplicationInfo::getTemplateId, TgApplicationInfo::getDataExpir)
                .in(TgApplicationInfo::getId, request.getApplyIds())
                .ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                // 限制一次性需求
                .eq(Objects.nonNull(request.getFilterOne()) && request.getFilterOne(), TgApplicationInfo::getRequireTimeType,
                        ApplicationConst.RequireTimeType.PERSISTENCE)
                // 限制对应的时间类型【月度、季度、半年度、年度】
                .in(CollectionUtils.isNotEmpty(request.getDeliverTimeTypes()), TgApplicationInfo::getDeliverTimeType,
                        request.getDeliverTimeTypes())
                .gt(TgApplicationInfo::getDataExpir, new Date())
                .list();

        // 过滤掉暂停的需求
        Set<String> noSet = Lambda.buildSet(applyList, TgApplicationInfo::getApplicationNo);
        Set<String> pauseNos = applicationFormDAO.queryPause(noSet);
        applyList = applyList.stream().filter(v -> !pauseNos.contains(v.getApplicationNo()))
                .collect(Collectors.toList());
        boolean noDetail = CollectionUtils.isEmpty(applyList);

        CurrentDataPlanDTO plan = dataPlanService.currentPeriod(request.getBizType());
        AssetsFlowBatch batch = new AssetsFlowBatch()
                .setBizId(request.getBizId())
                .setName(request.getName())
                .setAutoId(request.getAutoId())
                .setNeedQc(request.getNeedQc())
                .setRemark(request.getRemark())
                .setBizType(request.getBizType())
                .setExpectTime(request.getExpectTime())
                .setState(noDetail ? AssetsUpgradeStateEnum.failed.name() : AssetsUpgradeStateEnum.wait.name())
                .setPeriod(plan.getPeriod())
                .setFlowProcessType(plan.getFlowProcessType())
                .setCreator(SecurityUtils.getUserIdOrLocal());
        // 临时跑数 特殊处理
        if (BooleanUtils.isTrue(request.getTempType())) {
            if (StringUtils.isBlank(request.getPeriod())) {
                throw new CustomException("期数未填写");
            }
            batch.setFlowProcessType(FlowProcessTypeEnum.temp.name());
            batch.setPeriod(request.getPeriod());
        }
        if (StringUtils.isBlank(batch.getRemark())) {
            batch.setRemark(batch.getPeriod() + "-" + FlowProcessTypeEnum.getDescByName(batch.getFlowProcessType()));
        }

        String ids = request.getTemplateIds().stream().map(v -> v + "")
                .collect(Collectors.joining(","));
        batch.setTemplateIds(ids);
        assetsFlowBatchDAO.save(batch);

        if (!noDetail) {
            LocalDateTime now = LocalDateTime.now();
            List<AssetsFlowBatchDetail> details = applyList.stream().map(v -> {
                AssetsFlowBatchDetail detail = new AssetsFlowBatchDetail();
                detail.setBatchId(batch.getId()).setTemplateId(v.getTemplateId())
                        .setApplicationId(v.getId())
                        .setProjectName(v.getProjectName())
                        .setProjectId(v.getProjectId())
                        .setApplicantId(v.getApplicantId())
                        .setApplicantName(v.getApplicantName())
                        .setState(AssetsUpgradeStateEnum.wait.name())
                        .setDataExpire(v.getDataExpir())
                        .setCreateTime(now);
                return detail;
            }).collect(Collectors.toList());
            assetsFlowBatchDetailDAO.saveBatch(details);
        } else {
            // 抛出异常提醒了
            tgFlowProcessAlertFacade.sendAssetsFlowAlert(batch);
        }

        return AjaxResult.succeed();
    }

    @Override
    public List<AssetsFlowBatchVO> queryByBizIds(List<Long> bizIds) {
        List<AssetsFlowBatch> batchList = assetsFlowBatchDAO.list(new LambdaQueryWrapper<AssetsFlowBatch>()
                .in(AssetsFlowBatch::getBizId, bizIds));
        if (CollectionUtils.isEmpty(batchList)) {
            return Collections.emptyList();
        }

        List<Long> ids = batchList.stream().map(AssetsFlowBatch::getId).collect(Collectors.toList());
        List<AssetsFlowBatchDetail> details = assetsFlowBatchDetailDAO.queryByBatchId(ids);
        Map<Long, List<AssetsFlowBatchDetailVO>> detailMap = Optional.ofNullable(details).orElse(Collections.emptyList())
                .stream().map(i -> {
                    AssetsFlowBatchDetailVO e = new AssetsFlowBatchDetailVO();
                    BeanUtils.copyProperties(i, e);
                    return e;
                }).collect(Collectors.groupingBy(AssetsFlowBatchDetailVO::getBatchId));
        return batchList.stream().map(i -> {
            AssetsFlowBatchVO e = new AssetsFlowBatchVO();
            BeanUtils.copyProperties(i, e);
            e.setDetails(detailMap.get(i.getId()));
            return e;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AssetsFlowAutoBatchPageDTO> listAutoBatch(AutoFlowBatchPageRequest request) {
        boolean projectName = StringUtils.isNotBlank(request.getProjectName());
        List<String> search = AssetsFlowAutoBatchUtil.parseSearch(request.getProjectName());
        List<AssetsFlowAutoBatch> list = flowAutoBatchDAO.lambdaQuery()
                .like(StringUtils.isNotBlank(request.getAutoName()), AssetsFlowAutoBatch::getName, request.getAutoName())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        Set<Long> userIds = Lambda.buildSet(list, AssetsFlowAutoBatch::getCreator);
        Map<Long, String> userNameMap = Lambda.queryMapIfExist(userIds, sysUserService::selectUserNameMapByIds);

        return list.stream().map(v -> {
            AssetsFlowAutoBatchPageDTO dto = new AssetsFlowAutoBatchPageDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setCreator(userNameMap.get(v.getCreator()));

            if (Objects.equals(v.getAutoType(), AutoFlowTypeEnum.apply_id)) {
                List<Long> applyIds = Lambda.splitId(v.getApplyIds());
                dto.setRelateSize(applyIds.size());
                if (projectName) {
                    List<TgApplicationInfo> applys = applicationDAO.lambdaQuery()
                            .select(TgApplicationInfo::getProjectName)
                            .in(TgApplicationInfo::getId, applyIds)
                            .list();
                    List<String> names = Lambda.buildList(applys, TgApplicationInfo::getProjectName);
                    boolean match = names.stream().anyMatch(x -> search.stream().anyMatch(x::contains));
                    if (!match) {
                        return null;
                    }
                }
            } else {
                FlowAssetsAutoPageRequest nowReq = new FlowAssetsAutoPageRequest();
                nowReq.setTemplateIds(Lambda.splitId(v.getTemplateIds()));
                if (Objects.equals(v.getAutoType(), AutoFlowTypeEnum.apply_id)) {
                    nowReq.setApplyIds(Lambda.splitId(v.getApplyIds()));
                } else {
                    nowReq.setProjectName(v.getProjectName());
                    nowReq.setRequireTimeType(AssetsFlowAutoBatchUtil.parseInt(v.getRequireTimeType()));
                    nowReq.setDeliverTimeType(AssetsFlowAutoBatchUtil.parse(v.getDeliverTimeType()));
                    nowReq.setFlowName(AssetsFlowAutoBatchUtil.parse(v.getFlowName()));
                }

                AjaxResult<List<FlowAssetsPageDTO>> applyResult = this.listAssets(nowReq);
                List<FlowAssetsPageDTO> applyList = applyResult.getData();
                if (!applyResult.isSuccess() || CollectionUtils.isEmpty(applyList)) {
                    log.error("没有符合条件的申请单", new RuntimeException("定时 创建出数批次失败"));
                    return null;
                } else {
                    if (projectName) {
                        List<String> exist = Lambda.buildList(applyList, FlowAssetsPageDTO::getProjectName);
                        boolean match = exist.stream().anyMatch(x -> search.stream().anyMatch(x::contains));
                        if (!match) {
                            return null;
                        }
                    }
                    dto.setRelateSize(applyList.size());
                }
            }

            String cronCN = CronExpParserUtil.translateToChinese(v.getCron(), CronExpParserUtil.CRON_TIME_CN);
            dto.setCronCN(cronCN);
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 按调度时间排序, 实现逻辑为按调度规律复制任务
     */
    @Override
    public AjaxResult<List<AssetsFlowAutoBatchPageDTO>> listAutoBatchByScheduler(AutoFlowBatchPageRequest request) {
        List<AssetsFlowAutoBatchPageDTO> list = this.listAutoBatch(request);

        AutoFlowBatchPageRequest.TimeType timeType = AutoFlowBatchPageRequest.TimeType.of(request.getTimeType());
        if (Objects.isNull(timeType)) {
            return AjaxResult.error("参数缺失");
        }

        Date endDate = timeType.getEndDate();
        List<AssetsFlowAutoBatchPageDTO> result = list.stream()
                .peek(v -> {
                    Date nextExecution = CronUtils.getNextExecution(v.getCron());
                    v.setPlanTime(nextExecution);
                })
                .filter(v -> endDate.after(v.getPlanTime()))
                .sorted(Comparator.comparing(AssetsFlowAutoBatchPageDTO::getPlanTime))
                .collect(Collectors.toList());
//        list.stream().flatMap(v -> {
//                    try {
//                        CronExpression cron = new CronExpression(v.getCron());
//                        List<AssetsFlowAutoBatchPageDTO> ll = new ArrayList<>();
//                        Date cur = new Date();
//                        while (true) {
//                            Date tmp = cron.getNextValidTimeAfter(cur);
//                            if (tmp.after(endDate)) {
//                                break;
//                            }
//                            cur = tmp;
//
//                            AssetsFlowAutoBatchPageDTO dto = new AssetsFlowAutoBatchPageDTO();
//                            BeanUtils.copyProperties(v, dto);
//                            dto.setPlanTime(tmp);
//
//                            ll.add(dto);
//                        }
//
//                        return ll.stream();
//                    } catch (ParseException e) {
//                        log.error("", e);
//                    }
//
//                    return Stream.empty();
//                })
//                .collect(Collectors.toList());

        return AjaxResult.success(result);
    }

    @Override
    public AjaxResult<AssetsFlowAutoBatchDTO> queryAutoBatch(Long id) {
        if (Objects.isNull(id)) {
            return AjaxResult.error("任务不存在");
        }
        AssetsFlowAutoBatch batch = flowAutoBatchDAO.getById(id);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("任务不存在");
        }

        AssetsFlowAutoBatchDTO dto = new AssetsFlowAutoBatchDTO();
        BeanUtils.copyProperties(batch, dto);
        dto.setRequireTimeType(AssetsFlowAutoBatchUtil.parseInt(batch.getRequireTimeType()));
        dto.setDeliverTimeType(AssetsFlowAutoBatchUtil.parse(batch.getDeliverTimeType()));
        dto.setFlowName(AssetsFlowAutoBatchUtil.parse(batch.getFlowName()));
        dto.setAutoType(batch.getAutoType().name());

//        List<FlowAssetsPageDTO> list = assetsRes.getData();
//        List<Long> applyIds = Lambda.buildList(list, FlowAssetsPageDTO::getApplyId);
//
//        dto.setApplyIds(Lambda.splitId(batch.getApplyIds()));
//        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
//                .select(TgApplicationInfo::getId)
//                .in(TgApplicationInfo::getId, applyIds)
//                .ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
//                .gt(TgApplicationInfo::getDataExpir, new Date())
//                .list();
//        applyIds = Lambda.buildList(applyList);
//        dto.setApplyIds(applyIds);

        return AjaxResult.success(dto);
    }

    /**
     * 自动任务 模块 按cron调度创建 工作流出数任务 批次
     *
     * @see AssetsFlowServiceImpl#upsertAutoBatch 创建自动批次
     * @see AssetsFlowServiceImpl#registerNewJob 保存时注册XXLjob任务
     */
    @RegisterCronMethod
    @Transactional(rollbackFor = Exception.class)
    public void autoCreateBatch(Long autoId) {
        AssetsFlowAutoBatch auto = flowAutoBatchDAO.getById(autoId);
        if (Objects.isNull(auto)) {
            log.warn("invalid: autoId={}", autoId);
            return;
        }

        log.info("invoke={} {}", autoId, auto.getName());
        SecurityUtils.setLocalUserId(auto.getCreator());
        Integer count = assetsFlowBatchDAO.lambdaQuery()
                .eq(AssetsFlowBatch::getAutoId, autoId)
                .ge(AssetsFlowBatch::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .count();
        AssetsFlowBatchCreateRequest req = new AssetsFlowBatchCreateRequest();
        req.setName(auto.getName() + "-" + DateUtils.compactDate() + String.format("%02d", count + 1));
//        req.setFlowProcessType(auto.getFlowProcessType());
//        req.setRemark(req.getPeriod() + "-" + FlowProcessTypeEnum.getDescByName(req.getFlowProcessType()));
        req.setBizType(auto.getBizType());
        req.setExpectTime(LocalDateTime.now());
        req.setAutoId(autoId);
        req.setTemplateIds(Lambda.splitId(auto.getTemplateIds()));

        if (AutoFlowTypeEnum.apply_id.equals(auto.getAutoType())) {
            req.setApplyIds(Lambda.splitId(auto.getApplyIds()));
        } else {
            FlowAssetsAutoPageRequest nowReq = new FlowAssetsAutoPageRequest();
            nowReq.setTemplateIds(Lambda.splitId(auto.getTemplateIds()));
            nowReq.setProjectName(auto.getProjectName());
            nowReq.setRequireTimeType(AssetsFlowAutoBatchUtil.parseInt(auto.getRequireTimeType()));
            nowReq.setDeliverTimeType(AssetsFlowAutoBatchUtil.parse(auto.getDeliverTimeType()));
            nowReq.setFlowName(AssetsFlowAutoBatchUtil.parse(auto.getFlowName()));

            AjaxResult<List<FlowAssetsPageDTO>> applyResult = this.listAssets(nowReq);
            List<FlowAssetsPageDTO> applyList = applyResult.getData();
            if (!applyResult.isSuccess() || CollectionUtils.isEmpty(applyList)) {
                log.error("没有符合条件的申请单", new RuntimeException("定时 创建出数批次失败"));
                return;
            }

            List<Long> applyIds = Lambda.buildList(applyList, FlowAssetsPageDTO::getApplyId);
            req.setApplyIds(applyIds);
            List<Long> actualTempIds = applyList.stream().map(FlowAssetsPageDTO::getTemplateId)
                    .distinct().collect(Collectors.toList());
            req.setTemplateIds(actualTempIds);
        }
        log.info("Final: templateIds={} applyIds={}", req.getTemplateIds(), req.getApplyIds());

        AjaxResult<Void> createResult = this.createBatch(req);
        if (!createResult.isSuccess()) {
            log.error("autoId{} createResult={}", autoId, createResult);
            alertService.sendDevNormalMsg(autoId + " " + auto.getName() + ": " + createResult.getMsg());
        }
    }

    @Override
    public List<FlowAssetsPageDTO> queryAllScheduleAssets() {
        List<FlowAssetsPageDTO> result = new ArrayList<>();
        List<AssetsFlowAutoBatch> autoList = flowAutoBatchDAO.list();
        for (AssetsFlowAutoBatch auto : autoList) {
            FlowAssetsAutoPageRequest nowReq = new FlowAssetsAutoPageRequest();
            nowReq.setTemplateIds(Lambda.splitId(auto.getTemplateIds()));
            nowReq.setProjectName(auto.getProjectName());
            nowReq.setRequireTimeType(AssetsFlowAutoBatchUtil.parseInt(auto.getRequireTimeType()));
            nowReq.setDeliverTimeType(AssetsFlowAutoBatchUtil.parse(auto.getDeliverTimeType()));
            nowReq.setFlowName(AssetsFlowAutoBatchUtil.parse(auto.getFlowName()));

            AjaxResult<List<FlowAssetsPageDTO>> applyResult = this.listAssets(nowReq);
            List<FlowAssetsPageDTO> applyList = applyResult.getData();
            if (!applyResult.isSuccess() || CollectionUtils.isEmpty(applyList)) {
                log.error("没有符合条件的申请单");
                continue;
            }
            String str = CronExpParserUtil.translateToChinese(auto.getCron(), CronExpParserUtil.CRON_TIME_CN);
            for (FlowAssetsPageDTO dto : applyList) {
                dto.setCronCN(str);
            }
            result.addAll(applyList);
        }
        return result;
    }

    public Map<String, String> queryApplyFormScheduler() {
        List<FlowAssetsPageDTO> list = this.queryAllScheduleAssets();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }

        return list.stream().collect(Collectors.groupingBy(FlowAssetsPageDTO::getApplicationNo,
                Collectors.mapping(FlowAssetsPageDTO::getCronCN,
                        Collectors.joining(";")))
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> upsertAutoBatch(AssetsFlowAutoBatchCreateRequest request) {
        if (!CronUtils.isValid(request.getCron())) {
            return AjaxResult.error("cron表达式不正确");
        }

        Integer count = flowAutoBatchDAO.lambdaQuery()
                .ne(Objects.nonNull(request.getId()), AssetsFlowAutoBatch::getId, request.getId())
                .eq(AssetsFlowAutoBatch::getName, request.getName())
                .count();
        if (count > 0) {
            return AjaxResult.error("任务名重复");
        }
        AutoFlowTypeEnum typeEnum = AutoFlowTypeEnum.of(request.getAutoType());
        if (Objects.isNull(typeEnum)) {
            return AjaxResult.error("关联方式未填");
        }
        if (AutoFlowTypeEnum.apply_id.name().equals(request.getAutoType())) {
            if (CollectionUtils.isEmpty(request.getApplyIds())) {
                return AjaxResult.error("请选择出数的需求");
            }
        }

        AssetsFlowAutoBatch batch = new AssetsFlowAutoBatch()
                .setName(request.getName())
                .setBizType(request.getBizType())
                .setTemplateIds(Lambda.concatId(request.getTemplateIds()))
                .setFlowProcessType(request.getFlowProcessType())
                .setProjectName(request.getProjectName())
                .setRequireTimeType(AssetsFlowAutoBatchUtil.buildStr(request.getRequireTimeType()))
                .setDeliverTimeType(AssetsFlowAutoBatchUtil.buildStr(request.getDeliverTimeType()))
                .setFlowName(AssetsFlowAutoBatchUtil.buildStr(request.getFlowName()))
                .setCron(request.getCron())
                .setAutoType(typeEnum)
                .setApplyIds(Lambda.concatId(request.getApplyIds()))
                .setCreator(SecurityUtils.getUserId());

        if (Objects.nonNull(request.getId())) {
            batch.setId(request.getId());

            flowAutoBatchDAO.saveOrUpdate(batch);

            AssetsFlowAutoBatch exist = flowAutoBatchDAO.getById(request.getId());
            SysJob job = jobService.selectJobById(exist.getJobId());
            if (Objects.isNull(job)) {
                this.registerNewJob(request, batch);
            } else {
                job.setJobName("工作流出数-" + request.getName());
                job.setUpdateBy(SecurityUtils.getUsername());
                job.setInvokeTarget("assetsFlowService.autoCreateBatch(" + batch.getId() + "L)");
                job.setCronExpression(request.getCron());
                try {
                    jobService.updateJob(job);
                } catch (Exception e) {
                    log.error("", e);
                    throw new RuntimeException(e);
                }
            }
        } else {
            flowAutoBatchDAO.save(batch);
            registerNewJob(request, batch);
        }

        return AjaxResult.succeed();
    }

    private void registerNewJob(AssetsFlowAutoBatchCreateRequest request, AssetsFlowAutoBatch batch) {
        // 注册定时任务 执行创建和触发工作流任务
        try {
            SysJob job = new SysJob();
            job.setStatus("0");
            job.setConcurrent("1");
            job.setCronExpression(request.getCron());
            job.setInvokeTarget("assetsFlowService.autoCreateBatch(" + batch.getId() + "L)");
            job.setJobGroup("DEFAULT");
            job.setJobName("工作流出数-" + request.getName());
            job.setMisfirePolicy("1");
            String username = SecurityUtils.getUsername();
            job.setCreateBy(username);
            job.setUpdateBy(username);

            jobService.insertJob(job);

            flowAutoBatchDAO.lambdaUpdate()
                    .eq(AssetsFlowAutoBatch::getId, batch.getId())
                    .set(AssetsFlowAutoBatch::getJobId, job.getJobId())
                    .update();

            jobService.resumeJob(job);

            log.info("id={}", job.getJobId());
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public AjaxResult<Void> deleteAutoBatch(Long id) {
        if (Objects.isNull(id)) {
            return AjaxResult.error("任务不存在");
        }
        AssetsFlowAutoBatch batch = flowAutoBatchDAO.getById(id);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("任务不存在");
        }
        boolean remove = flowAutoBatchDAO.removeById(id);

        SysJob job = jobService.selectJobById(batch.getJobId());
        if (Objects.nonNull(job)) {
            try {
                jobService.deleteJob(job);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return remove ? AjaxResult.succeed() : AjaxResult.error("任务删除失败");
    }

    @Override
    public AjaxResult<Void> deleteBatch(Long batchId) {
        AssetsFlowBatch batch = assetsFlowBatchDAO.getById(batchId);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("批次不存在");
        }
        if (!Objects.equals(batch.getState(), AssetsUpgradeStateEnum.wait.name())) {
            return AjaxResult.error("待执行中任务才可删除");
        }

        assetsFlowBatchDAO.removeById(batchId);
        return AjaxResult.succeed();
    }

    /**
     *
     */
    @Override
    public AjaxResult<Void> editBatch(AssetsFlowBatchEditRequest request) {
        AssetsFlowBatch batch = assetsFlowBatchDAO.getById(request.getId());
        if (Objects.isNull(batch)) {
            return AjaxResult.error("批次不存在");
        }
        boolean update = assetsFlowBatchDAO.lambdaUpdate()
                .eq(AssetsFlowBatch::getId, request.getId())
                .set(AssetsFlowBatch::getPeriod, request.getPeriod())
                .set(AssetsFlowBatch::getFlowProcessType, request.getFlowProcessType())
                .set(AssetsFlowBatch::getRemark, request.getRemark())
                .update();
        if (!update) {
            return AjaxResult.error("编辑失败");
        }

        return AjaxResult.succeed();
    }

    /**
     *
     */
    @Override
    public AjaxResult<AssetsFlowBatchInfoDTO> batchDetail(Long id) {
        AssetsFlowBatch batch = assetsFlowBatchDAO.getById(id);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("批次不存在");
        }

        AssetsFlowBatchInfoDTO dto = new AssetsFlowBatchInfoDTO();
        List<AssetsFlowBatchDetail> details = assetsFlowBatchDetailDAO.queryByBatchId(id);
        BeanUtils.copyProperties(batch, dto);
        List<Long> applyIds = Lambda.buildList(details, AssetsFlowBatchDetail::getApplicationId);
        dto.setApplyIds(applyIds);
        return AjaxResult.success(dto);
    }


    /**
     * 工作流名称过滤列表
     */
    private List<FlowAssetsPageDTO> filterByFlowNameForAuto(FlowAssetsAutoPageRequest request, List<FlowAssetsPageDTO> data) {
        if (CollectionUtils.isEmpty(data)) {
            return data;
        }
        List<Long> applyIds = Lambda.buildList(data, FlowAssetsPageDTO::getApplyId);
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .in(TgApplicationInfo::getId, applyIds)
                .list();
        Map<Long, TgApplicationInfo> applyMap = Lambda.buildMap(applyList, TgApplicationInfo::getId);
        List<Long> tempIds = Lambda.buildList(applyList, TgApplicationInfo::getTemplateId);

        List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                .in(TgTemplateInfo::getId, tempIds)
                .list();
        Map<Long, TgTemplateInfo> tempMap = Lambda.buildMap(tempList, TgTemplateInfo::getId);

        for (FlowAssetsPageDTO dto : data) {
            TgApplicationInfo info = applyMap.get(dto.getApplyId());
            TgTemplateInfo temp = tempMap.get(info.getTemplateId());
            Integer flowId = DataAssetsUtil.getFinalSchedulerId(temp, info);

            String flowName = processDefService.queryProcessNameByIdWithCache(flowId);
            dto.setFlowName(flowName);
        }
        if (CollectionUtils.isNotEmpty(request.getFlowName())) {
            data = data.stream()
                    .filter(v -> Objects.nonNull(v.getFlowName())
                            && request.getFlowName().stream().anyMatch(fs -> v.getFlowName().contains(fs)))
                    .collect(Collectors.toList());
        }
        return data;
    }

    private List<FlowAssetsPageDTO> filterByProjectName(List<String> search, List<FlowAssetsPageDTO> data) {
        if (CollectionUtils.isEmpty(search) || CollectionUtils.isEmpty(data)) {
            return data;
        }

        return data.stream().filter(v -> search.stream()
                        .anyMatch(x -> (StringUtils.isNotBlank(v.getProjectName()) && v.getProjectName().contains(x))
                                || (StringUtils.isNotBlank(v.getNewProjectName()) && v.getNewProjectName().contains(x))))
                .collect(Collectors.toList());
    }


    /**
     * 工作流名称过滤列表
     */
    private List<FlowAssetsPageDTO> filterByFlowName(FlowAssetsPageRequest request, List<FlowAssetsPageDTO> data) {
        if (CollectionUtils.isEmpty(data)) {
            return data;
        }
        List<Long> applyIds = Lambda.buildList(data, FlowAssetsPageDTO::getApplyId);
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .in(TgApplicationInfo::getId, applyIds)
                .list();
        Map<Long, TgApplicationInfo> applyMap = Lambda.buildMap(applyList, TgApplicationInfo::getId);
        List<Long> tempIds = Lambda.buildList(applyList, TgApplicationInfo::getTemplateId);

        List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                .in(TgTemplateInfo::getId, tempIds)
                .list();
        Map<Long, TgTemplateInfo> tempMap = Lambda.buildMap(tempList, TgTemplateInfo::getId);

        for (FlowAssetsPageDTO dto : data) {
            TgApplicationInfo info = applyMap.get(dto.getApplyId());
            TgTemplateInfo temp = tempMap.get(info.getTemplateId());
            Integer flowId = DataAssetsUtil.getFinalSchedulerId(temp, info);

            String flowName = processDefService.queryProcessNameByIdWithCache(flowId);
            dto.setFlowName(flowName);
        }
        if (StringUtils.isNotBlank(request.getFlowName())) {
            data = data.stream()
                    .filter(v -> Objects.nonNull(v.getFlowName()) && v.getFlowName().contains(request.getFlowName()))
                    .collect(Collectors.toList());
        }
        return data;
    }
}
