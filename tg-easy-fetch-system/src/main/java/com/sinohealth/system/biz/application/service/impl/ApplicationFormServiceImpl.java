package com.sinohealth.system.biz.application.service.impl;

import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.application.service.ApplicationFormService;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.process.facade.TgFlowProcessFacade;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.dto.DataPlanBizDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Kuangcp
 * 2024-12-23 11:14
 */
@Slf4j
@Service
public class ApplicationFormServiceImpl implements ApplicationFormService {

    @Autowired
    private ApplicationFormDAO applicationFormDAO;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;

    @Autowired
    private AlertService alertService;
    @Autowired
    private DataPlanService dataPlanService;
    @Autowired
    private RedisLock redisLock;


    /**
     * 更新过期的需求单
     */
    @Scheduled(cron = "0 20 0 * * ?")
    public void refreshExpireScheduler() {
        redisLock.wrapperLock(RedisKeys.ApplyForm.EXPIRE_LOCK, () -> {
            List<ApplicationForm> watchList = applicationFormDAO.lambdaQuery()
                    .in(ApplicationForm::getApplyState, ApplyStateEnum.WATCH_EXPIRE)
                    .list();
            if (CollectionUtils.isEmpty(watchList)) {
                return;
            }
            List<Long> applyIds = Lambda.buildList(watchList, ApplicationForm::getApplicationId);
            List<TgApplicationInfo> expireApplys = applicationDAO.lambdaQuery()
                    .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                    .le(TgApplicationInfo::getDataExpir, new Date())
                    .in(TgApplicationInfo::getId, applyIds)
                    .list();
            if (CollectionUtils.isEmpty(expireApplys)) {
                return;
            }

            Set<String> nos = Lambda.buildSet(expireApplys, TgApplicationInfo::getApplicationNo);
            log.info("expire nos={}", nos);
            applicationFormDAO.lambdaUpdate()
                    .in(ApplicationForm::getApplicationNo, nos)
                    .set(ApplicationForm::getApplyState, ApplyStateEnum.expire.name())
                    .update();
        });
    }

