package com.sinohealth.system.biz.arkbi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.arkbi.api.ExtAnalysisApi;
import com.sinohealth.arkbi.api.ExtAnalysisUserApi;
import com.sinohealth.arkbi.param.*;
import com.sinohealth.arkbi.response.ApiResponseData;
import com.sinohealth.arkbi.vo.OauthClientTokenVo;
import com.sinohealth.arkbi.vo.UserBaseInfoVo;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.redis.RedisCache;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.arkbi.constant.BIActionFunc;
import com.sinohealth.system.biz.arkbi.dto.UserBiViewVO;
import com.sinohealth.system.biz.arkbi.service.ArkBiService;
import com.sinohealth.system.biz.dataassets.constant.BiViewStateEnum;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsBiViewDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsVersion;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;
import com.sinohealth.system.biz.dataassets.dto.request.ArkBiEditRequest;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.mapper.ArkbiAnalysisMapper;
import com.sinohealth.system.service.ArkbiAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-10 15:35
 */
@Slf4j
@Service("arkBiService")
public class ArkBiServiceImpl implements ArkBiService {

    @Value("${arkbi.server.client-id}")
    private String clientId;

    @Value("${arkbi.server.client-secret}")
    private String clientSecret;

    @Value("${arkbi.source-id.lan}")
    private String sourceIdLan;

    @Value("${arkbi.source-id.wlan}")
    private String sourceIdWlan;

    // DAO
    @Autowired
    private ArkbiAnalysisMapper arkbiAnalysisMapper;
    @Autowired
    private UserDataAssetsBiViewDAO userDataAssetsBiViewDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private RedisTemplate redisTemplate;

    // 服务
    @Autowired
    private ArkbiAnalysisService arkbiAnalysisService;

    // 外部API
    @Autowired
    private ExtAnalysisApi extAnalysisApi;
    @Autowired
    private ExtAnalysisUserApi extAnalysisUserApi;

    @Override
    public void checkDeleteExpireView() {
        List<UserDataAssetsBiView> views = userDataAssetsBiViewDAO.queryNeedDeleteView();
        if (CollectionUtils.isEmpty(views)) {
            return;
        }

        boolean success = deleteView(views, DeleteType.SOFT);
        if (!success) {
            log.warn("delete failed");
        }
    }

//    public boolean restoreView(List<UserDataAssetsBiView> views) {
//        return this.handleForBiView(views, (token, viewIds, vs) -> {
//            try {
//                ApiResponseData<Void> restoreRes = extAnalysisUserApi.restoreView(token, vs);
//                if (!restoreRes.isSuccess()) {
//                    log.error("delete failed: deleteRes={}", restoreRes);
//                    return false;
//                }
//                return true;
//            } catch (Exception e) {
//                log.error("", e);
//            }
//            return false;
//        });
//    }

