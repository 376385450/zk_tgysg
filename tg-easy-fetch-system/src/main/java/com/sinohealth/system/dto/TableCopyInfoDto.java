package com.sinohealth.system.dto;

import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.domain.TableTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableCopyInfoDto {

    private Long userId;
    //新表名
    private String username;

    //库来源
    private Long dirId;
    //表来源ID
    private Long fromTableId;

    //复制到哪个库
    private Long toDirId;
    //新表名
    private String toTableName;
    //新库id
    private String toTableId;
    //新表中文名
    private String toTableAlias;
    //是否复制数据
    private boolean copyData;
    //是否复制成功
    private boolean success = false;
    //是否有权限
    private boolean role = false;
    //提示信息
    private String message;
    //执行sql
    private String sql;

    private TableTask tableTask;

    private TableLog tableLog;

    private TableInfo tableInfo;
}
