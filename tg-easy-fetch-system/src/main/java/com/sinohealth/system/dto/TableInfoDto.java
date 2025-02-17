package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.domain.constant.DataDirConst;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jingjun
 * @since 2021/4/24
 */
@Data
@ApiModel("TableInfoDto")
public class TableInfoDto {
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long id;
    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("表中文名")
    private String tableAlias;
    private Long dirId;
    @ApiModelProperty("所属分类")
    private String dirPath;
    @ApiModelProperty("备注")
    private String comment;
    @ApiModelProperty("数据最近更新时间")
    private Date updateTime;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @ApiModelProperty("行数")
    private long totalRows;
    @ApiModelProperty("字段数量")
    private int totalFields;
    @ApiModelProperty("文件大小")
    private long storeSize;
    @ApiModelProperty("安全等级")
    private int safeLevel;
    @ApiModelProperty("权限")
    private Integer accessType;
    @ApiModelProperty("部门")
    private String deptName;
    @ApiModelProperty("负责人")
    private String managerName;
    @ApiModelProperty("是否关注")
    private boolean concern;
    @ApiModelProperty("表类型:0-静态,1-动态")
    private Integer schemeStatus;
    @ApiModelProperty("表更新周期:1-实时,2-每小时,3-每日,4-不更新,5-每周,6-每月,7-每季度,8-每年")
    private Integer schemeCycle;

    @ApiModelProperty("负责部门（分组名称）")
    private String groupName;
    @ApiModelProperty("负责人（用户名称）")
    private String leaderName;
    @ApiModelProperty("负责部门和负责人级联列表")
    private List<Map<String, Object>> list;

    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("流程id")
    private Long processId;

    /**
     * 是否有回传数据，true有，false没有
     *
     * @date 2022-02-14 09:49:29
     * @since 1.6.4.0
     */
    @ApiModelProperty("是否有回传数据，true有，false没有")
    private boolean hasReturnData = false;

    @ApiModelProperty("浏览条数")
    private Integer viewTotal;

    @ApiModelProperty("负责人（用户真实名称）")
    private String leaderRealName;

    @ApiModelProperty("全量数据可查看人员")
    private String viewUser;

    /**
     * @see com.sinohealth.system.domain.constant.DataDirConst.DocPermission
     * @see com.sinohealth.system.domain.constant.DataDirConst.TablePermission
     */
    @ApiModelProperty("权限组")
    private List<Integer> permissions;

    /**
     * @see DataDirConst.PreviewPermissionType
     */
    @ApiModelProperty("权限类型")
    private Integer permissionType;
}
