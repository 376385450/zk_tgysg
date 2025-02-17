package com.sinohealth.system.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 内网数据（复杂SQL 或者 单表）到外网数据的映射
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 09:37
 */
@Data
@TableName("tg_table_application_mapping_info")
public class TgTableApplicationMappingInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("数据资产id")
    private Long assetsId;

    /**
     * 关联的多个原始表名拼接
     */
    private String tableName;

    /**
     * @see TgTableApplicationMappingInfo#dataTableName
     */
    @Deprecated
    private String currentPgTableName;

    /**
     * 数据表名, 注意需要使用分布式表
     */
    private String dataTableName;

    private Long dataVolume;

    private Date dateUpdateTime;
}
