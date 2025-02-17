package com.sinohealth.system.biz.application.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.domain.ApplicationForm;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author Kuangcp
 * 2024-12-10 14:09
 */
public interface ApplicationFormDAO extends IService<ApplicationForm> {

    Optional<ApplicationForm> queryByNo(String applyNo);

    Set<String> queryPause(Collection<String> except);

    /**
     * 提交申请 新建或重新申请
     */
    void submitApply(Long id, String no, String period, String bizType);

    void auditApplyPass(Long id, String no, String period);

    void auditApplyReject(Long id, String no);

    void updateRunState(String no, ApplyRunStateEnum runState);

    void updateRunState(Collection<String> no, ApplyRunStateEnum runState);

    void acceptReject(String no, String period);

    AjaxResult<Void> markRunState(String applicationNo, Integer state);

    AjaxResult<Void> enterRun(String applicationNo, Integer state, String period);

}
