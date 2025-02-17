package com.sinohealth.system.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.domain.TgDocInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IDocService {
    AjaxResult<?> query(Map<String, Object> params);

    AjaxResult<?> createOrUpdate(TgDocInfo docInfo);

    AjaxResult<?> delete(Long id);

    List<TgDocInfo> queryByDocIds(List<Long> docIds);

    TgDocInfo getById(Long relatedId);

    List<TgDocInfo> getUnLinkedData(List<Long> fileAssetIds);

    void savePdfPath(String bizId, String pdfPath);

    AjaxResult<FileAssetsUploadDTO> uploadGetPath(MultipartFile file);

}
