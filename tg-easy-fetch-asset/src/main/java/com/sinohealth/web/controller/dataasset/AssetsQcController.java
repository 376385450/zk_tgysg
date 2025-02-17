package com.sinohealth.web.controller.dataasset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.dto.AssetsQcPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsQcPageRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-17 15:24
 */
@Api(tags = "资产QC")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/qc"})
public class AssetsQcController {

    private final AssetsQcService assetsQcService;

    @PostMapping("/page")
    public AjaxResult<IPage<AssetsQcPageDTO>> pageQuery(@RequestBody AssetsQcPageRequest request) {
        return assetsQcService.pageQuery(request);
    }

    @GetMapping("/createQc")
    public AjaxResult<Void> createQc() {
        return assetsQcService.createAllQc(null);
    }
}
