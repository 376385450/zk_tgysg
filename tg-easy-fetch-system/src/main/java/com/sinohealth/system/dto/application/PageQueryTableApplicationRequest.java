package com.sinohealth.system.dto.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: 张子锋
 * @Date: 2023/8/24 16:18
 */
@Data
@ApiModel("分页查询库表类型的审批单")
public class PageQueryTableApplicationRequest {

    @ApiModelProperty("当前页")
    @NotNull(message = "当前页不能为空")
    private Integer pageNum;

    @ApiModelProperty("分页大小")
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("流程状态集合（1：审核中、2：审核通过、3：审核失败、5：撤销审核")
    private List<Integer> processStatusList;

    @ApiModelProperty("申请时间排序，desc：降序，asc：升序")
    private String order = "desc";
}
