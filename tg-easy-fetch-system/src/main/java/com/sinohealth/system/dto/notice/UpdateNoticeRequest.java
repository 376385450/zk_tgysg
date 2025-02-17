package com.sinohealth.system.dto.notice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 15:16
 */
@Data
@ApiModel("更新公告的请求参数")
public class UpdateNoticeRequest {

    @ApiModelProperty("公告ID")
    @NotNull
    private Long id;

    @ApiModelProperty("公告类型")
    private String noticeType;

    @ApiModelProperty("公告名称")
    @NotBlank(message = "公告名称不能为空")
    private String name;

    @ApiModelProperty("公告内容")
    private String content;

    @ApiModelProperty("公告跳转")
    private List<Long> assetIds;

    @ApiModelProperty("是否置顶，0：不置顶，1：置顶")
    private Integer isTop;
}
