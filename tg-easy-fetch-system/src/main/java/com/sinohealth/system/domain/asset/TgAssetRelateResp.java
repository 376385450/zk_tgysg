package com.sinohealth.system.domain.asset;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/10/30
 */
@Data
public class TgAssetRelateResp implements Serializable {

    private Boolean isChangeTopCatalog = false;

    private List<TgAssetRelateParam> list;

}
