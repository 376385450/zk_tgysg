package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Jingjun
 * @since 2021/4/24
 */
@Data
@ApiModel("TableDataDto")
public class TableDataDto {

    @ApiModelProperty("标题")
    private List<Header> header;
    @ApiModelProperty("数据列表")
    private List<Map<String, Object>> list;
    private String sql;
    @ApiModelProperty("总数")
    private long total;
    private long realTotal;

    @ApiModel("Header")
    @Data
    @AllArgsConstructor
    public static class Header {
        /**
         * 等价于 fieldId 前端组件使用
         */
        Long id;
        Long fieldId;
        String filedName;
        String filedAlias;
        String dataType;
        boolean primaryKey;
        Integer saturability;
        Integer sum;
        Integer nullSum;
        String defaultShow;

        public Header(Long fieldId, String filedName, String filedAlias, String dataType, boolean primaryKey,
                      Integer saturability, Integer sum, Integer nullSum, String defaultShow) {
            this.id = fieldId;
            this.fieldId = fieldId;
            this.filedName = filedName;
            this.filedAlias = filedAlias;
            this.dataType = dataType;
            this.primaryKey = primaryKey;
            this.saturability = saturability;
            this.sum = sum;
            this.nullSum = nullSum;
            this.defaultShow = defaultShow;
        }
    }
}
