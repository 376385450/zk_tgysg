package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/11
 */
@Data
@ApiModel("部门")
public class DeptDTO implements Serializable {

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("name")
    private String name;

    @ApiModelProperty("全路径")
    private String fullPath;

   /* @ApiModelProperty("子部门")
    private List<DeptDTO> children;*/

}
