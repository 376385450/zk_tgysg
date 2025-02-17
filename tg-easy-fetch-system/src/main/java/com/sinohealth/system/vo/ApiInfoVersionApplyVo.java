package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * api接口订阅信息对象 api_subscribe_info
 *
 * @author dataplatform
 * @date 2021-07-05
 */
@ApiModel
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiInfoVersionApplyVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口服务version表主键ID
     */
    @ApiModelProperty("api服务id")
    private Long id;

    /** 数据仓库源表ID，数据源为2时允许有多张表，用json格式保存表ID和注释信息，示例[{"tableName":"表1","id":"1"},{"tableName":"表2","id":"2"}] */
    @ApiModelProperty("所有表id")
    private String tableId;

}
