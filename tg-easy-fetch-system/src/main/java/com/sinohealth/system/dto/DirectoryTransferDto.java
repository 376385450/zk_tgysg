package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: ChenJiaRong
 * Date:   2021/7/21
 * Explain:
 */
@Data
@ApiModel("转移字典目录对象")
public class DirectoryTransferDto {

    @ApiModelProperty("转移目录id")
    private Long treeId;

    @ApiModelProperty("转移字典id")
    private List<Long> dictId;

}
