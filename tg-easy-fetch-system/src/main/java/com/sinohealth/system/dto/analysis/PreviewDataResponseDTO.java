package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 执行SQL，响应参数
 *
 * @author linkaiwei
 * @date 2021/8/17 11:42
 * @since 1.4.1.0
 */
@Data
@ApiModel("执行SQL，响应参数")
@Accessors(chain = true)
public class PreviewDataResponseDTO implements Serializable {

    @ApiModelProperty("字段信息")
    private List<FieldDTO> fields;

    @ApiModelProperty("SQL执行返回结果")
    private List<Map<String, Object>> resultList;

    @ApiModelProperty("关联关系SQL")
    private String sql;

    @ApiModelProperty("行，度量")
    private List<String> rows;

    @ApiModelProperty("列，维度")
    private List<String> columns;

}
