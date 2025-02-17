package com.sinohealth.system.dto.application.deliver;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 14:08
 */
@Data
@JsonNaming
@Accessors(chain = true)
@ApiModel("数据说明文档校验返回实体")
public class ApplicationDataDescDocVerifyDTO implements Serializable {

    @ApiModelProperty("缺少数据说明文档的提数id")
    private List<Long> applyIds;

    private List<String> applyNames;
}
