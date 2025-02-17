package com.sinohealth.system.dto;

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
 * @date 2022-11-30 18:09
 */
@Data
@ApiModel("分配客户请求v2")
public class CustomerApplyAuthReqV2DTO implements Serializable {

    /**
     * 优先级 assetsId > ids
     * 如果 assetsId 不为空则读取 assetsId,否则读取ids
     */
    @ApiModelProperty("资产id")
    private Long assetsId;

    @Valid
    @ApiModelProperty("客户权限信息集合")
    @NotEmpty(message = "客户权限list不能为空")
    private List<CustomerApplyAuthUserItemDTO> list;

    @ApiModelProperty("客户权限信息删除集合")
    private List<Integer> deleteIds;

    @ApiModelProperty("数据目录id列表")
    private List<Long> ids;

    @ApiModelProperty("打包名称")
    private String packName;

    @ApiModelProperty("是否打包")
    private Boolean pack;

    @Data
    @ApiModel("CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO")
    public static class CustomerApplyAuthUserItemDTO implements Serializable {

        @Deprecated
        @ApiModelProperty("customer_apply_auth_id")
        private Integer id;

        @ApiModelProperty("用户id")
        @NotNull(message = "报表授权用户id不能为空")
        private Long userId;

        @ApiModelProperty("报表权限：1:查看;2下载")
        @NotBlank(message = "报表权限不能为空")
        private String authType;
    }


}
