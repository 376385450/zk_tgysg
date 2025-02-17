package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-13 13:49
 */
@Data
@JsonNaming
public class CustomerApplyAuthUpdateReqV2DTO implements Serializable {

    @Valid
    @NotEmpty(message = "list不能为空")
    private List<CustomerApplyAuthUserItemDTO> list;

    @Data
    @ApiModel("CustomerApplyAuthUpdateReqV2DTO.CustomerApplyAuthUserItemDTO")
    public static class CustomerApplyAuthUserItemDTO implements Serializable {

        @NotNull(message = "id不能为空")
        @ApiModelProperty("customer_apply_auth_id")
        private Long id;

        @ApiModelProperty("报表权限：1:查看;2下载")
        @NotBlank(message = "报表权限不能为空")
        private String authType;
    }
}
