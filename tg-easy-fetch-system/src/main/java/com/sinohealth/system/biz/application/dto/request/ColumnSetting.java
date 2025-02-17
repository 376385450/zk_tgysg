package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.service.impl.ApplicationServiceImpl;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author shallwetalk
 * @Date 2023/12/27
 * @see ApplicationServiceImpl#buildHeaders(TgApplicationInfo) 头部构建
 */
@Data
public class ColumnSetting implements Serializable {

    private Integer id;

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

    private String defaultShow;

    public boolean hasShow(){
        return Objects.equals(defaultShow, "y");
    }

}
