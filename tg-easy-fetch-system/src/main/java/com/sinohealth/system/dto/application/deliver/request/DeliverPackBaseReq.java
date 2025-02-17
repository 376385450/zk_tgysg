package com.sinohealth.system.dto.application.deliver.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-23 10:03
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DeliverPackBaseReq extends DeliverBaseReq {

    @ApiModelProperty("打包名称")
    private String packName;

    @ApiModelProperty("是否打包")
    private Boolean pack;

    public DeliverPackBaseReq(DeliverBaseReq base) {
        super();
        this.setAssetsId(base.getAssetsId());
        this.setNodeIds(base.getNodeIds());
    }
}
