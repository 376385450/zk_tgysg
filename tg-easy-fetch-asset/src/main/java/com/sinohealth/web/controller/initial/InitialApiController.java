package com.sinohealth.web.controller.initial;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Lists;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.*;
import com.sinohealth.common.utils.*;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.model.ResMaindataMainDepartmentSelectAllItemDataItem;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.api.cataloguemanageapi.DeptDTO;
import com.sinohealth.system.service.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2023-10-08 11:12
 * @Desc
 */

@Slf4j
@Api(value = "/api/initial", tags = {"资产门户数据初始化接口"})
@RestController
@RequestMapping({"/api/initial"})
public class InitialApiController extends BaseController {

    @Autowired
    private IAssetService assetService;
    @Autowired
    private DataAssetsCatalogueService catalogueService;
    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private IDocService docService;
    @Autowired
    private ITemplateService templateService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private IDataDirService dataDirService;

    /**
     * 初始化资产数据
     */
    @GetMapping("/initAssetInfo")
    public AjaxResult<?> initAssetInfo() {
        // 1. 获取已挂接的 related_id
        List<TgAssetInfo> tgAssetInfos = assetService.queryAll();

        // 2. 获取已挂接目录的表资产
        List<Long> tableAssetIds = tgAssetInfos.stream().filter(a -> a.getType().equals(AssetType.TABLE) && a.getRelatedId() != null)
                .mapToLong(a -> a.getRelatedId()).boxed().collect(Collectors.toList());
        List<TableInfo> unLinkedTableData = tableInfoService.getUnLinkedData(tableAssetIds);

        // 3. 获取未挂接目录的模型资产
        List<Long> modelAssetIds = tgAssetInfos.stream().filter(a -> a.getType().equals(AssetType.MODEL) && a.getRelatedId() != null)
                .mapToLong(a -> a.getRelatedId()).boxed().collect(Collectors.toList());
        List<TgTemplateInfo> unLinkedModelData = templateService.getUnLinkedData(modelAssetIds);

        // 4. 获取未挂接目录的文件资产
        List<Long> fileAssetIds = tgAssetInfos.stream().filter(a -> a.getType().equals(AssetType.FILE) && a.getRelatedId() != null)
                .mapToLong(a -> a.getRelatedId()).boxed().collect(Collectors.toList());
        List<TgDocInfo> unLinkedFileData = docService.getUnLinkedData(fileAssetIds);

        // 5. 获取初始化挂接目录
        AssetsCatalogue initialCatalogue = catalogueService.getInitialCatalogue();

        // 5. 将其记录在日志表
        String initialTime = DateUtils.getTime();

        // 6. 挂接表资产
        unLinkedTableData.stream().forEach(t -> {
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance();
            tgAssetInfo.setType(AssetType.TABLE);
            tgAssetInfo.setRelatedId(t.getId());
            tgAssetInfo.setAssetName(t.getTableAlias());
            tgAssetInfo.setAssetBindingDataName(t.getTableName());
            tgAssetInfo.setAssetBindingDataType(CommonConstants.TABLE_TYPE);
            tgAssetInfo.setAssetDescription(t.getComment());
            tgAssetInfo.setAssetSort(t.getDisSort().longValue());
            tgAssetInfo.setResourceType(ResourceType.TABLE_MANAGEMENT);
            tgAssetInfo.setIsFollowAssetMenuReadableRange(AuthItemEnum.FOLLOW_DIR_AUTH);
            if (t.getProcessId() != null) {
                tgAssetInfo.setIsFollowServiceMenuReadableRange(AuthItemEnum.CUSTOM_AUTH);
                tgAssetInfo.setProcessId(t.getProcessId());
            } else {
                tgAssetInfo.setIsFollowServiceMenuReadableRange(AuthItemEnum.FOLLOW_DIR_AUTH);
            }
            tgAssetInfo.setAssetOpenServices(new ArrayList<AssetPermissionType>() {{add(AssetPermissionType.DATA_QUERY_REQUEST);}});
            if (StringUtils.isNotBlank(t.getLeaderName())) {
                SysUser sysUser = sysUserService.selectUserByUserName(t.getLeaderName());
                if (sysUser != null) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                    tgAssetInfo.setAssetProvider(sinoPassUserDTO.getMainOrganizationId());
                    tgAssetInfo.setAssetManager(new ArrayList<String>() {{add(sysUser.getUserId().toString());}});
                } else {
                    tgAssetInfo.setAssetProvider("DEP3205690");
                }
            } else {
                tgAssetInfo.setAssetProvider("DEP3205690");
            }
            setCatalogue(initialCatalogue, t.getDirId(), tgAssetInfo);
            if (t.getStatus() == 1) {
                tgAssetInfo.setShelfState(ShelfState.LISTING.getStatus());
            } else {
                tgAssetInfo.setShelfState(ShelfState.UNLIST.getStatus());
            }
            if (t.getUpdateUserId() != 0) {
                String orgUserId = sysUserService.selectUserById(t.getUpdateUserId()).getOrgUserId();
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(orgUserId);
                tgAssetInfo.setUpdater(sinoPassUserDTO.getViewName());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tgAssetInfo.setCreateTime(dateFormat.format(t.getCreateTime()));
            tgAssetInfo.setUpdateTime(dateFormat.format(t.getUpdateTime()));
            JsonBeanConverter.convert2Json(tgAssetInfo);
            tgAssetInfo.insert();

            TgInitialLog tgInitialLog = TgInitialLog.newInstance();
            tgInitialLog.setType(AssetType.TABLE);
            tgInitialLog.setRelatedId(t.getId());
            tgInitialLog.setDataJson(JsonUtils.format(tgAssetInfo));
            tgInitialLog.setCreator(SecurityUtils.getRealName());
            tgInitialLog.setCreateTime(initialTime);
            tgInitialLog.insert();
        });

        // 7. 挂接模型资产
        unLinkedModelData.stream().forEach(m -> {
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance();
            tgAssetInfo.setType(AssetType.MODEL);
            tgAssetInfo.setRelatedId(m.getId());
            tgAssetInfo.setAssetName(m.getName());
            tgAssetInfo.setAssetDescription(m.getTempComment());
            tgAssetInfo.setAssetBindingDataName(m.getTemplateName());
            tgAssetInfo.setAssetBindingDataType(m.getTemplateType());
            tgAssetInfo.setIsFollowAssetMenuReadableRange(AuthItemEnum.FOLLOW_DIR_AUTH);
            if (m.getProcessId() != null) {
                tgAssetInfo.setIsFollowServiceMenuReadableRange(AuthItemEnum.CUSTOM_AUTH);
                tgAssetInfo.setProcessId(m.getProcessId());
            } else {
                tgAssetInfo.setIsFollowServiceMenuReadableRange(AuthItemEnum.FOLLOW_DIR_AUTH);
            }
            tgAssetInfo.setAssetOpenServicesJson(JsonUtils.format(new ArrayList<AssetPermissionType>() {{add(AssetPermissionType.TEMPLATE_APPLY_REQUEST);}}));
            tgAssetInfo.setAssetProvider("DEP3205690");
            tgAssetInfo.setAssetSort(m.getDisSort().longValue());
            if (m.getStatus() == 1) {
                tgAssetInfo.setShelfState(ShelfState.LISTING.getStatus());
            } else {
                tgAssetInfo.setShelfState(ShelfState.UNLIST.getStatus());
            }
            setCatalogue(initialCatalogue, m.getDirId(), tgAssetInfo);
            tgAssetInfo.setUpdater(m.getUpdater());
            tgAssetInfo.setCreateTime(m.getCreateTime());
            tgAssetInfo.setUpdateTime(m.getUpdateTime());
            tgAssetInfo.insert();

            TgInitialLog tgInitialLog = TgInitialLog.newInstance();
            tgInitialLog.setType(AssetType.MODEL);
            tgInitialLog.setRelatedId(m.getId());
            tgInitialLog.setDataJson(JsonUtils.format(tgAssetInfo));
            tgInitialLog.setCreator(SecurityUtils.getRealName());
            tgInitialLog.setCreateTime(initialTime);
            tgInitialLog.insert();
        });

        // 8. 挂接文件资产
        unLinkedFileData.stream().filter(f -> StringUtils.isNotBlank(f.getPath())).forEach(f -> {
            JsonBeanConverter.convert2Obj(f);
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance();
            tgAssetInfo.setType(AssetType.FILE);
            tgAssetInfo.setRelatedId(f.getId());
            tgAssetInfo.setAssetName(f.getName());
            tgAssetInfo.setAssetDescription(f.getComment());
            tgAssetInfo.setAssetBindingDataName(f.getName());
            tgAssetInfo.setAssetBindingDataType(f.getType());
            tgAssetInfo.setIsFollowAssetMenuReadableRange(AuthItemEnum.FOLLOW_DIR_AUTH);

            if (f.getProcessId() != null) {
                tgAssetInfo.setIsFollowServiceMenuReadableRange(AuthItemEnum.CUSTOM_AUTH);
                tgAssetInfo.setProcessId(f.getProcessId());
            } else {
                tgAssetInfo.setIsFollowServiceMenuReadableRange(AuthItemEnum.FOLLOW_DIR_AUTH);
            }

            tgAssetInfo.setAssetOpenServices(new ArrayList<AssetPermissionType>() {{
                add(AssetPermissionType.READ_FILE_REQUEST);
                add(AssetPermissionType.DOWNLOAD_PDF_REQUEST);
            }});

            if (ObjectUtils.isNotNull(f.getOwnerId())) {
                SysUser sysUser = sysUserService.selectUserById(f.getOwnerId());
                if (sysUser != null) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                    tgAssetInfo.setAssetProvider(sinoPassUserDTO.getMainOrganizationId());
                    tgAssetInfo.setAssetManager(new ArrayList<String>() {{add(sysUser.getUserId().toString());}});
                } else {
                    tgAssetInfo.setAssetProvider("DEP3205690");
                }
            } else {
                tgAssetInfo.setAssetProvider("DEP3205690");
            }

            tgAssetInfo.setServiceWhiteList(Lists.newArrayList());
            f.getWhitelistUsers().forEach(u -> {
                SysUser sysUser = sysUserService.selectUserById(u.getUserId());
                if (sysUser != null) {
                    tgAssetInfo.getServiceWhiteList().add(new TgAssetStaffParam() {{
                        setId(sysUser.getUserId().toString());
                        setType(2);
                        setName(sysUser.getRealName());
                        List<String> whiteList = new ArrayList<>();
                        if (u.getAuthorization().contains(DataDirConst.DocPermission.CAN_VIEW_PDF)) {
                            whiteList.add(AssetPermissionType.READ_FILE_REQUEST.getType());
                        }
                        if (u.getAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF)) {
                            whiteList.add(AssetPermissionType.DOWNLOAD_PDF_REQUEST.getType());
                        }
                        if (u.getAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC)) {
                            whiteList.add(AssetPermissionType.DOWNLOAD_SRC_REQUEST.getType());
                        }
                        setExpirationDate(DateUtils.parseDate("2025-01-01 23:59:59"));
                        setAssetOpenServices(whiteList);

                    }});
                }
            });