    @Override
    public boolean deleteView(List<UserDataAssetsBiView> views, DeleteType type) {
        return this.handleForBiView(views, (token, viewIds, vs) -> {
            try {
                log.info("delete viewIds:{} type:{}", viewIds, type);
                ApiResponseData<Void> deleteRes = extAnalysisUserApi.deleteView(token, viewIds, type, false);
                if (!deleteRes.isSuccess()) {
                    log.error("delete failed: deleteRes={}", deleteRes);
                    return false;
                }

                return true;
            } catch (Exception e) {
                log.error("", e);
            }
            return false;
        }, viewIds -> userDataAssetsBiViewDAO.updateDeleteView(viewIds, BiViewStateEnum.delete));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editArkBi(ArkBiEditRequest request) {
        final Long id = request.getId();
        final ArkbiAnalysis arkbiAnalysis = arkbiAnalysisMapper.selectById(id);
        arkbiAnalysis.setUpdateTime(LocalDateTime.now());
        arkbiAnalysis.setUpdateBy(SecurityUtils.getUserId());
        arkbiAnalysisMapper.updateById(arkbiAnalysis);
    }

    private boolean handleForBiView(List<UserDataAssetsBiView> views, BIActionFunc func, Consumer<Set<String>> post) {
        List<String> assetsVersions = Lambda.buildList(views, UserDataAssetsBiView::getAssetsVersion);
        try {
            log.info("prepare: versions={}", assetsVersions);
            Set<Long> assetsIds = Lambda.buildSet(views, UserDataAssetsBiView::getAssetsId);
            if (CollectionUtils.isEmpty(assetsIds)) {
                return false;
            }

            List<UserDataAssets> assets = userDataAssetsDAO.getBaseMapper().selectList(new QueryWrapper<UserDataAssets>().lambda()
                    .select(UserDataAssets::getId, UserDataAssets::getProjectName, UserDataAssets::getApplicantId)
                    .in(UserDataAssets::getId, assetsIds));
            Map<Long, Long> assetsUserMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getApplicantId);
            Map<Long, String> assetsNameMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getProjectName);

            Map<Long, Optional<UserBaseInfoVo>> biUserMap = assets.stream().map(UserDataAssets::getApplicantId).distinct()
                    .collect(Collectors.toMap(v -> v, v -> {
                        try {
                            return Optional.of(this.getBIUserBaseInfoVo(v));
                        } catch (Exception e) {
                            log.error("", e);
                            return Optional.empty();
                        }
                    }, (front, current) -> current));

            Map<Long, List<UserBiViewVO>> userViewMap = views.stream().map(v -> {
                Long applicantId = assetsUserMap.get(v.getAssetsId());
                if (Objects.isNull(applicantId)) {
                    log.error("not match user");
                    return null;
                }
                UserBiViewVO vo = new UserBiViewVO();
                vo.setView(v);
                vo.setApplicantId(applicantId);
                vo.setProjectName(assetsNameMap.get(v.getAssetsId()));
                return vo;
            }).filter(Objects::nonNull).collect(Collectors.groupingBy(UserBiViewVO::getApplicantId));

            // 按用户操作 删除/恢复 视图
            for (Map.Entry<Long, List<UserBiViewVO>> entry : userViewMap.entrySet()) {
                Optional<UserBaseInfoVo> biUser = biUserMap.get(entry.getKey());
                if (!biUser.isPresent()) {
                    log.error("get bi user error: user id={}", entry.getKey());
                    continue;
                }

                // 恢复的参数
                Set<String> existIds = new HashSet<>();
                List<RestoreViewParam> params = entry.getValue().stream().map(v -> {
                    if (!existIds.add(v.getView().getViewId())) {
                        log.info("repeat {}", v);
                        return null;
                    }
                    RestoreViewParam param = new RestoreViewParam();
                    param.setViewId(v.getView().getViewId());
                    param.setViewName(AssetsVersion.buildViewName(v.getProjectName(), v.getView().getVersion()));
                    return param;
                }).filter(Objects::nonNull).collect(Collectors.toList());

                // 删除的参数
                Set<String> viewIds = Lambda.buildSet(entry.getValue(), v -> v.getView().getViewId());
                boolean result = func.handleView(biUser.get().getLoginToken(), new ArrayList<>(viewIds), params);
                if (result) {
                    post.accept(viewIds);
                }
            }
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
        return true;
    }

    @Override
    public UserBaseInfoVo getBIUserBaseInfoVo(Long userId) throws Exception {
        OauthClientTokenVo data = getOauthClientTokenVo(clientId, clientSecret);
        Assert.notNull(data, "arkbi API客户端认证失败");

        //获取bi用户登录token
        UserBaseInfoVo biUser = getBiUser(userId, data);
        Assert.notNull(biUser, "arkbi 用户模拟登录失败");
        return biUser;
    }

    @Override
    public boolean deleteChart(Long id) {
        try {
            Long userId = SecurityUtils.getUserId();
            ArkbiAnalysis arkbiAnalysis = arkbiAnalysisService.getById(id);
            UserBaseInfoVo biUser = getBIUserBaseInfoVo(userId);
            if (Objects.nonNull(arkbiAnalysis)) {
                DeleteExtAnalysisParam extAnalysisParam = new DeleteExtAnalysisParam();
                extAnalysisParam.setExtAnalysisId(arkbiAnalysis.getAnalysisId());
                try {
                    extAnalysisUserApi.deleteViz(biUser.getLoginToken(), extAnalysisParam).getDataOrThrow();
                } catch (Exception e) {
                    if (Objects.equals(e.getMessage(), "Associated data exists , cannot be deleted!")) {
                        log.error("id={}", id);
                        throw new CustomException("该图表被引用，不允许删除!");
                    }
                    throw e;
                }
                arkbiAnalysisMapper.deleteByPrimaryKey(id);
            }
        } catch (Exception e) {
            log.error("", e);
            return false;
        }

        return true;
    }

    private UserBaseInfoVo getBiUser(Long userId, OauthClientTokenVo data) throws Exception {
        OAuthLoginParam oAuthLoginParam = new OAuthLoginParam();
        oAuthLoginParam.setClientId(clientId);
        oAuthLoginParam.setUserIdentity(String.valueOf(userId));
        ApiResponseData<UserBaseInfoVo> login = extAnalysisApi.login(data.getToken(), oAuthLoginParam);
        return login.getDataOrThrow();
    }

    private OauthClientTokenVo getOauthClientTokenVo(String clientId, String clientSecret) {
        return redisCache.getOrCacheObject(CommonConstants.ARKBI_ACCESS_TOKEN_CACHE_KEY, () -> {
            //获取accessToken,保存起来
            SimpleAuthParam simpleAuthParam = new SimpleAuthParam();
            simpleAuthParam.setClientId(clientId);
            simpleAuthParam.setClientSecret(clientSecret);
            ApiResponseData<OauthClientTokenVo> accessToken;
            try {
                accessToken = extAnalysisApi.accessToken(simpleAuthParam);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return accessToken.getData();
        }, 1, TimeUnit.HOURS);
    }
}
