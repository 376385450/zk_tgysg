package com.sinohealth.system.biz.dir.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-29 22:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TablePageVO {

    /**
     * 表id / 文档id
     * 混用这个字段 通过icon类型来区分业务数据范围
     */
    private Long id;
    /**
     * 元素所在目录id
     */
    private Long dirId;

    @ApiModelProperty("业务分类显示名")
    private String businessType;
    /**
     * 展示名： 表别名 文档名
     */
    private String displayName;
    private String leaderName;
    private String leaderOri;
    private String leaderNameOri;
    private String comment;
    private Integer status;

    @ApiModelProperty("业务分类 目录名")
    private String dirName;

    /**
     * 资产地图展示排序
     */
    private Integer disSort;

    private String tableName;

    private Date updateTime;
}
