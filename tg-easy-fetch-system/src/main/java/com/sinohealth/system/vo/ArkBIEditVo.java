package com.sinohealth.system.vo;

import com.sinohealth.arkbi.vo.UserBaseInfoVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "方舟BI编辑信息")
public class ArkBIEditVo {
    @ApiModelProperty("分析ID,每次分析对应一个唯一ID")
    private String extAnalysisId;
    @ApiModelProperty("BI用户信息")
    private UserBaseInfoVo userInfo;
    @ApiModelProperty("数据分析URL")
    private String url;
    @ApiModelProperty("编辑URL")
    private String previewUrl;
    @ApiModelProperty("预览URL")
    private String shareUrl;
    @ApiModelProperty("BI登录token")
    private String token;
    @ApiModelProperty("视图id")
    private String viewId;
}