package com.sinohealth.system.acl.impl;

import com.sinohealth.arkbi.api.ExtAnalysisApi;
import com.sinohealth.arkbi.api.FileServiceApi;
import com.sinohealth.arkbi.param.DownloadFileType;
import com.sinohealth.arkbi.param.OAuthLoginParam;
import com.sinohealth.arkbi.param.SimpleAuthParam;
import com.sinohealth.arkbi.response.ApiResponseData;
import com.sinohealth.arkbi.vo.OauthClientTokenVo;
import com.sinohealth.arkbi.vo.UserBaseInfoVo;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.redis.RedisCache;
import com.sinohealth.common.exception.BaseException;
import com.sinohealth.system.acl.ArkbiRepository;
import com.sinohealth.system.filter.ThreadContextHolder;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 10:07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArkbiRepositoryImpl implements ArkbiRepository {

    private final FileServiceApi fileServiceApi;

    private final ExtAnalysisApi extAnalysisApi;

    private final RedisCache redisCache;

    @Value("${arkbi.server.client-id}")
    private String clientId;

    @Value("${arkbi.server.client-secret}")
    private String clientSecret;


    @Override
    public InputStream getPdf(String extAnalysisId) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            UserBaseInfoVo biUser = getBIUserBaseInfoVo();
            Response response = fileServiceApi.downloadFile(biUser.getLoginToken(), extAnalysisId, DownloadFileType.PDF, null);
            InputStream inputStream = response.body().asInputStream();
            stopWatch.stop();
            log.info("bi获取pdf文件耗时{}ms", stopWatch.getTotalTimeMillis());
            return inputStream;
        } catch (Exception e) {
            log.error("", e);
            throw new BaseException("500", "下载bi pdf文件失败");
        }
    }

    @Override
    public InputStream getImage(String extAnalysisId) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            UserBaseInfoVo biUser = getBIUserBaseInfoVo();
            Response response = fileServiceApi.downloadFile(biUser.getLoginToken(), extAnalysisId, DownloadFileType.IMAGE, 1920);
            InputStream inputStream = response.body().asInputStream();
            stopWatch.stop();
            log.info("bi获取image文件耗时{}ms", stopWatch.getTotalTimeMillis());
            return inputStream;
        } catch (Exception e) {
            log.error("", e);
            throw new BaseException("500", "下载bi image文件失败");
        }
    }

    @Override
    public InputStream getExcel(String extAnalysisId) {
        try {
            UserBaseInfoVo biUser = getBIUserBaseInfoVo();
            Response response = fileServiceApi.downloadFile(biUser.getLoginToken(), extAnalysisId, DownloadFileType.EXCEL, null);
            InputStream inputStream = response.body().asInputStream();
            return inputStream;
        } catch (Exception e) {
            log.error("", e);
            throw new BaseException("500", "下载bi excel文件失败");
        }
    }

    @Override
    public InputStream getCsv(String extAnalysisId) {
        try {
            UserBaseInfoVo biUser = getBIUserBaseInfoVo();
            Response response = fileServiceApi.downloadFile(biUser.getLoginToken(), extAnalysisId, DownloadFileType.CSV, null);
            InputStream inputStream = response.body().asInputStream();
            return inputStream;
        } catch (Exception e) {
            log.error("", e);
            throw new BaseException("500", "下载bi csv文件失败");
        }
    }


    /**
     * 获取BI用户信息
     * @return
     * @throws Exception
     */
    private UserBaseInfoVo getBIUserBaseInfoVo() throws Exception {
        OauthClientTokenVo data = getOauthClientTokenVo();
        Assert.notNull(data, "arkbi API客户端认证失败");

        //获取bi用户登录token
        UserBaseInfoVo biUser = getBiUser(ThreadContextHolder.getSysUser().getUserId(), data);
        Assert.notNull(biUser, "arkbi 用户模拟登录失败");
        return biUser;
    }

    private OauthClientTokenVo getOauthClientTokenVo() {
        OauthClientTokenVo data = redisCache.getOrCacheObject(CommonConstants.ARKBI_ACCESS_TOKEN_CACHE_KEY, () -> {
            //获取accessToken,保存起来
            SimpleAuthParam simpleAuthParam = new SimpleAuthParam();
            simpleAuthParam.setClientId(clientId);
            simpleAuthParam.setClientSecret(clientSecret);
            ApiResponseData<OauthClientTokenVo> accessToken = null;
            try {
                accessToken = extAnalysisApi.accessToken(simpleAuthParam);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return accessToken.getData();
        });
        return data;
    }

    private UserBaseInfoVo getBiUser(Long userId, OauthClientTokenVo data) throws Exception {
        OAuthLoginParam oAuthLoginParam = new OAuthLoginParam();
        oAuthLoginParam.setClientId(clientId);
        oAuthLoginParam.setUserIdentity(String.valueOf(userId));
        ApiResponseData<UserBaseInfoVo> login = extAnalysisApi.login(data.getToken(), oAuthLoginParam);
        UserBaseInfoVo biUser = login.getData();
        return biUser;
    }
}
