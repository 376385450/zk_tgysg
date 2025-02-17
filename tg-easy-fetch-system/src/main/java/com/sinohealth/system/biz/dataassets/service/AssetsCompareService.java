package com.sinohealth.system.biz.dataassets.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.dto.AssetsCompareFilePageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsCompareLastSelectDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsComparePageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsComparePlanPageDTO;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareDownloadRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareFilePageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareFileRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsComparePageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsComparePlanRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsPlanSaveRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn 2024-05-20 10:29
 */
public interface AssetsCompareService {

    AjaxResult<List<String>> queryAllProdCode();

    AjaxResult<IPage<AssetsComparePageDTO>> pageQueryCompare(AssetsComparePageRequest request);

    AjaxResult<IPage<AssetsCompareFilePageDTO>> pageQueryCompareFile(AssetsCompareFilePageRequest request);

    AjaxResult<IPage<AssetsComparePlanPageDTO>> pageQueryPlan(AssetsComparePlanRequest request);

    AjaxResult<Void> savePlan(AssetsPlanSaveRequest request);

    AjaxResult<Void> deleteCompare(Long id);

    AjaxResult<Void> deleteFileCompare(Long id);


    void download(String ids, String type, HttpServletResponse response);

    AjaxResult<Long> preview(AssetsCompareDownloadRequest request);

    AjaxResult<AssetsCompareLastSelectDTO> lastSelect(Long userId);

    void mergeDownload(AssetsCompareDownloadRequest request, HttpServletResponse response);

    /**
     * @param assetsId
     * @param preVersion
     * @param curVersion
     * @param invoke     页面手动执行
     */
    AjaxResult<Void> createCompare(Long assetsId, Integer preVersion, Integer curVersion, boolean invoke);

    AjaxResult<FileAssetsUploadDTO> uploadTmpFile(MultipartFile file);

    AjaxResult<Void> createFileCompare(AssetsCompareFileRequest request);

    String queryFileRunLog(Long id);

    /**
     * 根据业务编号，获取数据对比任务信息
     *
     * @param bizIds 业务编号
     * @return 数据对比任务信息
     */
    List<AssetsCompare> queryByBizIds(List<Long> bizIds);

}
