package com.sinohealth.system.service;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.label.TgAssetLabelRelation;
import com.sinohealth.system.domain.label.TgLabelInfo;
import com.sinohealth.system.dto.label.AddLabelRequest;
import com.sinohealth.system.dto.label.DeleteLabelRequest;
import com.sinohealth.system.dto.label.PageQueryLabelRequest;
import com.sinohealth.system.dto.label.UpdateLabelRequest;
import com.sinohealth.system.vo.TgLabelInfoVo;

import java.util.List;
import java.util.Map;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:47
 */
public interface ILabelService {

    /**
     * 分页查询
     *
     * @param queryLabelRequest
     * @return
     */
    AjaxResult<PageInfo<TgLabelInfoVo>> pageQuery(PageQueryLabelRequest queryLabelRequest) throws InterruptedException;

    /**
     * 新增标签
     *
     * @param addLabelRequest
     * @return
     */
    AjaxResult<Object> addLabel(AddLabelRequest addLabelRequest);

    /**
     * 更新标签
     *
     * @param updateLabelRequest
     * @return
     */
    AjaxResult<Object> updateLabel(UpdateLabelRequest updateLabelRequest);

    /**
     * 删除标签
     *
     * @param deleteLabelRequest
     * @return
     */
    AjaxResult<Object> deleteLabel(DeleteLabelRequest deleteLabelRequest);

    /**
     * 更新资产和标签的关联关系
     *
     * @param tgAssetInfo
     */
    void updateLabelRelation(TgAssetInfo tgAssetInfo);

    /**
     * 根据资产ID获取标签集合
     *
     * @param assetId
     * @return
     */
    List<String> getLabels(Long assetId);

    List<Integer> searchLabelRelate(String labels);

    Map<Long,List<TgLabelInfo>> getFullLabels(List<Long> assetId);
}
