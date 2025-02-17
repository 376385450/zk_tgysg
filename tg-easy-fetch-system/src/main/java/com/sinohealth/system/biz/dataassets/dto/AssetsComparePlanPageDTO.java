package com.sinohealth.system.biz.dataassets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kuangcp
 * 2024-07-17 15:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsComparePlanPageDTO {

    private Long id;
    private String bizType;
    private String newProjectName;
    private String projectName;
    private String templateName;
    private Boolean planCompare;
}
