package com.sinohealth.system.dto.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 11:09
 */
@Data
@ApiModel("分页查询模型的审批单")
public class PageQueryModelApplicationRequest {

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

    @ApiModelProperty("需求性质 1：内部分析、2：交付客户、3：对外宣传")
    private Integer requireAttr;

    @ApiModelProperty("客户名称")
    private String clientNames;

    @ApiModelProperty("需求类型，1：一次性需求、2：持续性需求")
    private String requireTimeType;

    @ApiModelProperty("申请时间排序，desc：降序，asc：升序")
    private String order = "desc";
}
