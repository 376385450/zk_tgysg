package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-17 17:52
 */
@Data
public class AssetsPlanSaveRequest {

    private List<Long> ids;
}
