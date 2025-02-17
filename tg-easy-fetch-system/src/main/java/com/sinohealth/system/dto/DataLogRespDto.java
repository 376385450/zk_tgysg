package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;



/**
 * 资产标准概览添加对象 data_log
 *
 * @author dataplatform
 * @date 2021-05-24
 */
@Data
@ApiModel("资产标准概览添加对象")
public class DataLogRespDto {

    /** 自增ID */
    @ApiModelProperty("自增ID")
    private Long id;
    /** data_standard_dict_tree的id */
    @ApiModelProperty("data_standard_dict_tree的id")
    private Long treeId;
    /** type对应的id */
    @ApiModelProperty("type对应的id")
    private Long standardId;
    /** 类型名称 */
    @ApiModelProperty("类型名称")
    private String name;
    /** 类型：1：数据字典；2：编码目录；3：行业概念 */
    @ApiModelProperty("类型：1：数据字典；2：编码目录；3：行业概念")
    private Integer type;
    /** 操作类型：1：新增；2：修改；3：删除 */
    @ApiModelProperty("操作类型：1：新增；2：修改；3：删除")
    private Integer operationType;
    /** 操作备注 */
    @ApiModelProperty("操作备注")
    private String operationRemark;
    /** 创建人 */
    @ApiModelProperty("创建人")
    private Long createBy;
    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}