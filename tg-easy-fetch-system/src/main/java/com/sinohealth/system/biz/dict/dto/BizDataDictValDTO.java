package com.sinohealth.system.biz.dict.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 11:32
 */
@Data
public class BizDataDictValDTO {

    private Long dictId;

    @ApiModelProperty("字典值")
    private String val;

    @ApiModelProperty("展示名")
    private String name;

    @ApiModelProperty("排序")
    private Integer sort;

    public BizDataDictValDTO(String val) {
        this.val = val;
    }

    public BizDataDictValDTO() {
    }
}
