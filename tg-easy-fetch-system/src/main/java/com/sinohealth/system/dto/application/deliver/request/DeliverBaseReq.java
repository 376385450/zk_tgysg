package com.sinohealth.system.dto.application.deliver.request;

import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-23 10:00
 */
@Data
public class DeliverBaseReq implements Serializable {

    /**
     * @see AssetsNode#buildId(Long, String)
     */
    @ApiModelProperty("节点id 仪表盘/图表分析/数据资产 id")
    private List<String> nodeIds;

    @ApiModelProperty("资产id")
    private Long assetsId;
}
