package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;
import com.sinohealth.system.biz.application.dto.request.UserDataAssetsDistRequest;
import com.sinohealth.system.domain.TgApplicationInfo;

import java.util.List;

public interface DataAssetsService extends IService<TgApplicationInfo> {

    AjaxResult<List<String>> assetsTimeGra();

    AjaxResult<IPage<TgUserDataAssetsDistDto>> queryAssetsDistList(UserDataAssetsDistRequest request);

    void updateOwnerId(Long ownerId, Long id);

    AjaxResult<Void> markRunState(String applicationNo, Integer state);

}
