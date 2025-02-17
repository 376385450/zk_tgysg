package com.sinohealth.system.biz.table.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class TableComparePlanVO {
    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty(value = "表id")
    private Long tableId;

    @ApiModelProperty(value = "旧版id")
    private Long oldVersionId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "完成时间")
    private Long creator;

    @ApiModelProperty(value = "创建人")
    private Date updateTime;
}
