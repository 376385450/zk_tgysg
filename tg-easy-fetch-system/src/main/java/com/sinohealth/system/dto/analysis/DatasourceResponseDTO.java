package com.sinohealth.system.dto.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 数据源
 *
 * @author linkaiwei
 * @date 2021/8/16 17:09
 * @since 1.4.2.0
 */
@Data
@ApiModel("数据源")
@Accessors(chain = true)
public class DatasourceResponseDTO implements Serializable {

    @ApiModelProperty("数据集ID")
    private Long id;

    @ApiModelProperty("数据集英文名称")
    private String tableName;

    @ApiModelProperty("数据集名称")
    private String tableAlias;

    @JsonIgnore
    private Integer type;

    @JsonIgnore
    private String tables;

    @JsonIgnore
    private String fields;

    @ApiModelProperty("维度字段")
    private List<FieldDTO> dimensions;

    @ApiModelProperty("度量字段")
    private List<FieldDTO> measures;

}
