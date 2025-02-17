package com.sinohealth.system.biz.dataassets.helper;

import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareFileDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompareFile;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareCallbackRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareInvokeRequest;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-22 15:42
 */
@Slf4j
@Service
public class AssetsCompareInvoker {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private Validator validator;

    @Resource
    private AssetsCompareDAO assetsCompareDAO;
    @Resource
    private AssetsCompareFileDAO assetsCompareFileDAO;
    @Resource
    private UserDataAssetsDAO userDataAssetsDAO;
    @Resource
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Resource
    private AlertService alertService;


    public void invokeCompareById(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("no id to invoke");
            return;
        }
        List<AssetsCompare> assetsCompares = assetsCompareDAO.getBaseMapper().selectBatchIds(ids);

        Object repeatSwitch = redisTemplate.opsForValue().get(RedisKeys.Assets.COMPARE_REPEAT_RUN);
        Integer repeatInt = Optional.ofNullable(repeatSwitch).map(Object::toString).map(Integer::parseInt).orElse(0);
        if (repeatInt > 0) {
            this.invokeCompare(assetsCompares);
        } else {
            List<AssetsCompare> filtered = assetsCompares.stream().filter(v ->
                    Objects.equals(v.getState(), AssetsUpgradeStateEnum.wait.name())
                            || Objects.equals(v.getState(), AssetsUpgradeStateEnum.failed.name())
            ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filtered)) {
                log.warn("not need invoke");
                return;
            }
            this.invokeCompare(filtered);
        }
    }

    public void invokeFileCompareById(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("no id to invoke");
            return;
        }

        List<AssetsCompareFile> assetsCompares = assetsCompareFileDAO.listByIds(ids);
        Object repeatSwitch = redisTemplate.opsForValue().get(RedisKeys.Assets.COMPARE_REPEAT_RUN);
        Integer repeatInt = Optional.ofNullable(repeatSwitch).map(Object::toString).map(Integer::parseInt).orElse(0);
        if (repeatInt > 0) {
            this.invokeFileCompare(assetsCompares);
        } else {
            List<AssetsCompareFile> filtered = assetsCompares.stream().filter(v ->
                    Objects.equals(v.getState(), AssetsUpgradeStateEnum.wait.name())
                            || Objects.equals(v.getState(), AssetsUpgradeStateEnum.failed.name())
            ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filtered)) {
                log.warn("not need invoke");
                return;
            }
            this.invokeFileCompare(filtered);
        }
    }

    /**
     * @see AssetsCompareInvoker#callbackFileCompare
     */
    public void invokeFileCompare(List<AssetsCompareFile> compareList) {
        if (CollectionUtils.isEmpty(compareList)) {
            return;
        }

        Set<Long> ids = Lambda.buildSet(compareList);
        assetsCompareFileDAO.lambdaUpdate()
                .in(AssetsCompareFile::getId, ids)
                .set(AssetsCompareFile::getState, AssetsUpgradeStateEnum.running.name())
                .update();
        for (AssetsCompareFile compare : compareList) {
            AssetsCompareInvokeRequest req = AssetsCompareInvokeRequest.builder()
                    .compareId(compare.getId())
                    .assetsId(0L)
                    // 仅占位，不会对Python脚本有影响
                    .projectName("EMPTY")
                    .newPath(compare.getNewPath())
                    .oldPath(compare.getOldPath())
                    .resultDir("/ftp/sop_comp_file/" + compare.getCreator())
                    .callbackUrl(appProperties.getFileCompareSelfUrl())
                    .build();
            this.invokeCompareReq(req);
        }
    }

    public void invokeCompare(List<AssetsCompare> compareList) {
        if (CollectionUtils.isEmpty(compareList)) {
            return;
        }
        Set<Long> assetsIds = Lambda.buildSet(compareList, AssetsCompare::getAssetsId, Objects::nonNull);
        if (CollectionUtils.isEmpty(assetsIds)) {
            return;
        }
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getProjectName, UserDataAssets::getVersion,
                        UserDataAssets::getFtpStatus, UserDataAssets::getFtpPath)
                .in(UserDataAssets::getId, assetsIds).list();
        Map<Long, UserDataAssets> assetsMap = Lambda.buildMap(assets, UserDataAssets::getId);
        Map<Long, String> nameMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getProjectName);

        for (AssetsCompare compare : compareList) {
            UserDataAssets latest = assetsMap.get(compare.getAssetsId());
            UserDataAssets cur;
            if (Objects.equals(latest.getVersion(), compare.getCurVersion())) {
                cur = latest;
            } else {
                cur = userDataAssetsSnapshotDAO.queryByAssetsId(compare.getAssetsId(), compare.getCurVersion());
            }
            UserDataAssets pre = userDataAssetsSnapshotDAO.queryByAssetsId(compare.getAssetsId(), compare.getPreVersion());
            if (!cur.hasValidFtp() || !pre.hasValidFtp()) {
                log.error("存在资产的Excel尚未上传完，请稍后再试 compare:{}", compare);
                alertService.sendDevNormalMsg("存在资产的Excel尚未上传完，触发对比失败 " + compare.getId() + " " + cur.getProjectName());
                continue;
            }

            AssetsCompareInvokeRequest req = AssetsCompareInvokeRequest.builder()
                    .compareId(compare.getId())
                    .assetsId(compare.getAssetsId())
                    .projectName(nameMap.get(compare.getAssetsId()))
                    .newPath(cur.getFtpPath())
                    .oldPath(pre.getFtpPath())
                    .callbackUrl(appProperties.getAssetsCompareSelfUrl())
                    .build();
            this.invokeCompareReq(req);
        }
    }

    /**
     * 提交Compare任务到下游Python服务
     *
     * @see AssetsCompareInvoker#callbackAssetsCompare 结束回调
     */
    public void invokeCompareReq(AssetsCompareInvokeRequest param) {
        Set<ConstraintViolation<AssetsCompareInvokeRequest>> errors = validator.validate(param);
        if (CollectionUtils.isNotEmpty(errors)) {
            String msg = errors.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.joining(","));
            log.info("{} {} : {}", param.getCompareId(), param.getProjectName(), msg);
            throw new CustomException(msg);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBy = JsonUtils.format(param);
        log.info("jsonBy={}", jsonBy);
        HttpEntity<String> request = new HttpEntity<>(jsonBy, headers);

        assetsCompareDAO.lambdaUpdate()
                .eq(AssetsCompare::getId, param.getCompareId())
                .set(AssetsCompare::getState, AssetsUpgradeStateEnum.running.name())
                .set(AssetsCompare::getStartTime, LocalDateTime.now())
                .update();

        try {
            String resp = restTemplate.postForObject(appProperties.getAssetsCompareUrl(), request, String.class);
            if (Objects.nonNull(resp)) {
                log.info("resp={}", resp);
            }
        } catch (Exception e) {
            log.error("", e);
            assetsCompareDAO.lambdaUpdate()
                    .eq(AssetsCompare::getId, param.getCompareId())
                    .set(AssetsCompare::getState, AssetsUpgradeStateEnum.failed.name())
                    .update();
        }
    }


    /**
     * 提交Compare任务到下游Python服务
     */
    public void invokeFileCompareReq(AssetsCompareInvokeRequest param) {
        Set<ConstraintViolation<AssetsCompareInvokeRequest>> errors = validator.validate(param);
        if (CollectionUtils.isNotEmpty(errors)) {
            String msg = errors.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.joining(","));
            log.info("{} {} : {}", param.getCompareId(), param.getProjectName(), msg);
            throw new CustomException(msg);
        }

        param.setCallbackUrl(appProperties.getAssetsCompareSelfUrl());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jsonBy = JsonUtils.format(param);
        log.info("jsonBy={}", jsonBy);
        HttpEntity<String> request = new HttpEntity<>(jsonBy, headers);

        assetsCompareDAO.lambdaUpdate()
                .eq(AssetsCompare::getId, param.getCompareId())
                .set(AssetsCompare::getState, AssetsUpgradeStateEnum.running.name())
                .set(AssetsCompare::getStartTime, LocalDateTime.now())
                .update();

        try {
            String resp = restTemplate.postForObject(appProperties.getAssetsCompareUrl(), request, String.class);
            if (Objects.nonNull(resp)) {
                log.info("resp={}", resp);
            }
        } catch (Exception e) {
            log.error("", e);
            assetsCompareDAO.lambdaUpdate()
                    .eq(AssetsCompare::getId, param.getCompareId())
                    .set(AssetsCompare::getState, AssetsUpgradeStateEnum.failed.name())
                    .update();
        }
    }

    /**
     * Python 服务回调
     */
    public String callbackAssetsCompare(AssetsCompareCallbackRequest request) {
        log.info("request={}", request);
        Boolean success = validate(request, id -> {
            AssetsCompare assetsCompare = assetsCompareDAO.getById(request.getCompareId());
            return Objects.nonNull(assetsCompare);
        });
        if (success == null) {
            return "Error";
        }

        assetsCompareDAO.lambdaUpdate()
                .eq(AssetsCompare::getId, request.getCompareId())
                .set(success, AssetsCompare::getResultPath, request.getResultPath())
                .set(AssetsCompare::getState, success ? AssetsUpgradeStateEnum.success.name() : AssetsUpgradeStateEnum.failed.name())
                .set(AssetsCompare::getFinishTime, LocalDateTime.now())
                .set(AssetsCompare::getRunLog, request.getRunLog())
                .update();
        return "OK";
    }

    public String callbackFileCompare(AssetsCompareCallbackRequest request) {
        log.info("request={}", request);

        Boolean success = validate(request, id -> {
            AssetsCompareFile compare = assetsCompareFileDAO.getById(request.getCompareId());
            return Objects.nonNull(compare);
        });
        if (success == null) {
            return "Error";
        }

        assetsCompareFileDAO.lambdaUpdate()
                .eq(AssetsCompareFile::getId, request.getCompareId())
                .set(success, AssetsCompareFile::getResultPath, request.getResultPath())
                .set(AssetsCompareFile::getState, success ? AssetsUpgradeStateEnum.success.name() : AssetsUpgradeStateEnum.failed.name())
                .set(AssetsCompareFile::getFinishTime, LocalDateTime.now())
                .set(AssetsCompareFile::getProdCode, request.getNewProdCode())
                .set(AssetsCompareFile::getDataPeriod, request.getNewDataPeriod())
                .set(AssetsCompareFile::getRunLog, request.getRunLog())
                .update();
        return "OK";
    }

    private Boolean validate(AssetsCompareCallbackRequest request, Function<Long, Boolean> func) {
        if (Objects.isNull(request.getCompareId())) {
            log.warn("invalid req id");
            return null;
        }

        boolean success = BooleanUtils.isTrue(request.getSuccess());
        if (success && StringUtils.isBlank(request.getResultPath())) {
            log.warn("invalid req");
            return null;
        }

        Boolean valid = func.apply(request.getCompareId());
        if (!valid) {
            return null;
        }

        AssetsCompare assetsCompare = assetsCompareDAO.getById(request.getCompareId());
        if (Objects.isNull(assetsCompare)) {
            log.warn("invalid req id");
            return null;
        }

        // 成功才删前一个文件 重复对比调试时清理逻辑
        if (success && StringUtils.isNotBlank(assetsCompare.getResultPath())) {
            try (FtpClient ftp = FtpClientFactory.getInstance()) {
                ftp.open();
                ftp.delete(assetsCompare.getResultPath());
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return success;
    }

}
