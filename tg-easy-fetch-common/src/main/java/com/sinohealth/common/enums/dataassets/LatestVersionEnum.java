package com.sinohealth.common.enums.dataassets;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-23 14:31
 * @see com.sinohealth.system.biz.dataassets.domain.UserDataAssets#readFlag 成功更新资产的已读未读
 * @see com.sinohealth.system.biz.dataassets.domain.AssetsUpgradeTrigger#state 工作流更新资产失败
 * @see com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger#state 宽表更新资产失败
 */
public enum LatestVersionEnum {
    success_not_read(1), success_read(2), failed(3);

    private final int code;

    public int getCode() {
        return code;
    }

    LatestVersionEnum(int code) {
        this.code = code;
    }
}
