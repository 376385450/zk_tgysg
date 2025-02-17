package com.sinohealth.system.biz.dir.dto.node;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-11 15:41
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectNode implements AssetsNode {

    private String parentId;
    private String name;
    private List<AssetsNode> children;
    private String icon;
    private Boolean hidden;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String creatorName;
    private Long creator;
    private String nId;
    private String pId;

    private Long projectId;
    /**
     * 协作用户
     */
    private String helper;
    @ApiModelProperty("按钮列表")
    private List<Integer> actions;


    @Override
    public String getNodeId() {
        return AssetsNode.buildId(projectId, icon);
    }

    @Override
    public Long getBizId() {
        return projectId;
    }

}
