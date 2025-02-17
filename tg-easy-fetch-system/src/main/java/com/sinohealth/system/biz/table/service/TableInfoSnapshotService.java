package com.sinohealth.system.biz.table.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.table.dto.TableDiffListRequest;
import com.sinohealth.system.biz.table.dto.TableDiffPageRequest;
import com.sinohealth.system.biz.table.dto.TableDiffPlanCreateOrUpdateRequest;
import com.sinohealth.system.biz.table.dto.TableDiffRequest;
import com.sinohealth.system.biz.table.dto.TablePushDetailPageRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotPageRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotPushRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotUpdateRequest;
import com.sinohealth.system.biz.table.vo.TableComparePlanVO;
import com.sinohealth.system.biz.table.vo.TableInfoCompareTaskVO;
import com.sinohealth.system.biz.table.vo.TableInfoSnapshotPageVO;
import com.sinohealth.system.biz.table.vo.TableSnapInfoVO;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn 2024-04-18 14:24
 */
public interface TableInfoSnapshotService {

    /**
     * 尚书台回调，迭代底表版本
     *
     * @param shardTable 分布式表
     */
    AjaxResult<Void> rollingTable(String shardTable);

    AjaxResult<Void> rollingTablePartly(String shardTable);

    AjaxResult<IPage<TableInfoSnapshotPageVO>> pageQuery(TableSnapshotPageRequest request);

    AjaxResult<Void> manualPushTable(TableSnapshotPushRequest request);

    /**
     * 需求更新 创建底表资产更新任务,只取最新版
     */
    AjaxResult<Void> pushTable(TableSnapshotPushRequest request);

    /**
     * 创建计划任务
     */
    AjaxResult<Void> planPush(TableSnapshotPushRequest request);

    AjaxResult<TableSnapInfoVO> queryTablePlanInfo(Long tableId);

    AjaxResult<Void> cancelPlanPush(Long planId);

    /**
     * 计算底表 版本差异
     */
    AjaxResult<Void> calculateDiff(TableDiffRequest request);

    /**
     * 删除
     */
    AjaxResult<Void> deleteSnapshot(Long id);

    /**
     * 当表进入SOP阶段后，删除QC阶段全部版本
     */
    void asyncDeleteQcVersionTable(Long tableId);

    /**
     * 编辑更新
     */
    AjaxResult<Void> edit(TableSnapshotUpdateRequest request);

    /**
     * 底表比对分页结果
     *
     * @param request 参数信息
     * @return 库表比对信息
     */
    AjaxResult<IPage<TableInfoCompareTaskVO>> diffPage(TableDiffPageRequest request);

    /**
     * 批量查询比对信息
     *
     * @param request 请求信息
     * @return 库表比对信息
     */
    AjaxResult<List<TableInfoCompareTaskVO>> diffList(TableDiffListRequest request);

    /**
     * 删除底表比对结果
     *
     * @param taskId 任务编号
     * @return 是否成功
     */
    AjaxResult<Void> deleteDiff(Long taskId);

    /**
     * 作废任务
     *
     * @param taskId 任务编号
     * @return 是否成功
     */
    AjaxResult<Void> failDiff(Long taskId);

    /**
     * 底表比对计划新增或更新
     *
     * @param request 请求参数
     * @return 是否成功
     */
    AjaxResult<Void> compareCreateOrUpdate(TableDiffPlanCreateOrUpdateRequest request);

    /**
     * 删除底表比对计划结果
     *
     * @param planId 任务编号
     * @return 是否成功
     */
    AjaxResult<Void> deleteComparePlan(Long planId);

    /**
     * 底表比对计划详情
     *
     * @param tableId 表编号
     * @return 底表比对计划详情
     */
    AjaxResult<TableComparePlanVO> comparePlanDetail(Long tableId);

    AjaxResult<List<FlowAssetsPageDTO>> pageQueryAssets(TablePushDetailPageRequest request);

    /**
     * 根据业务关联编号获取对应的资产升级记录
     *
     * @param bizIds 业务编号
     * @return 资产升级记录
     */
    List<AssetsWideUpgradeTrigger> queryByBizIds(List<Long> bizIds);
}
