package com.sinohealth.system.service;

import com.sinohealth.system.domain.TgIntelligenceUserMapping;
import com.sinohealth.system.dto.UserCreateDto;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/7
 */
public interface AssetUserService {

    void addUserTgRelation(Long tgUserId, Long ysgUserId);

    void deleteUserTgRelation(Long tgUserId, Long ysgUserId);

    TgIntelligenceUserMapping findMappingByTgUserId(Long tgUserId);

    List<TgIntelligenceUserMapping> listAllMapping();


}
