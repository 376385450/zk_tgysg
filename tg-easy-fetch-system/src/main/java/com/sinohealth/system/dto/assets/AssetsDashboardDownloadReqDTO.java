package com.sinohealth.system.dto.assets;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 客户资产-仪表板下载
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 16:32
 */
@Data
public class AssetsDashboardDownloadReqDTO {

    @NotNull(message = "arkId不能为空")
    private Long arkId;

    @NotBlank(message = "downloadType不能为空")
    private String downloadType;

}
