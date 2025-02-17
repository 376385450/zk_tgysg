package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-31 17:34
 */
@Data
public class UserDataAssetsVersionPageDTO {

//    private Long id;

    /**
     * 资产id
     */
    private Long assetsId;

    private String templateType;

    /**
     * 申请id
     */
    private Long srcApplicationId;

    /**
     * 序号
     */
    private Integer sort;

    /**
     * 版本
     */
    private Integer version;

    private Boolean latest;

    /**
     * 版本变更类型
     *
     * @see AssetsSnapshotTypeEnum
     */
    private String snapshotType;

    /**
     * 对应宽表版本 组合字段
     *
     * @see TableInfoSnapshot#version
     * @see TableInfoSnapshot#versionPeriod
     */
    private String tableVersion;

    /**
     * 版本说明 来自上游
     */
    private String tableRemark;

    /**
     * 资产信息 来自资产
     */
    private String assetsRemark;
    /**
     * 期数
     */
    private String period;

    /**
     * 版本类型
     *
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    /**
     * 验收状态
     *
     * @see AcceptanceStateEnum
     */
    private String acceptState;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    private String expireType;

}
