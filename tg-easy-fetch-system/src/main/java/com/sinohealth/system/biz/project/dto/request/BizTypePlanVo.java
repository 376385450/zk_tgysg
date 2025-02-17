package com.sinohealth.system.biz.project.dto.request;

import lombok.Data;

/**
 * @author Kuangcp
 * 2024-12-11 11:03
 */
@Data
public class BizTypePlanVo {

    private String bizType;
    private Integer qc;
    private Integer sop;
    private Integer deliver;
}
