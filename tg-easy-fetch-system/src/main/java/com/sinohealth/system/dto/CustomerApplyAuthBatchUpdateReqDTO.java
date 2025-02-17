package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-02-16 11:07
 */
@Data
@ApiModel("客户报表权限批量修改请求参数")
public class CustomerApplyAuthBatchUpdateReqDTO {

    @Valid
    @NotEmpty
    private List<CustomerApplyAuthBatchUpdateItem> list;

    @Data
    @ApiModel("CustomerApplyAuthBatchUpdateReqDTO.CustomerApplyAuthBatchUpdateItem")
    public static class CustomerApplyAuthBatchUpdateItem {

        @NotNull(message = "authId不能为空")
        @ApiModelProperty("authId")
        private Long id;

        @NotNull(message = "报表权限不能为空")
        @ApiModelProperty("报表权限：1:查看;2下载")
        private String authType;

        @Valid
        private List<CustomerApplyAuthBatchUpdateItem> children;
    }

    @JsonIgnore
    public List<CustomerApplyAuthBatchUpdateItem> flat(List<CustomerApplyAuthBatchUpdateItem> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<CustomerApplyAuthBatchUpdateItem> result = new ArrayList<>();
        for (CustomerApplyAuthBatchUpdateItem item : list) {
            result.add(item);
            result.addAll(flat(item.getChildren()));
        }
        return result;
    }

}
