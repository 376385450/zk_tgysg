package com.sinohealth.system.biz.dataassets.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/9/19
 */
@Data
public class AssetValidateDTO implements Serializable {

    // 重复提示
    private String msg;


    private boolean repeat;

}
