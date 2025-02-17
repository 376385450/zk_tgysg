package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.biz.dataassets.dto.UserDataAssetsSyncDTO;
import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-23 15:03
 */
@Data
public class UserDataAssetsSyncRequest {

    private List<UserDataAssetsSyncDTO> assets;
}
