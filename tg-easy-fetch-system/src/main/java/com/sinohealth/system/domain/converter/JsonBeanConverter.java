package com.sinohealth.system.domain.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.TgCollectionUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.application.dto.ApplicationDistributedInfo;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.CustomMetricsLabelDto;
import com.sinohealth.system.biz.application.dto.DistributedInfo;
import com.sinohealth.system.biz.application.dto.PackTailFieldDto;
import com.sinohealth.system.biz.application.dto.PushMappingField;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;
import com.sinohealth.system.biz.application.dto.TopSettingApplyDto;
import com.sinohealth.system.biz.application.dto.TopSettingTempDto;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetStaffParam;
import com.sinohealth.system.domain.TgAssetWhitelistInfo;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.TgTemplatePackTailSetting;
import com.sinohealth.system.domain.WhiteListUser;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ColsInfoDto;
import com.sinohealth.system.dto.application.JoinInfoDto;
import com.sinohealth.system.dto.application.MetricsInfoDto;
import com.sinohealth.system.dto.application.SqlParts;
import com.sinohealth.system.dto.application.TemplateMetric;
import com.sinohealth.system.dto.assets.TgAssetMyApplicationPageResult;
import com.sinohealth.system.dto.auditprocess.ProcessNodeDetailDto;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-05-13 14:26
 * @Desc 负责 Json 和 Bean 之间的转换
 */
@Slf4j
public class JsonBeanConverter {
    public static TgDocInfo convert2Json(TgDocInfo tgDocInfo) {
        if (Objects.isNull(tgDocInfo.getWhitelistUsers())) {
            tgDocInfo.setWhitelistUsers(Collections.emptyList());
        }
        tgDocInfo.setWhitelistUserJson(JsonUtils.format(tgDocInfo.getWhitelistUsers()));
        return tgDocInfo;
    }

    public static TgApplicationInfo convert2Json(TgApplicationInfo tgApplicationInfo) {
        tgApplicationInfo.setJoinJson(JsonUtils.format(tgApplicationInfo.getJoinInfo()));
        tgApplicationInfo.setColsJson(JsonUtils.format(tgApplicationInfo.getColsInfo()));
        tgApplicationInfo.setMetricsJson(JsonUtils.format(tgApplicationInfo.getMetricsInfo()));
        tgApplicationInfo.setDataRangeJson(JsonUtils.format(tgApplicationInfo.getDataRangeInfo()));
        tgApplicationInfo.setHandleNodeJson(JsonUtils.format(tgApplicationInfo.getHandleNode()));
        tgApplicationInfo.setHandleIndexMappingJson(JsonUtils.format(tgApplicationInfo.getHandlerIndexMapping()));
        tgApplicationInfo.setTableAliasMappingJson(JsonUtils.format(tgApplicationInfo.getTableAliasMapping()));
        tgApplicationInfo.setDocAuthorizationJson(JsonUtils.format(tgApplicationInfo.getDocAuthorization()));
        tgApplicationInfo.setTemplateMetricsJson(JsonUtils.format(tgApplicationInfo.getTemplateMetrics()));
        tgApplicationInfo.setApplyDataRangeInfoJson(JsonUtils.format(tgApplicationInfo.getApplyDataRangeInfo()));

        tgApplicationInfo.setTopSettingJson(JsonUtils.format(tgApplicationInfo.getTopSetting()));
        tgApplicationInfo.setCustomMetricsJson(JsonUtils.format(tgApplicationInfo.getCustomMetrics()));
        tgApplicationInfo.setGranularityJson(JsonUtils.format(tgApplicationInfo.getGranularity()));
        tgApplicationInfo.setPermissionJson(JsonUtils.format(tgApplicationInfo.getPermission()));
        tgApplicationInfo.setAssetsAttachJson(JsonUtils.format(tgApplicationInfo.getAssetsAttach()));
        tgApplicationInfo.setDistributedJson(JsonUtils.format(tgApplicationInfo.getDistributeds()));
        return tgApplicationInfo;
    }

