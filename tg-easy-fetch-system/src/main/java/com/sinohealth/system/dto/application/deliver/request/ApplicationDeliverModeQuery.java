package com.sinohealth.system.dto.application.deliver.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 11:19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ApiModel("支持的交付方式查询")
public class ApplicationDeliverModeQuery extends DeliverPackBaseReq implements Serializable {


}
