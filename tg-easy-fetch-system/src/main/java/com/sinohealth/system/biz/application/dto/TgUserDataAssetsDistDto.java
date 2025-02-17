package com.sinohealth.system.biz.application.dto;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.auditprocess.AuditPageByTypeDto;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.impl.DataAssetsServiceImpl;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @see TgApplicationInfoMapper#queryAssetsDistList
 * @see DataAssetsServiceImpl#queryAssetsDistList 主逻辑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TgUserDataAssetsDistDto {

    @ApiModelProperty("申请id")
    private Long id;
    @ApiModelProperty("模板ID")
    private Long templateId;

    /**
     * 存储申请对应的 资产id， 另存为的资产不关联此处
     *
     * @see UserDataAssets#id
     */
    @ApiModelProperty("数据资产id")
    private Long assetsId;

    @ApiModelProperty("需求ID")
    private String applicationNo;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("流程版本")
    private Integer processVersion;

    @ApiModelProperty("模板名称")
    private String templateName;

    // 模板
    private Integer schedulerId;
    // 申请
    private Integer configSqlWorkflowId;
    private Integer workflowId;

    private String templateType;

    @ApiModelProperty("来源表ID")
    private Long baseTableId;

    /**
     * 来源表或工作流名称
     */
    @ApiModelProperty("来源表")
    private String tableName;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    private String applicantName;

//    @ApiModelProperty("申请人所属部门")
//    private String applicantDepartment;

    @ApiModelProperty("需求名称")
    private String projectName;

    @ApiModelProperty("项目名称")
    private String newProjectName;

    @ApiModelProperty("需求性质")
    private Integer requireAttr;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("需求类型 1：一次性需求、2：持续性需求")
    private Integer requireTimeType;

    @ApiModelProperty("需求客户名")
    private String clientNames;

    @ApiModelProperty("数据有效截止时间")
    private String dataExpir;

    @ApiModelProperty("流程状态")
    private Integer currentAuditProcessStatus;

    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 出数状态
     *
     * @see ApplyDataStateEnum
     */
    private String dataState;

    @ApiModelProperty("申请时间")
    private String createTime;

    /**
     * 申请关联的资产最后出数时间
     */
    private LocalDateTime assetsCreateTime;

    /**
     * 资产最新版本 出数次数
     */
    private Integer dataVersion;
    /**
     * 需求个数
     */
    private Integer dataAmount;
    /**
     * 需求成本，单位p
     */
    private BigDecimal dataCost;
    /**
     * 需求成本，单位分钟
     */
    private Integer dataCostMin;

//    @ApiModelProperty("导出次数")
//    private Integer exportTotal;
//
//    @ApiModelProperty("是否过期")
//    private String validityStatus;

    private String asql;
    @ApiModelProperty("长尾模板 出数SQL")
    private String tailSql;

    @ApiModelProperty("业务线")
    private String bizType;

    /**
     * @see ApplicationConfigTypeConstant
     */
    @ApiModelProperty("0：SQL模式，1：工作流模式")
    private Integer configType;

    @ApiModelProperty("配置的SQL")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String configSql;

    private FileAssetsUploadDTO assetsAttach;
    @JsonIgnore
    private String assetsAttachJson;

    @ApiModelProperty("产品粒度")
    private String productGra;

    @ApiModelProperty("时间粒度")
    private String timeGra;

    /**
     * 更多
     *
     * @see DataAssetsServiceImpl#buildActions
     * @see AuditPageByTypeDto#actionList
     * @see ApplicationConst.AuditAction
     */
    private List<Integer> actionList;

    /**
     * 是否关联字段库
     */
    private Boolean relateDict;

    /**
     * 版本类型
     *
     * @see com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum
     */
    private String snapshotType;
    /**
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private String deliverTimeType;

    @ApiModelProperty("数据量")
    private Long dataTotal;

    /**
     * @see ApplyStateEnum
     */
    @ApiModelProperty("需求单状态")
    private String applyState;
    /**
     * @see ApplyRunStateEnum
     * @see ApplicationForm#applyRunState
     */
    @ApiModelProperty("需求单流程状态")
    private String applyRunState;


    @ApiModelProperty("流程审核节点信息(JSON序列化)")
    @TableField(exist = false)
    private List<ProcessNodeEasyDto> handleNode;
    @ApiModelProperty("流程审核节点信息(JSON序列化)")
    @JsonIgnore
    private String handleNodeJson;
    @ApiModelProperty("当前索引")
    private Integer currentIndex;
    @ApiModelProperty("当前审核人")
    private String currentHandlers;
    @ApiModelProperty("出数人")
    private String handleUser;

    @ApiModelProperty("预估交付时间")
    private LocalDateTime expectDeliveryTime;

    @ApiModelProperty("Cron中文")
    private String cronCN;
    @ApiModelProperty("交付方式")
    private String dataType;

    /**
     * 状态表维护
     */
    @ApiModelProperty("期数")
    private String period;

    @ApiModelProperty("申请次数")
    private Integer applyCnt;

    @ApiModelProperty("需求份数")
    private Integer applyAmount;
}

