package com.sinohealth.system.biz.application.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-25 17:51
 */
public interface ApplicationTaskConfigDAO extends IService<ApplicationTaskConfig> {

    ApplicationTaskConfig queryByApplicationId(Long applicationId);
}
