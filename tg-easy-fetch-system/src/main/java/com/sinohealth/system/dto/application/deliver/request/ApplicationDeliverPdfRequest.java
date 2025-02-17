package com.sinohealth.system.dto.application.deliver.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 10:14
 */
@Data
@ApiModel("数据交付pdf请求")
public class ApplicationDeliverPdfRequest extends DeliverPackBaseReq implements Serializable {

}
