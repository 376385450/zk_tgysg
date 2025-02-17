package com.sinohealth.system.biz.dir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetMyDataDirTreeParam {
    private Long id;
    private Integer searchStatus;
    private Integer requireTimeType;
    private Integer requireAttr;
    private String searchProjectName;
    private String clientNames;
    private String searchBaseTable;
    private Long searchBaseTableId;
    private String expireType;
    private Integer target;
    private Long applicantId;
}
