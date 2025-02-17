package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/18
 */
@Data
@ApiModel("服务申请流程")
@AllArgsConstructor
@NoArgsConstructor
public class ServiceFlowDTO implements Serializable {

    @ApiModelProperty("服务申请流程id")
    private String id;

    @ApiModelProperty("服务申请流程名称")
    private String name;

}
