package com.sinohealth.system.biz.transfer.dto;

import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.constants.TopDurationType;
import com.sinohealth.system.biz.application.constants.TopPeriodTypeEnum;
import com.sinohealth.system.domain.TgApplicationInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 按顺序 字段和列 对应
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-03-08 16:42
 */
@Data
public class CrApplyVO implements CrExcelVO {

    //    @NotBlank(message = "需求ID不能为空")
    @ApiModelProperty("需求ID")
    private String applicationNo;

    @NotBlank(message = "需求名称不能为空")
    private String projectName;

    @ApiModelProperty("需求描述")
    @Size(max = 200, message = "需求描述长度超出限制")
    @Deprecated
    private String projectDesc;

    @NotBlank(message = "申请人不能为空")
    private String applicant;

    @NotBlank(message = "需求性质必填")
    private String requireAttr;

    @Size(max = 50, message = "合同编号长度超出限制")
    private String contractNo;

    // 通用模板 申请就填入 绑定的工作流
    /**
     * @see TgApplicationInfo#configType
     * @see TgApplicationInfo#workflowId
     */
    @ApiModelProperty("尚书台工作流")
    private String flowName;

    @NotBlank(message = "项目名称必填")
    private String project;

    @ApiModelProperty("常规交付周期")
    private String deliverTimeType;

//    @ApiModelProperty("期望交付时间")
//    private String deliverTime;

    @ApiModelProperty("交付延期天数")
    private String deliverDelay;
    @NotBlank(message = "模板名称必填")
    private String templateName;

    //    @NotBlank(message = "数据有效时间必填")
    private Date dataExpire;


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

    // ; 分隔 指标名
    @ApiModelProperty("指标列")
    private String dataKpi;

    /////////// TOP 设置
    /**
     * @see TopPeriodTypeEnum
     */
    private String topPeriodType;

    /**
     * TOP 时间条件
     */
    private String topScope;
    /**
     * 最近 N 月/季
     */
    private String lastDuration;
    /**
     * @see TopDurationType
     */
    private String durationType;

    @ApiModelProperty("top数量")
    private String topNum;
    @ApiModelProperty("top 目标对象字段")
    private String targetField;
    @ApiModelProperty("top 排序字段")
    private String sortField;
    @ApiModelProperty("top 分组字段")
    private String groupField;
    ///////////

    @ApiModelProperty("关联自定义列 市场")
    private String areaRangeTemp;

    public List<String> parseMetrics() {
        if (StringUtils.isBlank(dataKpi)) {
            return Collections.emptyList();
        }
        return Arrays.stream(dataKpi.split(";")).map(v -> v.replace("\n", "")).map(StringUtils::trimToEmpty).collect(Collectors.toList());
    }


    public Set<String> parseCols(String cols) {
        if (StringUtils.isBlank(cols)) {
            return Collections.emptySet();
        }

        return Arrays.stream(cols.split(";")).map(v -> v.replace("\n", "")).map(StringUtils::trimToEmpty).collect(Collectors.toSet());
    }

    public Set<String> parsePeriodCols() {
        return parseCols(periodDataCol);
    }

    public Set<String> parseProductCols() {
        return parseCols(productDataCol);
    }

    public Set<String> parseAreaCols() {
        return parseCols(areaDataCol);
    }

    public Set<String> parseOtherCols() {
        return parseCols(otherDataCol);
    }

}
