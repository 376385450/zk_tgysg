package com.sinohealth.system.biz.table.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TableDiffListRequest {
    @ApiModelProperty(value = "主键集合")
    private List<Long> ids;

    @ApiModelProperty(value = "关联id集合")
    private List<Long> bizIds;
}
