package com.sinohealth.web.controller.system;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.system.biz.application.constants.FieldType;
import com.sinohealth.system.biz.common.FileAdapter;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.template.dto.PowerPushBiTemplateVO;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetTemplateBindingInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.TgTemplatePackTailSetting;
import com.sinohealth.system.domain.converter.AssetBeanConverter;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.GuideDTO;
import com.sinohealth.system.dto.api.cataloguemanageapi.CatalogueDetailDTO;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.ITemplatePackTailSettingService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.Validator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2023-08-07 14:38
 * @Desc
 */
@Slf4j
@Api(value = "/api/table_management/template", tags = {"资产目录模型接口"})
@RestController
@RequestMapping({"/api/table_management/template"})
public class TemplateApiController {

    @Autowired
    DataAssetsCatalogueService assetCatalogueService;
    @Autowired
    private ITemplateService templateService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private IAssetService assetService;
    @Autowired
    private IDocService docService;
    @Autowired
    private ITemplatePackTailSettingService templatePackTailSettingService;
    @Autowired
    private FileApi fileApi;
    @Autowired
    private FileProperties fileProperties;
    @Autowired
    private Environment environment;
    @Autowired
    private FileAdapter fileAdapter;
    @Autowired
    private Validator validator;

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "TgAssetTemplateInfo", name = "templateInfo", value = "模型信息")
    })
    @ApiOperation(value = "新建/修改模型", notes = "状态为0 模型还没建完,不可用状态", httpMethod = "POST")
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/add")
    public AjaxResult upsertTemplateApi(@Valid @RequestBody TgAssetTemplateBindingInfo bind) {
        TgAssetTemplateBindingInfo result = new TgAssetTemplateBindingInfo();
        TgAssetInfo tgAssetInfo = bind.getTgAssetInfo();
        TgTemplateInfo templateInfo = bind.getBindingData();
        String templateType = templateInfo.getTemplateType();
        TemplateTypeEnum type = TemplateTypeEnum.valueOf(templateType);
        if (!Objects.equals(type, TemplateTypeEnum.wide_table)) {
            this.cleanWhileNotWide(templateInfo);
        }

        if (BooleanUtils.isTrue(templateInfo.getPackTail())) {
            if (CollectionUtils.isEmpty(templateInfo.getTailSettings())) {
                return AjaxResult.error("请完善数据打包配置内容");
            }
            for (TgTemplatePackTailSetting tailSetting : templateInfo.getTailSettings()) {
                if (Objects.isNull(tailSetting.getTailFilter()) || CollectionUtils.isEmpty(tailSetting.getTailFields())) {
                    return AjaxResult.error("请完善数据打包配置内容");
                }
                boolean exist = tailSetting.getTailFields().stream().anyMatch(v -> {
                    boolean noFill = Objects.isNull(v.getFieldType()) || Objects.isNull(v.getFieldId());
                    boolean metrics = Objects.equals(v.getFieldType(), FieldType.METRIC) && BooleanUtils.isNotTrue(v.getMarkNull())
                            && (StringUtils.isBlank(v.getVal()) || !isDouble(v.getVal()));
                    return noFill || metrics;
                });
                if (exist) {
                    return AjaxResult.error("请完善数据打包配置内容");
                }
            }
        }

        if (assetService.checkSameAssetName(tgAssetInfo.getId(), tgAssetInfo.getAssetName()) > 0) {
            return AjaxResult.error(InfoConstants.DUPLICATED_ASSET_NAME);
        }
        AjaxResult<?> ajaxResult = assetService.fillProcessId4FollowMenuDirItem(tgAssetInfo);
        if (!ajaxResult.isSuccess()) {
            return ajaxResult;
        }

        CatalogueDetailDTO tgCatalogueBaseInfo = assetCatalogueService.getCatalogueBaseInfo(tgAssetInfo.getAssetMenuId());
        AssetBeanConverter.asset2TemplateInfo(tgAssetInfo, tgCatalogueBaseInfo, templateInfo);

        AjaxResult<TgTemplateInfo> tempResult = templateService.upsertTemplate(templateInfo, bind.getConfirmUpgrade());
        if (!tempResult.isSuccess()) {
            return tempResult;
        }
        templateInfo = tempResult.getData();
        result.setBindingData(templateInfo);

        tgAssetInfo.setType(AssetType.MODEL);
        tgAssetInfo.setRelatedId(templateInfo.getId());
        tgAssetInfo.setAssetBindingDataType(templateInfo.getTemplateType());
        assetService.addAsset(tgAssetInfo);

        if (CollectionUtils.isNotEmpty(templateInfo.getTailSettings())) {
            List<TgTemplatePackTailSetting> settings = templatePackTailSettingService.findByTemplateId(templateInfo.getId());
            List<TgTemplatePackTailSetting> newSettings = templateInfo.getTailSettings();
            if (CollectionUtils.isNotEmpty(settings)) {
                List<Long> existIds =
                        settings.stream().map(TgTemplatePackTailSetting::getId).collect(Collectors.toList());
                List<Long> needDeleteIds = existIds.stream()
                        .filter(i -> newSettings.stream().noneMatch(e -> Objects.equals(i, e.getId()))).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(needDeleteIds)) {
                    templatePackTailSettingService.deleteByIds(needDeleteIds);
                }
            }
            for (TgTemplatePackTailSetting newSetting : newSettings) {
                newSetting.setTemplateId(templateInfo.getId());
                templatePackTailSettingService.save(JsonBeanConverter.convert2Json(newSetting));
            }
        } else {
            templatePackTailSettingService.deleteByTemplateId(templateInfo.getId());
        }
        return AjaxResult.success(result);
    }

    @GetMapping("/queryBizType")
    public AjaxResult<String> queryBizType(@RequestParam("templateId") Long templateId) {
        return templateService.queryBizType(templateId);
    }

    /**
     * 通用模板 上传附件
     */
    @PostMapping("/uploadFile")
    public AjaxResult<FileAssetsUploadDTO> uploadFileAssets(@RequestParam("file") MultipartFile file) {
//        Integer maxMib = fileProperties.getFtpAssetsMaxMib();
//        if (file.getSize() > maxMib * 1024 * 1024) {
//            return AjaxResult.error("上传失败，文件超过 " + maxMib + "Mib");
//        }
        return fileAdapter.uploadToFTP(file);
//        return docService.uploadGetPath(file);
    }

    /**
     * 审核 上传文件 文件交付类型的资产
     *
     * @see ApplicationApiController#config
     */
    @PostMapping("/uploadAssetsAttach")
    public AjaxResult<FileAssetsUploadDTO> uploadAssetsAttach(@RequestParam("file") MultipartFile file) {
        Integer maxMib = fileProperties.getFtpAssetsMaxMib();
        long maxByte = (long) maxMib * 1024L * 1024L;
        if (file.getSize() > maxByte) {
            return AjaxResult.error("上传失败，文件超过 " + maxMib + "Mib");
        }
        return fileAdapter.uploadToFTP(file);
    }


    /**
     * @see IDocService#uploadGetPath  上传
     */
    @ApiOperation("文档下载")
    @GetMapping(value = {"/downloadFile"})
    public void download(@RequestParam(value = "path", required = false) String path, HttpServletResponse response) throws Exception {
        if (StringUtils.isBlank(path)) {
            log.warn("empty path");
            response.getWriter().write("参数为空");
            return;
        }
        String ftpPrefix = fileProperties.getFtpPrefix();
        if (path.startsWith(ftpPrefix)) {
            try (FtpClient ftpClient = FtpClientFactory.getInstance()) {
                ftpClient.open();
                ftpClient.downloadFile(path, response.getOutputStream());
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            try {
                ServletOutputStream outputStream = response.getOutputStream();
                URLEncoder.encode(path, StandardCharsets.UTF_8.name());
                byte[] fileBts = fileApi.get(fileProperties.getFileStorageCode(), path);
                outputStream.write(fileBts);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    private void cleanWhileNotWide(TgTemplateInfo templateInfo) {
        if (Objects.isNull(templateInfo)) {
            return;
        }
        templateInfo.setBaseTableId(null);
        templateInfo.setBaseTableName(null);
        templateInfo.setColsInfo(new ArrayList<>());
    }

    private boolean isDouble(String val) {
        try {
            Double.parseDouble(val);
            return true;
        } catch (NumberFormatException ignore) {
            // ignore
        }
        return false;
    }

    @ApiOperation(value = "查询模型", notes = "", httpMethod = "GET")
    @GetMapping("/query")
    public AjaxResult<?> query(@RequestParam Map<String, Object> params) {
        Object tgTemplateInfos = templateService.query(params);
        return AjaxResult.success(tgTemplateInfos);
    }


    @ApiOperation(value = "查询操作指引", notes = "", httpMethod = "GET")
    @GetMapping("/guide")
    public AjaxResult<GuideDTO> guide(@RequestParam("assetId") Long assetId) {
        return AjaxResult.success(assetService.guide(assetId));
    }


    @ApiOperation(value = "删除模型", notes = "", httpMethod = "DELETE")
    @DeleteMapping("/delete")
    public AjaxResult delete(@RequestParam Map<String, Object> params) {
        String templateId = (String) params.get(CommonConstants.ID);
        if (Objects.isNull(templateId)) {
            return AjaxResult.error("参数为空");
        }

        Integer count = applicationService.countByTemplateId(templateId);
        if (count > 0) {
            return AjaxResult.error("提数模板已使用，不允许删除");
        }

        templateService.delete(params);
        assetService.delete(Long.valueOf(templateId), AssetType.MODEL);
        return AjaxResult.success();
    }

    @GetMapping("/packTailNameList")
    public AjaxResult<List<String>> packTailNameList() {
        return AjaxResult.success(templatePackTailSettingService.distinctNameList());
    }

    @ApiOperation(value = "模型状态启用禁用", notes = "提数模型", httpMethod = "PUT")
    @PutMapping("/status/{templateId}/update")
    public Object updateStatus(@PathVariable("templateId") Long templateId) {
        return templateService.updateStatus(templateId);
    }

    @ApiOperation(value = "查询模型关联表单", notes = "", httpMethod = "GET")
    @GetMapping("/queryBaseTableNames")
    public AjaxResult<?> queryBaseTableNames() {
        List<String> baseTableNames = TgTemplateInfo.newInstance().selectAll().stream()
                .map(TgTemplateInfo::getBaseTableName).distinct().collect(Collectors.toList());
        return AjaxResult.success(baseTableNames);
    }

    @GetMapping("/pb/queryList")
    public AjaxResult<List<PowerPushBiTemplateVO>> queryForPushBi(
            @RequestParam(value = "bizType", required = false) String bizType) {
        return AjaxResult.success(templateService.queryForPushBi(bizType));
    }

    /**
     * 当前用户使用到的模板
     */
    @GetMapping("/queryNameListByUser")
    public AjaxResult<List<PowerPushBiTemplateVO>> queryNameList(
            @RequestParam(value = "bizType", required = false) String bizType) {
        return AjaxResult.success(templateService.queryNameList(bizType));
    }

}
