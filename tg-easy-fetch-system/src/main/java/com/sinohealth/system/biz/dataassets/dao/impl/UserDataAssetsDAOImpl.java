package com.sinohealth.system.biz.dataassets.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import com.sinohealth.system.biz.dataassets.dto.request.MyAssetRequest;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsMapper;
import com.sinohealth.system.domain.constant.ApplicationConst;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:53
 */
@Repository
public class UserDataAssetsDAOImpl extends ServiceImpl<UserDataAssetsMapper, UserDataAssets> implements UserDataAssetsDAO {

    @Override
    public Set<Long> existTemplateId(Collection<Long> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return Collections.emptySet();
        }
        List<UserDataAssets> userDataAssets = this.baseMapper.selectList(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getTemplateId)
                .in(UserDataAssets::getTemplateId, templateIds));
        return userDataAssets.stream().map(UserDataAssets::getTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Map<Long, Long> queryApplyAssets(Collection<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyMap();
        }

        List<UserDataAssets> userDataAssets = this.baseMapper.selectList(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getSrcApplicationId, UserDataAssets::getId)
                .in(UserDataAssets::getSrcApplicationId, applyIds));
        return Lambda.buildMap(userDataAssets, UserDataAssets::getSrcApplicationId, UserDataAssets::getId);
    }

    @Override
    public IPage<UserDataAssetResp> queryUserAsset(IPage page, MyAssetRequest myAssetRequest) {
        myAssetRequest.setNowTime(new Date());
        final IPage<UserDataAssetResp> userDataAssetRespIPage = this.baseMapper.pageMyAsset(page, myAssetRequest);
        return userDataAssetRespIPage;
    }

    @Override
    public Map<Long, Long> queryAssetsApply(Collection<Long> assetsIds) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyMap();
        }

        List<UserDataAssets> userDataAssets = this.baseMapper.selectList(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getSrcApplicationId, UserDataAssets::getId)
                .in(UserDataAssets::getId, assetsIds));
        return Lambda.buildMap(userDataAssets, UserDataAssets::getId, UserDataAssets::getSrcApplicationId);
    }

    @Override
    public boolean validProjectName(String projectName) {
        // 不允许需求名为空
        if (Objects.isNull(projectName)) {
            return false;
        }

        Integer count = this.baseMapper.selectCount(new QueryWrapper<UserDataAssets>().lambda()
                .eq(UserDataAssets::getProjectName, projectName));
        return Objects.isNull(count) || count == 0;
    }

    @Override
    public String queryAssetsName(Long id) {
        UserDataAssets assets = this.baseMapper.selectOne(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getProjectName)
                .eq(UserDataAssets::getId, id));
        return Optional.ofNullable(assets).map(UserDataAssets::getProjectName).orElse(null);
    }

    @Override
    public Map<Long, String> queryAssetsName(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }

        List<UserDataAssets> assets = this.baseMapper.selectList(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getId, UserDataAssets::getProjectName)
                .in(UserDataAssets::getId, ids));
        return assets.stream().collect(Collectors.toMap(UserDataAssets::getId, UserDataAssets::getProjectName, (front, current) -> current));
    }

    @Override
    public Map<Long, Integer> queryVersion(Collection<Long> assetsIds) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyMap();
        }
        List<UserDataAssets> userDataAssets = this.baseMapper.selectList(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getVersion, UserDataAssets::getId)
                .in(UserDataAssets::getId, assetsIds));
        return Lambda.buildMap(userDataAssets, UserDataAssets::getId, UserDataAssets::getVersion);
    }

    @Override
    public Integer queryVersion(Long assetsId) {
        if (Objects.isNull(assetsId)) {
            return null;
        }
        return queryVersion(Collections.singleton(assetsId)).get(assetsId);
    }

    @Override
    public void fillValid(LambdaQueryChainWrapper<UserDataAssets> wrapper) {
        wrapper.ge(UserDataAssets::getDataExpire, LocalDateTime.now())
                // 作废
                .eq(UserDataAssets::getDeprecated, false)
                // 另存的资产
                .isNull(UserDataAssets::getCopyFromId);
    }

    /**
     * @param ids
     * @return
     */
    @Override
    public List<UserDataAssets> queryRelateAssetsById(Collection<Long> ids) {
        LambdaQueryChainWrapper<UserDataAssets> wrapper = lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getSrcApplicationId, UserDataAssets::getTemplateId,
                        UserDataAssets::getAssetTableName, UserDataAssets::getVersion)
                .in(UserDataAssets::getId, ids);

        this.fillValid(wrapper);

        return wrapper.list();
    }

    @Override
    public List<UserDataAssets> queryRelateAssets(Collection<Long> tempIds) {
        LambdaQueryChainWrapper<UserDataAssets> wrapper = lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getSrcApplicationId, UserDataAssets::getTemplateId,
                        UserDataAssets::getAssetTableName, UserDataAssets::getVersion)
                .in(UserDataAssets::getTemplateId, tempIds);

        this.fillValid(wrapper);

        return wrapper.list();
    }

    @Override
    public List<UserDataAssets> queryRelateAssets(Long tableId, Integer version,
                                                  Boolean skipAssertsBaseVersionFilter,
                                                  List<String> prodCodes) {
        LambdaQueryChainWrapper<UserDataAssets> wrapper = lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getBaseTableId,
                        UserDataAssets::getVersion, UserDataAssets::getSrcApplicationId, UserDataAssets::getProdCode)
                .eq(UserDataAssets::getTemplateType, TemplateTypeEnum.wide_table.name())
                .eq(UserDataAssets::getBaseTableId, tableId)
                // 一次性需求不自动升级版本
                .eq(UserDataAssets::getRequireTimeType, ApplicationConst.RequireTimeType.PERSISTENCE);
        if (Objects.isNull(skipAssertsBaseVersionFilter) || !skipAssertsBaseVersionFilter) {
            wrapper.ne(UserDataAssets::getBaseVersion, version);
        }
        if (CollectionUtils.isNotEmpty(prodCodes)) {
            wrapper.and(v -> {
                v.apply(" 1=2 ");
                for (String prodCode : prodCodes) {
                    v.or().like(UserDataAssets::getProdCode, prodCode);
                }
            });
        }

        this.fillValid(wrapper);

        return wrapper.list();
    }
}
