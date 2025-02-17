package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 15:51
 */
@Data
@Accessors(chain = true)
@ApiModel("客户资产目录树查询")
public class AssetsDirTreeQuery implements Serializable {

    @ApiModelProperty("查询关键字")
    private String searchKey;
}
