package com.sinohealth.system.domain.asset;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/11/13
 */
@Data
public class TgAssetReadableResp implements Serializable {

    private Long id;


    private String assetName;

}
