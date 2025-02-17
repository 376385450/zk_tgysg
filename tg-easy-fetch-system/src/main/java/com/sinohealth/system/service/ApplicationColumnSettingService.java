package com.sinohealth.system.service;

import com.sinohealth.system.biz.application.dto.request.ColumnSetting;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/12/28
 */
public interface ApplicationColumnSettingService {

    void saveColumnSetting(List<ColumnSetting> columns, Long applicationId);

}
