package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 08:55
 */
@Data
@ApiModel("数据另存为请求对象")
public class ApplicationSaveAsRequest implements Serializable {

    @ApiModelProperty("资产id")
    @NotNull(message = "资产id不能为空")
    private Long assetsId;

    @ApiModelProperty("报表名称")
    @NotBlank(message = "报表名称不能为空")
    private String projectName;

    @ApiModelProperty("所属项目id")
    @NotNull(message = "项目未选择")
    private Long projectId;

    @ApiModelProperty("复合筛选")
    private GetDataInfoRequestDTO query;

}
