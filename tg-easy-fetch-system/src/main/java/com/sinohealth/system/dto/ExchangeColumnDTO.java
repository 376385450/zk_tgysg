package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/11/7
 */
@Data
public class ExchangeColumnDTO implements Serializable {

    private String columnName;

    private String columnType;

    private String columnRemark;

}
