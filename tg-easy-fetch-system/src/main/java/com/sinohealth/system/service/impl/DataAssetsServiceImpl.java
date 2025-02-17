package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDetailDto;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;
import com.sinohealth.system.biz.application.dto.request.UserDataAssetsDistRequest;
import com.sinohealth.system.biz.application.util.ApplyUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.AcceptanceRecordDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowAutoBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.service.AssetsFlowService;
import com.sinohealth.system.biz.dataassets.util.DataAssetsUtil;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.domain.TableInfoDiy;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.DataAssetsService;
import com.sinohealth.system.service.ISysCustomerService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.service.RelationTableManageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataAssetsServiceImpl extends ServiceImpl<TgApplicationInfoMapper, TgApplicationInfo>
        implements DataAssetsService {

    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private ApplicationFormDAO applicationFormDAO;
    @Autowired
    private AssetsFlowAutoBatchDAO flowAutoBatchDAO;
    @Autowired
    private AcceptanceRecordDAO acceptanceRecordDAO;

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysCustomerService sysCustomerService;
    @Autowired
    private RelationTableManageService relationTableManageService;

    @Autowired
    private IntergrateProcessDefService processDefService;
    @Autowired
    private AssetsFlowService assetsFlowService;
    @Autowired
    private DataPlanService dataPlanService;
    @Autowired
    private AlertService alertService;

    @Override
    public AjaxResult<List<String>> assetsTimeGra() {
        List<TgTemplateInfo> list = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getGranularityJson)
                .list();
        List<String> names = list.stream().map(JsonBeanConverter::convert2Obj)
                .map(TgTemplateInfo::getGranularity)
                .flatMap(v -> v.stream().filter(x -> Objects.equals(x.getGranularity(), FieldGranularityEnum.time.name())))
                .flatMap(v -> v.getDetails().stream())
                .map(TemplateGranularityDetailDto::getName)
                .distinct()
                .collect(Collectors.toList());
        return AjaxResult.success(names);
    }

    /**
     * 全局需求管理 Service
     *
     * @see AuditProcessServiceImpl#queryAuditProcessAuditListByType(SysUser, Map) V1.9.7.1 自己抄袭自己
     * @see DataAssetsServiceImpl#buildActions 构建可操作的按钮
     */
    @Override
    public AjaxResult<IPage<TgUserDataAssetsDistDto>> queryAssetsDistList(UserDataAssetsDistRequest request) {
        if (Objects.nonNull(request.getEndTime())) {
            request.setEndTime(DateUtils.addDays(request.getEndTime(), 1));
        }
        if (StringUtils.isNoneBlank(request.getSearch())) {
            request.setSearch(request.getSearch().trim());
        }
        IPage<TgUserDataAssetsDistDto> pageRes = baseMapper.queryAssetsDistList(request.buildPage(), request);
        List<TgUserDataAssetsDistDto> list = pageRes.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return AjaxResult.success("", new Page<>());
        }

        List<Long> applyIds = Lambda.buildList(list, TgUserDataAssetsDistDto::getId);
        Map<Long, String> stateMap = Lambda.queryMapIfExist(applyIds,
                ids -> baseMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                        .select(TgApplicationInfo::getId, TgApplicationInfo::getDataState)
                        .in(TgApplicationInfo::getId, ids)), TgApplicationInfo::getId, TgApplicationInfo::getDataState);

        Map<Long, LocalDateTime> createMap = Lambda.queryMapIfExist(applyIds, ids -> userDataAssetsDAO.lambdaQuery()
                        .select(UserDataAssets::getId, UserDataAssets::getCreateTime)
                        .in(UserDataAssets::getSrcApplicationId, ids).list(),
                UserDataAssets::getId, UserDataAssets::getCreateTime);

        Set<Long> userIds = Lambda.buildSet(list, TgUserDataAssetsDistDto::getApplicantId);
        Map<Long, SysUser> userMap = Lambda.queryMapIfExist(userIds, sysUserService::selectUserByIds, SysUser::getUserId);

        List<Long> userIds2 = list.stream()
                .flatMap(v -> Arrays.stream(v.getCurrentHandlers().split(",")))
                .filter(com.sinohealth.common.utils.StringUtils::isNotBlank)
                .map(Long::valueOf)
                .distinct().collect(Collectors.toList());
        List<SysUser> userList = Lambda.queryListIfExist(userIds2, sysUserService::selectUserByIds);
        Map<Long, SysUser> userMap2 = userList.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

        Set<Long> assetsIds = Lambda.buildSet(list, TgUserDataAssetsDistDto::getAssetsId);
        Map<Long, UserDataAssets> assetsMap = Lambda.queryMapIfExist(assetsIds, v -> userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getVersion,
                        UserDataAssets::getDataTotal, UserDataAssets::getSnapshotType,
                        UserDataAssets::getFtpStatus, UserDataAssets::getFtpPath)
                .in(UserDataAssets::getId, v)
                .list(), UserDataAssets::getId);

        List<String> noList = Lambda.buildList(list, TgUserDataAssetsDistDto::getApplicationNo);
        List<TgApplicationInfo> noIdList = Lambda.queryListIfExist(noList, v -> applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getApplicationNo, TgApplicationInfo::getApplicantId)
                .in(TgApplicationInfo::getApplicationNo, v)
                .notIn(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.notShow)
                .list());
        Map<String, Long> noCntMap = noIdList.stream().collect(Collectors.groupingBy(TgApplicationInfo::getApplicationNo,
                Collectors.counting()));

        List<ApplicationForm> formList = applicationFormDAO.lambdaQuery()
                .in(ApplicationForm::getApplicationNo, noList)
                .list();
        Map<String, ApplicationForm> formMap = Lambda.buildMap(formList, ApplicationForm::getApplicationNo);

