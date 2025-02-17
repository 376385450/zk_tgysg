package com.sinohealth.system.domain;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Date;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-19 17:45
 */
@Data
@JsonNaming
public class ArkbiAnalysisDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 资产id 仪表板可能为多值,逗号分隔
     */
    private String assetsId;

    /**
     * BI分析ID
     */
    private String analysisId;

    /**
     * 编辑链接
     */
    private String editUrl;

    /**
     * 预览链接
     */
    private String previewUrl;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 状态,0:图表未保存，1:图表已保存
     */
    private Integer status;

    /**
     * 类型,dashboard:仪表板,chart:图表
     */
    private String type;

    /**
     * bi分享链接
     */
    private String shareUrl;

    /**
     * 分享链接密码
     */
    private String shareUrlPassword;

    /**
     * 父级ID,代表这条数据是从这个父级复制(推送到外网)而来
     */
    private Long parentId;

    private String projectName;

    private Long dirId;

}
