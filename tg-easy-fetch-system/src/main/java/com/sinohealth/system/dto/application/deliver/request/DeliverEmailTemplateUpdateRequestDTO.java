package com.sinohealth.system.dto.application.deliver.request;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 17:27
 */
@Data
@JsonNaming
@Accessors(chain = true)
public class DeliverEmailTemplateUpdateRequestDTO implements Serializable {

    /**
     * @see AssetsNode#buildId(Long, String)
     */
    @ApiModelProperty("节点id 仪表盘/图表分析/数据资产 id")
    private List<String> nodeIds;

    @ApiModelProperty("资产id")
    private Long assetsId;

    private String title;

    private List<String> receiveMails;

    private String content;
}
