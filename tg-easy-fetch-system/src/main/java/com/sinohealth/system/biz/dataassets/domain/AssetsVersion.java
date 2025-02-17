package com.sinohealth.system.biz.dataassets.domain;

import com.sinohealth.common.exception.CustomException;
import org.apache.commons.math3.util.Pair;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-23 16:59
 */
public interface AssetsVersion {

    Long getAssetsId();

    Integer getVersion();

    default String getAssetsVersion() {
        return AssetsVersion.buildAssetsVersion(getAssetsId(), getVersion());
    }

    static String buildAssetsVersion(Long assetsId, Integer version) {
        return assetsId + "#" + version;
    }

    static String buildViewName(String projectName, Integer version) {
        return projectName + "-V" + version;
    }

    static Pair<Long, Integer> parseAssetsVersion(String assetsVersion) {
        String[] pair = assetsVersion.split("#");
        if (pair.length != 2) {
            throw new CustomException("非法的资产版本 " + assetsVersion);
        }

        try {
            return Pair.create(Long.parseLong(pair[0]), Integer.parseInt(pair[1]));
        } catch (Exception e) {
            throw new CustomException("非法的资产版本 " + assetsVersion);
        }
    }
}
