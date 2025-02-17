package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/9/19
 */
@Data
@ApiModel("修改排序")
public class ChangeSortDTO implements Serializable {

    @ApiModelProperty("资产id")
    private Long assetId;

    @ApiModelProperty("排序号")
    private Long sortNum;

}
