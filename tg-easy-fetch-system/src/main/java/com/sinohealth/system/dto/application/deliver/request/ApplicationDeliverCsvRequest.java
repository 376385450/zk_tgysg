package com.sinohealth.system.dto.application.deliver.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 10:14
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ApiModel("数据交付csv请求")
public class ApplicationDeliverCsvRequest extends DeliverPackBaseReq implements Serializable {


}
