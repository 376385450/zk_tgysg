package com.sinohealth.system.dto.assets;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-10 13:40
 */
@Data
@JsonNaming
@ApiModel("批量修改子账号客户报表请求参数")
public class SubCustomerAssetsBatchUpdateReqDTO implements Serializable {

    @NotNull(message = "父账号id不能为空")
    private Long parentUserId;

    @NotNull(message = "子账号id不能为空")
    private Long subUserId;

    @Valid
    @NotEmpty(message = "请选择授权资产")
    private List<SubCustomerAssetsBatchUpdateItem> list;

    @Data
    @ApiModel("SubCustomerAssetsBatchUpdateReqDTO.SubCustomerAssetsBatchUpdateItem")
    public static class SubCustomerAssetsBatchUpdateItem implements Serializable {

        /**
         * 这里的id是父账号的资产id
         */
        @NotNull(message = "authId不能为空")
        @ApiModelProperty("authId")
        private Long id;

        @NotNull(message = "报表权限不能为空")
        @ApiModelProperty("报表权限：1:查看;2下载")
        private String authType;

        @Valid
        private List<SubCustomerAssetsBatchUpdateItem> children;
    }

}
