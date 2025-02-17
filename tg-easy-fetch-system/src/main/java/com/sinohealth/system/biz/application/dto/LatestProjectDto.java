package com.sinohealth.system.biz.application.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-04 15:16
 */
@Data
public class LatestProjectDto implements Serializable {
    private Long id;
    private String projectName;
}
