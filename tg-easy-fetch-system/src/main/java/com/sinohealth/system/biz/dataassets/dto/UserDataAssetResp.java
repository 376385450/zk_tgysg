package com.sinohealth.system.biz.dataassets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author shallwetalk
 * @Date 2024/1/12
 */
@Data
public class UserDataAssetResp implements Serializable {

    private Long assetId;

    private Long applicationId;

    private String applicationNo;

    private Integer requireAttr;

    private String applicationName;

    private Integer requireTimeType;

    private String bizType;

    private Date dataExpire;

    private Long copyFromId;

    private Long assetProjectId;

    private boolean mainAsset;

    private boolean hasDataExpired;

    private String applicantUser;

    private boolean containCopy;

    private String proType;

    private Long userId;

    private boolean currentUserAsset;

    private Integer relateCount;

}
