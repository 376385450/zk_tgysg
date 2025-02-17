package com.sinohealth.system.dto;

import com.sinohealth.system.domain.TgCustomerApplyAuth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("TgCustomerApplyAuthReqDto")
public class TgCustomerApplyAuthReqDto {

    @ApiModelProperty("申请id")
    private Long applyId;

    @ApiModelProperty("对外报表名")
    private String outTableName;

    @ApiModelProperty("客户权限信息集合")
    private List<TgCustomerApplyAuth> list;

    @ApiModelProperty("客户权限信息删除集合")
    private List<Integer> deleteIds;


}
