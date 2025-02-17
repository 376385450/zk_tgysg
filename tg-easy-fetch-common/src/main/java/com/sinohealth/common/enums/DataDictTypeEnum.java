package com.sinohealth.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Huangzk
 * @date 2021/5/14 15:26
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DataDictTypeEnum {

    TINYINT("tinyint", "数值"),
    SMALLINT("smallint", "数值"),
    INTEGER("int/integer", "数值"),
    BIGINT("bigint", "数值"),
    FLOAT("float", "数值"),
    DOUBLE("double", "数值"),
    DECIMAL("decimal", "数值"),
    DATE("date", "日期"),
    DATETIME("datetime", "日期"),
    TIMESTAMP("timestamp", "日期"),
    CHAR("char", "文本"),
    VARCHAR("varchar", "文本"),
    TINYBLOB("tinyblob", "文本"),
    TINYTEXT("tinytext", "文本"),
    BLOB("blob", "文本"),
    TEXT("text", "文本"),
    MEDIUMBLOB("mediumblob", "文本"),
    MEDIUMTEXT("mediumtext", "文本"),
    LONGBLOB("longblob", "文本"),
    LONGTEXT("longtext", "文本");

    String code;
    String name;

    public static DataDictTypeEnum valueOfCode(String code) {
        code = StringUtils.trimToNull(code);
        if (code == null) return null;
        for (DataDictTypeEnum value : values()) {
            if (value.getCode().equals(code)) return value;
        }
        return null;
    }




}
