package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.utils.DirCache;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 变更记录对象 table_log
 * 
 * @author jingjun
 * @date 2021-04-20
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_log")
@ApiModel("TableLog")
public class TableLog implements Serializable {

private static final long serialVersionUID=1L;


    /**  */
    @TableId(value = "id")
    private Long id;

    /**  */
    private Long tableId;

    @ApiModelProperty("变更表")
    private String tableName;

    @ApiModelProperty("变更表中文名")
    private String tableAlias;

    /**  */
    private Long dirId;

    @ApiModelProperty("变更类型-数字")
    private int logType;

    @ApiModelProperty("变更数量")
    private Integer updateCount;
    @ApiModelProperty("数据总量")
    private Integer dataCount;

    @ApiModelProperty("备注、变更描述")
    private String comment;
    @ApiModelProperty("变更内容")
    private String content;
    @ApiModelProperty("变更前内容")
    private String preContent;

    @ApiModelProperty("变更时间")
    private Date createTime;

    private Long operatorId;

    @ApiModelProperty("变更人")
    private String operator;

    @ApiModelProperty("变更类型-中文")
    public String getLogTypeName(){

        return LogType.findName(this.logType);
    }

    @ApiModelProperty("变更人-带组织前缀")
    @TableField(exist = false)
    private  String operatorOri;


}
