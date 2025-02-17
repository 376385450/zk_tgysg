package com.sinohealth.system.biz.dataassets.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-08-01 13:54
 * @see UserDataAssets
 * @see TgApplicationInfo
 */
@Data
public class UsableDataAssetsEntity implements IdTable {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Deprecated
    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("源 申请id")
    private Long srcApplicationId;

    private Integer version;
    /**
     * 资产内包含的品类，创建资产时获取
     */
    private String prodCode;

    /**
     * @see AssetsSnapshotTypeEnum
     */
    private String snapshotType;

    /**
     * @see com.sinohealth.common.enums.dataassets.AssetsExpireEnum
     */
    private String expireType;

    /**
     * 作废
     */
    private Boolean deprecated;

    /**
     * 成功状态下的 已读 未读
     *
     * @see AsyncTaskConst.ReadFlag
     */
    private Integer readFlag;

    // 宽表模式和其他模式 构造的SQL 申请构造，数据同步
    private String assetsSql;

    private String assetTableName;

    @ApiModelProperty("模板ID")
    @NotNull(message = "模板ID必填")
    private Long templateId;

    @NotNull(message = "模板版本必填")
    private Integer templateVersion;

    @ApiModelProperty("模板类型")
    private String templateType;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("流程版本")
    private Integer processVersion;

    @ApiModelProperty("基础表ID")
    private Long baseTableId;
    /**
     * 底表版本
     */
    private Integer baseVersion;

    /**
     * 底表 期数版本
     *
     * @see TableInfoSnapshot#versionPeriod
     */
    private String baseVersionPeriod;

    /**
     * 调度明细id
     *
     * @see AssetsFlowBatchDetail#id
     */
    private Long flowDetailId;

    @ApiModelProperty("基础表名")
    @Size(max = 100, message = "名称长度超出限制")
    @NotBlank(message = "表单名称不能为空白")
    private String baseTableName;

    @ApiModelProperty("相关表名")
    private String allTableNames;

    @ApiModelProperty("所有者ID")
    @NotNull(message = "所有者ID必填")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    @Size(max = 100, message = "申请人姓名长度超出限制")
    private String applicantName;

    @ApiModelProperty("申请人所属部门")
    private String applicantDepartment;

    @ApiModelProperty("需求名称")
    @NotNull(message = "需求名称必填")
    @Size(max = 100, message = "需求名称长度超出限制")
    @NotBlank(message = "需求名称不能为空白")
    private String projectName;

//    @ApiModelProperty("项目背景描述")
//    @Size(max = 100, message = "背景描述长度超出限制")
//    private String projectDesc;

    /**
     * @see RequireAttrType
     */
    @ApiModelProperty("需求性质 1：内部分析、2：交付客户、3：对外宣传")
    @NotNull(message = "需求性质必填")
    private Integer requireAttr;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("1：一次性需求、2：持续性需求")
    @NotNull(message = "需求次数必填")
    private Integer requireTimeType;

    @ApiModelProperty("需求客户名")
    @TableField(value = "client_names")
    @NotNull(message = "客户名称必填")
    @Size(max = 100, message = "需求客户名长度超出限制")
    private String clientNames;

    @ApiModelProperty("合同编号")
    @TableField(value = "contract_no")
    @NotNull(message = "合同编号")
    @Size(max = 50, message = "合同编号长度超出限制")
    private String contractNo;

    @ApiModelProperty("数据有效截止时间")
    @NotNull(message = "数据有效截止时间必填")
    private LocalDateTime dataExpire;

    @ApiModelProperty("数据量")
    private Long dataTotal;

    /**
     * 复制来源id
     */
    private Long copyFromId;

    @ApiModelProperty("申请最近更新时间")
    private String applyLastUpdateTime;

    @ApiModelProperty("分配客户会读取该值, 默认为0， 0 - 初次分配，需要同步， 1 - 无需同步")
    private Integer firstSyncTag = 0;

    @ApiModelProperty("判断是否需要更新同步CK数据至PG， 0 - 未在任务中， 1 - 同步任务中， 默认1")
    private Integer needSyncTag = CommonConstants.NOT_UPDATE_TASK;

    /**
     * @see ApplicationConst.ApplyStatus.DISABLE
     */
    private Integer status;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private String deliverTimeType;

    /**
     * 关联主项目 名称
     */
    private String newProjectName;
}
