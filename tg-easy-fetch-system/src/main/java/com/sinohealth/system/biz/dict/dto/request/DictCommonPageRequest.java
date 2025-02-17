package com.sinohealth.system.biz.dict.dto.request;

import com.alibaba.excel.annotation.ExcelProperty;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.preset.RangePresetTypeEnum;
import com.sinohealth.system.biz.dict.util.BizTypeUtil;
import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 13:37
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class DictCommonPageRequest extends PageRequest {

    @ApiModelProperty("搜索内容")
    private String searchContent;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("项目状态")
    private Integer projectStatus;

    /**
     * 多值,分隔
     *
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    /**
     * @see FieldGranularityEnum
     */
    @ExcelProperty("字段分类")
    @ApiModelProperty("字段分类 粒度")
    private String granularity;

    @ApiModelProperty("类型，1我的项目，2全部项目")
    @NotNull(message = "项目类型必填")
    private Integer type;

    /**
     * @see RangePresetTypeEnum
     */
    private String rangeType;

    public String buildBizType() {
        return BizTypeUtil.buildBizTypeWhere(this.bizType);
    }
}
