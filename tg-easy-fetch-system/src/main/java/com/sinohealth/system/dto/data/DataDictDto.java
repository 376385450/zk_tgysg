package com.sinohealth.system.dto.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;


/**
 * 数据字典添加对象 data_dict
 *
 * @author dataplatform
 * @date 2021-05-13
 */
@Data
@ApiModel("数据字典添加对象")
public class DataDictDto {


    /**
     * 自增ID
     */
    @ApiModelProperty("自增ID")
    private Long id;
    /**
     * data_standard_dict_tree的id
     */
    @ApiModelProperty("data_standard_dict_tree的id")
    private Long treeId;
    /**
     * 字段名称（数据库）
     */
    @ApiModelProperty("字段名称（数据库）")
    private String name;
    /**
     * 字段中文别名
     */
    @ApiModelProperty("字段中文别名")
    private String aliasName;
    /**
     * 字段释义
     */
    @ApiModelProperty("字段释义")
    private String definition;
    /**
     * 字段示例
     */
    @ApiModelProperty("字段示例")
    private String instance;
    /**
     * 字段规则
     */
    @ApiModelProperty("字段规则")
    private String rule;
    /**
     * 字段备注
     */
    @ApiModelProperty("字段备注")
    private String remark;
    /**
     * 状态：0：删除状态 ；1 正常状态
     */
    @ApiModelProperty("状态：0：删除状态 ；1 正常状态")
    private Integer status;
    @ApiModelProperty("字段类型")
    /** 字段类型 */
    private String type;
    @ApiModelProperty("字段类型名称")
    /** 字段类型名称 */
    private String typeName;
    /**
     * 图片地址
     */
    @ApiModelProperty("图片地址")
    private List<String> pictureUrl;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人ID")
    private Long createBy;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createName;
    /**
     * 更新人
     */
    @ApiModelProperty("更新人ID")
    private Long updateBy;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String updateName;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
