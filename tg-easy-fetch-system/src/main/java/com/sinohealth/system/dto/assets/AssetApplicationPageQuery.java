package com.sinohealth.system.dto.assets;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2023-09-03 13:24
 * @Desc
 */
@ApiModel(description = "/api/asset/my_application_query 接收的数据类")
@Data
public class AssetApplicationPageQuery {

    @ApiModelProperty("搜索内容")
    private String searchContent;

    // 库表申请和文件申请参数

    @ApiModelProperty("资产类型")
    private AssetType assetType;

    @ApiModelProperty("开放申请服务项(申请类型)")
    private String applicationType;

    // 模板申请参数

    @ApiModelProperty("需求性质")
    private Integer searchReqiureAttr;
    @ApiModelProperty("需求类型")
    private Integer searchRequireTimeType;
    @ApiModelProperty("客户名称")
    private String searchClient;
    @ApiModelProperty("流程状态")
    private Integer searchProcessStatus;
    /**
     * 出数状态
     * @see ApplyDataStateEnum
     */
    private String dataState;
    private String applicationNo;

    // 分页参数

    @ApiModelProperty("分页大小")
    private Integer pageSize;
    @ApiModelProperty("第几页")
    private Integer pageNum;
}
