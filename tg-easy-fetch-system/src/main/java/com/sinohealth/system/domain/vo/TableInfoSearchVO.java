package com.sinohealth.system.domain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-13 16:23
 */
@Data
public class TableInfoSearchVO {
    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("表英文名")
    private String tableName;

    @ApiModelProperty("表中文名")
    private String tableAlias;

}
