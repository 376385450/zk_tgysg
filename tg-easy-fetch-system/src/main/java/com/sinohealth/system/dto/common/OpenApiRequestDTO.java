package com.sinohealth.system.dto.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * openApi 请求参数
 *
 * @author linkaiwei
 * @date 2021/7/22 17:40
 * @since dev
 */
@ApiModel
@Data
public class OpenApiRequestDTO {

    @ApiModelProperty("页码")
    private Long page;

    @ApiModelProperty("一页数量")
    private Long size;

    @ApiModelProperty("token")
    private String token;

}
