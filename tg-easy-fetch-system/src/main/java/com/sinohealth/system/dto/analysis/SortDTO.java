package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 排序信息
 *
 * @author linkaiwei
 * @date 2021/08/18 17:15
 * @since 1.4.1.0
 */
@Data
@ApiModel("排序信息")
@Accessors(chain = true)
public class SortDTO implements Serializable {

    @ApiModelProperty("唯一id")
    private Long uniqueId;

    @ApiModelProperty("表ID")
    private Long tableId;

    @ApiModelProperty("字段ID")
    private Long fieldId;

    @ApiModelProperty("英文名称")
    private String fieldName;

    @ApiModelProperty("中文名称")
    private String fieldAlias;

    @ApiModelProperty("排序类型，asc正序，desc倒序")
    private String sortType;

}
