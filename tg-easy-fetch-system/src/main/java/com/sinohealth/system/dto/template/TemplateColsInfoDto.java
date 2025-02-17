package com.sinohealth.system.dto.template;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-12 13:59
 * @Desc
 */
@Data
public class TemplateColsInfoDto {
    private Long tableId;
    private String tableName;
    private List<Long> select;
    @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
    private Integer isItself = 1;
}
