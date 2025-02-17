package com.sinohealth.common.constant;

/**
 * @Author Zhangzifeng
 * @Date 2023/9/12 15:23
 */
public class AssetConstants {

    // 操作无权限响应码
    public static final int PERMISSION_LACK_ERROR_CODE = 3001;

    public static final String ASSET_DELETED = "当前资产不存在，请确认资产是否已被删除";

    public static final String ASSET_OFF = "当前资产已下架，无法使用当前服务，如有问题可联系资产提供方";

    public static final String ASSET_NO_READ = "暂无当前资产的阅读权限，无法使用当前服务，如有问题可联系资产提供方";
    public static final String ASSET_SERVICE_OFF = "当前资产关联的服务已下架，无法使用当前服务，如有问题可联系资产提供方";


    public static final String NOT_EXIST_ASSET = "当前不存在资产,是否传递错误参数";
    public static final String INVALID_ID = "Invalid id";
}
