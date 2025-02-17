package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/5/10
 */
@Data
@ApiModel("TableCopyDto")
public class TableCopyDto {
    @ApiModelProperty("表中文名")
    @NotEmpty
    private List<CopyTo> list;

    private Long dirId;

    @Data
    public static class CopyTo {
        @ApiModelProperty("表ID")
        private Long fromTableId;
        @ApiModelProperty("复制到哪个库")
        private Long toDirId;
        @ApiModelProperty("新表名")
        private String toTableName;
        @ApiModelProperty("新表中文名")
        private String toTableAlias;
        @ApiModelProperty("是否复制数据")
        private boolean copyData;
    }
}
