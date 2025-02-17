package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.system.biz.application.dto.ApplicationDistributedInfo;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.TopSettingApplyDto;
import com.sinohealth.system.biz.application.dto.request.ColumnSetting;
import com.sinohealth.system.biz.application.service.impl.ApplicationTaskConfigServiceImpl;
import com.sinohealth.system.biz.application.util.ApplyUtil;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ColsInfoDto;
import com.sinohealth.system.dto.application.JoinInfoDto;
import com.sinohealth.system.dto.application.MetricsInfoDto;
import com.sinohealth.system.dto.application.TemplateMetric;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.service.impl.ApplicationServiceImpl;
import com.sinohealth.system.service.impl.AuditProcessServiceImpl;
import com.sinohealth.system.service.impl.TableInfoServiceImpl;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 提数申请信息表(TgApplicationInfo)表实体类
 *
 * @author makejava
 * @see ApplicationServiceImpl#normalizeApplicationInfo(TgApplicationInfo) 参数修正
 * @since 2022-05-11 16:21:17
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "提数申请信息表(TgApplicationInfo)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_application_info")
@Accessors(chain = true)
public class TgApplicationInfo extends Model<TgApplicationInfo> implements IdTable {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("项目id")
    private Long projectId;

    @ApiModelProperty("重新申请会有此id,保留重新申请前的id,重新申请状态中可以预览前一申请数据")
    private Long oldApplicationId;

    @ApiModelProperty("重新申请会有此id,保留新申请的id")
    @TableField(value = "new_application_id")
    private Long newApplicationId;

    /**
     * 存储申请对应的 资产id， 另存为的资产不关联此处
     *
     * @see UserDataAssets#id
     */
    @ApiModelProperty("数据资产id")
    private Long assetsId;

    @ApiModelProperty("新资产id")
    private Long newAssetId;

    @ApiModelProperty("预估交付时间")
    private LocalDateTime expectDeliveryTime;

    @ApiModelProperty("资产交付时间  实际交付时间")
    private LocalDateTime assetsCreateTime;

    @ApiModelProperty("模板ID")
    @NotNull(message = "模板ID必填")
    private Long templateId;

    @Deprecated
    @NotNull(message = "模板版本必填")
    private Integer templateVersion;

    /**
     * @see ApplicationServiceImpl#insertOrUpdateApplication
     */
    @ApiModelProperty("需求ID")
    private String applicationNo;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("流程版本")
    private Integer processVersion;

    /**
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private String deliverTimeType;

    @ApiModelProperty("交付时间")
    private String deliverTime;

    @ApiModelProperty("延期天数")
    private Integer deliverDelay;

    @ApiModelProperty("初次期望时间")
    private Date expectTime;

    @Deprecated
    @ApiModelProperty("项目经理")
    private String pm;

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

    @ApiModelProperty("需求名称")
    @NotNull(message = "需求名称必填")
    @Size(max = 100, message = "需求名称长度超出限制")
    @NotBlank(message = "需求名称不能为空白")
    private String projectName;

    @ApiModelProperty("项目背景描述")
    @Size(max = 200, message = "背景描述长度超出限制")
    @Deprecated
    private String projectDesc;

    /**
     * 需求描述
     */
    @Size(max = 200, message = "需求描述长度超出限制")
    private String applyDesc;

    /**
     * @see RequireAttrType
     */
    @ApiModelProperty("需求性质 1：内部分析、2：交付客户、3：对外宣传")
    @NotNull(message = "需求性质必填")
    private Integer requireAttr;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("需求类型 1：一次性需求、2：持续性需求")
    @NotNull(message = "需求类型必填")
    private Integer requireTimeType;

    @Deprecated
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

    @ApiModelProperty("申请原因")
    @TableField(value = "apply_reason")
    @NotNull(message = "申请原因")
    @Size(max = 200, message = "申请原因长度超出限制")
    private String applyReason;

    @ApiModelProperty("数据有效截止时间")
    @NotNull(message = "数据有效截止时间必填")
    private Date dataExpir;

