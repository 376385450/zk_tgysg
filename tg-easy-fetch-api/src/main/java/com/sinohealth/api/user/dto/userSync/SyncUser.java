package com.sinohealth.api.user.dto.userSync;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/10/12
 */
@Data
public class SyncUser implements Serializable {

    private String tenantCode;

    private String tenantName;

    private String orgUserId;

    private String tgId;

    private String tgAccount;

    private String tgPhone;

    private String tgEmail;

    private String tgRealName;

    private String ysgId;

    private String ysgAccount;

    private String ysgPhone;

    private String ysgEmail;

    private String ysgRealName;

    private String errorMsg;

}
