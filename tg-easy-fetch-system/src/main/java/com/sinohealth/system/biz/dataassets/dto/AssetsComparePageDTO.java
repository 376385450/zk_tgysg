package com.sinohealth.system.biz.dataassets.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 10:34
 */
@Data
public class AssetsComparePageDTO {

    private Long id;
    private String bizType;
    private String projectName;
    private String assetsName;
    private String templateName;
    private String tableName;
    private String prodCode;
    private String curVersionPeriod;
    private String preVersionPeriod;
    private String state;
    private String applicant;
    private Boolean deleted;

    private String creator;
    private LocalDateTime createTime;
}
