package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @Author manleo
 * @Date 2021/7/5 16:53
 * @Version 1.0
 */
@Data
@ApiModel("查询API服务（草稿） 响应体")
@Accessors(chain = true)
public class ApiBaseInfoDto {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "api服务名称（发布前的草稿信息名称）")
    private String apiName;

    @ApiModelProperty(value = "接口描述")
    private String apiDesc;

    @ApiModelProperty("请求方式")
    private String requestMethod;

    @ApiModelProperty("数据来源   1-数据仓库源   2-自定义sql数据集")
    private String dataSource;

    @ApiModelProperty("数据仓库源表ID，数据源为2时允许有多张表，用json格式保存表ID和注释信息，示例[{\"tableName\":\"表1\",\"id\":\"1\"},{\"tableName\":\"表2\",\"id\":\"2\"}]")
    private String tableId;

    @ApiModelProperty("仓库表ID包含的字段ID（返回字段ID），用json格式保存。示例[{\"fieldName\":\"字段1\",\"id\":\"1\"},{\"fieldName\":\"字段2\",\"id\":\"2\"}] ,全部字段，传值“all”")
    private String tableFieldId;

    @ApiModelProperty("接口访问路径")
    private String requestPath;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("0-草稿 1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）")
    private String apiStatus;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("api接口请求参数(暂时用不上)")
    private String requestParam;

    @ApiModelProperty("api接口类型0-静态 1-动态（参考目标库表的类型）")
    private String apiType;

    @ApiModelProperty("接口更新周期1-实时,2-每小时,3-每日,4-不更新（参考目标库表的类型）")
    private String apiUpdateFrequency;

    @ApiModelProperty("api版本号（由操作用户定义）")
    private String apiVersionOut;

    @ApiModelProperty("接口英文名称")
    private String apiNameEn;

    @ApiModelProperty("接口关联组ID")
    private String groupId;

    @ApiModelProperty("sql语句（数据来源为2时需要回显）")
    private String sqlStatement;

    @ApiModelProperty("请求参数，json格式保存（无实际意义，目前写死2个参数pageSize、pageNum）")
    private String requestParamJson;

    @ApiModelProperty("返回结果集数据（暂时用不上）")
    private String returnResultJson;

    @ApiModelProperty("删除标志（0-未删除,1-已删除）")
    private String delStatus;

    @ApiModelProperty("创建人ID")
    private Long createId;

    @ApiModelProperty("数据库所属组ID，多个用英文逗号分割")
    private String datasourceGroupId;

    @ApiModelProperty("数据库所属组名称，多个用英文逗号分割")
    private String datasourceGroupName;

    @ApiModelProperty("数据源集合，数据仓库源表回显")
    private List<DataSource> dataSourceList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class DataSource {
        @ApiModelProperty("数据源ID")
        private Long dId;
        @ApiModelProperty("数据源层级 数值越大，层级越靠后")
        private Integer level;
    }

}
