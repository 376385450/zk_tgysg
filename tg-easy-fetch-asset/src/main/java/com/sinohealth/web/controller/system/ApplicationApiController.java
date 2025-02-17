package com.sinohealth.web.controller.system;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.annotation.IgnoreBodyLog;
import com.sinohealth.common.annotation.IgnoreLog;
import com.sinohealth.common.annotation.RepeatSubmit;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.dto.ApplicationFormPageDto;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.LatestProjectDto;
import com.sinohealth.system.biz.application.dto.SyncApplyDetailVO;
import com.sinohealth.system.biz.application.dto.request.ApplicationColumnSettingRequest;
import com.sinohealth.system.biz.application.dto.request.ApplicationFormPageRequest;
import com.sinohealth.system.biz.application.dto.request.ApplicationSaveAsRequest;
import com.sinohealth.system.biz.application.dto.request.DocApplyRequest;
import com.sinohealth.system.biz.application.dto.request.PreviewApplyRequest;
import com.sinohealth.system.biz.application.dto.request.SyncApplicationSaveRequest;
import com.sinohealth.system.biz.application.dto.request.TableApplyRequest;
import com.sinohealth.system.biz.application.entity.HistoryApplyQuoteEntity;
import com.sinohealth.system.biz.application.util.CostTimeUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.audit.dto.AuditRequest;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.ProcessDefStatusDTO;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetsVersionPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.biz.dataassets.dto.request.UserDataAssetsVersionEditRequest;
import com.sinohealth.system.biz.dataassets.dto.request.UserDataAssetsVersionPageRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dict.dao.BizDataDictDefineDAO;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.dao.KeyValDictDAO;
import com.sinohealth.system.biz.dict.domain.BizDataDictDefine;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.dto.request.FieldListRequest;
import com.sinohealth.system.biz.dict.service.BizDataDictService;
import com.sinohealth.system.biz.dict.service.FieldDictService;
import com.sinohealth.system.biz.process.service.TgFlowProcessCheckService;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.CustomFieldTemplate;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgDataRangeTemplate;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.ErrorCode;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.vo.CustomFieldTemplateVO;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ApplicationConfigRequest;
import com.sinohealth.system.dto.application.ColsInfoDto;
import com.sinohealth.system.dto.application.JoinInfoDto;
import com.sinohealth.system.dto.application.PageQueryFileApplicationRequest;
import com.sinohealth.system.dto.application.PageQueryModelApplicationRequest;
import com.sinohealth.system.dto.application.PageQueryTableApplicationRequest;
import com.sinohealth.system.dto.application.deliver.request.HistoryQueryRequest;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordQueryRequest;
import com.sinohealth.system.dto.auditprocess.AuditPageByTypeDto;
import com.sinohealth.system.dto.auditprocess.AuditPageDto;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import com.sinohealth.system.dto.table_manage.MetaDataFieldInfo;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.CustomFieldTemplateMapper;
import com.sinohealth.system.mapper.TableFieldInfoMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgDataRangeTemplateMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.IAuditProcessService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.SyncHelper;
import com.sinohealth.system.service.impl.DefaultSyncHelper;
import com.sinohealth.system.vo.ApplicationManageFileListVo;
import com.sinohealth.system.vo.ApplicationManageModelListVo;
import com.sinohealth.system.vo.ApplicationManageTableListVo;
import com.sinohealth.system.vo.TgApplicationInfoDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2023-08-05 14:30
 * @Desc
 */
@Slf4j
@Api(value = "/api/table_management/application", tags = {"资产门户提数申请接口"})
@RestController
@RequestMapping({"/api/table_management/application"})
public class ApplicationApiController extends BaseController {

    @Autowired
    private IApplicationService applicationService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IAuditProcessService auditProcessService;

    @Autowired
    private DefaultSyncHelper defaultSyncHelper;

    @Autowired
    private ITableFieldInfoService tableFieldInfoService;
    @Autowired
    private TgFlowProcessCheckService flowProcessCheckService;

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private ApplicationDataUpdateRecordDAO applicationDataUpdateRecordDAO;
    @Autowired
    private CustomFieldTemplateMapper customFieldTemplateMapper;
    @Autowired
    private TgTableApplicationMappingInfoDAO tableApplicationMappingInfoDAO;
    @Autowired
    private TgDataRangeTemplateMapper dataRangeTemplateMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AppProperties appProperties;

    @Autowired
    private BizDataDictService bizDataDictService;
    @Autowired
    private TableFieldInfoMapper tableFieldInfoMapper;
    @Autowired
    private FieldDictService fieldDictService;
    @Autowired
    private FieldDictDAO fieldDictDAO;
    @Autowired
    private BizDataDictDefineDAO bizDataDictDefineDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private KeyValDictDAO keyValDictDAO;
    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private AssetsUpgradeTriggerService assetsUpgradeTriggerService;
    @Autowired
    private SyncHelper syncHelper;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;

    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;

