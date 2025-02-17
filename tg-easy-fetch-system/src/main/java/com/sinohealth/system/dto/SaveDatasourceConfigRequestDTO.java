package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 保存数据源信息
 *
 * @author linkaiwei
 * @date 2021/11/26 11:40
 * @since 1.6.2.0
 */
@Data
@ApiModel("保存数据源信息")
@Accessors(chain = true)
public class SaveDatasourceConfigRequestDTO implements Serializable {

    @ApiModelProperty("数据源名称")
    private String name;

    @ApiModelProperty("数据库名称")
    private String sourceName;

    @ApiModelProperty("数据库连接地址")
    private String url;

    @ApiModelProperty("数据库账号")
    private String username;

    @ApiModelProperty("数据库密码")
    private String password;

    /**
     * 数据库类型，详情见{@link com.sinohealth.bi.enums.DatabaseEnum}，对应 feature 字段
     */
    @ApiModelProperty("数据库类型，mysql、hive2、impala")
    private String type;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String remark;

}
