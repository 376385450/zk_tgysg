package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("QueryTableCountDto")
public class QueryTableCountDto {

    @ApiModelProperty("表格数据")
    private List<QueryTableCount> list;

    @ApiModelProperty("表格类型数据")
    private Map<String, String> typeData;

    @Getter
    @Setter
    @ApiModel("表格数据")
    public static class QueryTableCount {
        @ApiModelProperty("日期")
        private String date;

        @ApiModelProperty("数据集合")
        private Map<String, Integer> dataList;
    }


}