    @GetMapping("/checkExist")
    public AjaxResult<Void> checkExistHandingReApply(@RequestParam(value = "applyId", required = false) Long applyId,
                                                     @RequestParam(value = "assetsId", required = false) Long assetsId) {
        if (Objects.isNull(applyId) && Objects.isNull(assetsId)) {
            return AjaxResult.error("参数缺失");
        }
        if (Objects.isNull(assetsId)) {
            TgApplicationInfo origin = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                    .select(TgApplicationInfo::getAssetsId)
                    .eq(TgApplicationInfo::getId, applyId)
            );
            assetsId = Optional.ofNullable(origin).map(TgApplicationInfo::getAssetsId).orElse(null);
        }
        // 资产类判断
        Integer handingCount;
        if (Objects.nonNull(assetsId)) {
            handingCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                    .eq(TgApplicationInfo::getAssetsId, assetsId)
                    .isNotNull(TgApplicationInfo::getOldApplicationId)
                    .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDITING)
            );
        } else {
            TgApplicationInfo curApply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                    .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationType, TgApplicationInfo::getBaseTableId,
                            TgApplicationInfo::getDocId, TgApplicationInfo::getNewAssetId, TgApplicationInfo::getApplicantId)
                    .eq(TgApplicationInfo::getId, applyId));
            if (Objects.equals(curApply.getApplicationType(), ApplicationConst.ApplicationType.DOC_APPLICATION)) {
                handingCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                        .eq(TgApplicationInfo::getDocId, curApply.getDocId())
                        .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDITING)
                        .eq(TgApplicationInfo::getApplicantId, curApply.getApplicantId())
                );
            } else if (Objects.equals(curApply.getApplicationType(), ApplicationConst.ApplicationType.TABLE_APPLICATION)) {
                handingCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                        .eq(TgApplicationInfo::getBaseTableId, curApply.getBaseTableId())
                        .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDITING)
                        .eq(TgApplicationInfo::getApplicantId, curApply.getApplicantId())
                );
            } else if (Objects.equals(curApply.getApplicationType(), ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION)) {
                handingCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                        .eq(TgApplicationInfo::getNewAssetId, curApply.getNewAssetId())
                        .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDITING)
                        .eq(TgApplicationInfo::getApplicantId, curApply.getApplicantId())
                );
            } else if (Objects.equals(curApply.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)) {
                // 此场景只会出现在没创建出资产的情况
                return AjaxResult.succeed();
            } else {
                return AjaxResult.error("不支持的申请类型");
            }
        }
        if (Objects.nonNull(handingCount) && handingCount > 0) {
            return AjaxResult.error("当前申请单已发起重新申请，不允许重复重新申请");
        }
        return AjaxResult.succeed();
    }

    /**
     * 提数申请
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "TgApplicationInfo", name = "applicationInfo")
    })
    @ApiOperation(value = "新建/修改提数申请", notes = "提数申请", httpMethod = "POST")
    @PostMapping("/add")
    public AjaxResult addTemplateApplication(@RequestBody TgApplicationInfo apply,
                                             HttpServletRequest request) {
        Long userId = SecurityUtils.getUserId();
        String addApplicationKey = RedisKeys.getAddApplicationKey(userId);
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(addApplicationKey, "", Duration.ofSeconds(10));
        if (BooleanUtils.isNotTrue(lock)) {
            return AjaxResult.error("请勿重复操作");
        }
        if (apply.getNewAssetId() == null) {
            return AjaxResult.error("资产ID必填");
        }
        if (apply.getPermission() == null || apply.getPermission().isEmpty()) {
            return AjaxResult.error("资产权限信息必选");
        }

        String token = request.getHeader("Authorization");
        try {
            AjaxResult<TgApplicationInfo> addResult = applicationService.addTemplateApplication(apply);
            if (!addResult.isSuccess()) {
                String param = JsonUtils.format(apply);
                log.error("ADD token={} param={}", token, param);
            }
            return addResult;
        } catch (Exception e) {
            String param = JsonUtils.format(apply);
            log.error("ADD token={} param={}", token, param);
            log.error("", e);
            return AjaxResult.error("提交异常: " + e.getMessage());
        } finally {
            redisTemplate.delete(addApplicationKey);
        }
    }

    @PostMapping("/pageQueryApplicationByNo")
    public AjaxResult<IPage<ApplicationFormPageDto>> pageQueryApplicationByNo(@RequestBody ApplicationFormPageRequest request) {
        return applicationService.pageQueryApplicationByNo(request);
    }

    @ApiOperation(value = "删除申请草稿", httpMethod = "POST")
    @PostMapping("/deleteApplication")
    public AjaxResult deleteApplication(@RequestBody TgApplicationInfo apply) {
        return applicationService.deleteApplication(apply);
    }

    @ApiOperation(value = "修改申请列设置", httpMethod = "POST")
    @PostMapping("/editColumnSetting")
    public AjaxResult editColumnSetting(@RequestBody ApplicationColumnSettingRequest request) {
        return applicationService.editColumnSetting(request);
    }

    @ApiOperation(value = "提数申请预览", httpMethod = "POST")
    @PostMapping("/previewApply")
    public AjaxResult<ApplicationDataDto> previewApply(@Validated @RequestBody PreviewApplyRequest applyRequest) {
        try {
            final GetDataInfoRequestDTO requestDTO = applyRequest.getRequestDTO();
            requestDTO.setPageNum(1);
            requestDTO.setPageSize(10);
            final TgApplicationInfo applicationInfo = applyRequest.getApplicationInfo();
            if (applicationInfo.getNewAssetId() == null) {
                return AjaxResult.error("资产ID必填");
            }
            if (applicationInfo.getPermission() == null || applicationInfo.getPermission().isEmpty()) {
                return AjaxResult.error("资产权限信息必选");
            }
            return applicationService.previewApply(applicationInfo, requestDTO);
        } catch (Exception e) {
            if (e instanceof CustomException) {
                return AjaxResult.error(e.getMessage());
            }
            log.error("", e);
            return AjaxResult.error(ApplicationConst.ErrorMsg.buildCkMsg(e));
        }
    }


    @ApiOperation(value = "新建文档申请", httpMethod = "POST")
    @PostMapping("/addDoc")
    public AjaxResult<TgApplicationInfo> addDoc(@RequestBody @Valid DocApplyRequest docApplyRequest) {
        Long userId = SecurityUtils.getUserId();
        String addApplicationKey = RedisKeys.getAddApplicationKey(userId);
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(addApplicationKey, "", Duration.ofSeconds(10));
        if (BooleanUtils.isNotTrue(lock)) {
            return AjaxResult.error("请勿重复操作");
        }

        try {
            return applicationService.addDocApplication(docApplyRequest);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("提交异常");
        } finally {
            redisTemplate.delete(addApplicationKey);
        }
    }

    /**
     * 数据交换申请
     */
    @PostMapping("/addSyncApply")
    @RepeatSubmit
    public AjaxResult addSyncApplication(@RequestBody @Validated SyncApplicationSaveRequest param) {
        return applicationService.addSyncApplication(param);
    }

    @ApiOperation(value = "新建表单申请", httpMethod = "POST")
    @PostMapping("/addTable")
    public AjaxResult<TgApplicationInfo> addTable(@RequestBody @Valid TableApplyRequest tableApplyRequest) {
        Long userId = SecurityUtils.getUserId();
        String addApplicationKey = RedisKeys.getAddApplicationKey(userId);
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(addApplicationKey, "", Duration.ofSeconds(10));
        if (BooleanUtils.isNotTrue(lock)) {
            return AjaxResult.error("请勿重复操作");
        }

        try {
            return applicationService.createTableApplication(tableApplyRequest);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("提交异常");
        } finally {
            redisTemplate.delete(addApplicationKey);
        }
    }


    /**
     * 仅调试使用，功能上被预览替代
     */
    @ApiOperation(value = "尝试提数申请", notes = "尝试提数申请", httpMethod = "POST")
    @PostMapping("/tryApply")
    public Object tryApply(@RequestBody TgApplicationInfo applicationInfo) {
        AjaxResult result = applicationService.tryApplication(applicationInfo);
        return AjaxResult.error(result.getMsg());
    }

    /**
     * 查询表关联的自定义公式
     */
    @GetMapping("/queryCustomByTableId")
    public AjaxResult<List<CustomFieldTemplateVO>> queryCustomByTableId(@Param("tableId") Long tableId) {
        if (Objects.isNull(tableId)) {
            return AjaxResult.error("参数缺失");
        }

        List<CustomFieldTemplate> templates = customFieldTemplateMapper
                .selectList(new QueryWrapper<CustomFieldTemplate>().lambda()
                        .eq(CustomFieldTemplate::getBaseTableId, tableId));
        List<CustomFieldTemplateVO> voList = templates.stream().map(v -> {
            CustomFieldTemplateVO vo = new CustomFieldTemplateVO();
            BeanUtils.copyProperties(v, vo);
            vo.setComputeWay(v.getId());
            return vo;
        }).collect(Collectors.toList());
        return AjaxResult.success(voList);
    }


    @ApiOperation(value = "申请状态启用禁用", notes = "提数申请", httpMethod = "PUT")
    @PutMapping("/status/{assetsId}/update")
    public Object updateStatus(@PathVariable("assetsId") Long assetsId) {
        return applicationService.updateStatus(assetsId);
    }

    @ApiOperation(value = "查询提数申请", notes = "", httpMethod = "GET")
    @GetMapping("/query")
    public AjaxResult qeury(@RequestParam Map<String, Object> params) {

        Object tgApplicationInfos = applicationService.query(params);

        return AjaxResult.success(tgApplicationInfos);
    }

    /**
     * 查询列的数据枚举值
     * <p>
     * 1. 数据资产： 支持全字段级联，常规+通用模式查询资产表的字段值， 宽表模式查资产表字段值（绑+未绑 字段库）
     * 2. 预设数据范围和模板管理和提数申请：仅支持产品粒度级联，宽表模式查原始底表（绑+未绑 字段库），常规+通用模式查关联的数据字典
     * 3. 资产目录->底表预览：支持全字段级联，查底表的字段值（绑+未绑 字段库）
     */
    @PostMapping("/field/data_range/query")
    public AjaxResult queryDataRangeApi(@RequestBody DataRangeQueryDto rangeQuery) {
        // 常规+通用 类型的 申请和模板：字典值 支持级联
        if (Objects.nonNull(rangeQuery.getFieldDictId())) {
            AjaxResult res = bizDataDictService.dataRangeSearch(rangeQuery);
            res.put("type", "数据字典");
            return res;
        }

        LoginUser user = SecurityUtils.getLoginUser();

        // 资产数据 级联查询
        UserDataAssets assets = Optional.ofNullable(rangeQuery.getAssetsId()).map(userDataAssetsDAO::getById).orElse(null);
        if (Objects.nonNull(assets)) {
            return this.queryAssetsTable(rangeQuery, user, assets);
        }

        // 宽底表+申请+模板 级联查询
        Long colId = rangeQuery.getColId();
        if (Objects.nonNull(colId) && colId > 0) {
            return this.queryWideTable(rangeQuery, colId, user);
        }

        return AjaxResult.error("not supported");
    }

    /**
     * 资产数据 级联查询：资产id存在，直接查资产表 区分内外ck实例
     */
    private AjaxResult queryAssetsTable(DataRangeQueryDto rangeQuery, LoginUser user, UserDataAssets assets) {
        String colName;
        String tableName;
        // 依据客户类型区分 连接的数据源
        DataSourceType sourceType;
        if (Objects.equals(user.getUser().getUserInfoType(), CommonConstants.CUSTOMER_UESR)) {
            sourceType = DataSourceType.CUSTOMER_CK;
            TgTableApplicationMappingInfo mappingInfo = tableApplicationMappingInfoDAO.getByAssetsId(rangeQuery.getAssetsId());
            colName = rangeQuery.getFieldName();
            tableName = mappingInfo.getDataTableName();
        } else {
            if (Objects.nonNull(assets.getCopyFromId())) {
                sourceType = DataSourceType.SLAVE;
                colName = rangeQuery.getFieldName();
                tableName = "(" + assets.getAssetsSql() + ")";
            } else {
                sourceType = DataSourceType.SLAVE;
                colName = rangeQuery.getFieldName();
                tableName = assets.getAssetTableName();
            }
        }
        // 剔除自身字段 条件
        cleanItems(rangeQuery);
        AjaxResult res = applicationService.queryDataRange(colName, tableName, assets.getAssetTableName(), rangeQuery, sourceType);
        res.put("type", "资产");
        return res;
    }

    /**
     * 宽底表+申请+模板 级联查询 （通过产品标准表/原始底表）
     */
    private AjaxResult queryWideTable(DataRangeQueryDto rangeQuery, Long colId, LoginUser user) {
        String colName;
        String tableName;
        MetaDataFieldInfo tableField = applicationService.getMetaDataFieldInfo(String.valueOf(colId));
        if (Objects.nonNull(tableField)) {
            rangeQuery.setFieldName(tableField.getColName());
        }

        // V 1.9.8 去除产品标准表逻辑
//            if (Objects.nonNull(tableField) && this.needCascade(tableField.getColName())) {
//                // 剔除自身字段 条件
//                cleanItems(rangeQuery);
//
//                AjaxResult<DataRangePageDto> res = applicationService.queryCascadeDataRange(tableField, rangeQuery);
//                res.put("type", "申请+模板");
//                return res;
//            }

        // 客户端查询
        TgTableApplicationMappingInfo mappingInfo = null;
        if (Objects.isNull(tableField)) {
            mappingInfo = tableApplicationMappingInfoDAO.getByAssetsId(rangeQuery.getAssetsId());
            Assert.isTrue(mappingInfo != null, "客户资产不存在： " + rangeQuery.getAssetsId());
        }

        // 依据客户类型区分 连接的数据源
        DataSourceType sourceType;
        if (Objects.equals(user.getUser().getUserInfoType(), CommonConstants.CUSTOMER_UESR)) {
            sourceType = DataSourceType.CUSTOMER_CK;
        } else {
            sourceType = DataSourceType.SLAVE;
        }
        if (Objects.nonNull(tableField)) {
            colName = tableField.getColName();
            // 模板+申请 关联的原始底表
            tableName = tableField.getTableName();

            List<FilterDTO.FilterItemDTO> items = rangeQuery.getFilterItems();

            if (CollectionUtils.isNotEmpty(items)) {
                List<Long> dictIds = Lambda.buildList(items, FilterDTO.FilterItemDTO::getFieldId);
                List<TableFieldInfo> tableFieldInfos = tableFieldInfoMapper.selectList(new QueryWrapper<TableFieldInfo>().lambda()
                        .select(TableFieldInfo::getId, TableFieldInfo::getRelationColId, TableFieldInfo::getFieldName)
                        .in(TableFieldInfo::getRelationColId, dictIds)
                );
                Map<Long, TableFieldInfo> fieldMap = Lambda.buildMap(tableFieldInfos, TableFieldInfo::getRelationColId);
                for (FilterDTO.FilterItemDTO item : items) {
                    Long fieldId = item.getFieldId();
                    TableFieldInfo tableFieldInfo = fieldMap.get(fieldId);
                    if (Objects.nonNull(tableFieldInfo)) {
                        String fieldName = tableFieldInfo.getFieldName();
                        item.setFieldName(fieldName);
                    }

                }
            }
        } else {
            // 客户端 资产表 废弃
            colName = rangeQuery.getFieldName();
            tableName = mappingInfo.getDataTableName();
        }

        cleanItems(rangeQuery);
        AjaxResult res = applicationService.queryDataRange(colName, tableName, null, rangeQuery, sourceType);
        res.put("type", "宽表");
        return res;
    }

    private static void cleanItems(DataRangeQueryDto rangeQuery) {
        // 剔除自身字段 条件
        if (CollectionUtils.isNotEmpty(rangeQuery.getFilterItems())) {
            rangeQuery.getFilterItems().removeIf(v ->
                    Objects.equals(v.getFieldName(), rangeQuery.getFieldName())
                            || Objects.isNull(v.getValue())
                            || StringUtils.isBlank(v.getValue().toString())
            );
        }
    }

    private boolean needCascade(String fieldName) {
        String cascadeFields = appProperties.getCascadeFields();

        if (StringUtils.isBlank(cascadeFields)) {
            return false;
        }
        String[] pairs = cascadeFields.split("#");
        for (String pair : pairs) {
            String[] fields = pair.split(",");
            for (String field : fields) {
                if (Objects.equals(field, fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param fieldIds 字段库id
     */
    @ApiOperation(value = "判断字段是否有级联关系", httpMethod = "GET")
    @GetMapping("/field/data_range/judgeFieldRelation")
    public AjaxResult judgeFieldRelation(@Param("fieldIds") String fieldIds,
                                         @Param("templateType") String templateType) {
        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(templateType);
        if (StringUtils.isBlank(fieldIds) || !typeOpt.isPresent()) {
            return AjaxResult.success();
        }

        List<Long> ids = Arrays.stream(fieldIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return AjaxResult.success();
        }

        if (typeOpt.get().isSchedulerTaskType()) {
            AjaxResult<List<FieldDictDTO>> listResult = fieldDictService.listQuery(FieldListRequest.builder().ids(ids).build());
            if (!listResult.isSuccess()) {
                return AjaxResult.error("字段库映射错误");
            }

            List<Long> dictIds = listResult.getData().stream().map(FieldDictDTO::getDictId)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dictIds)) {
                return AjaxResult.success();
            }
            List<BizDataDictDefine> defines = bizDataDictDefineDAO.getBaseMapper()
                    .selectList(new QueryWrapper<BizDataDictDefine>().lambda().in(BizDataDictDefine::getId, dictIds));

            for (Map.Entry<String, List<BizDataDictDefine>> entry : defines.stream()
                    .filter(v -> StringUtils.isNoneBlank(v.getQuoteTable()))
                    .collect(Collectors.groupingBy(BizDataDictDefine::getQuoteTable))
                    .entrySet()) {
                Set<String> sameTables = entry.getValue().stream().map(BizDataDictDefine::getQuoteCol).collect(Collectors.toSet());
                if (sameTables.size() > 1) {
                    String msg = sameTables.stream().map(v -> "字段" + v).collect(Collectors.joining(","));
                    return AjaxResult.error(msg + "存在级联关系，当前配置可能存在无法出数情况，请及时修改");
                }
            }
        } else {
            // 用字段库查回原始字段
            List<TableFieldInfo> fields = tableFieldInfoMapper.selectList(new QueryWrapper<TableFieldInfo>().lambda()
                    .in(TableFieldInfo::getRelationColId, ids));
            List<String> fieldNames = fields.stream().map(TableFieldInfo::getFieldName).collect(Collectors.toList());

            List<List<String>> cascadeFields = appProperties.buildCascadeFieldsList();
            for (List<String> cascadeField : cascadeFields) {
                Collection<String> result = CollectionUtils.intersection(fieldNames, cascadeField);
                if (result.size() > 1) {
                    String msg = result.stream().map(v -> "字段" + v).collect(Collectors.joining(","));
                    return AjaxResult.error(msg + "存在级联关系，当前配置可能存在无法出数情况，请及时修改");
                }
            }

        }
        return AjaxResult.success();
    }


    @ApiOperation(value = "查询申请日期字段", notes = "查询申请日期字段", httpMethod = "GET")
    @PostMapping("/field/period_range/query")
    public AjaxResult<List<TableFieldInfo>> queryPeriodField(@RequestBody List<Long> ids) {
        List<TableFieldInfo> dateFields = ids.stream()
                .map(id -> tableFieldInfoService.getById(id))
                .filter(Objects::nonNull)
                .filter(f -> f.getDataType().toUpperCase().startsWith("DATE"))
                .map(f -> f.setFieldName(tableInfoService.getById(f.getTableId()).getTableAlias() + "-" + f.getFieldAlias()))
                .collect(Collectors.toList());

        return AjaxResult.success(dateFields);
    }

    // 资产 预览

    @PostMapping(value = "/{assetsId}/dataInfo")
    @ApiOperation(value = "我的资产数据-数据详情（复合筛选）", response = TableDataDto.class)
    @IgnoreBodyLog
    public AjaxResult<ApplicationDataDto> assetsDataInfoApi(@ApiParam(hidden = true) @PathVariable("assetsId") Long assetsId,
                                                            @Validated @RequestBody DataPreviewRequest requestDTO) {
        try {
            return applicationService.queryAssetsDataFromCk(assetsId, requestDTO);
        } catch (CustomException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(ApplicationConst.ErrorMsg.buildCkMsg(e));
        }
    }

    /**
     * 宽表模式 申请单 数据预览 未创建资产
     */
    @PostMapping(value = "/{applyId}/applyDataInfo")
    @ApiOperation(value = "申请-数据详情（复合筛选）", response = TableDataDto.class)
    public Object getApplyDataInfoApi(@ApiParam(hidden = true) @PathVariable("applyId") Long applyId,
                                      @Validated @RequestBody GetDataInfoRequestDTO requestDTO) {
        try {
            return applicationService.getApplicationDataFromCk(applyId, requestDTO, null);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(ApplicationConst.ErrorMsg.buildCkMsg(e));
        }
    }

    @PostMapping(value = "/{applyId}/previewDataInfo")
    @ApiOperation(value = "预览申请-数据详情", response = TableDataDto.class)
    public Object previewDataInfoApi(@ApiParam(hidden = true) @PathVariable("applyId") Long applyId,
                                     @Validated @RequestBody GetDataInfoRequestDTO requestDTO) {
        try {
            requestDTO.setPageNum(1);
            requestDTO.setPageSize(10);
            return applicationService.getApplicationDataFromCk(applyId, requestDTO, null);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(ApplicationConst.ErrorMsg.buildCkMsg(e));
        }
    }


    @GetMapping(value = "/{applyId}/metadata/fields")
    @ApiOperation(value = "我的数据-数据详情-申请表单元数据", response = TableFieldInfo.class)
    public AjaxResult metaDataFieldsApi(@ApiParam(hidden = true) @PathVariable("applyId") Long assetsId) {
        UserDataAssets assets = new UserDataAssets().selectById(assetsId);
        if (Objects.isNull(assets)) {
            return AjaxResult.error("资产不存在");
        }
        if (Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            return AjaxResult.success(applicationService.getApplicationFieldMeta(assets.getSrcApplicationId()).mergeFieldsWithPeriod());
        }

        return AjaxResult.success(applicationService.queryAssetsFieldMeta(assets, null).mergeFieldsWithPeriod());
    }


    @GetMapping(value = "/{applyId}/basic_info")
    @ApiOperation(value = "我的数据-数据详情-申请表元数据", response = TableFieldInfo.class)
    public AjaxResult applyBasicInfoApi(@ApiParam(hidden = true) @PathVariable("applyId") Long id,
                                        @RequestParam(value = "version", required = false) Integer version) {
        return applicationService.getAssetsBasicInfo(id, version);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params")
    })
    @ApiOperation(value = "查询用户提数申请项目", notes = "", httpMethod = "GET")
    @GetMapping("/project_name/query")
    public AjaxResult qeuryProjectNames(@RequestParam Map<String, Object> params) {
        sysUserService.selectUserById(1L);
        Object tgApplicationInfos = applicationService.query(params);

        return AjaxResult.success(tgApplicationInfos);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params", value = "")
    })
    @ApiOperation(value = "删除提数申请", notes = "", httpMethod = "DELETE")
    @DeleteMapping("/delete")
    public AjaxResult delete(@RequestParam Map<String, Object> params) {

        applicationService.delete(params);
        return AjaxResult.success();
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params", value = "")
    })
    @ApiOperation(value = "我的申请 申请列表", notes = "", httpMethod = "GET")
    @GetMapping("/audit_process/query")
    public AjaxResult queryAuditProcessApplicationList(@RequestParam Map<String, Object> params) {
        Object auditProcessInfos = auditProcessService.queryAuditProcessApplicationList(params);
        return AjaxResult.success(auditProcessInfos);
    }

    @PostMapping(value = "/queryAssetsSnapshot")
    @ApiOperation(value = "我的资产数据-数据历史版本", response = TableDataDto.class)
    public AjaxResult<IPage<UserDataAssetsVersionPageDTO>> queryAssetsSnapshot(@RequestBody UserDataAssetsVersionPageRequest request) {
        return userDataAssetsService.pageQueryAssetsSnapshot(request);
    }

    @PostMapping(value = "/editAssetsInfo")
    public AjaxResult<Void> editAssetsInfo(@RequestBody @Validated UserDataAssetsVersionEditRequest request) {
        return userDataAssetsService.editAssetsInfo(request);
    }


    @GetMapping("/audit_process/query/count")
    public AjaxResult queryAuditProcessApplicationListCount(@RequestParam Map<String, Object> params) {
        params.put("applicationType", ApplicationConst.ApplicationType.DATA_APPLICATION);
        params.putIfAbsent("searchStatus", CommonConstants.NORMAL);
        Integer dataCount = auditProcessService.qeuryAuditProcessApplicationListCount(params);

        params.put("applicationType", ApplicationConst.ApplicationType.TABLE_APPLICATION);
        params.putIfAbsent("searchStatus", CommonConstants.NORMAL);
        Integer tableCount = auditProcessService.qeuryAuditProcessApplicationListCount(params);

        params.put("applicationType", ApplicationConst.ApplicationType.DOC_APPLICATION);
        Integer docCount = auditProcessService.qeuryAuditProcessApplicationListCount(params);

        return AjaxResult.success(new HashMap<String, Object>() {{
            put("dataCount", dataCount);
            put("docCount", docCount);
            put("tableCount", tableCount);
        }});
    }

    /**
     * @see ApplicationApiController#auditProcessApi 审核
     */
    @ApiOperation(value = "申请详情", notes = "", httpMethod = "GET")
    @GetMapping("/audit_process/detail")
    public AjaxResult<TgApplicationInfoDetailVO> queryAuditProcessDetailApi(@RequestParam Long applicationId) {
        TgApplicationInfoDetailVO auditProcessInfos = auditProcessService.queryAuditProcessApplicationDetail(applicationId);
        return AjaxResult.success(auditProcessInfos);
    }

    @ApiOperation(value = "复制申请详情", notes = "", httpMethod = "GET")
    @GetMapping("/audit_process/copyDetail")
    public AjaxResult copyDetailApi(@RequestParam Long applicationId) {
        TgApplicationInfoDetailVO auditProcessInfos = auditProcessService.queryAuditProcessApplicationDetail(applicationId);
        List<ApplicationGranularityDto> list = auditProcessInfos.getGranularity();
        // 复制模板数据
        if (CollectionUtils.isNotEmpty(list)) {
            for (ApplicationGranularityDto dto : list) {
                Long oldId = dto.getRangeTemplateId();
                TgDataRangeTemplate origin = dataRangeTemplateMapper.selectById(oldId);
                if (Objects.isNull(origin)) {
                    dto.setRangeTemplateId(null);
                    continue;
                }
                TgDataRangeTemplate copy = new TgDataRangeTemplate();
                copy.setDataRangeConfig(origin.getDataRangeConfig());
                copy.setCreator(origin.getCreator());
                copy.setUpdater(origin.getUpdater());
                copy.setCreateTime(new Date());
                copy.setUpdateTime(new Date());
                dataRangeTemplateMapper.insert(copy);
                Long newId = copy.getId();
                dto.setRangeTemplateId(newId);
            }
        }
        return AjaxResult.success(auditProcessInfos);
    }

    @ApiOperation(value = "数据交换 申请详情", notes = "", httpMethod = "GET")
    @GetMapping("/audit_process/syncDetail")
    public AjaxResult syncDetailApi(@RequestParam Long applicationId) {
        SyncApplyDetailVO syncApplyDetailVO = auditProcessService.querySyncDetail(applicationId);
        return AjaxResult.success(syncApplyDetailVO);
    }

    /**
     * 用于复制申请，交互调整，已废弃
     */
    @Deprecated
    @ApiOperation(value = "查询流程申请详情-历史申请", notes = "", httpMethod = "GET")
    @GetMapping("/audit_process/detailForHistory")
    public AjaxResult queryAuditProcessApplicationDetailForHistory(@RequestParam Long applicationId) {
        TgApplicationInfo applicationInfo = auditProcessService.queryAuditProcessApplicationDetail(applicationId);
        TgTemplateInfo template = JsonBeanConverter.convert2Obj(new TgTemplateInfo().selectById(applicationInfo.getTemplateId()));
        // 通用提数
        if (Objects.isNull(template)) {
            return AjaxResult.success(applicationInfo);
        }
        List<JoinInfoDto> joinInfo = applicationInfo.getJoinInfo();
        joinInfo.removeIf(v -> v.getIsItself() == CommonConstants.TEMPLATE);
        joinInfo.addAll(template.getJoinInfo());

        // 置换select选择状态 保留申请的字段选中状态，加入模板全部字段
        List<Long> applySelect = null;
        if (CollectionUtils.isNotEmpty(applicationInfo.getColsInfo())) {
            ColsInfoDto colsInfoDto = applicationInfo.getColsInfo().get(0);
            applySelect = colsInfoDto.getSelect();
        }

        // 替换模板最新的字段信息进来，但是保留驱动表顺序（第一位）
        ColsInfoDto templateFirst = template.getColsInfo().get(0);
        applicationInfo.getColsInfo().removeIf(v -> Objects.equals(v.getIsItself(), CommonConstants.TEMPLATE));
        applicationInfo.getColsInfo().addAll(template.getColsInfo());
        templateFirst.setCopySelect(templateFirst.getSelect());
        templateFirst.setSelect(applySelect);
        applicationInfo.getColsInfo().remove(templateFirst);
        applicationInfo.getColsInfo().add(0, templateFirst);

        applicationInfo.getMetricsInfo().removeIf(v -> Objects.equals(v.getIsItself(), CommonConstants.TEMPLATE));
        applicationInfo.getMetricsInfo().addAll(template.getMetricsInfo());

        applicationInfo.setDataRangeInfo(template.getDataRangeInfo());

        return AjaxResult.success(applicationInfo);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params")
    })
    @ApiOperation(value = "查询流程申请审核列表", httpMethod = "GET")
    @GetMapping("/audit_process/audit/query")
    public AjaxResult queryAuditProcessAuditList(@RequestParam Map<String, Object> params) {
        SysUser user = ThreadContextHolder.getSysUser();
        Object auditProcessInfos = auditProcessService.queryAuditProcessAuditList(user, params);
        return AjaxResult.success(auditProcessInfos);
    }


    @ApiOperation(value = "文档审核记录列表", httpMethod = "GET")
    @GetMapping("/audit_process/audit/queryAll")
    public AjaxResult queryAllAuditProcessList(@RequestParam Map<String, Object> params) {
        params.put("applicationType", ApplicationConst.ApplicationType.DOC_APPLICATION);
        List<AuditPageDto> auditProcessInfos = auditProcessService.queryAuditProcessAuditList(null, params);
        return AjaxResult.success(auditProcessInfos);
    }

    @ApiOperation(value = "查询流程申请审核详情", notes = "", httpMethod = "GET")
    @GetMapping("/audit_process/audit/detail")
    public AjaxResult qeuryAuditProcessAuditDetail(@RequestParam Long applicationId) {
        Object auditProcessInfos = auditProcessService.qeuryAuditProcessAuditDetail(applicationId);
        return AjaxResult.success(auditProcessInfos);
    }

    @GetMapping("/audit_process/validateCount")
    public AjaxResult validateDataCount(@RequestParam Long applicationId) {
        Pair<Boolean, Long> result = auditProcessService.overLimit(applicationId);
        return result.getKey()
                ? AjaxResult.error(ErrorCode.DATA_OVER_LIMIT_CONFIRM, "当前审批的数据量超过100W，数据量为" + result.getValue() + "，是否确认审批？")
                : AjaxResult.success("", result.getValue());
    }

    @ApiOperation(value = "审核", httpMethod = "POST")
    @PostMapping("/audit_process/audit")
    public AjaxResult auditProcessApi(@RequestBody @Validated AuditRequest request) {
        if (Objects.nonNull(request.getDeliverDay()) && (request.getDeliverDay() < 1 || request.getDeliverDay() > 1000)) {
            return AjaxResult.error("请输入有效的交付时间");
        }
        ttl.execute(() -> {
            try {
                Object auditProcessInfos = auditProcessService.auditProcess(request);
                log.info("auditProcessInfos={}", auditProcessInfos);
            } catch (Exception e) {
                log.error("{}", request, e);
            }
        });
        return AjaxResult.succeed();
    }

    @ApiOperation(value = "批量审核", httpMethod = "POST")
    @PostMapping("/audit_process/batch_audit")
    public AjaxResult batchAuditProcess(@RequestBody List<AuditRequest> nodes) {
        List<Long> ids = Lambda.buildList(nodes, AuditRequest::getApplicationId);
        if (CollectionUtils.isEmpty(ids)) {
            return AjaxResult.error("申请单为空");
        }
        this.markRun(ids);
        ttl.execute(() -> nodes.forEach(auditProcessService::auditProcess));
        return AjaxResult.success("ok");
    }

    private void markRun(List<Long> ids) {
        List<TgApplicationInfo> infos = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getTemplateId)
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)
                .in(TgApplicationInfo::getId, ids)
        );

        Set<Long> templateIds = Lambda.buildSet(infos, TgApplicationInfo::getTemplateId);
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }

        List<TgTemplateInfo> need = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getId)
                .in(TgTemplateInfo::getId, templateIds)
                .eq(TgTemplateInfo::getTemplateType, TemplateTypeEnum.wide_table.name())
        );
        Set<Long> needIds = Lambda.buildSet(need, TgTemplateInfo::getId);
        if (CollectionUtils.isEmpty(needIds)) {
            return;
        }

        List<Long> markIds = infos.stream().filter(v -> needIds.contains(v.getTemplateId()))
                .map(TgApplicationInfo::getId).collect(Collectors.toList());
        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getDataState, ApplyDataStateEnum.run.name())
                .in(TgApplicationInfo::getId, markIds)
        );
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "applicationId")
    })
    @ApiOperation(value = "撤销流程申请", httpMethod = "GET")
    @GetMapping("/withdraw")
    public AjaxResult withdrawApplication(@RequestParam("applicationId") Long applicationId) {
        SysUser sysUser = ThreadContextHolder.getSysUser();
        applicationService.withdrawApplication(sysUser.getUserId(), applicationId);
        return AjaxResult.success();
    }

    /**
     * 内网CK同步数据到外网CK
     */

    @GetMapping("/{assetsId}/sync")
    public AjaxResult syncLocalCk2RemoteCk(@PathVariable("assetsId") Long assetsId) {
        UserDataAssets userDataAssets = new UserDataAssets().selectById(assetsId);
        ApplicationDataDto applicationDataDto = applicationService.queryAssetsDataFromCk(assetsId, new DataPreviewRequest() {{
            setPageNum(1);
            setPageSize(1);
        }}).getData();
        Object result = defaultSyncHelper.syncApplicationTableToCustomerDatasource(applicationDataDto, userDataAssets);
        return AjaxResult.success(result);
    }

    /**
     * TODO 确认接口是否废弃
     */

    @GetMapping("existPassedApplication")
    public Object existPassedApplication(@RequestParam("projectName") String projectName) {
        Boolean exist = applicationService.existApplicationByName(ApplicationConst.ApplicationType.DATA_APPLICATION,
                projectName, ApplicationConst.AuditStatus.AUDIT_PASS);
        return AjaxResult.success(exist);
    }


    @ApiOperation("资产 另存为")
    @PostMapping("/saveAs")
    public AjaxResult<Long> saveAsApi(@Valid @RequestBody ApplicationSaveAsRequest request) {
        Long copyAssetsId = userDataAssetsService.saveAs(request);
        return AjaxResult.success(copyAssetsId);
    }


    @PostMapping("/listDataUpdateRecord")
    public AjaxResult<TableDataInfo<ApplicationDataUpdateRecord>> listDataUpdateRecord(@RequestBody UpdateRecordQueryRequest request) {
        IPage<ApplicationDataUpdateRecord> pageResult = applicationDataUpdateRecordDAO.pageQueryByAppId(request);
        TableDataInfo<ApplicationDataUpdateRecord> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setRows(pageResult.getRecords());
        rspData.setTotal(pageResult.getTotal());
        return AjaxResult.success(rspData);
    }


    @GetMapping("/queryLatestProject")
    @IgnoreLog
    public AjaxResult<List<LatestProjectDto>> queryLatestProject(@RequestParam(value = "templateId", required = false) Long templateId) {
        return applicationService.queryLatestProject(templateId);
    }

    @PostMapping("/queryHistoryQuote")
    @IgnoreLog
    public AjaxResult<IPage<HistoryApplyQuoteEntity>> queryHistoryQuote(@RequestBody HistoryQueryRequest request) {
        request.setUserId(SecurityUtils.getUserId());
        return applicationService.queryHistoryQuote(request);
    }

    @PostMapping("/workflow/config")
    @ApiOperation("工作流审核配置")
    public AjaxResult<Boolean> config(@Valid @RequestBody ApplicationConfigRequest request) {
        request.setDataCostMin(CostTimeUtil.calcCostMin(request.getDataCost()));

        if (ApplicationConfigTypeConstant.FILE_TYPE.equals(request.getType())) {
            return applicationService.bindFile(request);
        } else {
            //如果是SQL模式先创建尚书台自动化流程，然后保存到application中
            AjaxResult<TgApplicationInfo> bindResult = applicationService.bindWorkflowOrSQL(request);
            if (bindResult.isSuccess()) {
                if (!Objects.equals(request.getType(), ApplicationConfigTypeConstant.WORK_FLOW_TYPE)) {
                    Integer flowId = bindResult.getData().getConfigSqlWorkflowId();
                    request.setWorkflowId(flowId);
                }
//            assetsUpgradeTriggerService.upsertWorkflowConfig(request);
            }
            return AjaxResult.success(bindResult.isSuccess());
        }
    }

    /**
     * @see ApplicationApiController#config 先配置再执行
     */
    @ApiOperation("工作流执行")
    @GetMapping("/workflow/execute")
    public AjaxResult executeWorkFlow(@RequestParam("applicationId") Long applicationId) {
        return userDataAssetsService.mixExecuteForApply(applicationId);
    }

    @ApiOperation("查询工作流执行状态")
    @GetMapping("/workflow/status")
    public AjaxResult<ProcessDefStatusDTO> queryWorkflowStatus(@RequestParam("applicationId") Long applicationId) {
        return userDataAssetsService.queryWorkflowStatus(applicationId);
    }

