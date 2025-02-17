package com.sinohealth.system.biz.application.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/12/29
 */
@Data
public class ApplicationColumnSettingRequest {

    private Long applicationId;

    private Boolean exportProjectName;

    private List<ColumnSetting> columnSettings;

}
