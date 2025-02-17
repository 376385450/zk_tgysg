package com.sinohealth.system.biz.dataassets.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.domain.UserFileAssets;
import com.sinohealth.system.biz.dataassets.dto.request.FileAssetsCreateRequest;

import java.util.Collection;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 15:14
 */
public interface UserFileAssetsService {

    /**
     * @return true 代表需要用户指定覆盖动作
     */
    AjaxResult<Void> createFileAssets(FileAssetsCreateRequest request);

    boolean existsFile(String filename, Long projectId);

    AjaxResult<Void> deleteById(Long id);

    List<UserFileAssets> queryAvailableAssets(Collection<Long> projectIds);

    void savePdfPath(String bizId, String pdfPath);

}
