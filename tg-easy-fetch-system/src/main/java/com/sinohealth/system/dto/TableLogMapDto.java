package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("TableLogMapDto")
public class TableLogMapDto {
    @ApiModelProperty("日期")
    private String date;
    @JsonIgnore
    private long time;

    @ApiModelProperty("多个类型")
    private List<CountType> list=new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CountType {
        @ApiModelProperty("类型")
        private Integer logType;
        @ApiModelProperty("数量")
        private long size;
    }
}
