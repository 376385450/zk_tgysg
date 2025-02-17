package com.sinohealth.system.converter;

import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.dto.assets.AssetsDirDTO;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 15:37
 */
public class CustomerAuth2AssetsDirConverter {

    public static AssetsDirDTO convert(TgCustomerApplyAuth auth) {
        AssetsDirDTO assetsDirDTO = new AssetsDirDTO();
        assetsDirDTO.setId(auth.getId());
        assetsDirDTO.setDirName(auth.getNodeName());
        assetsDirDTO.setParentId(auth.getParentId());
        assetsDirDTO.setSort(0);
        assetsDirDTO.setIcon(auth.getIcon());
        assetsDirDTO.setNodeId(auth.getNodeId());
        assetsDirDTO.setStatus(auth.getStatus());
        assetsDirDTO.setAuthType(auth.getAuthType());
        assetsDirDTO.setUpdateBy(auth.getUpdateBy());
        return assetsDirDTO;
    }

}
