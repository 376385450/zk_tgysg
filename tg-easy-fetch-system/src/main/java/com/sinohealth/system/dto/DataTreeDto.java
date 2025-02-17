package com.sinohealth.system.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/19
 */
@Data
@ApiModel("DataTreeDto")
public class DataTreeDto {

    private Long id;

    private String dirName;

    private Long parentId;

    private Integer datasourceId;

    private Boolean isTable = false;

    @ApiModelProperty("通用类型")
    private JSONObject data;

    private Integer sort;
    @ApiModelProperty("表前缀")
    private String prefix;

    private String only;

    private List<DataTreeDto> children = new ArrayList<>();
}
