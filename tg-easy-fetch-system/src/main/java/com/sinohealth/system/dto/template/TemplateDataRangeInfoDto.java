package com.sinohealth.system.dto.template;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-05-12 14:00
 * @Desc
 */

@Data
public class TemplateDataRangeInfoDto {
    private Long tableId;
    private String tableName;
    private String copyfieldName;
    private Long colName;
    private String content;
    private Long computeWay;
    private Long isNecessary;
    private String type;
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself = 1;
}
