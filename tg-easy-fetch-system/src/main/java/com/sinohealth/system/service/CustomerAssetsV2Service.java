package com.sinohealth.system.service;

import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.assets.*;

import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 17:27
 */
public interface CustomerAssetsV2Service {

    /**
     * 当前客户资产树
     * @param query
     * @return
     */
    List getTree(AssetsDirTreeQuery query);

    /**
     * 指定客户资产树
     * @param query
     * @return
     */
    List getCustomerTree(CustomerAuthTreeQuery query);

    /**
     * 查询子账号资产目录树
     * 子账号可分配的是父账号资产的子集
     * @param query
     */
    List getSubCustomerTree(SubCustomerAuthTreeQuery query);


    /**
     * 获取客户授权表字段
     * @param applyId
     * @return
     */
    List<AuthTableFieldDTO> getAuthTableFields(Long applyId);

    /**
     * 获取授权表数据
     * @param applyId
     * @param requestDTO
     *
     * @return
     */
    Object getAuthTableData(Long assetsId, GetDataInfoRequestDTO requestDTO);

    /**
     * 客户资产-表单数据下载
     * @param reqDTO
     * @throws Exception
     */
    HuaweiPath downloadApply(AssetsFormDownloadReqDTO reqDTO) throws Exception;

    /**
     * 客户资产-图表分析下载
     * @param reqDTO
     * @throws Exception
     */
    HuaweiPath downloadChart(AssetsChartDownloadReqDTO reqDTO) throws Exception;

    /**
     * 客户资产-仪表板下载
     * @param reqDTO
     * @throws Exception
     */
    void downloadDashboard(AssetsDashboardDownloadReqDTO reqDTO) throws Exception;

    /**
     * 客户资产-更新启用、禁用状态
     * @param reqDTO
     */
    void updateAuthStatus(AssetsAuthStatusUpdateReqDTO reqDTO);

    TgCustomerApplyAuth getApplyAuthNode(Long applyId);

    TgCustomerApplyAuth getArkbiAuthNode(Long arkbiId);

    Long getDataVolume(Long applicationId, String whereSql);
}
