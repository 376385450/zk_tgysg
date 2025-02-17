package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Kuangcp
 * 2024-07-17 15:57
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class AssetsComparePlanRequest extends PageRequest {

    private String projectName;

    private String bizType;

    private Long templateId;
}
