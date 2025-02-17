package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.util.Date;

@ApiModel
@Data
public class ApiInfoVersionLimitingDto implements Serializable {

    @ApiModelProperty(value = "apiId")
    private Long id;

    /**
     * api服务名称（发布后的api接口名）
     */
    @ApiModelProperty(value = "api服务名称（发布后的api接口名）")
    private String apiName;

    /**
     * 接口英文名称
     */
    @ApiModelProperty(value = "接口英文名称")
    private String apiNameEn;

    /**
     * 接口访问路径
     */
    @ApiModelProperty(value = "接口访问路径")
    private String requestPath;

    /**
     * api版本号(内部维护字段)
     */
    @ApiModelProperty(value = "api版本号(内部维护字段)")
    private Long apiVersion;

    /**
     * api版本号（由操作用户定义）
     */
    @ApiModelProperty(value = "api版本号（由操作用户定义）")
    private String apiVersionOut;

    /**
     * 创建人ID
     **/
    @ApiModelProperty(value = "创建人ID")
    private Long createId;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 接口关联组ID
     */
    @ApiModelProperty(value = "接口关联组ID",hidden = true)
    private Long groupId;

    /**
     * 接口关联组ID
     */
    @ApiModelProperty(value = "接口关联组名称")
    private String groupName;
}
