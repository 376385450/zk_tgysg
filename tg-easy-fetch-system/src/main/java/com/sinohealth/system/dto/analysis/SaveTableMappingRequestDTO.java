package com.sinohealth.system.dto.analysis;

import com.sinohealth.system.dto.TableMappingDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 字段映射，请求参数
 *
 * @author linkaiwei
 * @date 2021/11/4 09:50
 * @since 1.6.1.0
 */
@Data
@ApiModel("字段映射，请求参数")
@Accessors(chain = true)
public class SaveTableMappingRequestDTO implements Serializable {

    /**
     * 表ID
     */
    @JsonIgnore
    private Long tableId;

    @ApiModelProperty("字段映射信息列表")
    @NotEmpty(message = "字段映射信息列表不能为空")
    private List<TableMappingDTO> list;

}
