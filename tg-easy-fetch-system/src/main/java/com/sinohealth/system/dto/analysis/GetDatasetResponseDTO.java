package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询自定义数据集，响应参数
 *
 * @author linkaiwei
 * @date 2021/8/16 14:17
 * @since 1.4.1.0
 */
@Data
@ApiModel("查询自定义数据集，响应参数")
@Accessors(chain = true)
public class GetDatasetResponseDTO implements Serializable {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("英文名称")
    private String englishName;

    @ApiModelProperty("类型，1自定义数据集，2EXCEL数据集")
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

    @ApiModelProperty("数据集SQL语句")
    private String datasetSql;


    @ApiModelProperty("创建人ID")
    private Long createBy;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人ID")
    private Long updateBy;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态：0删除，1正常，2停用")
    private Integer status;

}
