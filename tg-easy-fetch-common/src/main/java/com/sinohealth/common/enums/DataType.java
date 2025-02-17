package com.sinohealth.common.enums;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author Zeng jinming
 * @since 2021/5/10
 */
public enum DataType {
    Mysql(Arrays.asList(""));

    private List<String> dataType;
    private DataType(List<String> dataType){
        this.dataType=dataType;
    }

    public boolean contain(String dataType){
        return this.dataType.contains(dataType);
    }
}
