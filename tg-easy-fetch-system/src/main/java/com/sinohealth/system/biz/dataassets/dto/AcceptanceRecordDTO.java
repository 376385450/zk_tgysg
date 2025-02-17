package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-21 15:00
 */
@Data
public class AcceptanceRecordDTO {

    @ApiModelProperty("主键自增")
    private Long id;

    private Long applicationId;

    private Long assetsId;

    /**
     * 如果资产有版本，存储资产版本
     */
    private Integer version;

    /**
     * @see AcceptanceStateEnum
     */
    private String state;

    private String acceptType;

    private String user;

    private String remark;

    private LocalDateTime acceptTime;

}
