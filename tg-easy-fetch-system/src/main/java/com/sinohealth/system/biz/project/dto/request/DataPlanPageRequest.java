package com.sinohealth.system.biz.project.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Kuangcp
 * 2024-12-11 11:31
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataPlanPageRequest extends PageRequest {

    private String bizType;
}
