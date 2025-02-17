package com.sinohealth.system.biz.project.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-12-11 11:01
 */
@Data
public class DataPlanYearRequest {

    private List<BizTypePlanVo> plans;
}
