package com.sinohealth.system.dto;

import com.sinohealth.system.domain.TableLog;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("TableStatisticDto")
public class TableStatisticDto {
    @ApiModelProperty("数据库总数")
    private int totalSchema;
    @ApiModelProperty("表总数")
    private int totalTable;
    @ApiModelProperty("存储体量")
    private long totalStore;
    @ApiModelProperty("数据体量前20")
    private List<BigTableDto> bigTableList=new ArrayList<>();
    @ApiModelProperty("变更记录")
    private List<TableLog> logList=new ArrayList<>();
    @ApiModelProperty("存储体量+单位MB")
    public String getTotalStoreName() {
        return BigDecimal.valueOf(totalStore).divide(BigDecimal.valueOf(1024 * 1024* 1024), 2, RoundingMode.HALF_UP).toString() + "GB";
    }
}
