package com.sinohealth.system.dto.template;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @Author Rudolph
 * @Date 2022-05-12 13:58
 * @Desc
 */

@Data
public class TemplateJoinInfoDto {
    private Long tableId1;
    @Size(max = 100, message = "表名长度超出限制")
    private String tableName1;
    private Long joinCol1;
    private String joinColName1;
    private Long tableId2;
    @Size(max = 100, message = "表名长度超出限制")
    private String tableName2;
    private Long joinCol2;
    private String joinColName2;
    private Long joinType;
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself = 1;
    @JsonIgnore
    private String tableDistributeName1;
    @JsonIgnore
    private String tableDistributeName2;
}
