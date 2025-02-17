package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Author manleo
 * @Date 2021/7/5 16:53
 * @Version 1.0
 */
@Data
@ApiModel("新增API服务 请求体")
@Accessors(chain = true)
public class AddApiBaseInfoRequest {

    @ApiModelProperty(value = "api_info_id")
    private Long apiBaseInfoId;

    @ApiModelProperty(value = "api服务名称")
    @NotBlank(message = "api服务名称不能为空")
    private String apiName;

    @ApiModelProperty(value = "接口描述")
    private String apiDesc;

    @ApiModelProperty(value = "请求方式 例：GET/PUT/POST/DELETE")
    private String requestMethod;

    @ApiModelProperty(value = "数据来源   1-数据仓库源   2-自定义sql数据集")
    @NotBlank(message = "数据源类型不能为空")
    private String dataSource;

    @ApiModelProperty(value = "数据仓库源表ID，数据源为2时允许有多张表，用json格式保存表ID和注释信息，示例[{\"tableName\":\"表1\",\"id\":\"1\"},{\"tableName\":\"表2\",\"id\":\"2\"}]")
    private String tableId;

    @ApiModelProperty(value = "当数据源类型为2时，该字段值可以为空" +
            "仓库表ID包含的字段ID，用json格式保存。示例[{\"fieldName\":\"字段1\",\"id\":\"1\"},{\"fieldName\":\"字段2\",\"id\":\"2\"}]，全部字段，传值“all” ")
    private String tableFieldId;

    @ApiModelProperty(value = "接口访问路径")
    @NotBlank(message = "接口访问地址不能为空")
    private String requestPath;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "api接口请求参数(暂时用不上)")
    private String requestParam;

    @ApiModelProperty(value = "api接口类型0-静态 1-动态（参考目标库表的类型）")
    private String apiType;

    @ApiModelProperty(value = "表更新周期:1-实时,2-每小时,3-每日,4-不更新（参考目标库表的类型）")
    private String apiUpdateFrequency;

    @ApiModelProperty(value = "api版本号（由操作用户定义）")
    private String apiVersionOut;

    @ApiModelProperty(value = "接口英文名称,接口发布时，不能为空")
    private String apiNameEn;

    @ApiModelProperty(value = "接口关联分组ID，接口发布时，不能为空")
    private String groupId;

    @ApiModelProperty(value = "sql语句,数据源类型为2时，不能为空")
    private String sqlStatement;

    @ApiModelProperty(value = "请求参数，json格式保存（无实际意义，目前写死2个参数）示例：{\"pageSize\":\"10\",\"pageNum\":\"1\"}")
    private String requestParamJson;

    @ApiModelProperty(value = "返回结果集数据（暂时用不上）")
    private String returnResultJson;

}
