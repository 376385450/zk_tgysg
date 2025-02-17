package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AssetsDataDownloadGetRequest {
    /**
     * 用户资产id
     */
    private String assetsIds;
}
