package com.sinohealth.system.dto.table_manage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.dto.TableRelationDto;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
@ApiModel("TableInfoManageDto")
public class TableInfoManageDto {

    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long id;
    @ApiModelProperty("表名")
    private String tableName;
    @JsonIgnore
    @ApiModelProperty("分布式表名")
    private String tableNameDistributed;
    @ApiModelProperty("表中文名")
    private String tableAlias;
    @ApiModelProperty("表目录Id")
    private Long dirId;
    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("流程id")
    private Long processId;

    @ApiModelProperty("描述")
    private String comment;
    @ApiModelProperty("数据最近更新时间")
    private Date updateTime;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @ApiModelProperty("负责人")
    private String leaderName;
    @ApiModelProperty("浏览条数")
    private  Integer viewTotal;
    @ApiModelProperty("查询开发条数")
    private Integer queryLimit;
    @ApiModelProperty("全量数据可查看人员")
    private  String viewUser;
    @ApiModelProperty("字段列表")
    private  List<TableFieldInfo> tableFieldInfos;
    @ApiModelProperty("字段关联信息")
    private  List<TableRelationDto> relations;
    @ApiModelProperty("审核流程")
    private  List<TemplateAuditProcessEasyDto> templateAuditProcessEasyDtos;
    private String localSql;
}
