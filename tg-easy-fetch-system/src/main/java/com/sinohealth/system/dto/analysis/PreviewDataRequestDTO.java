package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 预览数据，请求参数
 *
 * @author linkaiwei
 * @date 2021/8/17 11:49
 * @since 1.4.1.0
 */
@Data
@ApiModel("预览数据，请求参数")
@Accessors(chain = true)
public class PreviewDataRequestDTO implements Serializable {

    @ApiModelProperty("表信息")
    private List<TableDTO> tables;

    @ApiModelProperty("表关联信息")
    private List<LinkDTO> links;

    @ApiModelProperty("字段信息")
    private List<FieldDTO> fields;

    @ApiModelProperty("过滤信息")
    private FilterDTO filter;

    @ApiModelProperty("排序信息")
    private List<SortDTO> sorts;

    @ApiModelProperty("行数筛选，默认输出500行")
    private Long limit = 500L;

}
