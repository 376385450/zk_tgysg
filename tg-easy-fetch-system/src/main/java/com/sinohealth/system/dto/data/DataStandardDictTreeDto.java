package com.sinohealth.system.dto.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * 数据标准类型编辑对象 data_standard_dict_tree
 *
 * @author dataplatform
 * @date 2021-05-11
 */
@Data
@ApiModel("数据标准类型编辑对象")
public class DataStandardDictTreeDto {


    /** 自增ID */
    @ApiModelProperty("自增ID")
    private Long id;

    /** 名称 */
    @ApiModelProperty("名称")
    @NotNull
    private String name;

    /** 父id */
    @ApiModelProperty("父id")
    private Long parentId;

    /** 类型：1：数据字典；2：编码目录；3：行业概念 */
    @ApiModelProperty("类型：1：数据字典；2：编码目录；3：行业概念")
    @NotNull
    private Integer type;

    @ApiModelProperty("排序")
    @NotNull
    private Integer sort;

    /** 子类型  0: 否   ; 1  是 */
    @ApiModelProperty("子类型  0: 否   ; 1  是")
    private Integer childType;

    /** 分类级别：0->1级；1->2级 */
    @ApiModelProperty("分类级别：0->1级；1->2级")
    private Integer level;
}
