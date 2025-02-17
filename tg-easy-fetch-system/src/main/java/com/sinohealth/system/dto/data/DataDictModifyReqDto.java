package com.sinohealth.system.dto.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
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
public class DataDictModifyReqDto {


    /**
     * 自增ID
     */
    @ApiModelProperty("自增ID")
    private Long id;
    /**
     * data_standard_dict_tree的id
     */
    @ApiModelProperty(value = "data_standard_dict_tree的id", required = true)
    @NotNull
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
    @ApiModelProperty("字段类型")
    /** 字段类型 */
    @NotNull
    private String type;
    @ApiModelProperty("字段类型名称")
    /** 字段类型名称 */
    @NotNull
    private String typeName;
    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private Long createBy;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private Long updateBy;


    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;

    /**
     * 图片文件
     */
    @ApiModelProperty("图片文件")
    private MultipartFile[] pictureFile;


    /**
     *
     */
    @ApiModelProperty("删除图片文件名")
    private List<String> pictureNameDeleteList;

}
