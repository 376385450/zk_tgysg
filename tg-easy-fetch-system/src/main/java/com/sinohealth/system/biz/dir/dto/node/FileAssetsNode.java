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
 * 2024-01-11 18:35
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAssetsNode implements AssetsNode {
    private String parentId;
    private String name;
    private List<AssetsNode> children;
    private String icon;
    private Boolean hidden;
    private LocalDateTime createTime;
    private String creatorName;
    private LocalDateTime updateTime;
    private Long creator;
    private String nId;
    private String pId;

    /**
     * 文件资产id
     */
    private Long assetsId;
    private String newProjectName;
    private String customerName;
    @ApiModelProperty("按钮列表")
    private List<Integer> actions;

    @Override
    public String getNodeId() {
        return AssetsNode.buildId(assetsId, icon);
    }

    @Override
    public Long getBizId() {
        return assetsId;
    }
}
