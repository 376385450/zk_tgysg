package com.sinohealth.system.biz.message.dto;

import com.sinohealth.common.enums.AssetType;
import com.sinohealth.system.vo.AssetPermissions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/2/26
 */
@Data
public class MessageDTO implements Serializable {

    private String id;

    private Integer messageType;

    private String title;

    private boolean isRead;

    private Long assetId;

    private String assetName;

    private Long applicationId;

    private Long userAsserId;

    private Integer version;

    private String nodeId;

    // 1. 详情页，2. 预览页
    private Integer type;

    private AssetType assetType;

    private List<AssetPermissions> asset;

    private String applicationContent;

    private String content;

    private Date noticeTime;

}
