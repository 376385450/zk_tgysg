package com.sinohealth.system.biz.dir.dto.node;

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
public class CustomerNode implements AssetsNode {

    private String parentId;
    /**
     * 客户名
     */
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

    private Long customerId;

    @Override
    public String getNodeId() {
        return AssetsNode.buildId(customerId, icon);
    }

    @Override
    public Long getBizId() {
        return customerId;
    }
}
