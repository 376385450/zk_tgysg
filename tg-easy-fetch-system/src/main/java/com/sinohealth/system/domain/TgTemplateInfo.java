package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.DistributedInfo;
import com.sinohealth.system.biz.application.dto.PackTailFieldDto;
import com.sinohealth.system.biz.application.dto.PushMappingField;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
import com.sinohealth.system.biz.application.dto.TopSettingTempDto;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dir.entity.DisplaySort;
import com.sinohealth.system.biz.template.dto.validate.TemplateAddGroup;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ColsInfoDto;
import com.sinohealth.system.dto.application.JoinInfoDto;
import com.sinohealth.system.dto.application.MetricsInfoDto;
import com.sinohealth.system.service.impl.TableInfoServiceImpl;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 模板信息表(TgTemplateInfo)表实体类
 *
 * @author makejava
 * @since 2022-05-11 16:21:18
 */
@ApiModel(description = "模板信息表(TgTemplateInfo)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_template_info")
@EqualsAndHashCode(callSuper = false)
public class TgTemplateInfo extends Model<TgTemplateInfo> implements IAssetBindingData, DisplaySort, IdTable {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("模板名称")
    @Size(max = 100, message = "名称长度超出限制")
    @NotBlank(message = "模板名称不能为空白", groups = {TemplateAddGroup.Basic.class})
    private String templateName;

    @ApiModelProperty("流程ID")
    @NotNull(message = "流程ID必填", groups = {TemplateAddGroup.Basic.class})
    private Long processId;

    @ApiModelProperty("1：自选维度、2：固定维度")
    private Long colAttr;

    @ApiModelProperty("1：允许新增、2：不允许新增")
    private Long joinTableAttr;

    @ApiModelProperty("基础表ID")
    private Long baseTableId;

    @ApiModelProperty("基础表名")
    @Size(max = 100, message = "名称长度超出限制", groups = {TemplateAddGroup.WildTable.class})
    @NotBlank(message = "表单名称不能为空白", groups = {TemplateAddGroup.WildTable.class})
    private String baseTableName;

    @ApiModelProperty("关联数据信息(JSON序列化)")
    @TableField(exist = false)
    private List<JoinInfoDto> joinInfo;

    @ApiModelProperty("维度数据信息(JSON序列化)")
    @TableField(exist = false)
    @NotEmpty(message = "请至少选择一个维度", groups = {TemplateAddGroup.WildTable.class})
    private List<ColsInfoDto> colsInfo;

    @ApiModelProperty("指标数据信息(JSON序列化)")
    @TableField(exist = false)
    private List<MetricsInfoDto> metricsInfo;

    @ApiModelProperty("数据范围信息(JSON序列化)")
    @TableField(exist = false)
    private FilterDTO dataRangeInfo;

    @ApiModelProperty("关联数据信息(JSON序列化)")
    @JsonIgnore
    private String joinJson;
    @ApiModelProperty("维度数据信息(JSON序列化)")
    @JsonIgnore
    private String colsJson;
    @ApiModelProperty("指标数据信息(JSON序列化)")
    @JsonIgnore
    private String metricsJson;
    @ApiModelProperty("数据范围信息(JSON序列化)")
    @JsonIgnore
    private String dataRangeJson;

    @ApiModelProperty("关联表单")
    @TableField(exist = false)
    private String joinTables;

    @ApiModelProperty("状态")
    private Long status;

    @ApiModelProperty("同一个表单下的排序")
    private Integer sortIndex;

    @ApiModelProperty("使用次数")
    private Long usedTimes;

    @ApiModelProperty("创建者")
    private String creator;

    @ApiModelProperty("更新者")
    private String updater;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    @Deprecated
    @ApiModelProperty("用于指定日期聚合使用的字段")
    private String periodField;

    @Deprecated
    @ApiModelProperty("用于指定日期聚合使用的类型(年/半年度/季度/月份/日)")
    private String periodType;

    @ApiModelProperty("限制申请时 日期聚合使用的字段")
    @TableField(exist = false)
    private List<Long> applicationPeriodField;

    @ApiModelProperty("自选维度 必选的字段维度")
    @TableField(exist = false)
    private List<Long> mustSelectFields;

    @Deprecated
    private Boolean allowApplicationPeriod;

    @ApiModelProperty("限制申请时 日期聚合使用的字段 JSON")
    @JsonIgnore
    private String applicationPeriodFieldJson;

    @ApiModelProperty("自选维度 必选的字段维度 JSON")
    @JsonIgnore
    private String mustSelectFieldsJson;

    @ApiModelProperty("指标配置 指标库 (JSON序列化)")
    @TableField(exist = false)
    private List<CustomMetricsLabelDto> customMetrics;
    @JsonIgnore
    private String customMetricsJson;

    @TableField(exist = false)
    private TopSettingTempDto topSetting;
    @JsonIgnore
    private String topSettingJson;

    /**
     * 注意前端渲染导致传入空对象数据的处理
     */
    @ApiModelProperty("粒度")
    @TableField(exist = false)
    private List<TemplateGranularityDto> granularity;
    @JsonIgnore
    private String granularityJson;

