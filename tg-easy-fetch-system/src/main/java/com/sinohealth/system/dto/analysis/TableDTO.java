package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 表信息
 *
 * @author linkaiwei
 * @date 2021/08/18 15:57
 * @since 1.4.1.0
 */
@ApiModel("表信息")
@Data
@Accessors(chain = true)
public class TableDTO implements Serializable {

    @ApiModelProperty("唯一id")
    @NotNull(message = "唯一id不能为空")
    private Long uniqueId;

    @ApiModelProperty("表id")
    private Long tableId;

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("中文名称")
    private String tableAlias;

    @ApiModelProperty("数据目录ID")
    private Long dataDirId;

    @ApiModelProperty("数据目录名称")
    private String dataDirName;

    @ApiModelProperty("数据源ID")
    private Long sourceId;

    @ApiModelProperty("数据源名称")
    private String sourceName;

    @ApiModelProperty("是否是主表")
    private Boolean factTable;

    // 以下字段是前端用
    private Integer x;
    private Integer y;
    private String tableIds;
    private String bgc;
    private String color;
    private String source;

}
