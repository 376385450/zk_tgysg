package com.sinohealth.system.dto.assets;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 16:47
 */
@Data
public class AssetsDownloadReqDTO {

    @NotNull(message = "dirId不能为空")
    private Long dirId;

    @NotBlank(message = "downloadType不能为空")
    private String downloadType;
}
