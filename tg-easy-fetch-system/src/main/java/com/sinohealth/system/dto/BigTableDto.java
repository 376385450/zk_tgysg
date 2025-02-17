package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("BigTableDto")
public class BigTableDto {
    @ApiModelProperty("表ID")
    private Long tableId;
    @ApiModelProperty("表中文")
    private String tableAlias;
    @ApiModelProperty("表名")
    private String tableName;
    @JsonIgnore
    private long store;


    @ApiModelProperty("表单体量GB")
    public  String getTableStore(){
        return BigDecimal.valueOf(store).divide(BigDecimal.valueOf(1024*1024*1024),2, RoundingMode.HALF_UP).toPlainString();
    }
}
