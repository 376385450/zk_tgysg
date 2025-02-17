package com.sinohealth.web.controller.pdf;

import com.sinohealth.common.enums.pdf.BizType;
import com.sinohealth.saas.office.api.PdfCallBackApi;
import com.sinohealth.saas.office.model.dto.PdfConvertDTO;
import com.sinohealth.system.biz.dataassets.service.UserFileAssetsService;
import com.sinohealth.system.service.IDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author shallwetalk
 * @Date 2023/10/19
 */
@RestController
@RequestMapping("/api/pdfCallBack")
@Slf4j
public class PdfCallBackController implements PdfCallBackApi {

    @Autowired
    private IDocService iDocService;
    @Autowired
    private UserFileAssetsService userFileAssetsService;

    @Override
    @PostMapping("/convertCallBack")
    public void convertCallBack(@RequestBody PdfConvertDTO pdfConvertDTO) {
        final String bizType = pdfConvertDTO.getBizType();
//        log.info("收到pdf保存回调:{}", JSON.toJSONString(pdfConvertDTO));
        if (bizType.equals(BizType.DOC_CREATE.name())) {
            iDocService.savePdfPath(pdfConvertDTO.getBizId(), pdfConvertDTO.getDestUrl());
        } else {
            userFileAssetsService.savePdfPath(pdfConvertDTO.getBizId(), pdfConvertDTO.getDestUrl());
        }
    }

}
