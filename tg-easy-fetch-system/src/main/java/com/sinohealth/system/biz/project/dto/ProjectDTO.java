package com.sinohealth.system.biz.project.dto;

import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:39
 */
@Data
public class ProjectDTO {

    private Long id;

    private String name;

    private Long customerId;

    private String customerShortName;

    private Integer customerType;

    private Long projectManager;

    private String projectManagerName;

    private List<String> collaborationUserIds;

    private List<String> collaborationUser;

    private Integer relateAssetCount;

    private String description;

    private Integer relationApply;

    private Integer status;

    private Boolean isProjectManager;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("关联需求(数据资产)")
    private List<UserDataAssetResp> assets;

    private List<Long> uploadFileUsers;

}
