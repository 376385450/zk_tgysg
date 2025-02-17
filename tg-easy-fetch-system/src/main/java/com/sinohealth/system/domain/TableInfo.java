package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.biz.dir.entity.DisplaySort;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】对象 table_info
 *
 * @author jingjun
 * @date 2021-04-20
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_info")
@ApiModel("table_info")
public class TableInfo implements Serializable, IAssetBindingData, DisplaySort, IdTable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("表英文名")
    private String tableName;

    @ApiModelProperty("分布式表名")
    private String tableNameDistributed;

    @ApiModelProperty("表中文名")
    private String tableAlias;

    /**
     * 安全等级
     *
     * @date 2022-02-10 10:41:03
     * @since 1.6.4.0
     * @deprecated 废弃，改成使用冷热度
     */
    @Deprecated
    private Integer safeLevel;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long dirId;

    @ApiModelProperty("数据行数")
    private Long totalRow;

    private String comment;

    @ApiModelProperty("0删除 1 正常 2 停用")
    private Integer status;

    private Long createUserId;

    private Date createTime;

    private Long updateUserId;
    @ApiModelProperty("更新时间")
    private Date updateTime;

    @TableField(exist = false)
    @ApiModelProperty("权限类型1-5,1只读，5管理")
    private Integer accessType;

    @ApiModelProperty(value = "当日使用次数")
    private int queryTimes;

    @ApiModelProperty(value = "累计使用次数")
    private int totalQueryTimes;

    @ApiModelProperty(value = "数据体量")
    private long dataLength;

    /**
     * 版本
     */
    private Integer version;
    /**
     * 数据同步时间
     */
    private LocalDateTime syncTime;

    private String localSql;
    @TableField(exist = false)
    @ApiModelProperty("是否关注")
    private boolean concern;

    /*@ApiModelProperty("所属层级")
    public String getDirPath() {
        return DirCache.getDir(dirId).getDirPath();
    }*/

    @TableField(exist = false)
    @JsonIgnore
    private String copySql;

    @TableField(exist = false)
    @ApiModelProperty("表所属的数据源名称")
    private String sourceName;

    @TableField(exist = false)
    @ApiModelProperty("表所属的数据源类型，mysql：MYSQL，hive2：HIVE")
    private String databaseType;

    @ApiModelProperty(value = "表类型:0-静态,1-动态")
    private Integer schemeStatus;

    @ApiModelProperty(value = "表更新周期:1-实时,2-每小时,3-每日,4-不更新")
    private Integer schemeCycle;

    @ApiModelProperty("负责部门（分组名称）")
    private String groupName;

    @ApiModelProperty("负责人（用户名称）")
    private String leaderName;

    @ApiModelProperty("热度，1：0~30天内有使用（查询或者变更过）为高热度；2：30~90天内有使用为 中等温度；3：90天为低热度表；4：365天无人使用为冷表")
    private Integer heat;

    @ApiModelProperty("浏览条数")
    private Integer viewTotal;

    @TableField(exist = false)
    @ApiModelProperty("表目录名称")
    private String dirName;
    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("流程id")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long processId;

    @ApiModelProperty("是否上传表：0否；1是")
    private int isDiy;

    /**
     * 资产地图展示排序
     */
    private Integer disSort;

    @ApiModelProperty("可查看全量数据的人员 逗号拼接")
    private String viewUser;

    @ApiModelProperty("负责人带企业架构")
    @TableField(exist = false)
    private String leaderNameOri;

    @ApiModelProperty("提数目标+审核流程信息")
    @TableField(exist = false)
    private List<TemplateAuditProcessEasyDto> templateAuditProcessEasyDtos;

    public static TableInfo newInstance() {
        return new TableInfo();
    }

    @Override
    public String getName() {
        return tableAlias;
    }

    @Override
    public void fillDisSort(Integer sort) {
        this.disSort = sort;
    }
}
