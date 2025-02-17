package com.sinohealth.system.dto.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 数据字典添加对象 data_dict
 *
 * @author dataplatform
 * @date 2021-05-13
 */
@Data
@ApiModel("DataDictBatchModifyReqDto")
public class DataDictBatchModifyReqDto {


    /** data_standard_dict_tree的id */
    @ApiModelProperty(value = "data_standard_dict_tree的id", required = true)
    @NotNull
    private Long treeId;

    @NotNull
    private List<Long> ids;

}
