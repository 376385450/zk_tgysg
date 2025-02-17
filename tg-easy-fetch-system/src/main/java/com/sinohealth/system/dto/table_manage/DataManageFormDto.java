package com.sinohealth.system.dto.table_manage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("DataManageFormDto")
public class DataManageFormDto {

    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("表中文名")
    private String tableAlias;
    @ApiModelProperty("表目录Id")
    private Long dirId;
    @ApiModelProperty("描述")
    private String comment;
    @ApiModelProperty("负责人")
    private String leaderName;
    @ApiModelProperty("全量数据可查看人员")
    private  String viewUser;
    @ApiModelProperty("负责人")
    private String leaderNameOri;
    @ApiModelProperty("提数模板+审核流程")
    private String templateAuditInfo;
    @ApiModelProperty("启用禁用状态")
    private Integer status;

}
