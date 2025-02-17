package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.application.dto.request.ColumnSetting;
import com.sinohealth.system.domain.ApplicationColumnSetting;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/12/29
 */
public interface ApplicationColumnSettingDAO  extends IService<ApplicationColumnSetting> {

    List<ColumnSetting> getByApplicationId(Long applicationId);

}
