package com.sinohealth.system.biz.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@Data
@ApiModel("资产应用情况")
public class AssetApply implements Serializable {

    @ApiModelProperty("部门名")
    private String deptName;

    @ApiModelProperty("库表数量")
    private Integer tableCount;

    @ApiModelProperty("模型数量")
    private Integer moduleCount;

    @ApiModelProperty("文件数量")
    private Integer fileCount;


}