    public static TgApplicationInfo convert2Obj(TgApplicationInfo tgApplicationInfo) {
        tgApplicationInfo.setJoinInfo(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getJoinJson(),
                JoinInfoDto.class)).orElse(new ArrayList<>()));
        tgApplicationInfo.setColsInfo(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getColsJson(),
                ColsInfoDto.class)).orElse(new ArrayList<>()));
        tgApplicationInfo.setMetricsInfo(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getMetricsJson(),
                MetricsInfoDto.class)).orElse(new ArrayList<>()));
        tgApplicationInfo.setDocAuthorization(Optional.ofNullable(JsonUtils.parseArray(
                tgApplicationInfo.getDocAuthorizationJson(), Integer.class)).orElse(new ArrayList<>()));
        tgApplicationInfo.setTemplateMetrics(JsonUtils.parse(tgApplicationInfo.getTemplateMetricsJson(), TemplateMetric.class));
        tgApplicationInfo.setAssetsAttach(JsonUtils.parse(tgApplicationInfo.getAssetsAttachJson(), FileAssetsUploadDTO.class));
        tgApplicationInfo.setDataRangeInfo(JsonUtils.parse(tgApplicationInfo.getDataRangeJson(), FilterDTO.class));
        tgApplicationInfo.setApplyDataRangeInfo(JsonUtils.parse(tgApplicationInfo.getApplyDataRangeInfoJson(), FilterDTO.class));
        tgApplicationInfo.setHandleNode(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getHandleNodeJson(),
                ProcessNodeEasyDto.class)).orElse(new ArrayList<>()));
        tgApplicationInfo.setHandlerIndexMapping(Optional.ofNullable(JsonUtils.parse(tgApplicationInfo.getHandleIndexMappingJson(),
                new TypeReference<Map<Long, Map<String, Integer>>>() {
                })).orElse(new HashMap<>()));
        tgApplicationInfo.setTableAliasMapping(Optional.ofNullable(JsonUtils.parse(tgApplicationInfo.getTableAliasMappingJson(),
                new TypeReference<Map<String, String>>() {
                })).orElse(new HashMap<>()));

        tgApplicationInfo.setTopSetting(JsonUtils.parse(tgApplicationInfo.getTopSettingJson(), TopSettingApplyDto.class));
        tgApplicationInfo.setCustomMetrics(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getCustomMetricsJson(),
                CustomMetricsLabelDto.class)).orElse(new ArrayList<>()));
        tgApplicationInfo.setGranularity(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getGranularityJson(),
                ApplicationGranularityDto.class)).orElse(new ArrayList<>()));

        tgApplicationInfo.setPermission(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getPermissionJson(),
                AssetPermissionType.class)).orElse(new ArrayList<>()));

        tgApplicationInfo.setDistributeds(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getDistributedJson(),
                ApplicationDistributedInfo.class)).orElse(new ArrayList<>()));
        return tgApplicationInfo;
    }

    public static void convert2Obj(TgUserDataAssetsDistDto tgApplicationInfo) {
        tgApplicationInfo.setAssetsAttach(JsonUtils.parse(tgApplicationInfo.getAssetsAttachJson(), FileAssetsUploadDTO.class));
        tgApplicationInfo.setHandleNode(Optional.ofNullable(JsonUtils.parseArray(tgApplicationInfo.getHandleNodeJson(),
                ProcessNodeEasyDto.class)).orElse(new ArrayList<>()));
    }


    public static TgAssetInfo convert2Json(TgAssetInfo tgAssetInfo) {

        tgAssetInfo.setAssetManagerJson(JsonUtils.format(tgAssetInfo.getAssetManager()));
        tgAssetInfo.setAssetLabelsJson(JsonUtils.format(tgAssetInfo.getAssetLabels()));
        tgAssetInfo.setAssetOpenServicesJson(JsonUtils.format(tgAssetInfo.getAssetOpenServices()));
        tgAssetInfo.setCustomAssetReadableWhitelistJson(JsonUtils.format(tgAssetInfo.getCustomAssetReadableWhiteList()));
        tgAssetInfo.setServiceWhitelistJson(JsonUtils.format(tgAssetInfo.getServiceWhiteList()));
        tgAssetInfo.setNonAuditAssetOpenServicesJson(JsonUtils.format(tgAssetInfo.getNonAuditAssetOpenServices()));

        return tgAssetInfo;
    }

    public static TgAssetInfo convert2Obj(TgAssetInfo tgAssetInfo) {

        tgAssetInfo.setAssetManager(Optional.ofNullable(JsonUtils.parse(tgAssetInfo.getAssetManagerJson(), new TypeReference<List<String>>() {
        })).orElse(new ArrayList<>()));
        tgAssetInfo.setAssetOpenServices(Optional.ofNullable(JsonUtils.parse(tgAssetInfo.getAssetOpenServicesJson(), new TypeReference<List<AssetPermissionType>>() {
        })).orElse(new ArrayList<>()));
        tgAssetInfo.setCustomAssetReadableWhiteList(Optional.ofNullable(JsonUtils.parse(tgAssetInfo.getCustomAssetReadableWhitelistJson(), new TypeReference<List<TgAssetStaffParam>>() {
        })).orElse(new ArrayList<>()));
        tgAssetInfo.setServiceWhiteList(Optional.ofNullable(JsonUtils.parse(tgAssetInfo.getServiceWhitelistJson(), new TypeReference<List<TgAssetStaffParam>>() {
        })).orElse(new ArrayList<>()));
        tgAssetInfo.setNonAuditAssetOpenServices(Optional.ofNullable(JsonUtils.parse(tgAssetInfo.getNonAuditAssetOpenServicesJson(), new TypeReference<List<AssetPermissionType>>() {
        })).orElse(new ArrayList<>()));

        return tgAssetInfo;
    }

    public static TgAssetWhitelistInfo convert2Json(TgAssetWhitelistInfo tgAssetWhitelistInfo) {

        tgAssetWhitelistInfo.setAssetOpenServicesJson(JsonUtils.format(tgAssetWhitelistInfo.getAssetOpenServices()));

        return tgAssetWhitelistInfo;
    }

    public static TgAssetWhitelistInfo convert2Obj(TgAssetWhitelistInfo tgAssetWhitelistInfo) {

        tgAssetWhitelistInfo.setAssetOpenServices(Optional.ofNullable(JsonUtils.parse(tgAssetWhitelistInfo.getAssetOpenServicesJson(), new TypeReference<List<AssetPermissionType>>() {
        })).orElse(new ArrayList<>()));

        return tgAssetWhitelistInfo;
    }

    public static TgAssetMyApplicationPageResult convert2Obj(TgAssetMyApplicationPageResult tgAssetMyApplicationPageResult, Map<Long, SysUser> userMap) {

        tgAssetMyApplicationPageResult.setAssetOpenServices(Optional.ofNullable(JsonUtils.parse(tgAssetMyApplicationPageResult.getAssetOpenServicesJson(), new TypeReference<List<AssetPermissionType>>() {
        })).orElse(new ArrayList<>()));

        String currentHandlerNames = "";
        if (!"".equals(tgAssetMyApplicationPageResult.getCurrentHandlers())) {
            currentHandlerNames = StringUtils.join(Arrays.stream(tgAssetMyApplicationPageResult.getCurrentHandlers().split(","))
                    .map((id) -> {
                        SysUser sysUser = userMap.get(Long.valueOf(id));
                        SinoPassUserDTO temp = null;
                        try {
                            temp = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                        } catch (NullPointerException e) {
                            log.error("主数据人员组织编码为 NULL：{}", sysUser == null ? null : sysUser.getOrgUserId());
                        }

                        return temp != null ? temp.getViewName() : "";
                    }).collect(Collectors.toList()), ",");
        }

        tgAssetMyApplicationPageResult.setCurrentHandlerNames(currentHandlerNames);

        return tgAssetMyApplicationPageResult;
    }

    public static TgAuditProcessInfo convert2Json(TgAuditProcessInfo auditProcessInfo) {
        auditProcessInfo.getProcessChainDetailInfo().forEach((n) -> n.setNoticesJson(JsonUtils.format(n.getNoticesInfo())));
        auditProcessInfo.getSucessNode().forEach((n) -> n.setNoticesJson(JsonUtils.format(n.getNoticesInfo())));
        auditProcessInfo.getRejectNode().forEach((n) -> n.setNoticesJson(JsonUtils.format(n.getNoticesInfo())));
        auditProcessInfo.setProcessChainDetailJson(JsonUtils.format(auditProcessInfo.getProcessChainDetailInfo()));
        auditProcessInfo.setSuccessNodeJson(JsonUtils.format(auditProcessInfo.getSucessNode()));
        auditProcessInfo.setRejectNodeJson(JsonUtils.format(auditProcessInfo.getRejectNode()));

        return auditProcessInfo;
    }

    public static String convert2Json(SqlParts sqlParts) {
        return JsonUtils.format(sqlParts);
    }

    public static TgTemplateInfo convert2Json(TgTemplateInfo templateInfo) {
        if (templateInfo != null) {
            templateInfo.setJoinJson(JsonUtils.format(templateInfo.getJoinInfo()));
            templateInfo.setColsJson(JsonUtils.format(templateInfo.getColsInfo()));
            templateInfo.setMetricsJson(JsonUtils.format(templateInfo.getMetricsInfo()));
            templateInfo.setApplicationPeriodFieldJson(JsonUtils.format(templateInfo.getApplicationPeriodField()));
            templateInfo.setMustSelectFieldsJson(JsonUtils.format(templateInfo.getMustSelectFields()));
            templateInfo.setDataRangeJson(JsonUtils.format(templateInfo.getDataRangeInfo()));

            templateInfo.setCustomMetricsJson(JsonUtils.format(templateInfo.getCustomMetrics()));
            templateInfo.setTopSettingJson(JsonUtils.format(templateInfo.getTopSetting()));
            templateInfo.setGranularityJson(JsonUtils.format(templateInfo.getGranularity()));

            templateInfo.setTailFieldsJson(JsonUtils.format(templateInfo.getTailFields()));
            templateInfo.setTailFilterJson(JsonUtils.format(templateInfo.getTailFilter()));

            templateInfo.setPushFieldsJson(JsonUtils.format(templateInfo.getPushFields()));
            templateInfo.setDistributedJson(JsonUtils.format(templateInfo.getDistributeds()));
        }
        return templateInfo;
    }

    public static TgTemplatePackTailSetting convert2Json(TgTemplatePackTailSetting setting) {
        if (setting != null) {
            setting.setTailFieldsJson(JsonUtils.format(setting.getTailFields()));
            setting.setTailFilterJson(JsonUtils.format(setting.getTailFilter()));
        }
        return setting;
    }

    public static TgTemplateInfo convert2Obj(TgTemplateInfo templateInfo) {
        if (templateInfo != null) {
            templateInfo.setJoinInfo(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getJoinJson(), JoinInfoDto.class)).orElse(new ArrayList<>()));
            templateInfo.setColsInfo(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getColsJson(), ColsInfoDto.class)).orElse(new ArrayList<>()));
            templateInfo.setMetricsInfo(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getMetricsJson(), MetricsInfoDto.class)).orElse(new ArrayList<>()));
            templateInfo.setApplicationPeriodField(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getApplicationPeriodFieldJson(), Long.class)).orElse(new ArrayList<>()));
            templateInfo.setMustSelectFields(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getMustSelectFieldsJson(), Long.class)).orElse(new ArrayList<>()));
            templateInfo.setDataRangeInfo(JsonUtils.parse(templateInfo.getDataRangeJson(), FilterDTO.class));

            templateInfo.setCustomMetrics(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getCustomMetricsJson(), CustomMetricsLabelDto.class)).orElse(new ArrayList<>()));
            templateInfo.setTopSetting(JsonUtils.parse(templateInfo.getTopSettingJson(), TopSettingTempDto.class));
            templateInfo.setGranularity(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getGranularityJson(), TemplateGranularityDto.class)).orElse(new ArrayList<>()));

            templateInfo.setTailFields(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getTailFieldsJson(), PackTailFieldDto.class)).orElse(new ArrayList<>()));
            templateInfo.setTailFilter(JsonUtils.parse(templateInfo.getTailFilterJson(), FilterDTO.class));

            templateInfo.setPushFields(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getPushFieldsJson(), PushMappingField.class)).orElse(new ArrayList<>()));
            templateInfo.setDistributeds(Optional.ofNullable(JsonUtils.parseArray(templateInfo.getDistributedJson(),
                    DistributedInfo.class)).orElse(new ArrayList<>()));
        }
        return templateInfo;
    }

    public static TgTemplatePackTailSetting convert2Obj(TgTemplatePackTailSetting setting) {
        if (setting != null) {
            setting.setTailFields(Optional.ofNullable(JsonUtils.parseArray(setting.getTailFieldsJson(), PackTailFieldDto.class)).orElse(new ArrayList<>()));
            setting.setTailFilter(JsonUtils.parse(setting.getTailFilterJson(), FilterDTO.class));
        }
        return setting;
    }


    public static TgDocInfo convert2Obj(TgDocInfo tgDocInfo) {
        tgDocInfo.setWhitelistUsers(Optional.ofNullable(JsonUtils.parseArray(tgDocInfo.getWhitelistUserJson(), WhiteListUser.class)).orElse(TgCollectionUtils.newArrayList()));
        return tgDocInfo;
    }

    public static TgAuditProcessInfo convert2Obj(TgAuditProcessInfo auditProcessInfo) {
        if (Objects.isNull(auditProcessInfo)) {
            throw new CustomException("审批流不存在");
        }
        auditProcessInfo.setProcessChainDetailInfo(Optional.ofNullable(JsonUtils.parseArray(auditProcessInfo.getProcessChainDetailJson(), ProcessNodeDetailDto.class)).orElse(new ArrayList<>()));
        auditProcessInfo.setSucessNode(Optional.ofNullable(JsonUtils.parseArray(auditProcessInfo.getSuccessNodeJson(), ProcessNodeDetailDto.class)).orElse(new ArrayList<>()));
        auditProcessInfo.setRejectNode(Optional.ofNullable(JsonUtils.parseArray(auditProcessInfo.getRejectNodeJson(), ProcessNodeDetailDto.class)).orElse(new ArrayList<>()));
        auditProcessInfo.setWays(new ArrayList<>());
        auditProcessInfo.setSucways(new ArrayList<>());
        auditProcessInfo.setRejways(new ArrayList<>());
        for (ProcessNodeDetailDto p : auditProcessInfo.getProcessChainDetailInfo()) {
            List<String> wayz = returnAndSetParams(p);
            auditProcessInfo.getWays().add(wayz);
        }

        for (ProcessNodeDetailDto p : auditProcessInfo.getSucessNode()) {
            List<String> sucWayz = returnAndSetParams(p);
            auditProcessInfo.getSucways().add(sucWayz);
        }

        for (ProcessNodeDetailDto p : auditProcessInfo.getRejectNode()) {
            List<String> rejWayz = returnAndSetParams(p);
            auditProcessInfo.getRejways().add(rejWayz);
        }
        auditProcessInfo.setUsedTimes(Optional.ofNullable(auditProcessInfo.getTemplateName()).map(v -> v.split("、").length).orElse(0));

        return auditProcessInfo;
    }


    public static SqlParts convert2Obj(String sqlPartsJson) {
        SqlParts sqlParts = JsonUtils.parse(sqlPartsJson, SqlParts.class);
        return sqlParts;
    }

    private static List<String> returnAndSetParams(ProcessNodeDetailDto p) {

        ArrayList<String> wayz = new ArrayList<>();
        p.getNoticesInfo().forEach((n) -> {
            wayz.add(n.getWay());
        });

        List<String> collect = wayz.stream().map(w -> w.split(","))
                .flatMap(Arrays::stream).distinct().collect(Collectors.toList());
        return collect;
    }

}