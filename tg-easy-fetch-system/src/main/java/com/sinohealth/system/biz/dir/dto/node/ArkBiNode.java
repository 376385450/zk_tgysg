package com.sinohealth.system.biz.dir.dto.node;

import com.sinohealth.system.dto.ArkbiChartDataDirItemDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @see ArkbiChartDataDirItemDto
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-11 15:53
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArkBiNode implements AssetsNode {

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

    private Long id;

    /**
     * 资产ID
     */
    private Set<Object> assetsIds;

    /**
     * 申请人
     */
    private Set<Long> applicantIds;

    /**
     * 预览链接
     */
    private String extAnalysisId;

    /**
     * 源项目名称
     */
    private Set<String> projectNames;

    @ApiModelProperty("按钮列表")
    private List<Integer> actions;

    private Integer version;

    @Override
    public String getNodeId() {
        return AssetsNode.buildId(id, icon);
    }

    @Override
    public Long getBizId() {
        return id;
    }
}
