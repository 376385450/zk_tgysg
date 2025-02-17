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
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("数据交付image请求")
public class ApplicationDeliverImageRequest extends DeliverPackBaseReq implements Serializable {
}
