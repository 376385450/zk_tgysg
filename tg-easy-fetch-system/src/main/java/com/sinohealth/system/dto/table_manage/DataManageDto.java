package com.sinohealth.system.dto.table_manage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@ApiModel("DataManageDto")
public class DataManageDto {

    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("目录名称")
    private String dirName;
    @ApiModelProperty("上级id")
    private Long parentId;

    private Integer sort;

    @ApiModelProperty("表单对象")
    private  List<DataManageFormDto> dataManageFormDtos;

    private List<DataManageDto> children = new ArrayList<>();


}
