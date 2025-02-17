package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 查询指定数据源，请求参数
 *
 * @author linkaiwei
 * @date 2021/8/16 17:09
 * @since 1.4.1.0
 */
@ApiModel("查询指定数据源，请求参数")
@Data
@Accessors(chain = true)
public class GetSourcesRequestDTO implements Serializable {

    @ApiModelProperty("表信息")
    @NotNull(message = "表信息不能为空")
    private List<TableDTO> tables;


    /**
     * 表信息
     *
     * @author linkaiwei
     * @date 2021/09/08 19:09
     * @since 1.4.2.0
     */
    @ApiModel("查询指定数据源，表信息")
    @Data
    @Accessors(chain = true)
    public static class TableDTO implements Serializable {

        @ApiModelProperty("唯一id")
        private Long uniqueId;

        @ApiModelProperty("表ID")
        private Long tableId;
    }

}
