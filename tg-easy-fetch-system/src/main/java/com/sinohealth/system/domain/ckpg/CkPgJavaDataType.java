package com.sinohealth.system.domain.ckpg;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * 枚举名为 CK数据库的 类型名
 */
@Slf4j
public enum CkPgJavaDataType {
    IntervalYear(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalQuarter(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalMonth(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalWeek(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalDay(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalHour(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalMinute(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    IntervalSecond(Types.INTEGER, "int2", Integer.class, true, 19, 0),
    UInt64(Types.BIGINT, "int8", BigInteger.class, false, 19, 0),
    UInt32(Types.INTEGER, "int8", Long.class, false, 10, 0),
    UInt16(Types.SMALLINT, "int4", Integer.class, false, 5, 0),
    UInt8(Types.TINYINT, "int4", Integer.class, false, 3, 0),
    Int64(Types.BIGINT, "int8", Long.class, true, 20, 0, "BIGINT"),
    Int32(Types.INTEGER, "int4", Integer.class, true, 11, 0, "INTEGER", "INT"),
    Int16(Types.SMALLINT, "int4", Integer.class, true, 6, 0, "SMALLINT"),
    Int8(Types.TINYINT, "int4", Integer.class, true, 4, 0, "TINYINT"),
    Date(Types.DATE, "date", java.sql.Date.class, false, 10, 0),
    Date32(Types.DATE, "date", java.sql.Date.class, false, 10, 0),
    DateTime(Types.TIMESTAMP, "timestamp", Timestamp.class, false, 19, 0, "TIMESTAMP"),
    DateTime64(Types.TIMESTAMP, "timestamp", Timestamp.class, false, 19, 0, "TIMESTAMP"),
    Enum8(Types.VARCHAR, "varchar(255)", String.class, false, 0, 0),
    Enum16(Types.VARCHAR, "varchar(255)", String.class, false, 0, 0),
    Float32(Types.FLOAT, "decimal", Float.class, true, 8, 2, "FLOAT"),
    Float64(Types.DOUBLE, "decimal", Double.class, true, 17, 2, "DOUBLE"),
    Decimal32(Types.DECIMAL, "decimal", BigDecimal.class, true, 9, 2),
    Decimal64(Types.DECIMAL, "decimal", BigDecimal.class, true, 18, 2),
    Decimal128(Types.DECIMAL, "decimal", BigDecimal.class, true, 38, 2),
    Decimal(Types.DECIMAL, "decimal", BigDecimal.class, true, 38, 2, "DEC"),
    /**
     * TODO 使用场景？
     */
    UUID(Types.OTHER, "varchar(255)", java.util.UUID.class, false, 36, 0),
    String(Types.VARCHAR, "varchar", String.class, false, 0, 0,
            "LONGBLOB",
            "MEDIUMBLOB",
            "TINYBLOB",
            "MEDIUMTEXT",
            "CHAR",
            "VARCHAR",
            "TEXT",
            "TINYTEXT",
            "LONGTEXT",
            "BLOB"),
    FixedString(Types.CHAR, "varchar(255)", String.class, false, -1, 0, "BINARY"),
    Nothing(Types.NULL, "varchar(255)", Object.class, false, 0, 0),
    Nested(Types.STRUCT, "varchar(255)", String.class, false, 0, 0),
    Tuple(Types.OTHER, "varchar(255)", String.class, false, 0, 0),
    Array(Types.ARRAY, "array", java.sql.Array.class, false, 0, 0),
    AggregateFunction(Types.OTHER, "varchar(255)", String.class, false, 0, 0),
    Unknown(Types.OTHER, "varchar(255)", String.class, false, 0, 0),
    ;

    private final int sqlType;
    private final String pgSQLType;
    private final Class<?> javaClass;
    private final boolean signed;
    private final int defaultPrecision;
    private final int defaultScale;
    private final String[] aliases;

    CkPgJavaDataType(int sqlType, String pgSQLType, Class<?> javaClass,
                     boolean signed, int defaultPrecision, int defaultScale,
                     String... aliases) {
        this.sqlType = sqlType;
        this.pgSQLType = pgSQLType;
        this.javaClass = javaClass;
        this.signed = signed;
        this.defaultPrecision = defaultPrecision;
        this.defaultScale = defaultScale;
        this.aliases = aliases;
    }

    public String getPgSQLType() {
        return pgSQLType;
    }

    public int getSqlType() {
        return sqlType;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public boolean isSigned() {
        return signed;
    }

    public int getDefaultPrecision() {
        return defaultPrecision;
    }

    public int getDefaultScale() {
        return defaultScale;
    }

    public static CkPgJavaDataType fromTypeString(String typeString) {
        String s = typeString.trim();
        for (CkPgJavaDataType dataType : values()) {
            if (s.equalsIgnoreCase(dataType.name())) {
                return dataType;
            }
            for (String alias : dataType.aliases) {
                if (s.equalsIgnoreCase(alias)) {
                    return dataType;
                }
            }
        }
        return CkPgJavaDataType.Unknown;
    }

    public static CkPgJavaDataType resolveDefaultArrayDataType(String typeName) {
        for (CkPgJavaDataType chDataType : values()) {
            if (chDataType.name().equals(typeName)) {
                return chDataType;
            }
        }
        log.warn("not mapping: typeName={}", typeName);
        return CkPgJavaDataType.String;
    }
}
