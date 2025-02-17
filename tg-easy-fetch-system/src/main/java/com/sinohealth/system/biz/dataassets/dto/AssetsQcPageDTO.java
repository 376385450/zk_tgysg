package com.sinohealth.system.biz.dataassets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-17 15:40
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsQcPageDTO {

    private Long id;
    private String batchNo;
    private String templateName;
    private Integer skuCnt;
    private Integer brandCnt;
    private String state;

    /**
     * 关联全流程名称
     */
    private String flowProcessName;

    private String flowProcessCategory;

    private LocalDateTime createTime;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    /**
     * 耗时
     */
    private String costTime;

    private String creator;

}
