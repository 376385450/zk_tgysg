package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2023/11/6
 */
@Data
@TableName(value = "tg_data_sync_field_config", autoResultMap = true)
public class TgDataSyncFieldConfig {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer syncApplicationId;

    private String sourceColumnName;

    private String sourceColumnTypeName;

    private String sourceColumnRemark;

    private String targetColumnName;

    private String targetColumnTypeName;

    private String targetColumnRemark;

}
