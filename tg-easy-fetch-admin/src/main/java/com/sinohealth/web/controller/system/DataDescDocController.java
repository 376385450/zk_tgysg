package com.sinohealth.web.controller.system;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.DataDescDocDTO;
import com.sinohealth.system.dto.DataDescDocUpdateReqDTO;
import com.sinohealth.system.service.DataDescriptionService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-06 15:25
 */
@Api("数据说明文档管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/table_management/application/dataDescDoc")
public class DataDescDocController {

    private final DataDescriptionService dataDescriptionService;

    @PostMapping("/update")
    public AjaxResult<Integer> updateDataDescDoc(@Valid @RequestBody DataDescDocUpdateReqDTO reqDTO) {
        Integer descId = dataDescriptionService.update(reqDTO);
        return AjaxResult.success(descId);
    }

    @PostMapping("/detail/{assetsId}")
    public AjaxResult<DataDescDocDTO> getDetail(@PathVariable("assetsId") Long assetsId) {
        DataDescDocDTO detail = dataDescriptionService.getDetail(assetsId);
        return AjaxResult.success(detail);
    }

}
