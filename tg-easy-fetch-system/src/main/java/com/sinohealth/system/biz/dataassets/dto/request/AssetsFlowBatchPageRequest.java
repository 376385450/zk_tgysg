package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Kuangcp
 * 2024-07-15 15:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AssetsFlowBatchPageRequest extends PageRequest {
    private String name;
    private String templateName;
    private String bizType;
    private String state;

}
