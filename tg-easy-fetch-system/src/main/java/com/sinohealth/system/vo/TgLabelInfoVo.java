package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 17:06
 */
@Data
@ApiModel("分页查询标签")
public class TgLabelInfoVo {

    @ApiModelProperty("标签主键")
    private Long id;

    @ApiModelProperty("标签名称")
    private String name;

    @ApiModelProperty("资产名称集合")
    private List<String> assetNameList;

    @ApiModelProperty("资产名称合并字符串")
    private String assetNameStr;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("更新时间")
    private String updateTime;
}
