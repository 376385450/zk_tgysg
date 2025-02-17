package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-17 14:38
 */
@Data
public class AssetsCompareDownloadRequest {

    private List<Long> autoIds;
    private List<Long> handleIds;
    private List<Long> fileIds;

    private List<String> prodCodeList;

    private Boolean saveSelect;

}
