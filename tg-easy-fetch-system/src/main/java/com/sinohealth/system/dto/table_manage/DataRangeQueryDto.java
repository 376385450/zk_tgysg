package com.sinohealth.system.dto.table_manage;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.dto.analysis.FilterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-08-11 14:44
 * @Desc
 */

@Data
public class DataRangeQueryDto {
    /**
     * 宽表真实字段id
     */
    private Long colId;
    private List<String> data;
    private Integer isAll = 1;
    private Integer pageNum = 1;
    private Integer pageSize = 50;
    private String searchContent = "";
    private Integer searchOrder = CommonConstants.ASC;
    private String isSelected;

    /**
     * 底表 资产地图 的表名
     */
    private String baseTable;

    /**
     * 客户筛选使用：资产id 真实表
     */
    @ApiModelProperty("数据资产id")
    private Long assetsId;

    /**
     * 客户筛选使用：字段名
     */
    private String fieldName;

    /**
     * 级联查询：同级其他过滤条件 注意：排除查询字段本身
     */
    private List<FilterDTO.FilterItemDTO> filterItems;

    /**
     * 级联查询 目标数据表
     */
    private String targetTable;

    /**
     * 精确查询
     */
    private Boolean extractQuery;

    /**
     * 字段库 字段id 常规+通用
     * 前端传入情况：没有tableId并且字段库没有匹配该数据fieldId的数据时就传
     */
    private Long fieldDictId;

    /**
     * 资产版本
     */
    private Integer version;
}
