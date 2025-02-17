package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.constant.AssetConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.ShelfState;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncProcessDefService;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgCogradientInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.converter.PersonalServiceBeanConvertor;
import com.sinohealth.system.domain.personalservice.PersonalServiceView;
import com.sinohealth.system.dto.personalservice.JudgePermissionRequest;
import com.sinohealth.system.dto.personalservice.PageQueryServiceRequest;
import com.sinohealth.system.mapper.PersonalServiceMapper;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.mapper.TgCogradientInfoMapper;
import com.sinohealth.system.mapper.TgDocInfoMapper;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import com.sinohealth.system.service.IPersonalService;
import com.sinohealth.system.vo.PersonalServiceVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 10:57
 */
@Service
public class PersonalServiceImpl implements IPersonalService {

    @Resource
    private PersonalServiceMapper personalServiceMapper;
    @Resource
    private TgDocInfoMapper tgDocInfoMapper;
    @Resource
    private TgAssetInfoMapper tgAssetInfoMapper;
    @Autowired
    private TgCogradientInfoMapper tgCogradientInfoMapper;

    @Resource
    private DataAssetsCatalogueService dataAssetsCatalogueService;
    @Autowired
    private IntegrateSyncProcessDefService processDefService;

    /**
     * 分页查询我的服务
     *
     * @param pageRequest
     * @return
     */
    @Override
    public AjaxResult<PageInfo<PersonalServiceVo>> pageQuery(PageQueryServiceRequest pageRequest) {
        pageRequest.setUserId(SecurityUtils.getUserId());
        // TODO slow sql
        IPage<PersonalServiceView> page = personalServiceMapper.queryDataView(pageRequest.buildPage(), pageRequest);

        Map<Long, String> docTypeMap = new HashMap<>();
        if (AssetType.FILE.name().equals(pageRequest.getAssetType()) && CollectionUtils.isNotEmpty(page.getRecords())) {
            List<Long> relatedIdList = page.getRecords().stream().map(PersonalServiceView::getRelatedId).collect(Collectors.toList());
            docTypeMap = tgDocInfoMapper.selectBatchIds(relatedIdList).stream().collect(Collectors.toMap(TgDocInfo::getId, TgDocInfo::getType, (front, current) -> current));
        }

        // 数据交换
        List<Integer> flowIds = Lambda.buildNonNullList(page.getRecords(), PersonalServiceView::getFlowId);
        List<TgCogradientInfo> processList = Lambda.queryListIfExist(flowIds, v -> tgCogradientInfoMapper
                .selectList(new QueryWrapper<TgCogradientInfo>().lambda().in(TgCogradientInfo::getProcessId, v)));
        Map<Integer, Integer> onleStateMap = Lambda.buildMap(processList, TgCogradientInfo::getProcessId, TgCogradientInfo::getStatus);

        for (PersonalServiceView item : page.getRecords()) {
            // 设置资产目录的中文
            item.setCataloguePathCn(dataAssetsCatalogueService.getCataloguePathCn(item.getCataloguePath()));
            if (AssetType.FILE.name().equals(pageRequest.getAssetType())) {
                item.setDocType(docTypeMap.get(item.getRelatedId()));
            }

            if (StringUtils.isNotBlank(item.getOpenServiceJson()) && item.getOpenServiceJson().contains(AssetPermissionType.DATA_EXCHANGE.getType())) {
                Map map = processDefService.getTaskInfo(item.getFlowId());
                if (map != null) {
                    item.setLastRunningState((Integer) map.get("state"));
                }
                item.setTaskStatus(onleStateMap.get(item.getFlowId()));
            }

        }
        return AjaxResult.success(PageUtil.convert(page, PersonalServiceBeanConvertor::fromEntity));
    }

    /**
     * 根据资产查询权限
     *
     * @param assetId
     * @return
     */
    @Override
    public AjaxResult<List<String>> queryPermissionList(Long assetId) {

        TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(assetId);
        if (tgAssetInfo == null) {
            return AjaxResult.success(new ArrayList<>(0));
        }

        // 1.资产总的开放权限
        List<String> allPermissionList = JsonUtils.parseArray(tgAssetInfo.getAssetOpenServicesJson(), String.class);

        // 2.用户已申请得到的权限
        Set<String> obtainedPermissionSet = new HashSet<>();
        List<PersonalServiceView> validPerServiceList = personalServiceMapper.queryValidApplyByAssetId(SecurityUtils.getUserId(), assetId);
        validPerServiceList.stream()
                .map(item -> JsonUtils.parseArray(item.getOpenServiceJson(), String.class))
                .flatMap(item -> item.stream())
                .forEach(item -> obtainedPermissionSet.add(item));

        // 3.根据用户已获得的权限情况，已获取的权限不带_REQUESST返回,未获得的权限带_REQUESST返回（TODO 临时过滤数据交换）
        List<String> finalPermissionList = new ArrayList<>();
        allPermissionList.stream()
                .filter(item -> !AssetPermissionType.DATA_EXCHANGE_REQUEST.getType().equals(item))
                .forEach(item -> {
                    if (obtainedPermissionSet.contains(item)) {
                        finalPermissionList.add(item.replace("_REQUEST", ""));
                    } else {
                        finalPermissionList.add(item);
                    }
                });
        return AjaxResult.success(finalPermissionList);
    }

    /**
     * 判断是否有操作权限
     *
     * @param judgePermissionRequest
     * @return
     */
    @Override
    public AjaxResult<Object> judgePermission(JudgePermissionRequest judgePermissionRequest) {

        // 0.查出对应的资产实体
        TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(judgePermissionRequest.getAssetId());
        if (tgAssetInfo == null) {
            return AjaxResult.error(AssetConstants.PERMISSION_LACK_ERROR_CODE, AssetConstants.ASSET_OFF);
        }

        // 1.按照顺序，判断资产上下架状态、判断是否拥有该资产的阅读权限、判断资产关联的服务是否已下架
        String errorMsg = null;
        if (!ShelfState.LISTING.getStatus().equals(tgAssetInfo.getShelfState())) {
            errorMsg = AssetConstants.ASSET_OFF;
        } else if (!dataAssetsCatalogueService.assetReadAble(tgAssetInfo.getType().name(), tgAssetInfo.getRelatedId())) {
            errorMsg = AssetConstants.ASSET_NO_READ;
        } else if (!JsonUtils.parseArray(tgAssetInfo.getAssetOpenServicesJson(), String.class)
                .contains(judgePermissionRequest.getPermission() + "_REQUEST")) {
            errorMsg = AssetConstants.ASSET_SERVICE_OFF;
        }

        if (errorMsg != null) {
            return AjaxResult.error(AssetConstants.PERMISSION_LACK_ERROR_CODE, errorMsg);
        }
        return AjaxResult.success();
    }
}
