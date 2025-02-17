package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Kuangcp
 * 2024-12-11 10:42
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ApplicationFormPageRequest extends PageRequest {
    private String applicationNo;
}
