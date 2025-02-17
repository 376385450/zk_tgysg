package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.arkbi.param.DownloadFileType;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.system.biz.dataassets.dto.request.UserDataAssetsSyncRequest;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.dto.DataDirUpdateReqDTO;
import com.sinohealth.system.dto.GetDashboardEditParam;
import com.sinohealth.system.dto.SaveArkbiParam;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.vo.ApplicationSelectListVo;
import com.sinohealth.system.vo.ArkBIEditVo;

import java.io.OutputStream;
import java.util.List;

/**
 * 数据目录Service接口
 *
 * @author jingjun
 * @date 2021-04-16
 */
public interface IMyDataDirService extends IService<DataDir> {

    Integer newDir(DataDir dataDir);

    int update(DataDir dataDir);

    void updateV2(DataDirUpdateReqDTO reqDTO);

    Integer delete(Long DirId);

    @Deprecated
    List<DataDirDto> getDirTreeGroup(Long id, Integer searchStatus, String searchProjectName, String searchBaseTable, Long searchBaseTableId, String expireType, String clientNames, Integer requireTimeType, Integer requireAttr);

    List<DataDirDto> getDirTree(Long applyId);

    List<DataManageFormDto> listTablesByDirId(Long dirId);

    List<DataDir> selectSonOfParentDir(Long parentId);

    DataDir getByAssets(Long assetsId);

    ArkBIEditVo createBIChart(Long assetsId, Integer version, Long userId) throws Exception;

    AjaxResult syncData(Long assetId);

    void updateBIChart(SaveArkbiParam param) throws Exception;

    /**
     * 创建仪表板
     *
     * @param param 数据资产
     * @return 编辑URL
     */
    ArkBIEditVo createBIDashboard(GetDashboardEditParam param, Long userId) throws Exception;

    ArkBIEditVo createEmptyBIDashboard(Long userId) throws Exception;

    /**
     * 获取BI图表/仪表板编辑URL
     */
    ArkBIEditVo getBIModify(String analysisId, Long userId) throws Exception;

    /**
     * 从BI系统前端跳转回到当前系统后触发仪表板更新
     */
    void updateBIDashboard(SaveArkbiParam param) throws Exception;

    AjaxResult<List<String>> queryDepDashboard(String extId);

    List<ApplicationSelectListVo> getApplicationList();

    void downloadBIFile(String extAnalysisId, DownloadFileType csv, OutputStream outputStream) throws Exception;

    /**
     * 创建一份复制（图表/仪表板）
     *
     * @param extAnalysisId 原始id
     * @return 编辑URL
     */
    ArkBIEditVo crateBICopy(String extAnalysisId) throws Exception;

    ArkbiAnalysis createBICopyForCustomer(String extAnalysisId, Long customerId) throws Exception;

    List<DataDirDto> getMyDataDirTreeGroup();

    /**
     * 将数据资产同步为BI视图
     */
    AjaxResult<Void> syncAssetsToBiView(UserDataAssetsSyncRequest request);

    void pullSaveAs(Long userId);
}
