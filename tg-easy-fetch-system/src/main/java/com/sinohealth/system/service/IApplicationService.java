package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.system.biz.application.bo.FieldMetaBO;
import com.sinohealth.system.biz.application.dto.ApplicationFormPageDto;
import com.sinohealth.system.biz.application.dto.ApplyMetricsDto;
import com.sinohealth.system.biz.application.dto.HistoryApplyQuoteDto;
import com.sinohealth.system.biz.application.dto.LatestProjectDto;
import com.sinohealth.system.biz.application.dto.OneItem;
import com.sinohealth.system.biz.application.dto.ParseSqlBatchRequest;
import com.sinohealth.system.biz.application.dto.request.ApplicationColumnSettingRequest;
import com.sinohealth.system.biz.application.dto.request.ApplicationFormPageRequest;
import com.sinohealth.system.biz.application.dto.request.DocApplyRequest;
import com.sinohealth.system.biz.application.dto.request.SyncApplicationSaveRequest;
import com.sinohealth.system.biz.application.dto.request.TableApplyRequest;
import com.sinohealth.system.biz.application.dto.request.TransHistoryApplyRequest;
import com.sinohealth.system.biz.application.entity.HistoryApplyQuoteEntity;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsAutoPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.vo.TableInfoSearchVO;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.ApplicationConfigRequest;
import com.sinohealth.system.dto.application.PageQueryFileApplicationRequest;
import com.sinohealth.system.dto.application.PageQueryModelApplicationRequest;
import com.sinohealth.system.dto.application.PageQueryTableApplicationRequest;
import com.sinohealth.system.dto.application.deliver.request.HistoryQueryRequest;
import com.sinohealth.system.dto.table_manage.DataRangePageDto;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import com.sinohealth.system.dto.table_manage.MetaDataFieldInfo;
import com.sinohealth.system.vo.ApplicationManageFileListVo;
import com.sinohealth.system.vo.ApplicationManageModelListVo;
import com.sinohealth.system.vo.ApplicationManageTableListVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IApplicationService {
    /**
     * 宽表申请
     */
    AjaxResult<TgApplicationInfo> createTableApplication(TableApplyRequest tableApplyRequest);

    /**
     * 模板
     */
    AjaxResult tryApplication(TgApplicationInfo applicationInfo);

    /**
     * 模板申请
     */
    AjaxResult<TgApplicationInfo> addTemplateApplication(TgApplicationInfo applicationInfo);

    /**
     * 数据交换申请
     */
    AjaxResult addSyncApplication(SyncApplicationSaveRequest param);

    /**
     * 预览模板申请
     */
    AjaxResult previewApply(TgApplicationInfo applicationInfo, GetDataInfoRequestDTO requestDTO);

    /**
     * 文档申请
     */
    AjaxResult<TgApplicationInfo> addDocApplication(DocApplyRequest docApplyRequest);

    @Deprecated
    AjaxResult<TgApplicationInfo> addTableApplication(TableApplyRequest tableApplyRequest);

    AjaxResult editColumnSetting(ApplicationColumnSettingRequest request);

    Object query(Map<String, Object> params);

    Object delete(Map<String, Object> params);

    /**
     * @param colName     列名
     * @param logicTable  逻辑表 当资产是另存出来时，是一段复杂查询SQL
     * @param assetsTable 资产表
     * @param sourceType  数据源
     */
    AjaxResult queryDataRange(String colName,
                              String logicTable,
                              String assetsTable,
                              DataRangeQueryDto dataRangeQueryDto,
                              DataSourceType sourceType);

    AjaxResult<IPage<ApplicationFormPageDto>> pageQueryApplicationByNo(ApplicationFormPageRequest request);

    AjaxResult deleteApplication(TgApplicationInfo apply);

    /**
     * 级联查询
     */
    AjaxResult<DataRangePageDto> queryCascadeDataRange(MetaDataFieldInfo metaDataFieldInfo, DataRangeQueryDto dataRangeQueryDto);

    String getReadableUsers(String readableUserIds);

    MetaDataFieldInfo getMetaDataFieldInfo(String id);

    void withdrawApplication(Long userId, Long applicationId);

    @Deprecated
    List<TgApplicationInfo> queryApplicationByUserId(String applicationType);

    Boolean existApplicationByName(String applicationType, String projectName, Integer auditStatus);

    /**
     * 获取 数据源CK 元信息 以及表的实际数据
     */
    AjaxResult<ApplicationDataDto> queryAssetsDataFromCk(Long assetsId, DataPreviewRequest requestDTO);

    /**
     * 查询 资产或申请 的CK数据
     */
    AjaxResult<ApplicationDataDto> getApplicationDataFromCk(Long applyId, GetDataInfoRequestDTO requestDTO, UserDataAssets assets);

    /**
     * @param applyId 提数申请id
     */
    FieldMetaBO getApplicationFieldMeta(Long applyId);

    /**
     * @param fieldIds 附加字段库id
     */
    FieldMetaBO queryAssetsFieldMeta(UserDataAssets dataAssets, Set<Long> fieldIds);

    List<ApplicationDataDto.Header> buildHeaders(UserDataAssets dataAssets);

    AjaxResult<Map<String, Object>> getAssetsBasicInfo(Long assetsId, Integer version);

    Object updateStatus(Long assetsId);

    Object getSearchTableSource();

    List<TableInfoSearchVO> getSearchTableAlias();

    Long countApplicationDataFromCk(String mainSql, String whereSql);

    String handleWhereSql(GetDataInfoRequestDTO requestDTO);

    List<TgApplicationInfo> queryByIds(List<Long> applicationIds);

    List<Map<String, Object>> getTopResources(Map<String, Object> parameterMap);

    List<TgApplicationInfo> listAllNormalDataApplications(List<Long> assetIds);

    boolean hasTableUsedInApplication(String tableName);

    AjaxResult<TgApplicationInfo> bindWorkflowOrSQL(ApplicationConfigRequest request);

    AjaxResult<Boolean> bindFile(ApplicationConfigRequest request);

    /**
     * 依据模板查询关联的申请
     */
    AjaxResult<List<LatestProjectDto>> queryLatestProject(Long templateId);

    AjaxResult<List<HistoryApplyQuoteDto>> queryHistoryQuote(Long templateId, String name, Integer state);

    AjaxResult<IPage<HistoryApplyQuoteEntity>> queryHistoryQuote(HistoryQueryRequest request);

    Integer countByTemplateId(String templateId);

    /**
     * 依据申请id 查询出使用的指标
     */
    AjaxResult<List<ApplyMetricsDto>> queryMetricsByApply(Long applicationId);

    AjaxResult<List<OneItem>> batchParseSql(ParseSqlBatchRequest t);

    void fillFieldIdForFilter(Long tableId, FilterDTO filter);

    void fillFieldIdForFilter(TgTemplateInfo template, FieldGranularityEnum type, FilterDTO filter);

    AjaxResult<Void> transferApply(HttpServletRequest req, TransHistoryApplyRequest request);

    AjaxResult<Void> transferTailApply(HttpServletRequest req, TransHistoryApplyRequest request);

    AjaxResult<String> applyDetail(Long projectId);

    /**
     * 宽表CMH SKU 模板审核
     */
    AjaxResult<Void> auditAllSku(Integer batch, Long no);

    String auditOneCache(Long applyId, Object no);

    /**
     * 宽表 长尾模板审核
     */
    AjaxResult<Void> auditAllTail(Integer batch, Long no);

    /**
     * 工作流式 品牌审核
     */
    AjaxResult<Void> auditFlowSchedule(Integer batch, Long no, Integer con);

    AjaxResult<Void> auditRangeFlowSchedule(Integer batch, Long no, Integer con);

    AjaxResult<Void> auditInsFlowSchedule(Integer batch, Long no, Integer con);

    AjaxResult<Void> auditCusFlowSchedule(Integer batch, Long no, Integer con);

    AjaxResult<Void> stopFlowSchedulerPool();

    /**
     * 分页查询库表类型的申请
     *
     * @param pageRequest
     * @return
     */
    AjaxResult<PageInfo<ApplicationManageTableListVo>> pageQueryTableApplication(PageQueryTableApplicationRequest pageRequest);

    /**
     * 分页查询文件类型的申请
     *
     * @param pageRequest
     * @return
     */
    AjaxResult<PageInfo<ApplicationManageFileListVo>> pageQueryFileApplication(PageQueryFileApplicationRequest pageRequest);

    /**
     * 分页查询模型的申请
     *
     * @param pageRequest
     * @return
     */
    AjaxResult<PageInfo<ApplicationManageModelListVo>> pageQueryModelApplication(PageQueryModelApplicationRequest pageRequest);


    List<TgApplicationInfo> getValidApplicationByAssetIdAndUserId(Long assetId, Long userId);

    List<TgApplicationInfo> queryApplicationByApplicantIdAndName(List<Long> assetIds, Long userId);

    List<TgApplicationInfo> queryOldApplicationWithoutNewAssetId();

    BaseDataSourceParamDto upsertDs(Integer dsId, String schema, String database);

    String fixNullTable(String ids);

    AjaxResult<List<FlowAssetsPageDTO>> pageQueryRelateApply(FlowAssetsPageRequest request, List<Long> applyIds);

    AjaxResult<List<FlowAssetsPageDTO>> pageQueryRelateApply(FlowAssetsAutoPageRequest request, List<Long> applyIds);

}
