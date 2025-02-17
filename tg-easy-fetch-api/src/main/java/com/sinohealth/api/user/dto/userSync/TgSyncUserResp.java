package com.sinohealth.api.user.dto.userSync;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/10/11
 */
@Data
public class TgSyncUserResp implements Serializable {


    private List<SyncUser> successList;

    private List<SyncUser> failureList;


}
