package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Table;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.util.ApplicationSqlUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 数据预览，请求参数
 *
 * @author linkaiwei
 * @date 2021/11/19 09:53
 * @since 1.6.1.0
 */
@Data
@ApiModel("数据预览，请求参数")
@Accessors(chain = true)
public class GetDataInfoRequestDTO implements Serializable {

    @ApiModelProperty(value = "选择显示字段", position = 1)
    private String fieldIds;

    @ApiModelProperty(value = "显示字段的别名")
    private String fieldNames;

    @Deprecated
    @ApiModelProperty(value = "过滤信息列表-已废弃", position = 2)
    private List<Filter> filterList;

    @ApiModelProperty(value = "排序字段", position = 3)
    private String sortingField;

    @ApiModelProperty(value = "排序类型;asc或者desc", position = 4)
    private String sortBy;

    @ApiModelProperty(value = "分页信息，页码", position = 5)
    private Integer pageNum = 1;

    @ApiModelProperty(value = "分页信息，每页数量", position = 6)
    private Integer pageSize = 30;

    @ApiModelProperty(value = "过滤信息（带且或）", position = 7)
    private FilterDTO filter;

    @ApiModelProperty(value = "下载类型（用于一些下载场景）", position = 8)
    private String downloadType;

    @ApiModelProperty(value = "资产id")
    private Long assetId;

    /**
     * 过滤信息
     *
     * @author linkaiwei
     * @date 2021/11/19 09:57
     * @since 1.6.1.0
     */
    @Data
    @ApiModel("过滤信息列表")
    @Accessors(chain = true)
    public static class Filter implements Serializable {

        @ApiModelProperty(value = "字段名")
        String fieldName;

        @ApiModelProperty(value = "条件")
        String condition;

        @ApiModelProperty(value = "逗号左边值")
        String leftVal;

        @ApiModelProperty(value = "逗号右边值")
        String rightVal;

    }

    @JsonIgnore
    public String buildWhereSQL() {
        if (filter == null) {
            return "";
        }
        // 构建 where 语句
        String whereSql = "";
        Table table = new Table();
        table.setUniqueId(1L);
        table.setFactTable(true);

        com.sinohealth.bi.data.Filter targetFilter = new com.sinohealth.bi.data.Filter();
        ApplicationSqlUtil.convertToFilter(filter, targetFilter);
        final ClickHouse mySql = new ClickHouse(Collections.singletonList(table),  targetFilter);
        whereSql = mySql.getWhereSql();
//        StringBuffer sb = new StringBuffer();
//        QuerySqlUtil.findOthersSql(filter.getFilters(), sb);
//        whereSql = whereSql + sb;
        whereSql = whereSql.replace("t_1.", "").replace("WHERE", "");

        return whereSql;
    }

    @JsonIgnore
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }

}
