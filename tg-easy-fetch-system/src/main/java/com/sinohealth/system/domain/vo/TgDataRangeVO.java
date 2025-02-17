package com.sinohealth.system.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zhangyanping
 * @date 2023/5/15 16:16
 */
@Data
@ToString
@ApiModel
public class TgDataRangeVO {
    @ApiModelProperty(name = "范围ID用于模型的增删改查")
    private Long dataRangeId;

    @ApiModelProperty(name = "分组信息")
    private List<TgDataRangeGroupVO> groupList;

    @ApiModelProperty(name = "是否需要判断粒度字段")
    private Boolean hasCanChoose;
}
