package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2023/12/28
 */
@Data
@TableName("application_column_setting")
public class ApplicationColumnSetting {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    // 英文字段名
    private String filedName;

    // 官方中文名
    private String filedAlias;

    // 自定义命名
    private String customName;

    private String dataType;

    private boolean primaryKey;

    private Integer sort;

    private boolean rangeField;

    /**
     * y n 前端组件值
     */
    private String defaultShow;

}