    @ApiModelProperty("关联数据信息(JSON序列化)")
    @TableField(exist = false)
    @Deprecated
    private List<JoinInfoDto> joinInfo;

    /**
     * 存放的是选择列信息，但是1.7改版后出现了三个情况
     * 1. 模板配置的粒度内的字段去看 granularity
     * 2. 申请层配置关联表的字段选用情况还是看这里, 并且要忽略掉数组内已经放到粒度的那些字段，避免重复
     */
    @ApiModelProperty("维度数据信息(JSON序列化)")
    @TableField(exist = false)
    private List<ColsInfoDto> colsInfo;

    @ApiModelProperty("提数模板中的聚合指标(JSON序列化)")
    @TableField(exist = false)
    private TemplateMetric templateMetrics;

    @ApiModelProperty("指标数据信息(JSON序列化)")
    @TableField(exist = false)
    private List<MetricsInfoDto> metricsInfo;

    @ApiModelProperty("模板数据范围信息(JSON序列化)")
    @TableField(exist = false)
    private FilterDTO dataRangeInfo;

    @ApiModelProperty("申请数据范围信息(JSON序列化)")
    @TableField(exist = false)
    private FilterDTO applyDataRangeInfo;

    @ApiModelProperty("流程审核节点信息(JSON序列化)")
    @TableField(exist = false)
    private List<ProcessNodeEasyDto> handleNode;
    @ApiModelProperty("流程审核节点信息(JSON序列化)")
    @JsonIgnore
    private String handleNodeJson;

    /**
     * 用户id -> 索引 -> 审核状态
     */
    @ApiModelProperty("审核人-节点映射")
    @JsonIgnore
    @TableField(exist = false)
    private Map<Long, Map<String, Integer>> handlerIndexMapping;

    @ApiModelProperty("表名-别名映射")
    @JsonIgnore
    @TableField(exist = false)
    private Map<String, String> tableAliasMapping;

    @ApiModelProperty("当前审核人")
    private String currentHandlers;


    @ApiModelProperty("所有审核人")
    @JsonIgnore
    private String allHandlers = "";

    @ApiModelProperty("状态链")
    @JsonIgnore
    private String statusChain = "";

    @ApiModelProperty("当前索引")
    private Integer currentIndex;

    /**
     * @see ApplicationConst.AuditStatus
     * @see AuditProcessServiceImpl#postHandleForPass 审核通过
     * @see UserDataAssetsService#executeWorkFlow 通用模板执行
     */
    @ApiModelProperty("流程状态")
    private Integer currentAuditProcessStatus;

    @ApiModelProperty("当前节点审核状态")
    private Integer currentAuditNodeStatus;

    /**
     * 评估结果
     */
    private String evaluationResult;

    @ApiModelProperty("关联数据信息(JSON序列化)")
    @JsonIgnore
    private String joinJson;

    @ApiModelProperty("维度数据信息(JSON序列化)")
    @JsonIgnore
    private String colsJson;
    @ApiModelProperty("提数模板中的聚合指标(JSON字符串)")
    @JsonIgnore
    private String templateMetricsJson;
    @ApiModelProperty("申请数据范围信息(JSON序列化)")
    @JsonIgnore
    private String applyDataRangeInfoJson;

    @ApiModelProperty("指标数据信息(JSON序列化)")
    @JsonIgnore
    private String metricsJson;

    @ApiModelProperty("数据范围信息(JSON序列化)")
    @JsonIgnore
    private String dataRangeJson;

    @ApiModelProperty("审核人-节点映射")
    @JsonIgnore
    private String handleIndexMappingJson;

    @ApiModelProperty("表名-别名映射")
    @JsonIgnore
    private String tableAliasMappingJson;

    @ApiModelProperty("SQL片段")
    @JsonIgnore
    @TableField(value = "asql_parts_json")
    private String sqlPartsJson;

    @ApiModelProperty("top设置")
    @TableField(exist = false)
    private TopSettingApplyDto topSetting;
    @JsonIgnore
    private String topSettingJson;