//    @ApiOperation("查询工作流执行状态")
//    @GetMapping("/autoTransfer")
//    public AjaxResult queryWorkflowStatus(@RequestParam("ids") String ids) {
//
//        List<Long> collect = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
//        syncHelper.autoTransfer(collect);
//        return AjaxResult.success("操作成功！！！");
//    }

    @ApiOperation("查询库表类型的审批列表")
    @PostMapping("/table/pageQuery")
    public AjaxResult<PageInfo<ApplicationManageTableListVo>> pageQueryTableApplication(
            @Validated @RequestBody PageQueryTableApplicationRequest pageRequest) {
        return applicationService.pageQueryTableApplication(pageRequest);
    }

    @ApiOperation("查询文件类型的审批列表")
    @PostMapping("/file/pageQuery")
    public AjaxResult<PageInfo<ApplicationManageFileListVo>> pageQueryFileApplication(
            @Validated @RequestBody PageQueryFileApplicationRequest pageRequest) {
        return applicationService.pageQueryFileApplication(pageRequest);
    }


    @ApiOperation("查询模型的审批列表")
    @PostMapping("/model/pageQuery")
    public AjaxResult<PageInfo<ApplicationManageModelListVo>> pageQueryModelApplication(
            @Validated @RequestBody PageQueryModelApplicationRequest pageRequest) {
        return applicationService.pageQueryModelApplication(pageRequest);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Map<String, Object>", name = "params")
    })
    @ApiOperation(value = "待审核 已审核", httpMethod = "GET")
    @GetMapping("/audit_process/audit/query_by_type")
    public AjaxResult<List<AuditPageByTypeDto>> queryAuditProcessAuditListByType(@RequestParam Map<String, Object> params) {
        SysUser user = ThreadContextHolder.getSysUser();
        return auditProcessService.queryAuditProcessAuditListByType(user, params);
    }

    @GetMapping("/updateContractNo")
    public AjaxResult<Void> updateContractNo(@RequestParam(value = "applicationId", required = false) Long applicationId,
                                             @RequestParam(value = "contractNo", required = false) String contractNo) {
        if (Objects.isNull(applicationId) || StringUtils.isBlank(contractNo)) {
            return AjaxResult.error("参数缺失");
        }
        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getId, applicationId)
                .set(TgApplicationInfo::getContractNo, contractNo)
        );
        return AjaxResult.succeed();
    }

    /**
     * 提数申请时的 是否交付版本提示
     */
    @GetMapping("/queryCurPeriod")
    public AjaxResult<String> queryCurPeriod(@RequestParam("templateId") Long templateId) {
        return flowProcessCheckService.queryCurPeriod(templateId);
    }
}
