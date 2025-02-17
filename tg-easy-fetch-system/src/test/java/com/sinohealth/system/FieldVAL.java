package com.sinohealth.system;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DataDictDataTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.dict.FieldUseWayEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-30 16:53
 */
@Data
public class FieldVAL {

    @ExcelProperty("字段ID")
    private Long id;

    @ExcelProperty(value = "字段英文名")
    @ApiModelProperty("字段英文名")
    private String fieldName;

    @ExcelProperty("字段中文名")
    @ApiModelProperty("中文名")
    private String name;

    @ExcelProperty("字段描述")
    @ApiModelProperty("字段描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sort;

    /**
     * @see DataDictDataTypeEnum
     */
    @ApiModelProperty("字段数据类型")
    private String dataType;

    /**
     * @see FieldGranularityEnum
     */
    @ExcelProperty("字段分类")
    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    @ApiModelProperty("关联字典id")
    private Long dictId;

    /**
     * 前端渲染
     */
    @ApiModelProperty("关联字典")
    private String dictName;

    /**
     * @see BizTypeEnum
     */
    @ExcelProperty("业务线")
    @ApiModelProperty("业务线")
    @NotBlank(message = "业务线必填")
    private String bizType;

    /**
     * @see FieldUseWayEnum
     */
    @ExcelProperty("使用途径")
    @ApiModelProperty("使用途径")
    private String useWay;

    @ApiModelProperty("是否启用")
    private Boolean enable;

    @ApiModelProperty("创建人名称")
    private String creator;

    @ApiModelProperty("更新人名称")
    private String updater;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

    @Override
    public String toString() {
        return
                "\"" + id + "\"" +
                        ", \"" + fieldName + "\"" +
                        ", \"" + name + "\"" +
                        ", \"" + description + "\"" +
                        ", \"" + sort + "\"" +
                        ", \"" + dataType + "\"" +
                        ", \"" + granularity + "\"" +
                        ", \"" + dictId + "\"" +
                        ", \"" + dictName + "\"" +
                        ", \"" + bizType.replace(","," ") + "\"" +
                        ", \"" + useWay + "\"" +
                        ", \"" + enable + "\"" +
                        ", \"" + creator + "\"" +
                        ", \"" + updater + "\"" +
                        ", \"" + createTime + "\"" +
                        ", \"" + updateTime + "\"";
    }
}
