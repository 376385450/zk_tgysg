package com.sinohealth.web.controller.dataasset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
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
import com.sinohealth.system.biz.dataassets.service.AssetsCompareService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 10:30
 */
@Api(tags = "资产新旧对比")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/compare"})
public class AssetsCompareController {

    @Resource
    private AssetsCompareService compareService;

    @GetMapping("/listProdCode")
    public AjaxResult<List<String>> queryAllProdCode() {
        return compareService.queryAllProdCode();
    }

    @PostMapping("/page")
    public AjaxResult<IPage<AssetsComparePageDTO>> pageQuery(
            @RequestBody @Validated AssetsComparePageRequest request) {
        return compareService.pageQueryCompare(request);
    }

    @PostMapping("/pageFile")
    public AjaxResult<IPage<AssetsCompareFilePageDTO>> pageQueryFileCompare(
            @RequestBody @Validated AssetsCompareFilePageRequest request) {
        return compareService.pageQueryCompareFile(request);
    }

    @GetMapping("/delete/{id}")
    public AjaxResult<Void> deleteCompare(@PathVariable("id") Long id) {
        return compareService.deleteCompare(id);
    }


    @GetMapping("/deleteFile/{id}")
    public AjaxResult<Void> deleteFileCompare(@PathVariable("id") Long id) {
        return compareService.deleteFileCompare(id);
    }

    /**
     * 同步式下载 单tab 单个或多个
     */
    @GetMapping("/download")
    public void download(@RequestParam(value = "ids", required = false) String ids,
                         @RequestParam(value = "type") String type, HttpServletResponse response) {
        compareService.download(ids, type, response);
    }

    /**
     * 跨tab 同步式 批量下载
     */
    @PostMapping("/download")
    public void download(@RequestBody AssetsCompareDownloadRequest request, HttpServletResponse response) {
        compareService.mergeDownload(request, response);
    }

    @PostMapping("/preview")
    public AjaxResult<Long> preview(@RequestBody AssetsCompareDownloadRequest request) {
        return compareService.preview(request);
    }

    @GetMapping("/lastSelect")
    public AjaxResult<AssetsCompareLastSelectDTO> lastSelect() {
        return compareService.lastSelect(SecurityUtils.getUserId());
    }

    @PostMapping("/pagePlan")
    public AjaxResult<IPage<AssetsComparePlanPageDTO>> pageQueryPlan(@RequestBody AssetsComparePlanRequest request) {
        return compareService.pageQueryPlan(request);
    }

    @PostMapping("/savePlan")
    public AjaxResult<Void> savePlan(@RequestBody AssetsPlanSaveRequest request) {
        return compareService.savePlan(request);
    }

    /**
     * 手动创建对比
     */
    @GetMapping("/createCompare")
    public AjaxResult<Void> createCompare(@RequestParam("assetsId") Long assetsId,
                                          @RequestParam("oldVersion") Integer preVersion,
                                          @RequestParam("newVersion") Integer curVersion) {
        return compareService.createCompare(assetsId, preVersion, curVersion, true);
    }

    /**
     * 上传对比文件
     */
    @PostMapping("/uploadFile")
    public AjaxResult<FileAssetsUploadDTO> uploadTmpFile(@RequestParam("file") MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        log.info("name={}", originalFilename);
        return compareService.uploadTmpFile(file);
    }

    @PostMapping("/createFileCompare")
    public AjaxResult<Void> createFileCompare(@RequestBody AssetsCompareFileRequest request) {
        return compareService.createFileCompare(request);
    }

    @GetMapping("/queryFileRunLog")
    public AjaxResult<String> queryFileRunLog(@RequestParam("id") Long id) {
        return AjaxResult.success("", compareService.queryFileRunLog(id));
    }

}
