package com.sinohealth.system.vo;

import com.sinohealth.system.domain.TgApplicationInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-26 14:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TgApplicationInfoDetailVO extends TgApplicationInfo {

    @ApiModelProperty("限制申请时 日期聚合使用的字段")
    private List<Long> applicationPeriodField;

    @ApiModelProperty("自选维度 必选的字段维度")
    private List<Long> mustSelectFields;

    /**
     * @see TgApplicationInfo#asql 同值
     */
    private String applySQL;

    private String assetsSQL;

    private Boolean allowApplicationPeriod;

    @ApiModelProperty("1：自选维度、2：固定维度")
    private Long colAttr;

    private String tableAlias;

    @ApiModelProperty("1：允许新增、2：不允许新增")
    private Long joinTableAttr;

    private String bizType;
    private String templateType;
    @ApiModelProperty("模板名称")
    @Size(max = 100, message = "名称长度超出限制")
    private String templateName;

    private String assetName;
    private Long assetId;

    /**
     * 展示 级联指标开关
     */
    private Boolean cascade;
    private String cascadeField;
}
