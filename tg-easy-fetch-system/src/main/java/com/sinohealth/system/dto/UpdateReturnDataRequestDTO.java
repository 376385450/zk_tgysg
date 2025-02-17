package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 修改回传数据
 *
 * @author linkaiwei
 * @date 2022/2/15 09:21
 * @since 1.6.4.0
 */
@Data
@ApiModel("修改回传数据")
@Accessors(chain = true)
public class UpdateReturnDataRequestDTO implements Serializable {

    @ApiModelProperty("自增code")
    private String increas_code;

    @ApiModelProperty("批准文号或注册证号")
    private String approval_number;

    @ApiModelProperty("药监名称")
    private String sic_drug_name;

    @ApiModelProperty("产品规格")
    private String sic_specification;

    @ApiModelProperty("包装")
    private String sic_packing;

    @ApiModelProperty("上市许可持有人")
    private String marketing_authorization_holder;

    @ApiModelProperty("生产企业")
    private String sic_company;

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("产品编码")
    private String product_code;

    @ApiModelProperty("产品标准编码")
    private String product_standard_code;

    @ApiModelProperty("药品编码唯一ID")
    private String unique_id;

    @ApiModelProperty("是否已经处理过")
    private Boolean is_handle;

}
