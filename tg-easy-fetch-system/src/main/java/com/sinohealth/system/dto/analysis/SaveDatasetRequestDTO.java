package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 更新自定义数据集，请求参数
 *
 * @author linkaiwei
 * @date 2021/08/16 11:09
 * @since 1.4.1.0
 */
@Data
@ApiModel("更新自定义数据集，请求参数")
@Accessors(chain = true)
public class SaveDatasetRequestDTO {

    @ApiModelProperty("名称")
    @NotBlank(message = "数据集名称不能为空")
    @Size(min = 1, max = 20, message = "数据集名称长度不能超过20个字")
    private String name;

    @ApiModelProperty("英文名称")
    @NotBlank(message = "数据集英文名称不能为空")
    private String englishName;

    @ApiModelProperty("类型，1自定义数据集，2EXCEL数据集")
    @NotNull(message = "数据集类型不能为空")
    private Integer type;

    @ApiModelProperty("描述")
    private String description;


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
    private Long limit;

}
