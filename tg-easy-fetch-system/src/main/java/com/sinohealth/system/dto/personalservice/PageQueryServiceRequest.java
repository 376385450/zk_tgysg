package com.sinohealth.system.dto.personalservice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 10:46
 */
@Data
@ApiModel("分页查询我的服务列表参数")
public class PageQueryServiceRequest {

    @ApiModelProperty("资产类型，MODEL、TABLE、FILE")
    @NotBlank(message = "资产类型不能为空")
    private String assetType;

    @ApiModelProperty("当前页")
    @NotNull(message = "当前页不能为空")
    private Integer pageNum;

    @ApiModelProperty("分页大小")
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("服务类型")
    private String assetOpenService;

    @ApiModelProperty("服务状态，1：可使用，0：已过期")
    private Integer serviceStatus;

    @ApiModelProperty("排序，升序：asc，降序：desc")
    private String order;

    public <T> IPage<T> buildPage() {
        return new Page<T>().setSize(this.getPageSize()).setCurrent(this.getPageNum());
    }

    private Long userId;
}
