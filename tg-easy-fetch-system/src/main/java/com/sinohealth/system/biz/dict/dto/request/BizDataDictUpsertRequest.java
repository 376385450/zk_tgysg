package com.sinohealth.system.biz.dict.dto.request;

import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DataDictDataTypeEnum;
import com.sinohealth.common.enums.dict.DataDictEnum;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizDataDictUpsertRequest {

    private Long id;

    @ApiModelProperty("字典名称")
    @NotBlank(message = "字典名称未填")
    private String name;

    @ApiModelProperty("字典说明")
    private String description;

    /**
     * @see DataDictEnum
     */
    @NotBlank(message = "配置方式未选择")
    private String dictType;

    @ApiModelProperty("系统字典")
    private Boolean systemDict;

    /**
     * @see DataDictDataTypeEnum
     */
    private String dataType;

    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    @NotBlank(message = "业务线未填")
    private String bizType;

    @ApiModelProperty("维表引用")
    private String quoteSql;

    @ApiModelProperty("字典项")
    private List<BizDataDictValDTO> valList;
}
