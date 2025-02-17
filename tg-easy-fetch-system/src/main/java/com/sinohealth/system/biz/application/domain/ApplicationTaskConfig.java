package com.sinohealth.system.biz.application.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.system.biz.application.constants.TopPeriodTypeEnum;
import com.sinohealth.system.biz.application.service.impl.ApplicationTaskConfigServiceImpl;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.service.DataRangeTemplateService;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;

/**
 * 常规模板（模板上绑定工作流） 通用模板（申请上绑工作流）提交后构造出参数表内对应记录
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-25 15:29
 * @see ApplicationTaskConfigServiceImpl#saveApplicationTaskConfig(TgApplicationInfo)
 */
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "提数申请 自动化工作流 参数表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_application_task_config")
@Accessors(chain = true)
public class ApplicationTaskConfig extends Model<ApplicationTaskConfig> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("需求ID")
    private Long applicationId;
    @ApiModelProperty("需求编号")
    private String applicationNo;
    @ApiModelProperty("需求名称")
    private String applicationName;
    @ApiModelProperty("项目编号")
    private String projectNo;
    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty("申请人姓名")
    private String projectUser;
    @ApiModelProperty("申请人所属部门")
    private String projectUnit;
    @ApiModelProperty("项目经理姓名")
    private String projectManager;
    @ApiModelProperty("项目背景描述")
    private String projectBackground;
    @ApiModelProperty("需求性质")
    private String projectType;
    @ApiModelProperty("合同编号")
    private String projectHtno;
    @ApiModelProperty("客户名称")
    private String projectCustomer;
    @ApiModelProperty("需求类型")
    private String projectIsConst;

    @ApiModelProperty("业务线")
    private String businessLine;

    @ApiModelProperty("需求模块，对应模板名称")
    private String businessBlock;

    @ApiModelProperty("数据有效期至")
    private String validDate;

    @ApiModelProperty("期待交付时间")
    private String expectDate;

    @ApiModelProperty("项目更新频率")
    private String projectUpdateFre;

    @ApiModelProperty("常规交付周期")
    private String regularLeadPeriod;

    @ApiModelProperty("时间粒度")
    private String periodGranular;
    @ApiModelProperty("时间列")
    private String periodDataCol;
    @ApiModelProperty("时间范围")
    private String periodScope;

    @ApiModelProperty("市场粒度")
    private String areaGranular;
    @ApiModelProperty("市场列")
    private String areaDataCol;
    @ApiModelProperty("市场范围")
    private String areaScope;

    @ApiModelProperty("产品粒度")
    private String productGranular;
    @ApiModelProperty("产品列")
    private String productDataCol;
    @ApiModelProperty("产品范围")
    private String productScope;

    @ApiModelProperty("会员粒度")
    private String memberGranular;
    @ApiModelProperty("会员列")
    private String memberDataCol;
    @ApiModelProperty("会员范围")
    private String memberScope;

    @ApiModelProperty("其他粒度")
    private String otherGranular;
    @ApiModelProperty("其他列")
    private String otherDataCol;
    @ApiModelProperty("其他范围")
    private String otherScope;

    @ApiModelProperty("指标列")
    private String dataKpi;

    /**
     * 自定义时间 条件树分类树的聚合
     *
     * @see DataRangeTemplateService#buildTargetSqlMap(Collection, String)
     */
    @ApiModelProperty("自定义时间")
    private String zdyPeriod;
    @ApiModelProperty("自定义市场")
    private String zdyArea;
    @ApiModelProperty("自定义产品标签")
    private String zdyProduct;
    @ApiModelProperty("自定义会员")
    private String zdyMember;
    @ApiModelProperty("自定义其他")
    private String zdyOther;


    @ApiModelProperty("TOP设置 多项设置")
    private String topKpi;

    /**
     * @see TopPeriodTypeEnum
     */
    private String topPeriodType;

    /**
     * SQL 或者 N;月
     */
    private String topPeriod;

    /**
     * 自定义参数 JSON
     */
    @ApiModelProperty("JSON自定义")
    private String zdyParam;

    @ApiModelProperty("工作流名称")
    private String flowName;

    @ApiModelProperty("需求特殊说明")
    private String remark;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("审批通过时间 最后一个节点时间")
    private LocalDateTime approveTime;

    @ApiModelProperty("需求状态")
    private String projectStatus;

    private String assetsQc;
    private String projectScope;
    /**
     * 是否启用推送
     */
    private Boolean pushPowerBi;

    private Boolean active;

    @ApiModelProperty("数据有效截止时间")
    @NotNull(message = "数据有效截止时间必填")
    private Date dataExpir;


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

    @ApiModelProperty("自定义区间")
    private String zdyQj;
}
