package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 字段排序信息
 *
 * @author linkaiwei
 * @date 2021/11/10 10:27
 * @since 1.6.1.0
 */
@Data
@ApiModel("字段排序信息")
@Accessors(chain = true)
public class TableFieldSortDTO implements Serializable {

    @NotEmpty(message = "字段排序列表不能为空")
    @ApiModelProperty("字段排序列表")
    private List<FieldSortDTO> list;


    @Data
    @ApiModel("字段排序")
    @Accessors(chain = true)
    public static class FieldSortDTO implements Serializable {

        @NotNull(message = "字段ID不能为空")
        @ApiModelProperty("字段ID")
        private Long id;

        @NotNull(message = "字段排序不能为空")
        @ApiModelProperty("字段排序")
        private Integer sort;

    }

}
