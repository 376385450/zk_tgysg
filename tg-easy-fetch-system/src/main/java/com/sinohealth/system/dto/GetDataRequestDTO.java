package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 数据集成-获取表数据
 *
 * @author linkaiwei
 * @date 2021/11/15 14:35
 * @since 1.6.1.0
 */
@Data
@ApiModel("数据集成-获取表数据")
@Accessors(chain = true)
public class GetDataRequestDTO implements Serializable {

    @ApiModelProperty(value = "数据源类型，1自定义EXCEL，2MySQL数据源", position = 1)
    @NotNull(message = "数据源类型不能为空")
    private Integer sourceType;

    @ApiModelProperty(value = "是否限制展示10条，true展示10条，false展示全部，默认true", position = 2)
    private Boolean limit = true;

    @ApiModelProperty(value = "上传文件（1自定义EXCEL，必传）", position = 3)
    private MultipartFile file;

    @ApiModelProperty(value = "表ID（2MySQL数据源，必传）", position = 4)
    private Long tableId;

    @ApiModelProperty(value = "字段ID列表（2MySQL数据源，必传）", position = 5)
    private List<Long> fieldIdList;

}
