package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author Rudolph
 * @Date 2023-08-08 10:19
 * @Desc
 */
@Getter
@AllArgsConstructor
public enum AssetType {
    MODEL("MODEL", "模板", "模板申请"),
    TABLE("TABLE", "库表", "提数申请"),
    FILE("FILE", "文件", "文件申请");

    private String type;

    private String name;

    private String applicationName;


}
