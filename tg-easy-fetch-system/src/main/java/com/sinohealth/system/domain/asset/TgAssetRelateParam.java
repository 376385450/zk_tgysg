package com.sinohealth.system.domain.asset;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/10/30
 */
@Data
public class TgAssetRelateParam implements Serializable{

    private String typeName;

    private List<RelateAssetInfo> children;

}

