package com.sinohealth.system.biz.arkbi.dto;

import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-10 16:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserBiViewVO {

    private UserDataAssetsBiView view;

    private Long applicantId;

    private String projectName;
}
