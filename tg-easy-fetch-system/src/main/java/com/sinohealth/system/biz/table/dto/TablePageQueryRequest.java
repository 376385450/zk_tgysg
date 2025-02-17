package com.sinohealth.system.biz.table.dto;

import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-29 21:57
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TablePageQueryRequest extends PageRequest {

    @ApiModelProperty("搜索内容")
    private String searchContent;

    @ApiModelProperty("目录")
    private Long dirId;

    /**
     * 排序字段
     */
    private String orderField;
    /**
     * ASC DESC
     */
    private String orderSort;
}