            if (!f.getNeed2Audit()) {
//                // 获取所有的部门
//                List<DeptDTO> depts = catalogueService.getSelectedDept();
//
//                // 不需要审核可预览
//                depts.forEach(dept -> {
//                    tgAssetInfo.getServiceWhiteList().add(new TgAssetStaffParam() {{
//                        setId(dept.getId());
//                        setType(1);
//                        setName(dept.getName());
//                        List<String> whiteList = new ArrayList<>();
//                        whiteList.add(AssetPermissionType.READ_FILE_REQUEST.getType());
//                        // 不需要审核可下载PDF
//                        if (f.getCanDownloadPdf()) {
//                            whiteList.add(AssetPermissionType.DOWNLOAD_PDF_REQUEST.getType());
//                        }
//                        // 不需要审核可下载SRC
//                        if (f.getCanDownloadSourceFile()) {
//                            whiteList.add(AssetPermissionType.DOWNLOAD_SRC_REQUEST.getType());
//                        }
//                        setExpirationDate(DateUtils.parseDate("2025-01-01 23:59:59"));
//                        setAssetOpenServices(whiteList);
//                    }});
//                });
                List<AssetPermissionType> nonAuditAssetOpenServices = new ArrayList<>();
                nonAuditAssetOpenServices.add(AssetPermissionType.READ_FILE_REQUEST);
                if (f.getCanDownloadPdf()) {
                    nonAuditAssetOpenServices.add(AssetPermissionType.DOWNLOAD_PDF_REQUEST);
                }
                if (f.getCanDownloadSourceFile()) {
                    nonAuditAssetOpenServices.add(AssetPermissionType.DOWNLOAD_SRC_REQUEST);
                }
                tgAssetInfo.setNonAuditAssetOpenServices(nonAuditAssetOpenServices);
            }

