package com.sinohealth.system.dto.application.deliver.request;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 17:13
 */
@EqualsAndHashCode(callSuper = false)
@Data
@JsonNaming
@ApiModel("交付邮件模板查询")
public class DeliverEmailTemplateQuery extends DeliverBaseReq implements Serializable {

}
