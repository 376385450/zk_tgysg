package com.sinohealth.system.biz.project.dto.request;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author Kuangcp
 * 2024-12-11 11:06
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DataPlanDetailPageRequest extends PageRequest {
    /**
     * 版本类型
     */
    private List<String> flowProcessType;

    private List<String> period;

    private List<String> bizType;

    private Date startTime;

    private Date endTime;

}
