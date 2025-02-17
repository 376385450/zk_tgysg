package com.sinohealth.system.dto.label;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:39
 */
@Data
@ApiModel("分页查询标签列表参数")
public class PageQueryLabelRequest {

    @ApiModelProperty("当前页")
    @NotNull(message = "当前页不能为空")
    private Integer pageNum;

    @ApiModelProperty("分页大小")
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @ApiModelProperty("标签名称")
    private String name;
}
