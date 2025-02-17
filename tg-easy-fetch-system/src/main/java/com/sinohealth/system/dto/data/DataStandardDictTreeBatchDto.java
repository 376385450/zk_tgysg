package com.sinohealth.system.dto.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * DataStandardDictTreeBatchDto
 *
 * @author dataplatform
 * @date 2021-05-11
 */
@Data
@ApiModel("DataStandardDictTreeBatchDto")
public class DataStandardDictTreeBatchDto {
    /**
     * 父id
     */
    @ApiModelProperty("父id")
    @NotNull
    private Long parentId;

    /**
     * 类型：1：数据字典；2：编码目录；3：行业概念
     */
    @ApiModelProperty("类型：1：数据字典；2：编码目录；3：行业概念")
    @NotNull
    private Integer type;

    @ApiModelProperty("当前父id的所有子目录")
    @Valid
    @NotNull
    private List<Custom> custom;


    @ApiModel("Custom")
    @Data
    @Accessors(chain = true)
    public static class Custom {

        /**
         * 自增ID
         */
        @ApiModelProperty("自增ID")
        @NotNull
        private Long id;

        @ApiModelProperty("排序")
        @NotNull
        private Integer sort;
    }
}