            setCatalogue(initialCatalogue, f.getDirId(), tgAssetInfo);
            if (f.getStatus() == 1) {
                tgAssetInfo.setShelfState(ShelfState.LISTING.getStatus());
            } else {
                tgAssetInfo.setShelfState(ShelfState.UNLIST.getStatus());
            }
            tgAssetInfo.setUpdater(f.getUpdater());
            tgAssetInfo.setAssetSort(f.getDisSort().longValue());
            tgAssetInfo.setCreateTime(f.getCreateTime());
            tgAssetInfo.setUpdateTime(f.getUpdateTime());
            JsonBeanConverter.convert2Json(tgAssetInfo);
            tgAssetInfo.insert();

            tgAssetInfo.getServiceWhiteList().forEach(
                    i -> assetService.insertAssetAuth(tgAssetInfo, i, WhitlistServiceType.SERVICE_AUTH)
            );

            TgInitialLog tgInitialLog = TgInitialLog.newInstance();
            tgInitialLog.setType(AssetType.FILE);
            tgInitialLog.setRelatedId(f.getId());
            tgInitialLog.setDataJson(JsonUtils.format(tgAssetInfo));
            tgInitialLog.setCreator(SecurityUtils.getRealName());
            tgInitialLog.setCreateTime(initialTime);
            tgInitialLog.insert();
        });

        return AjaxResult.success();
    }

    private void setCatalogue(AssetsCatalogue initialCatalogue, Long dirId, TgAssetInfo tgAssetInfo) {
        if (dirId == null || dirId == 0) {
            tgAssetInfo.setAssetMenuId(initialCatalogue.getId());
            return;
        }
        DataDir dir = dataDirService.getById(dirId);
        if (dir == null) {
            tgAssetInfo.setAssetMenuId(initialCatalogue.getId());
            return;
        }
        String dirName = dir.getDirName();

        AssetsCatalogue catalogue = catalogueService.getCatalogueByDirName(dirName);
        if (catalogue == null) {
            tgAssetInfo.setAssetMenuId(initialCatalogue.getId());
            return;
        }
        tgAssetInfo.setAssetMenuId(catalogue.getId());
    }

    /**
     * 初始化申请数据
     */
    @GetMapping("/initApplicationInfo")
    public AjaxResult<?> initApplicationInfo() {
        // 1. 获取申请表中未标记 new_asset_id 的记录
        List<TgApplicationInfo> tgApplicationInfos = applicationService.queryOldApplicationWithoutNewAssetId();
        Map<String, List<TgApplicationInfo>> applicationInfoMap = tgApplicationInfos.stream().collect(Collectors.groupingBy(TgApplicationInfo::getApplicationType));

        // 2. 获取表申请
        List<TgApplicationInfo> tableApplication = applicationInfoMap.getOrDefault("table", Lists.newArrayList());

        // 3. 获取模型申请
        List<TgApplicationInfo> modelApplication = applicationInfoMap.getOrDefault("data", Lists.newArrayList());

        // 4. 获取文件申请
        List<TgApplicationInfo> fileApplication = applicationInfoMap.getOrDefault("doc", Lists.newArrayList());

        // 5. 更新申请记录中的 new_asset_id 和 permission_json
        tableApplication.forEach(a -> {
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectOne(new QueryWrapper<TgAssetInfo>() {{
                eq("type", AssetType.TABLE);
                eq("related_id", a.getBaseTableId());
            }});
            if (tgAssetInfo != null) {
                a.setNewAssetId(tgAssetInfo.getId());
                a.setPermission(new ArrayList<AssetPermissionType>() {{add(AssetPermissionType.DATA_QUERY_REQUEST);}});
            }
            JsonBeanConverter.convert2Json(a);
            a.updateById();
        });

        modelApplication.forEach(a -> {
            JsonBeanConverter.convert2Obj(a);
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectOne(new QueryWrapper<TgAssetInfo>() {{
                eq("type", AssetType.MODEL);
                eq("related_id", a.getTemplateId());
            }});
            if (tgAssetInfo != null) {
                a.setNewAssetId(tgAssetInfo.getId());
                a.setPermission(new ArrayList<AssetPermissionType>() {{add(AssetPermissionType.TEMPLATE_APPLY_REQUEST);}});
            }
            JsonBeanConverter.convert2Json(a);
            a.updateById();

        });

        fileApplication.forEach(a -> {
            JsonBeanConverter.convert2Obj(a);
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectOne(new QueryWrapper<TgAssetInfo>() {{
                eq("type", AssetType.FILE);
                eq("related_id", a.getDocId());
            }});
            if (tgAssetInfo != null) {

                a.setNewAssetId(tgAssetInfo.getId());
                a.setPermission(new ArrayList<AssetPermissionType>() {{

                    a.getDocAuthorization().add(DataDirConst.DocPermission.CAN_VIEW_PDF);

                    if (a.getDocAuthorization().contains(DataDirConst.DocPermission.CAN_VIEW_PDF)) {
                        a.getPermission().add(AssetPermissionType.READ_FILE_REQUEST);
                    }
                    if (a.getDocAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF)) {
                        a.getPermission().add(AssetPermissionType.DOWNLOAD_PDF_REQUEST);
                    }
                    if (a.getDocAuthorization().contains(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC)) {
                        a.getPermission().add(AssetPermissionType.DOWNLOAD_SRC_REQUEST);
                    }
                }});
            }
            JsonBeanConverter.convert2Json(a);
            a.updateById();
        });

        return AjaxResult.success();
    }
}