    @ApiModelProperty("指标列")
    @TableField(exist = false)
    private List<CustomMetricsLabelDto> customMetrics;

    @ApiModelProperty("添加导出 需求名称")
    private Boolean exportProjectName;

    @ApiModelProperty("列设置")
    @TableField(exist = false)
    private List<ColumnSetting> columnSettings;

    @JsonIgnore
    private String customMetricsJson;

    @ApiModelProperty("申请 粒度")
    @TableField(exist = false)
    private List<ApplicationGranularityDto> granularity;
    @JsonIgnore
    private String granularityJson;

    /**
     * 业务扩展字段
     *
     * @see ApplicationTaskConfigServiceImpl#fillZdyParam 解析和处理
     */
    private String customExt;

    @ApiModelProperty("宽表模板 出数SQL")
    @JsonIgnore
    private String asql;

    @ApiModelProperty("长尾模板 出数SQL")
    @JsonIgnore
    private String tailSql;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    @ApiModelProperty("申请通过时间")
    private String applyPassedTime;

    @ApiModelProperty("对外数据表名字")
    private String outTableName;

    @ApiModelProperty("新增、修改")
    private String applyType;

    @ApiModelProperty("用于指定日期聚合使用的字段ID")
    private String periodField;

    @ApiModelProperty("用于指定日期聚合使用的类型(年/半年度/季度/月份/日)")
    private String periodType;

    /**
     * @see ApplicationConst.ApplicationType
     */
    @ApiModelProperty("申请类型：data 数据申请(默认)， doc 文档申请")
    private String applicationType = "data";

    @ApiModelProperty("文档ID，用于doc文档申请")
    private Long docId;

    @ApiModelProperty("文档名称")
    private String docName;

    @ApiModelProperty("申请描述")
    private String applyComment;

    @ApiModelProperty("需求备注 通用模式")
    private String applyRemark;

    /**
     * @see FileAssetsUploadDTO 数组
     */
    @ApiModelProperty("需求附件 通用模式")
    private String remarkFiles;

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

    @TableField(exist = false)
    private FileAssetsUploadDTO assetsAttach;

    /**
     * 资产附件 文件类型资产
     */
    @JsonIgnore
    private String assetsAttachJson;

    /**
     * 是否启用推送
     */
    private Boolean pushPowerBi;

    /**
     * 推送的项目名称
     */
    private String pushProjectName;

    ////////////////
    /**
     * 启用 自定义标签
     */
    private Boolean customTag;
    /**
     * 项目名称
     */
    private String tagProjectName;
    /**
     * 自定义标签
     */
    private String tagTags;
    /**
     * 关联客户
     */
    private String tagClient;
    /**
     * 关联应用表名
     */
    private String tagTableName;
    /**
     * 关联标签id
     */
    private String tagIds;
    /**
     * 是否新增列
     */
    private Boolean tagNewField;
    /**
     * 是否级联指标
     */
    private Boolean tagCascade;
    ////////////////

    /**
     * 开启QC
     */
    private Boolean assetsQc;
    /**
     * QC 项目类型
     */
    private String projectScope;


    @ApiModelProperty("文档权限")
    @TableField(exist = false)
    private List<Integer> docAuthorization;

    @ApiModelProperty("文档权限JSON")
    @JsonIgnore
    private String docAuthorizationJson;

    @ApiModelProperty("资产权限控制")
    @TableField(exist = false)
    private List<AssetPermissionType> permission;

    @ApiModelProperty("资产权限控制JSON")
    @JsonIgnore
    private String permissionJson;

    @ApiModelProperty("数据量")
    private Long dataTotal;

    /**
     * 复制来源id
     */
    private Long copyFromId;

    /**
     * 出数状态 宽表模式内部控制，工作流类型通过尚书台回调控制
     *
     * @see ApplyDataStateEnum
     */
    private String dataState;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    //////////////// 审核 配置弹窗内修改的字段 ////////////////
    /**
     * @see ApplicationConfigTypeConstant
     */
    @ApiModelProperty("0：SQL模式，1：工作流模式")
    private Integer configType;

