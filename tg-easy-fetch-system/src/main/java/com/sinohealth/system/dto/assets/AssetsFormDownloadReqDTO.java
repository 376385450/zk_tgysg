package com.sinohealth.system.dto.assets;

import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 客户资产-数据表单下载
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 16:32
 */
@Data
public class AssetsFormDownloadReqDTO {

    @NotNull(message = "资产id不能为空")
    private Long assetsId;

    @NotBlank(message = "downloadType不能为空")
    private String downloadType;

    private GetDataInfoRequestDTO filter;
}
