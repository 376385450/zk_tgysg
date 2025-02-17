package com.sinohealth.system.dto.data;

import com.sinohealth.system.dto.TableLogMapDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Jingjun
 * @since 2021/5/28
 */
@Getter
@Setter
@ApiModel("DataStandardStatisticDto")
public class DataStandardStatisticDto {
    @ApiModelProperty("数据字典")
    private DataStandardCountDto dict;
    @ApiModelProperty("行业标准")
    private DataStandardCountDto business;
    @ApiModelProperty("编码目录")
    private DataStandardCountDto dir;
    @ApiModelProperty("堆叠图，30天变化")
    private List<TableLogMapDto> list;
}
