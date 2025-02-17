package com.sinohealth.system.biz.dict.dto.request;

import com.sinohealth.common.enums.dict.DataDictEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-08 14:29
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataDictPageRequest extends DictCommonPageRequest{

    /**
     * @see DataDictEnum
     */
    @ApiModelProperty("字典 配置方式")
    private String dictType;
}
