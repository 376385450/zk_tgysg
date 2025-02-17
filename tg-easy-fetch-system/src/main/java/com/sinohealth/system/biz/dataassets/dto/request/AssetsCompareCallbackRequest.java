package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-22 17:39
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsCompareCallbackRequest {
    private Long compareId;
    private String resultPath;
    private Boolean success;
    private String traceId;

    private String newProdCode;
    private String newDataPeriod;
    private String runLog;
}
