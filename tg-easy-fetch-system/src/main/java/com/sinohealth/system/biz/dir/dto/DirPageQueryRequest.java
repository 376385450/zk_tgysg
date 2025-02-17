package com.sinohealth.system.biz.dir.dto;

import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-20 21:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DirPageQueryRequest extends PageRequest {

    @ApiModelProperty("搜索内容")
    private String searchContent;
    @ApiModelProperty("目录")
    private Long dirId;
    @ApiModelProperty("类型 资产类型")
    private String applicationType;

    @ApiModelProperty("目录集合 仅后端使用")
    private Collection<Long> dirIds;

    private Integer status;

}
