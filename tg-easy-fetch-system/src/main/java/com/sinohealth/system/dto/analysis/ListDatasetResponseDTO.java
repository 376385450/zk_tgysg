package com.sinohealth.system.dto.analysis;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 自定义数据集列表，响应参数
 *
 * @author linkaiwei
 * @date 2021/8/16 14:17
 * @since 1.4.1.0
 */
@Data
@ApiModel("自定义数据集列表，响应参数")
@Accessors(chain = true)
public class ListDatasetResponseDTO implements Serializable {

    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("英文名称")
    private String englishName;

    @ApiModelProperty("类型，1自定义数据集，2EXCEL数据集")
    private Integer type;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("创建人ID")
    private Long createBy;

    @ApiModelProperty("创建人姓名")
    private String createByName;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新人ID")
    private Long updateBy;

    @ApiModelProperty("更新人姓名")
    private String updateByName;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("状态：0删除，1正常，2停用")
    private Integer status;

    @ApiModelProperty("Excel数据集对应的表ID")
    private String tables;

}
