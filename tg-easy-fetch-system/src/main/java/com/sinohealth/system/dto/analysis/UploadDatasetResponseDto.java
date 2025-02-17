package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Huangzk
 * @date 2021/8/25 11:25
 */
@Data
@ApiModel("自定义数据源上传excel返回")
@Accessors(chain = true)
public class UploadDatasetResponseDto {
    /** dirId */
    @ApiModelProperty("dirId")
    private Long dirId;
    /** tableName */
    @ApiModelProperty("tableName")
    private String tableName;
    /** tableId */
    @ApiModelProperty("tableId")
    private Long tableId;
    @ApiModelProperty("字段类型")
    /** 上传字段个数 */
    private Integer fieldTotal;
    /** 上传行数 */
    @ApiModelProperty("上传行数")
    private Integer rows;
}
