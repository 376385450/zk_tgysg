package com.sinohealth.system.dto;

import com.sinohealth.common.utils.DirCache;
import com.sinohealth.system.domain.TableFieldInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/28
 */
@Data
public class TableMapDto {

    @ApiModelProperty("表Id")
    private Long id;

    private Long dirId;

    @ApiModelProperty("层级ID，用于区分颜色 ")
    private Long levelId;

    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("表中文名")
    private String tableAlias;
    @ApiModelProperty("权限")
    private Integer accessType;
    @ApiModelProperty("安全等级")
    private Integer safeLevel;

    private String comment;

    List<TableFieldInfo> fields = new ArrayList<>();

    private List<TableRelationDto> relationList = new ArrayList<>();

    public String getDirName(){
        return DirCache.getDir(dirId).getDirName();
    }
}
