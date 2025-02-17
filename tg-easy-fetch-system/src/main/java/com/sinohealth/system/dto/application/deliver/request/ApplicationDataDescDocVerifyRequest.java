package com.sinohealth.system.dto.application.deliver.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 14:04
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ApiModel("数据说明文档校验请求")
public class ApplicationDataDescDocVerifyRequest extends DeliverBaseReq implements Serializable {

}
