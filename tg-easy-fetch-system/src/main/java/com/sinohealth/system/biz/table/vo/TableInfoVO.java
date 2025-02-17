package com.sinohealth.system.biz.table.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-03-09 20:42
 */
@Data
public class TableInfoVO implements Serializable {

    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("表英文名")
    private String tableName;

    @ApiModelProperty("分布式表名")
    private String tableNameDistributed;

    @ApiModelProperty("表中文名")
    private String tableAlias;

    private Long dirId;

    private String comment;

    @ApiModelProperty("0删除 1 正常 2 停用")
    private Integer status;
}
