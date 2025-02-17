package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AssetsDataDownloadRequest {
    /**
     * 用户资产id
     */
    @NotNull(message = "资产id集合不可为空")
    private List<Long> assetsIds;
}
