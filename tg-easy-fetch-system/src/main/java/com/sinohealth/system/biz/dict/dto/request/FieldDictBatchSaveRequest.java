package com.sinohealth.system.biz.dict.dto.request;

import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 13:47
 */
@Data
public class FieldDictBatchSaveRequest {

    @ApiModelProperty("字段集合")
    @NotEmpty(message = "字段未填")
    private List<FieldDictDTO> fields;
}
