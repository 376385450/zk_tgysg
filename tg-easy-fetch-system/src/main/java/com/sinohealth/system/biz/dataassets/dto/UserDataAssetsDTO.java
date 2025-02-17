package com.sinohealth.system.biz.dataassets.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.enums.dataassets.AssetsTypeEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-05 21:47
 */
@Data
public class UserDataAssetsDTO {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("源 申请id")
    private Long srcApplicationId;

    private Integer version;

    /**
     * @see AssetsTypeEnum
     */
    private String assetsType;

    // 构造的SQL 申请构造，数据同步
    private String assetsSql;

    private String assetTableName;

    @ApiModelProperty("模板ID")
    @NotNull(message = "模板ID必填")
    private Long templateId;
    @ApiModelProperty("模板类型")
    private String templateType;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("流程版本")
    private Integer processVersion;

    @ApiModelProperty("模板名称")
    @TableField(exist = false)
    @Size(max = 100, message = "名称长度超出限制")
    private String templateName;

    @ApiModelProperty("表id")
    private Long tableId;

    @ApiModelProperty("基础表ID")
    @NotNull
    private Long baseTableId;

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

    @ApiModelProperty("项目名称")
    @NotNull(message = "项目名称必填")
    @Size(max = 100, message = "项目名称长度超出限制")
    @NotBlank(message = "项目名称不能为空白")
    private String projectName;

    @ApiModelProperty("项目背景描述")
    @Size(max = 100, message = "背景描述长度超出限制")
    private String projectDesc;

    /**
     * @see RequireAttrType
     */
    @ApiModelProperty("需求性质 1：内部分析、2：交付客户、3：对外宣传")
    @NotNull(message = "需求性质必填")
    private Integer requireAttr;

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

    @ApiModelProperty("可读用户")
    @NotNull(message = "可读用户必填")
    @Size(max = 100, message = "可读用户名长度超出限制")
    private String readableUsers = "";

    @ApiModelProperty("可读用户姓名")
    @TableField(exist = false)
    private String readableUserNames = "";

    @ApiModelProperty("数据有效截止时间")
    @NotNull(message = "数据有效截止时间必填")
    private LocalDateTime dataExpire;

    @ApiModelProperty("数据量")
    private Long dataTotal;

    /**
     * 另存标记
     */
    private Boolean copy;

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
}
