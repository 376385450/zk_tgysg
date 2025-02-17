package com.sinohealth.system.vo;

import com.sinohealth.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 * 统计结果视图对象 mall_package
 *
 * @author dataplatform
 * @date 2021-08-02
 */
@Data
@ApiModel("统计结果视图对象")
public class SysStatisticalResultVo {


    @Excel(name = "表单名称（中文）")
    @ApiModelProperty("表单名称（中文）")
    private String tableAlias;

    @Excel(name = "表单名称（英文）")
    @ApiModelProperty("表单名称（英文）")
    private String tableName;

    @Excel(name = "上次统计时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date formerLime;

    @Excel(name = "本次统计时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date newLime;

    @Excel(name = "上次数据行数")
    @ApiModelProperty("上次数据行数")
    private Long totalRowsFormer;

    @Excel(name = "本次数据行数")
    @ApiModelProperty("本次数据行数")
    private Long totalRowsNew;

    @Excel(name = "数据行数变化量")
    @ApiModelProperty("数据行数变化量")
    private Long totalRowsVariation;

    @Excel(name = "上次数据体量")
    @ApiModelProperty("上次数据体量")
    private Long tableMakeFormer;

    @Excel(name = "本次数据体量")
    @ApiModelProperty("本次数据体量")
    private Long tableMakeNew;

    @Excel(name = "数据体量变化量")
    @ApiModelProperty("数据体量变化量")
    private Long tableMakeVariation;


}
