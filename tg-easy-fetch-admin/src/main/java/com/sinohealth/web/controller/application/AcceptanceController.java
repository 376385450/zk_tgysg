package com.sinohealth.web.controller.application;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.dataassets.dto.AcceptanceRecordDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AcceptListRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AcceptRequest;
import com.sinohealth.system.biz.dataassets.service.AcceptanceRecordService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:16
 */
@Api(tags = {"验收管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping({"/acceptance","/api/acceptance"})
public class AcceptanceController {

    private final AcceptanceRecordService acceptanceRecordService;

    @PostMapping("/accept")
    public AjaxResult<Void> accept(@RequestBody @Validated AcceptRequest acceptRequest) {
        acceptRequest.setApplicantId(SecurityUtils.getUserId());
        return acceptanceRecordService.accept(acceptRequest);
    }

    @PostMapping("/list")
    public AjaxResult<List<AcceptanceRecordDTO>> list(@RequestBody AcceptListRequest request) {
        return acceptanceRecordService.list(request);
    }

}
