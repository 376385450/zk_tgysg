package com.sinohealth.system.dto.notice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:29
 */
@Data
@ApiModel("分页查询公告请求参数")
public class PageQueryNoticeRequest {

    @ApiModelProperty("当前页")
    @NotNull(message = "当前页不能为空")
    private Integer pageNum;

    @ApiModelProperty("分页大小")
    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @ApiModelProperty("搜索字符串，输入类型或者名称")
    private String searchStr;

    @ApiModelProperty("已读未读")
    private Boolean read;

    private Set<Long> readList;

}