    /**
     * 定时检查 持续性需求 需求单状态 完成->待处理
     *
     * @see TgFlowProcessFacade#filterDeliverTimeType 月份处理
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void refreshHandleFormScheduler() {
        redisLock.wrapperLock(RedisKeys.ApplyForm.EXPIRE_LOCK, () -> {
        });

        List<ApplicationForm> finishList = applicationFormDAO.lambdaQuery()
                .eq(ApplicationForm::getApplyState, ApplyStateEnum.normal.name())
                .eq(ApplicationForm::getApplyRunState, ApplyRunStateEnum.finish.name())
                .list();
        if (CollectionUtils.isEmpty(finishList)) {
            return;
        }

        LocalDate expect = LocalDate.now().plusDays(3);
        Map<String, DataPlanBizDTO> nextMap = dataPlanService.queryNextDeliverDate();
        Map<String, DataPlanBizDTO> runNextMap = nextMap.entrySet().stream()
                .filter(v -> !v.getValue().getDeliverDate().isAfter(expect))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (front, current) -> current));
        if (MapUtils.isEmpty(runNextMap)) {
            return;
        }
        Map<String, List<ApplicationForm>> bizMap = finishList.stream()
                .filter(v -> runNextMap.containsKey(v.getBizType()))
                .collect(Collectors.groupingBy(ApplicationForm::getBizType));
        if (MapUtils.isEmpty(bizMap)) {
            return;
        }

        List<Long> applyIds = bizMap.values().stream().flatMap(Collection::stream)
                .map(ApplicationForm::getApplicationId).collect(Collectors.toList());
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getTemplateId, TgApplicationInfo::getDeliverTimeType)
                .eq(TgApplicationInfo::getRequireTimeType, ApplicationConst.RequireTimeType.PERSISTENCE)
                .in(TgApplicationInfo::getId, applyIds).list();
        if (CollectionUtils.isEmpty(applyList)) {
            return;
        }
        Map<Long, TgApplicationInfo> applyMap = Lambda.buildMap(applyList, TgApplicationInfo::getId);

        Map<String, List<String>> updateMap = new HashMap<>();
        for (Map.Entry<String, DataPlanBizDTO> entry : runNextMap.entrySet()) {
            String bizType = entry.getKey();
            List<String> timeType = dataPlanService.queryNextDeliverTimeType(bizType);
            List<ApplicationForm> list = bizMap.get(bizType);
            if (CollectionUtils.isEmpty(list)) {
                continue;
            }

            List<String> updateNos = new ArrayList<>();
            updateMap.put(entry.getKey(), updateNos);
            for (ApplicationForm form : list) {
                TgApplicationInfo info = applyMap.get(form.getApplicationId());
                if (Objects.isNull(info)) {
                    continue;
                }

                if (timeType.contains(info.getDeliverTimeType())
                        && !Objects.equals(form.getPeriod(), entry.getValue().getPeriod())) {
                    updateNos.add(form.getApplicationNo());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : updateMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                log.info("update {}: {}", entry.getKey(), entry.getValue());
                applicationFormDAO.lambdaUpdate()
                        .set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.wait_run.name())
                        .set(ApplicationForm::getPeriod, runNextMap.get(entry.getKey()).getPeriod())
                        .in(ApplicationForm::getApplicationNo, entry.getValue())
                        .update();
            }
        }
    }

    /**
     * 待处理 => 处理中
     */
    @Override
    public void runApplication(Long id, String no) {
        log.info("Run Apply: id={} no={}", id, no);
        // 注意： 一次性 持续性的分类只看最新通过单据的状态
        // 二次出数定义：底表更新，二次点执行，工作流出数，全流程管理；
        // 一次出数定义（申请，重新申请） 抽象为只看 enterRun 字段
        Optional<ApplicationForm> formOpt = applicationFormDAO.lambdaQuery().eq(ApplicationForm::getApplicationNo, no).oneOpt();
        if (!formOpt.isPresent()) {
            alertService.sendDevNormalMsg("处理中：需求单缺失 " + no);
            return;
        }
        ApplicationForm form = formOpt.get();
        boolean preFail = Objects.equals(form.getApplyRunState(), ApplyRunStateEnum.run_failed.name());
        // 白名单匹配则直接流转 一次（申请，重新申请）全部流转，二次： 验收不通过，处理失败
        if (BooleanUtils.isFalse(form.getEnterRun()) || preFail) {
            this.updateRunningState(id);
            return;
        }

        // 注意 enterRun 为true 意味着是二次出数

        // 不符合白名单的黑名单逻辑， 匹配则不流转
        //  一次性二次
        // 持续性 二次，非交付
        Optional<TgApplicationInfo> applyOpt = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getRequireTimeType, TgApplicationInfo::getTemplateId)
                .eq(TgApplicationInfo::getId, id)
                .oneOpt();
        if (!applyOpt.isPresent()) {
            alertService.sendDevNormalMsg("处理中：申请单缺失" + id);
            return;
        }
        TgApplicationInfo info = applyOpt.get();
        if (Objects.equals(info.getRequireTimeType(), ApplicationConst.RequireTimeType.ONCE)) {
            return;
        }
        Optional<TgTemplateInfo> tempOpt = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getBizType)
                .eq(TgTemplateInfo::getId, info.getTemplateId())
                .oneOpt();
        if (!tempOpt.isPresent()) {
            alertService.sendDevNormalMsg("处理中：模板缺失 " + id + " " + info.getTemplateId());
            return;
        }
        TgTemplateInfo temp = tempOpt.get();
        CurrentDataPlanDTO dto = dataPlanService.currentPeriod(temp.getBizType());
        if (!Objects.equals(dto.getFlowProcessType(), FlowProcessTypeEnum.deliver.name())) {
            return;
        }
        // 剩下的流转
        this.updateRunningState(id);
    }

    @Override
    public void updateRunState(String no, ApplyRunStateEnum runState) {
        applicationFormDAO.updateRunState(no, runState);
    }

    private void updateRunningState(Long id) {
        applicationFormDAO.lambdaUpdate()
                .eq(ApplicationForm::getApplicationId, id)
                .set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.running.name())
                .set(ApplicationForm::getEnterRun, true)
//                .set(ApplicationForm::getApplyState, ApplyStateEnum.normal.name())
                .update();
    }
}
