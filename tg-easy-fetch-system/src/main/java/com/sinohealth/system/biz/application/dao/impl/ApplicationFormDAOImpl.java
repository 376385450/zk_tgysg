package com.sinohealth.system.biz.application.dao.impl;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.application.mapper.ApplicationFormMapper;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.service.impl.DataAssetsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kuangcp
 * 2024-12-10 14:09
 */
@Slf4j
@Repository
public class ApplicationFormDAOImpl
        extends ServiceImpl<ApplicationFormMapper, ApplicationForm>
        implements ApplicationFormDAO {

    @Override
    public Optional<ApplicationForm> queryByNo(String applyNo) {
        return lambdaQuery().eq(ApplicationForm::getApplicationNo, applyNo).oneOpt();
    }

    /**
     *
     */
    @Override
    public Set<String> queryPause(Collection<String> except) {
        if (CollectionUtils.isEmpty(except)) {
            return Collections.emptySet();
        }

        List<ApplicationForm> list = lambdaQuery()
                .select(ApplicationForm::getApplicationNo)
                .in(ApplicationForm::getApplicationNo, except)
                .eq(ApplicationForm::getApplyState, ApplyStateEnum.pause.name())
                .list();
        return list.stream().map(ApplicationForm::getApplicationNo).collect(Collectors.toSet());
    }

    @Override
    public void submitApply(Long id, String no, String period, String bizType) {
        Optional<ApplicationForm> lastOpt = lambdaQuery()
                .eq(ApplicationForm::getApplicationNo, no)
                .oneOpt();
        boolean present = lastOpt.isPresent();
        if (present) {
            // 刚提交的单，未审核通过，不予更新状态, 只有驳回的单才更新状态 绑定id不变
            ApplicationForm form = lastOpt.get();
            if (Objects.equals(form.getApplicationId(), id)
                    && Objects.equals(form.getApplyRunState(), ApplyRunStateEnum.audit_reject.name())) {
                lambdaUpdate()
                        .eq(ApplicationForm::getApplicationNo, no)
                        .set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.wait_audit.name())
                        .set(ApplicationForm::getApplyState, ApplyStateEnum.none.name())
                        .set(ApplicationForm::getPeriod, period)
                        .update();
            }
        } else {
            // 初始化 绑定申请单的id
            ApplicationForm form = new ApplicationForm();
            form.setApplicationNo(no)
                    .setApplicationId(id)
                    .setApplyState(ApplyStateEnum.none.name())
                    .setApplyRunState(ApplyRunStateEnum.wait_audit.name())
                    .setPeriod(period)
                    .setBizType(bizType)
            ;
            save(form);
        }

    }

    /**
     * 审核通过 更换绑定的申请id
     * 待审核 -> 待处理
     */
    @Override
    public void auditApplyPass(Long id, String no, String period) {
        lambdaUpdate()
                .eq(ApplicationForm::getApplicationNo, no)
                .set(ApplicationForm::getApplicationId, id)
                .set(ApplicationForm::getEnterRun, false)
                .set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.wait_run.name())
                .set(ApplicationForm::getApplyState, ApplyStateEnum.normal.name())
                .set(ApplicationForm::getPeriod, period)
                .update();
    }

    /**
     *
     */
    @Override
    public void auditApplyReject(Long id, String no) {
        Optional<ApplicationForm> lastOpt = lambdaQuery()
                .eq(ApplicationForm::getApplicationNo, no)
                .oneOpt();
        if (!lastOpt.isPresent()) {
            log.error("Invalid data state: id={} no={}", id, no);
            return;
        }
        ApplicationForm form = lastOpt.get();
        if (Objects.equals(form.getApplicationId(), id)) {
            lambdaUpdate()
                    .eq(ApplicationForm::getApplicationNo, no)
                    .set(ApplicationForm::getApplicationId, id)
                    .set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.audit_reject.name())
                    .set(ApplicationForm::getApplyState, ApplyStateEnum.none.name())
                    .update();
        }
    }

    @Override
    public void updateRunState(String no, ApplyRunStateEnum runState) {
        log.info("Change {} {}", no, runState.name());
        // TODO 前置状态检查
        lambdaUpdate()
                .eq(ApplicationForm::getApplicationNo, no)
                .set(ApplicationForm::getApplyRunState, runState.name())
                // 失败时标记需要进入处理中
                .set(ApplicationForm::getEnterRun, !Objects.equals(runState, ApplyRunStateEnum.run_failed))
                .update();
    }

    @Override
    public void updateRunState(Collection<String> no, ApplyRunStateEnum runState) {
        if (CollectionUtils.isEmpty(no)) {
            return;
        }
        log.info("Change {} {}", no, runState.name());
        // TODO 前置状态检查
        lambdaUpdate()
                .in(ApplicationForm::getApplicationNo, no)
                .set(ApplicationForm::getApplyRunState, runState.name())
                // 失败时标记需要进入处理中
                .set(ApplicationForm::getEnterRun, !Objects.equals(runState, ApplyRunStateEnum.run_failed))
                .update();
    }

    /**
     *
     */
    @Override
    public void acceptReject(String no, String period) {
        log.info("Accept Reject {}", no);

        // TODO 前置状态检查
        lambdaUpdate()
                .eq(ApplicationForm::getApplicationNo, no)
                .set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.wait_run)
                // 失败时标记需要进入处理中
                .set(ApplicationForm::getEnterRun, false)
                .set(ApplicationForm::getPeriod, period)
                .update();
    }

    /**
     * @see DataAssetsServiceImpl#buildActions 可操作按钮
     */
    @Override
    public AjaxResult<Void> markRunState(String applicationNo, Integer state) {
        if (Objects.isNull(state) || StringUtils.isBlank(applicationNo)) {
            return AjaxResult.error("参数缺失");
        }

        LambdaUpdateChainWrapper<ApplicationForm> wrapper = lambdaUpdate();
        if (Objects.equals(state, ApplicationConst.AuditAction.FINISH)) {
            wrapper.set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.finish.name());

            wrapper.eq(ApplicationForm::getApplyRunState, ApplyRunStateEnum.wait_run.name());
        } else if (Objects.equals(state, ApplicationConst.AuditAction.PAUSE)) {
            wrapper.set(ApplicationForm::getApplyState, ApplyStateEnum.pause.name());

            wrapper.eq(ApplicationForm::getApplyState, ApplyStateEnum.normal.name());
        } else if (Objects.equals(state, ApplicationConst.AuditAction.RESUME)) {
            wrapper.eq(ApplicationForm::getApplyState, ApplyStateEnum.pause.name());

            wrapper.set(ApplicationForm::getApplyState, ApplyStateEnum.normal.name());
        } else if (Objects.equals(state, ApplicationConst.AuditAction.DEPRECATED)) {
//            wrapper.eq(ApplicationForm::getApplyState, ApplyStateEnum.pause.name());
            wrapper.set(ApplicationForm::getApplyState, ApplyStateEnum.deprecated.name());
        } else {
            return AjaxResult.error("不支持的操作");
        }

        boolean update = wrapper.eq(ApplicationForm::getApplicationNo, applicationNo).update();
        if (update) {
            return AjaxResult.succeed();
        } else {
            return AjaxResult.error("操作失败");
        }
    }

    @Override
    public AjaxResult<Void> enterRun(String applicationNo, Integer state, String period) {
        if (Objects.isNull(state) || StringUtils.isBlank(applicationNo)) {
            return AjaxResult.error("参数缺失");
        }

        LambdaUpdateChainWrapper<ApplicationForm> wrapper = lambdaUpdate();
        if (Objects.equals(state, ApplicationConst.AuditAction.ENTER_RUN)) {
            wrapper.set(ApplicationForm::getApplyRunState, ApplyRunStateEnum.wait_run.name());
            wrapper.set(ApplicationForm::getPeriod, period);
            wrapper.eq(ApplicationForm::getApplyRunState, ApplyRunStateEnum.finish.name());
        } else {
            return AjaxResult.error("不支持的操作");
        }

        boolean update = wrapper
                .eq(ApplicationForm::getApplicationNo, applicationNo)
                .update();
        if (update) {
            return AjaxResult.succeed();
        } else {
            return AjaxResult.error("操作失败");
        }
    }
}
