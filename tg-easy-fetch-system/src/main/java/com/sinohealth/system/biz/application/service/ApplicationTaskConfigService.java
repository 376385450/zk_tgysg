package com.sinohealth.system.biz.application.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.TgApplicationInfo;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-07 09:53
 */
public interface ApplicationTaskConfigService {

    /**
     * 保存工作流模板申请 配置参数
     */
    void saveApplicationTaskConfig(TgApplicationInfo applyInfo);

    /**
     * 填充没有配置的申请记录
     */
    AjaxResult<Void> appendSaveApplyConfig(Integer batch);
}
