package com.sinohealth.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-08-30 15:00
 * @Desc
 */
@Getter
public enum AssetPermissionType {

    DATA_EXCHANGE_REQUEST("DATA_EXCHANGE_REQUEST", "数据交换申请", "数据交换"),
    DATA_EXCHANGE("DATA_EXCHANGE", "数据交换", "数据交换"),

    DATA_EXCHANGE_OFFLINE("DATA_EXCHANGE_OFFLINE", "数据交换下线", "数据交换下线"),
    DATA_QUERY_REQUEST("DATA_QUERY_REQUEST", "数据查询申请", "数据查询"),
    DATA_QUERY("DATA_QUERY", "数据查询", "数据查询"),

    DATA_QUERY_OFFLINE("DATA_QUERY_OFFLINE", "数据查询下线","数据查询下线"),
    READ_FILE_REQUEST("READ_FILE_REQUEST", "文件阅读申请", "文件阅读"),
    READ_FILE("READ_FILE", "文件阅读", "文件阅读"),

    READ_FILE_OFFLINE("READ_FILE_OFFLINE", "文件阅读下线", "文件阅读下线"),
    DOWNLOAD_PDF_REQUEST("DOWNLOAD_PDF_REQUEST", "文件PDF下载申请", "文件PDF下载"),
    DOWNLOAD_PDF("DOWNLOAD_PDF", "文件PDF下载", "文件PDF下载"),

    DOWNLOAD_PDF_OFFLINE("DOWNLOAD_PDF_OFFLINE", "文件PDF下载下线", "文件PDF下载下线"),
    DOWNLOAD_SRC_REQUEST("DOWNLOAD_SRC_REQUEST", "源文件下载申请", "源文件下载"),
    DOWNLOAD_SRC("DOWNLOAD_SRC", "源文件下载", "源文件下载"),

    DOWNLOAD_SRC_OFFLINE("DOWNLOAD_SRC_OFFLINE", "源文件下载下线", "源文件下载下线"),
    TEMPLATE_APPLY_REQUEST("TEMPLATE_APPLY_REQUEST", "模板提数申请", "模板提数"),
    TEMPLATE_APPLY("TEMPLATE_APPLY", "模板提数", "模板提数"),

    TEMPLATE_APPLY_OFFLINE("TEMPLATE_APPLY_OFFLINE", "模板提数下线", "模板提数下线"),
    CHECK_PERSONAL_DATA_REQUEST("CHECK_PERSONAL_DATA_REQUEST", "查看我的数据申请", "查看我的数据"),

    CHECK_PERSONAL_DATA("CHECK_PERSONAL_DATA", "查看我的数据", "查看我的数据"),

    CHECK_PERSONAL_DATA_OFFLINE("CHECK_PERSONAL_DATA_OFFLINE", "查看我的数据下线", "查看我的数据下线");

    private String type;

    private String typeName;

    private String showName;

    AssetPermissionType(String type, String typeName, String showName) {
        this.type = type;
        this.typeName = typeName;
        this.showName = showName;
    }

    public static AssetPermissionType getByType(String type) {
        for(AssetPermissionType assetPermissionType : values()) {
            if(assetPermissionType.getType().equals(type)) {
                return assetPermissionType;
            }
        }
        return null;
    }

    public static AssetPermissionType getByTypeName(String typeName) {
        for(AssetPermissionType assetPermissionType : values()) {
            if(assetPermissionType.getTypeName().equals(typeName)) {
                return assetPermissionType;
            }
        }
        return null;
    }

    public static List<AssetPermissionType> checkOffLine(List<AssetPermissionType> requestPermission, List<AssetPermissionType> targetPermission) {

        List<AssetPermissionType> result = new ArrayList<AssetPermissionType>();
        for (AssetPermissionType assetPermissionType : targetPermission) {
            result.add(assetPermissionType);
            if (assetPermissionType.equals(DATA_EXCHANGE) && !requestPermission.contains(DATA_EXCHANGE_REQUEST)) {
                result.add(DATA_EXCHANGE_OFFLINE);
            } else if (assetPermissionType.equals(DATA_QUERY) && !requestPermission.contains(DATA_QUERY_REQUEST)) {
                result.add(DATA_QUERY_OFFLINE);
            } else if (assetPermissionType.equals(READ_FILE) && !requestPermission.contains(READ_FILE_REQUEST)) {
                result.add(READ_FILE_OFFLINE);
            } else if (assetPermissionType.equals(DOWNLOAD_PDF) && !requestPermission.contains(DOWNLOAD_PDF_REQUEST)) {
                result.add(DOWNLOAD_PDF_OFFLINE);
            } else if (assetPermissionType.equals(DOWNLOAD_SRC) && !requestPermission.contains(DOWNLOAD_SRC_REQUEST)) {
                result.add(DOWNLOAD_SRC_OFFLINE);
            } else if (assetPermissionType.equals(TEMPLATE_APPLY) && !requestPermission.contains(TEMPLATE_APPLY_REQUEST)) {
                result.add(TEMPLATE_APPLY_OFFLINE);
            } else if (assetPermissionType.equals(CHECK_PERSONAL_DATA) && !requestPermission.contains(CHECK_PERSONAL_DATA_REQUEST)) {
                result.add(CHECK_PERSONAL_DATA_OFFLINE);
            }
        }


        return result;

    }



}
