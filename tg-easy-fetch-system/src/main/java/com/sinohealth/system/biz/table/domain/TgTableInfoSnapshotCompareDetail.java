package com.sinohealth.system.biz.table.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author monster
 * @Date 2024-07-11 10:43
 */
@Data
@TableName("tg_table_info_snapshot_compare_detail")
@ApiModel("库表快照比对详细表")
@Accessors(chain = true)
public class TgTableInfoSnapshotCompareDetail implements Serializable {
    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty(value = "比对id")
    private Long compareId;

    @ApiModelProperty(value = "类型")
    private String category;

    @ApiModelProperty(value = "数据源类型")
    private String dataSource;

    @ApiModelProperty(value = "附加信息")
    private String attach;

    @TableLogic
    private Long deleted;

    @ApiModelProperty(value = "表名称")
    private String tableName;

    @ApiModelProperty(value = "数据统计条数")
    private Long dataCount;

    @ApiModelProperty(value = "处理时常")
    private Long processTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
