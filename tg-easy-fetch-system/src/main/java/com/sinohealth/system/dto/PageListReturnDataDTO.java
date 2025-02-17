package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 回传数据分页列表
 *
 * @author linkaiwei
 * @date 2022/2/15 10:17
 * @since 1.6.4.0
 */
@Data
@ApiModel("回传数据分页列表")
public class PageListReturnDataDTO implements Serializable {

    @ApiModelProperty("数据列表")
    private List<Map<String, Object>> list;

    @ApiModelProperty("总数")
    private long total;

}
