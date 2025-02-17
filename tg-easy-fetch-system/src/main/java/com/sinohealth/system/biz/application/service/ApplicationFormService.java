package com.sinohealth.system.biz.application.service;

import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;

/**
 * @author Kuangcp
 * 2024-12-23 11:13
 */
public interface ApplicationFormService {

    void refreshHandleFormScheduler();

    void refreshExpireScheduler();

    void runApplication(Long id, String no);

    void updateRunState(String no, ApplyRunStateEnum runState);
}
