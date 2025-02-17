package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.system.domain.TgIntelligenceUserMapping;
import com.sinohealth.system.dto.UserCreateDto;
import com.sinohealth.system.mapper.TgIntelligenceUserMappingMapper;
import com.sinohealth.system.service.AssetUserService;
import com.sinohealth.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/7
 */
@Service
public class AssetUserServiceImpl implements AssetUserService {


    @Autowired
    TgIntelligenceUserMappingMapper tgIntelligenceUserMappingMapper;


    @Override
    public void addUserTgRelation(Long tgUserId, Long ysgUserId) {
        final TgIntelligenceUserMapping tgIntelligenceUserMapping = new TgIntelligenceUserMapping();
        tgIntelligenceUserMapping.setTgUserId(tgUserId);
        tgIntelligenceUserMapping.setYsgUserId(ysgUserId);
        tgIntelligenceUserMappingMapper.insert(tgIntelligenceUserMapping);
    }

    @Override
    public void deleteUserTgRelation(Long tgUserId, Long ysgUserId) {
        final TgIntelligenceUserMapping mappingByTgUserId = findMappingByTgUserId(tgUserId);
        tgIntelligenceUserMappingMapper.deleteById(mappingByTgUserId.getId());
    }

    @Override
    public TgIntelligenceUserMapping findMappingByTgUserId(Long tgUserId) {
        final LambdaQueryWrapper<TgIntelligenceUserMapping> eq = Wrappers.<TgIntelligenceUserMapping>lambdaQuery()
                .eq(TgIntelligenceUserMapping::getTgUserId, tgUserId);
        return tgIntelligenceUserMappingMapper.selectOne(eq);
    }


    @Override
    public List<TgIntelligenceUserMapping> listAllMapping() {
        return tgIntelligenceUserMappingMapper.selectList(Wrappers.emptyWrapper());
    }
}
