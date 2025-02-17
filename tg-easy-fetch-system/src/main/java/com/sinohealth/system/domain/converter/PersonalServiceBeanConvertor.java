package com.sinohealth.system.domain.converter;

import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.PersonalServiceStatusEnum;
import com.sinohealth.common.enums.ShelfState;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.personalservice.PersonalServiceView;
import com.sinohealth.system.vo.PersonalServiceVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 15:08
 */
public class PersonalServiceBeanConvertor {

    public static PersonalServiceVo fromEntity(PersonalServiceView entity) {
        // 设置服务状态
        PersonalServiceVo personalServiceVo = new PersonalServiceVo();
        personalServiceVo.setServiceStatus(
                !entity.getAssetShelfState().equals(ShelfState.LISTING.getStatus())
                        ? PersonalServiceStatusEnum.INVALID.getCode()
                        : DateUtils.hasDataExpiredOnlyDays(entity.getExpireDate())
                        ? PersonalServiceStatusEnum.EXPIRED.getCode()
                        : PersonalServiceStatusEnum.VALID.getCode())
                .setOpenService(buildOpenService(entity.getOpenServiceJson()))
                .setApplyId(entity.getApplyId())
                .setAssetId(entity.getAssetId())
                .setApplyDate(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, entity.getApplyDate()))
                .setExpireDate(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, entity.getExpireDate()))
                .setAssetName(entity.getAssetName())
                .setCataloguePathCn(entity.getCataloguePathCn())
                .setTaskName(entity.getTaskName())
                .setTaskStatus(entity.getTaskStatus())
                .setFlowId(entity.getFlowId())
                .setLastRunningState(entity.getLastRunningState())
                .setRelatedId(entity.getRelatedId())
                .setDocType(entity.getDocType())
                .setProcessId(entity.getProcessId())
                .setAssetBindingDataName(entity.getAssetBindingDataName())
                .setPermissionList(buildPermission(entity))
                .setAssetShelfState(entity.getAssetShelfState());
        return personalServiceVo;
    }

    /**
     * openService转换
     *
     * @param str
     * @return
     */
    private static String convertOpenService(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        AssetPermissionType permissionType = AssetPermissionType.getByType(str);
        if (permissionType != null) {
            return permissionType.getTypeName().replace("申请", "");
        }
        return null;
    }

    private static String buildOpenService(String openServiceJson) {
        if (StringUtils.isBlank(openServiceJson)) {
            return null;
        }
        List<AssetPermissionType> assetPermissionTypes = Optional.ofNullable(JsonUtils.parseArray(openServiceJson,
                AssetPermissionType.class)).orElse(new ArrayList<>());
        return assetPermissionTypes.stream()
                .map(item -> item.getTypeName().replace("申请", "")).collect(Collectors.joining("/"));
    }

    private static List<String> buildPermission(PersonalServiceView entity) {
        if (entity == null) {
            return null;
        }
        List<String> permissionList;
        if (DateUtils.hasDataExpiredOnlyDays(entity.getExpireDate())) {
            permissionList = Optional.ofNullable(JsonUtils.parseArray(entity.getAllOpenServiceJson(),
                    String.class)).orElse(new ArrayList<>());
            return permissionList.stream().collect(Collectors.toList());
        } else {
            permissionList = Optional.ofNullable(JsonUtils.parseArray(entity.getOpenServiceJson(),
                    String.class)).orElse(new ArrayList<>());
            return permissionList.stream()
                    .map(item -> item.replace("_REQUEST", "")).collect(Collectors.toList());
        }
    }
}