//        Map<String, String> cronMap = assetsFlowService.queryApplyFormScheduler();

        Map<Long, AcceptanceRecord> acceptMap = Lambda.queryMapIfExist(applyIds, v -> acceptanceRecordDAO.lambdaQuery()
                        .select(AcceptanceRecord::getApplicationId)
                        .in(AcceptanceRecord::getApplicationId, v)
                        .groupBy(AcceptanceRecord::getApplicationId)
                        .list(),
                AcceptanceRecord::getApplicationId);

        return AjaxResult.success(PageUtil.convertMap(pageRes, entity -> {
            JsonBeanConverter.convert2Obj(entity);
            // 处理申请者
            if (entity.getApplicantId() != null) {
                SysUser user = userMap.get(entity.getApplicantId());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        entity.setApplicantName(sinoPassUserDTO.getViewName());
                    }
                }
            }

            boolean normalType = TemplateTypeEnum.isNormalType(entity.getTemplateType());
            boolean customizedType = TemplateTypeEnum.isCustomizedType(entity.getTemplateType());
            if (!TemplateTypeEnum.wide_table.name().equals(entity.getTemplateType())) {
                Integer flowId = DataAssetsUtil.getFinalSchedulerId(entity);
                String flowName = processDefService.queryProcessNameByIdWithCache(flowId);
                entity.setTableName(flowName);
            }

            List<Integer> actionList = this.buildActions(entity, assetsMap, formMap, acceptMap);
            entity.setActionList(actionList);
            Optional<UserDataAssets> assetsOpt = Optional.ofNullable(assetsMap.get(entity.getAssetsId()));
            entity.setDataVersion(assetsOpt.map(UserDataAssets::getVersion).orElse(0));

//            entity.setCronCN(cronMap.get(entity.getApplicationNo()));

            entity.setDataState(stateMap.get(entity.getId()));
            entity.setAssetsCreateTime(createMap.get(entity.getAssetsId()));

//            this.handleGra(entity::setTimeGra, entity::getTimeGra);
//            this.handleGra(entity::setProductGra, entity::getProductGra);

            int applyAmount = Optional.ofNullable(entity.getDataAmount()).orElse(1)
                    * Optional.ofNullable(entity.getDataVersion()).orElse(1);
            entity.setApplyAmount(applyAmount);
            entity.setApplyCnt(Optional.ofNullable(noCntMap.get(entity.getApplicationNo())).map(Long::intValue).orElse(1));

            if (normalType) {
                entity.setDataType("工作流");
            } else if (customizedType) {
                if (Objects.isNull(entity.getConfigType())) {
                    entity.setDataType(null);
                } else {
                    entity.setDataType(ApplicationConfigTypeConstant.getDesc(entity.getConfigType()));
                }
            } else {
                entity.setDataType("表");
            }

            entity.setDataTotal(assetsOpt.map(UserDataAssets::getDataTotal).orElse(null));
            if (Objects.equals(entity.getApplyState(), ApplyStateEnum.none.name())
                    || Objects.equals(entity.getDataType(), "文件")) {
                entity.setDataTotal(null);
            }

            entity.setSnapshotType(assetsOpt.map(UserDataAssets::getSnapshotType).orElse(null));
            ApplyUtil.cleanGra(entity);

            Optional<ProcessNodeEasyDto> nodeOpt = ApplyUtil.lastNode(entity);
            if (Objects.equals(entity.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDITING)) {
                String currentHandlerNames = "";
                if (!"".equals(entity.getCurrentHandlers())) {
                    currentHandlerNames = Arrays.stream(entity.getCurrentHandlers().split(","))
                            .map((id) -> {
                                SysUser sysUser = userMap2.get(Long.valueOf(id));
                                SinoPassUserDTO temp = null;
                                try {
                                    temp = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                                } catch (NullPointerException e) {
                                    log.error("主数据人员组织编码为 NULL：{}", sysUser.getOrgUserId());
                                }

                                return temp != null ? ApplyUtil.userName(temp.getViewName()) : "";
                            }).collect(Collectors.joining(","));
                }
                entity.setHandleUser(currentHandlerNames);
            } else {
                nodeOpt.map(ProcessNodeEasyDto::getHandlerName).map(ApplyUtil::userName).ifPresent(entity::setHandleUser);
            }

            return entity;
        }));
    }

    private void handleGra(Consumer<String> set, Supplier<String> get) {
        String gra = get.get();
        if (com.sinohealth.common.utils.StringUtils.isNotBlank(gra)) {
            gra = gra.replace("\"", "");
            gra = gra.replace("[", "");
            gra = gra.replace("]", "");
            set.accept(gra);
        }
    }

    /**
     * 需求管理
     *
     * @see AuditProcessServiceImpl#buildActions 审核页 等价
     */
    private List<Integer> buildActions(TgUserDataAssetsDistDto dto,
                                       Map<Long, UserDataAssets> assetsMap,
                                       Map<String, ApplicationForm> formMap,
                                       Map<Long, AcceptanceRecord> acceptMap) {
        List<Integer> act = new ArrayList<>();
        act.add(ApplicationConst.AuditAction.DETAIL);

        boolean isOnce = Objects.equals(dto.getRequireTimeType(), ApplicationConst.RequireTimeType.ONCE);
        boolean schedulerTaskType = TemplateTypeEnum.isSchedulerTaskType(dto.getTemplateType());
        boolean customizedType = TemplateTypeEnum.isCustomizedType(dto.getTemplateType());
        ApplicationForm form = formMap.get(dto.getApplicationNo());
        // 暂停需求不可执行 过期，作废，均不可执行
        if (Objects.equals(form.getApplyState(), ApplyStateEnum.normal.name())) {
            if (schedulerTaskType) {
                act.add(ApplicationConst.AuditAction.RUN);
            }
            act.add(ApplicationConst.AuditAction.PAUSE);
        }

        if (Objects.equals(form.getApplyState(), ApplyStateEnum.normal.name())
                || Objects.equals(form.getApplyState(), ApplyStateEnum.none.name())
                || Objects.equals(form.getApplyState(), ApplyStateEnum.pause.name())) {

            boolean notUsable = Objects.equals(form.getApplyRunState(), ApplyRunStateEnum.wait_audit.name())
                    || Objects.equals(form.getApplyRunState(), ApplyRunStateEnum.audit_reject.name());
            if (customizedType && !notUsable) {
                act.add(ApplicationConst.AuditAction.CONFIG);
            }
            if (Objects.equals(form.getApplyState(), ApplyStateEnum.pause.name())) {
                act.add(ApplicationConst.AuditAction.RESUME);
            }
        }

        if (Objects.equals(form.getApplyRunState(), ApplyRunStateEnum.wait_run.name())) {
            act.add(ApplicationConst.AuditAction.FINISH);
        }
        if (Objects.equals(form.getApplyRunState(), ApplyRunStateEnum.finish.name())
                && !isOnce) {
            act.add(ApplicationConst.AuditAction.ENTER_RUN);
        }

        Integer auditProcess = dto.getCurrentAuditProcessStatus();
        if (Objects.equals(auditProcess, ApplicationConst.AuditStatus.AUDIT_PASS)) {
            if (Objects.equals(dto.getDataState(), ApplyDataStateEnum.success.name())) {
                if (!Objects.equals(dto.getConfigType(), ApplicationConfigTypeConstant.FILE_TYPE)) {
                    act.add(ApplicationConst.AuditAction.PREVIEW);
                }

                Long aid = dto.getAssetsId();
                Optional.ofNullable(assetsMap.get(aid))
                        .map(UserDataAssets::getFtpPath).filter(com.sinohealth.common.utils.StringUtils::isNoneBlank)
                        .ifPresent(v -> act.add(ApplicationConst.AuditAction.DOWNLOAD));
            }
            if (acceptMap.containsKey(dto.getId())) {
                act.add(ApplicationConst.AuditAction.ACCEPTANCE_RECORD);
            }
            act.add(ApplicationConst.AuditAction.DEPRECATED);
        }
        return act;
    }

    public AjaxResult<Void> markRunState(String applicationNo, Integer state) {
        if (Objects.equals(state, ApplicationConst.AuditAction.ENTER_RUN)) {
            Optional<ApplicationForm> formOpt = applicationFormDAO.lambdaQuery()
                    .eq(ApplicationForm::getApplicationNo, applicationNo)
                    .oneOpt();

            Optional<String> bizType = formOpt.map(ApplicationForm::getApplicationId)
                    .flatMap(v -> applicationDAO.lambdaQuery().select(TgApplicationInfo::getId, TgApplicationInfo::getTemplateId)
                            .eq(TgApplicationInfo::getId, v).oneOpt())
                    .map(TgApplicationInfo::getTemplateId)
                    .flatMap(v -> templateInfoDAO.lambdaQuery().select(TgTemplateInfo::getBizType)
                            .eq(TgTemplateInfo::getId, v).oneOpt())
                    .map(TgTemplateInfo::getBizType);
            if (!bizType.isPresent()) {
                alertService.sendDevNormalMsg(applicationNo + " 提前处理失败，映射错误");
                return AjaxResult.error("数据错误");
            }

            CurrentDataPlanDTO dto = dataPlanService.currentPeriod(bizType.get());
            return applicationFormDAO.enterRun(applicationNo, state, dto.getPeriod());
        } else {
            return applicationFormDAO.markRunState(applicationNo, state);
        }
    }

    @Override
    @Transactional
    public void updateOwnerId(Long ownerId, Long id) {
        this.update(Wrappers.<TgApplicationInfo>update().eq("id", id).set("applicant_id", ownerId));
        relationTableManageService.update(Wrappers.<TableInfoDiy>update().eq("create_by", id).set("create_by", ownerId));
    }

    private String getReadableUsers(String readableUsers) {
        StringBuilder sb4User = new StringBuilder();
        StringBuilder sb4Customer = new StringBuilder();
        Arrays.stream(readableUsers.split(",")).forEach(uid -> {
            Optional<SysUser> userOpt = Optional.ofNullable(sysUserService.selectUserById(Long.valueOf(uid)));
            userOpt.ifPresent((user) -> {
                Integer type = user.getUserInfoType();
                if (type == 2) {
                    Optional<SinoPassUserDTO> orgUserInfo = Optional.ofNullable(SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId()));
                    orgUserInfo.ifPresent(u -> {
                        sb4User.append(orgUserInfo.get().getViewName());
                        sb4User.append("\n");
                    });
                }

                if (type == 3) {
                    Optional<SysCustomer> sysCustomerOpt = Optional.ofNullable(sysCustomerService.getByUserId(user.getUserId()));
                    sysCustomerOpt.ifPresent(c -> sb4Customer.append("客户 - ").append(sysCustomerOpt.get().getFullName()).append("\n"));

                }
            });
        });
        return "".equals(sb4Customer.toString()) ? sb4User.toString() : sb4User + "\n" + sb4Customer;
    }
}