    @ApiModelProperty("配置的SQL")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String configSql;

    @ApiModelProperty("配置SQL生成的工作流ID")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer configSqlWorkflowId;

    @ApiModelProperty("工作流ID")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer workflowId;

    /**
     * 是否关联字段库
     */
    private Boolean relateDict;

    //////////////////////

    /**
     * 工作流实例 uuid
     *
     * @see com.sinohealth.web.controller.system.ApplicationApiController#executeWorkFlow(Long)
     */
    private String flowInstanceId;

    /**
     * false 标识不打包
     */
    @ApiModelProperty("是否开启打包")
    private Boolean packTailSwitch;

    @ApiModelProperty("打包方式(打包名称)")
    private String packTailName;

    @ApiModelProperty("打包配置id")
    private Long packTailId;

    /**
     * 是否开启 分布信息
     */
    private Boolean openDistributed;

    /**
     * 分布信息
     */
    @ApiModelProperty("分布信息")
    @TableField(exist = false)
    private List<ApplicationDistributedInfo> distributeds;

    @ApiModelProperty("分布信息JSON")
    @JsonIgnore
    private String distributedJson;

    /**
     * 选择的时间粒度汇总 , 分隔
     */
    private String timeGra;

    /**
     * 选择的产品粒度汇总
     */
    private String productGra;

    /**
     * 获取主表和关联表
     *
     * @return 表名 , 分隔
     */
    public String calcTableInfo() {
        List<String> tables = TgCollectionUtils.newArrayList();
        if (org.apache.commons.collections4.CollectionUtils.size(joinInfo) > 0) {
            joinInfo.forEach(j -> {
                String tableName1 = SpringUtils.getBean(TableInfoServiceImpl.class).getDetail(j.getTableId1()).getTableName();
                String tableName2 = SpringUtils.getBean(TableInfoServiceImpl.class).getDetail(j.getTableId2()).getTableName();
                tables.add(tableName1);
                tables.add(tableName2);
            });
            return StringUtils.join(tables.stream().filter(StringUtils::isNotBlank).distinct().toArray(), ",");
        } else {
            return baseTableName;
        }
    }

    public Set<Long> calcTableIds() {
        Set<Long> result = new HashSet<>();
        result.add(baseTableId);
        if (Objects.nonNull(joinInfo) && joinInfo.size() > 0) {
            joinInfo.forEach(j -> {
                Optional.ofNullable(j.getTableId1()).ifPresent(result::add);
                Optional.ofNullable(j.getTableId2()).ifPresent(result::add);
            });
        }

        return result;
    }

    public List<CustomMetricsLabelDto> calcSelectMetrics() {
        return customMetrics.stream().filter(v -> BooleanUtils.isTrue(v.getSelect())).collect(Collectors.toList());
    }

    public static TgApplicationInfo newInstance() {
        return new TgApplicationInfo();
    }

    public void fillGraInfo(){
        if (CollectionUtils.isEmpty(granularity)) {
            return;
        }
        granularity.stream()
                .filter(v -> FieldGranularityEnum.time.name().equals(v.getGranularity()))
                .findFirst().ifPresent(v -> {
                    List<String> str = v.getSelectGranularity();
                    if (CollectionUtils.isEmpty(str)) {
                        return;
                    }
                    String pair = ApplyUtil.GRA_SPLIT + String.join(ApplyUtil.GRA_SPLIT, str) + ApplyUtil.GRA_SPLIT;
                    this.setTimeGra(pair);
                });
        granularity.stream()
                .filter(v -> FieldGranularityEnum.product.name().equals(v.getGranularity())).findFirst()
                .ifPresent(v -> {
                    List<String> str = v.getSelectGranularity();
                    if (CollectionUtils.isEmpty(str)) {
                        return;
                    }
                    String pair = ApplyUtil.GRA_SPLIT + String.join(ApplyUtil.GRA_SPLIT, str) + ApplyUtil.GRA_SPLIT;
                    this.setProductGra(pair);
                });
    }
}

