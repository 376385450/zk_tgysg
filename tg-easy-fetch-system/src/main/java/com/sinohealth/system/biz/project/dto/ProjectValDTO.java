package com.sinohealth.system.biz.project.dto;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-16 14:48
 */
@Data
@Builder
public class ProjectValDTO {

    private Long projectId;
    private String projectName;
}
