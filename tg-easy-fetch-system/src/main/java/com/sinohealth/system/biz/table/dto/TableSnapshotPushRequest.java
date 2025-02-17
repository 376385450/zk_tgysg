package com.sinohealth.system.biz.table.dto;

import com.sinohealth.common.enums.process.FlowProcessUpdateType;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 11:43
 */
@Data
public class TableSnapshotPushRequest {

    @NotNull(message = "tableId 不能为空")
    private Long tableId;

    @ApiModelProperty("说明")
    private String remark;

//    @NotBlank(message = "期数不能为空")
//    private String versionPeriod;

    private Integer preVersion;

//    /**
//     * 当前版本类型
//     *
//     * @see FlowProcessTypeEnum
//     */
//    @NotBlank(message = "版本类型不能为空")
//    private String flowProcessType;

    /**
     * @see FlowProcessUpdateType
     */
    private String updateType;

    /**
     * 关联品类 , 拼接
     */
    private List<String> prodCodes;

    /**
     * 数据对比 版本类型
     *
     * @see FlowProcessTypeEnum
     */
    private String compareType;
    @ApiModelProperty("关联业务id")
    private Long bizId;

    @ApiModelProperty("是否需要qc")
    private Boolean needQc;

    @ApiModelProperty("是否需要数据对比")
    private Boolean needCompare;

    @ApiModelProperty("是否跳过用户资产版本限制")
    private Boolean skipAssertsBaseVersionFilter;

    private Long createBy;

    /**
     * 申请单时间类型
     */
    private List<String> deliverTimeTypes;
}