    @ApiModelProperty("是否开启粒度选择")
    private Boolean customGranularity;
    @ApiModelProperty("自定义设置项任一项必填")
    private Boolean customAnyRequired;

    /**
     * @see ApplicationConst
     */
    @ApiModelProperty("SQL构造模式 1 子查询 2 单层SQL")
    private Integer sqlBuildMode;

    /**
     * @see TemplateTypeEnum
     */
    @ApiModelProperty("模板类型")
    @NotNull(message = "模板类型必填", groups = {TemplateAddGroup.Basic.class})
    private String templateType;

    @ApiModelProperty("业务线")
    @NotNull(message = "业务线必填", groups = {TemplateAddGroup.Basic.class})
    private String bizType;

    @ApiModelProperty("样例数据库表")
    @TableField(exist = false)
    private String exampleTable;

    @ApiModelProperty("目录id")
    @NotNull(message = "目录必填", groups = {TemplateAddGroup.Basic.class})
    private Long dirId;

    @ApiModelProperty("工作流id 常规模板")
    @NotNull(message = "工作流必填", groups = {TemplateAddGroup.Normal.class})
    private Integer schedulerId;

    private String tempComment;

    /**
     * 业务扩展字段
     *
     * @see TgApplicationInfo#customExt
     */
    private String customExt;

    /**
     * 模板版本 发生编辑后版本加1
     */
//    @Deprecated
//    private Integer version;
    /**
     * 资产地图展示排序
     */
    private Integer disSort;

    @ApiModelProperty("是否开启打包长尾")
    private Boolean packTail;

    @ApiModelProperty("打包长尾条件")
    @TableField(exist = false)
    private FilterDTO tailFilter;
    @JsonIgnore
    private String tailFilterJson;

    @TableField(exist = false)
    @ApiModelProperty("长尾处理字段")
    private List<PackTailFieldDto> tailFields;
    @JsonIgnore
    private String tailFieldsJson;

    @TableField(exist = false)
    @ApiModelProperty("长尾打包配置")
    private List<TgTemplatePackTailSetting> tailSettings;

    /**
     * 是否开启推数逻辑，推送到PowerBI下关联的库表，使用PowerBI查看数据
     * <p>
     * 所有数据资产都支持
     */
    private Boolean pushPowerBi;

    /**
     * 启用 自定义标签
     */
    private Boolean customTag;

    /**
     * 推表名 目前固定数据源为华为云上BI库
     */
    private String pushTableName;

    @TableField(exist = false)
    private List<PushMappingField> pushFields;

    /**
     * 推数字段
     */
    @JsonIgnore
    private String pushFieldsJson;

    /**
     * 函数QC GP
     */
    private Boolean assetsQc;

    /**
     * 资产对比 Python
     */
    private Boolean assetsCompare;
//    /**
//     * @see AssetsQcTypeEnum
//     */
//    private String assetsQcType;

    /**
     * 是否开启 分布信息
     */
    private Boolean openDistributed;

    /**
     * 分层/分布信息 提示说明
     */
    private String distributedDescription;

    /**
     * 分布信息json
     */
    @JsonIgnore
    private String distributedJson;

    /**
     * 分布信息
     */
    @TableField(exist = false)
    @ApiModelProperty("分布信息配置")
    private List<DistributedInfo> distributeds;

    /**
     * 分布信息 - 分布列字段
     */
    @TableField(exist = false)
    @ApiModelProperty("分布信息 - 分布列字段")
    private List<FieldDictDTO> distributedFields;

    public static TgTemplateInfo newInstance() {
        return new TgTemplateInfo();
    }

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public String getTableInfo() {
        List<String> tables = TgCollectionUtils.newArrayList();

        if (Objects.nonNull(joinInfo) && !joinInfo.isEmpty()) {
            joinInfo.forEach(j -> {
                TableInfoServiceImpl info = SpringUtils.getBean(TableInfoServiceImpl.class);
                Optional.ofNullable(info.getBaseMapper().selectById(j.getTableId1()))
                        .map(TableInfo::getTableName).ifPresent(tables::add);
                Optional.ofNullable(info.getBaseMapper().selectById(j.getTableId2()))
                        .map(TableInfo::getTableName).ifPresent(tables::add);

//                String tableName1 = info.getDetail(j.getTableId1()).getTableName();
//                String tableName2 = info.getDetail(j.getTableId2()).getTableName();
//                tables.add(tableName1);
//                tables.add(tableName2);
            });
            return StringUtils.join(tables.stream().filter(StringUtils::isNotBlank)
                    .distinct().toArray(), ",");
        } else {
            return baseTableName;
        }
    }

    @Override
    public void fillDisSort(Integer sort) {
        this.disSort = sort;
    }

    public Set<Long> calcTableIds() {
        Set<Long> result = new HashSet<>();
        result.add(baseTableId);
        if (Objects.nonNull(joinInfo) && !joinInfo.isEmpty()) {
            joinInfo.forEach(j -> {
                Optional.ofNullable(j.getTableId1()).ifPresent(result::add);
                Optional.ofNullable(j.getTableId2()).ifPresent(result::add);
            });
        }

        return result;
    }

    @Override
    public String getName() {
        return templateName;
    }

}

