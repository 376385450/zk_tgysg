package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 【请填写功能名称】对象 table_statistics
 *
 * @author dataplatform
 * @date 2021-05-07
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_statistics")
@ApiModel("TableStatistics")
public class TableStatistics implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id")
    private Long id;


    private Long tableId;


    private Long dirId;

    @ApiModelProperty(value = "内容权限账号数")
    private int totalAccount;
    @ApiModelProperty(value = "收藏账号数")
    private int totalConcern;

    @ApiModelProperty(value = "当日使用次数")
    private int queryTimes;

    @ApiModelProperty(value = "累计使用次数")
    private int totalQueryTimes;


    @ApiModelProperty(value = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

}
