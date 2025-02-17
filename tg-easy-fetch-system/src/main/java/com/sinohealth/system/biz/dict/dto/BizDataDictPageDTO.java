package com.sinohealth.system.biz.dict.dto;

import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DataDictDataTypeEnum;
import com.sinohealth.common.enums.dict.DataDictEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-08 16:54
 */
@Data
@ApiModel(description = "字典")
public class BizDataDictPageDTO {

    private Long id;
    private Long dictId;

    private String name;

    @ApiModelProperty("字典说明")
    private String description;

    /**
     * @see DataDictEnum
     */
    @ApiModelProperty("字典 配置方式")
    private String dictType;

    @ApiModelProperty("系统字典")
    private Boolean systemDict;

    /**
     * @see DataDictDataTypeEnum
     */
    @ApiModelProperty("数据类型")
    private String dataType;

    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("维表引用/导入")
    private String quoteSql;

    @ApiModelProperty("更新人名称")
    private String updater;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
