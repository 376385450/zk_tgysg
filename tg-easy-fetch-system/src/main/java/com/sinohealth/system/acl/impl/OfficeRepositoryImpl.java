package com.sinohealth.system.acl.impl;

import com.alibaba.fastjson.JSON;
import com.sinohealth.common.enums.pdf.BizType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.saas.office.api.PdfApi;
import com.sinohealth.saas.office.model.OfficeResponse;
import com.sinohealth.saas.office.model.dto.PdfConvertDTO;
import com.sinohealth.saas.office.model.dto.PdfWatermarkDTO;
import com.sinohealth.saas.office.model.dto.ServiceCallBackDTO;
import com.sinohealth.saas.office.model.param.PdfAddWatermarkFromFileParam;
import com.sinohealth.saas.office.model.param.PdfAddWatermarkFromUrlParam;
import com.sinohealth.saas.office.model.param.PdfConvertFromFileParam;
import com.sinohealth.saas.office.model.param.PdfConvertFromUrlAsyncParam;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.config.FileProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-10 10:15 下午
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfficeRepositoryImpl implements OfficeRepository {

    private final PdfApi pdfApi;

    private final FileProperties fileProperties;

    @Value("${sinohealth.file.appName:}")
    private String appName;

    @Value("${sinohealth.file.callBackUrl:}")
    private String callBackUrl;

    @Override
    public String watermark2Url(MultipartFile multipartFile, String watermark) {
        PdfAddWatermarkFromFileParam param = PdfAddWatermarkFromFileParam.builder()
                .watermark("中康" + watermark)
                .appName(appName)
                .build();
        OfficeResponse<PdfWatermarkDTO> response = pdfApi.watermark2Url(multipartFile, JSON.toJSONString(param));
        if (response.isSuccess()) {
            return response.getData().getDestUrl();
        } else {
            log.error("response: {}", response);
            throw new CustomException("添加水印失败");
        }
    }

    @Override
    public InputStream watermark2Bytes(MultipartFile multipartFile, String watermark) {
        PdfAddWatermarkFromFileParam param = PdfAddWatermarkFromFileParam.builder()
                .watermark("中康" + watermark)
                .appName(appName)
                .build();
        OfficeResponse<byte[]> officeResponse = pdfApi.watermark2Bytes(multipartFile, JSON.toJSONString(param));
        if (officeResponse.isSuccess()) {
            return new ByteArrayInputStream(officeResponse.getData());
        } else {
            log.error("response: {}", officeResponse);
            throw new CustomException("添加水印失败");
        }
    }

    @Override
    public String watermark(String sourcePdfUrl, String watermark) {
        PdfAddWatermarkFromUrlParam param = PdfAddWatermarkFromUrlParam.builder()
                .sourceUrl(sourcePdfUrl)
                .watermark("中康" + watermark)
                .appName(appName)
                .build();
        OfficeResponse<PdfWatermarkDTO> response = pdfApi.watermark2Url(param);
        if (response.isSuccess()) {
            return response.getData().getDestUrl();
        } else {
            log.error("response: {}", response);
            throw new CustomException("添加水印失败");
        }
    }

    @Override
    public String transformPdf(MultipartFile multipartFile) {
        PdfConvertFromFileParam param = PdfConvertFromFileParam.builder().appName(appName).build();
        OfficeResponse<PdfConvertDTO> response = pdfApi.convert2PdfUrl(multipartFile, JSON.toJSONString(param));
        if (response.isSuccess()) {
            return response.getData().getDestUrl();
        } else {
            log.error("response: {}", response);
            throw new CustomException("转换pdf失败");
        }
    }

    @Override
    public void transformPdfAsync(String filePath, BizType bizType, String bizId) {
        final PdfConvertFromUrlAsyncParam param = new PdfConvertFromUrlAsyncParam();
        param.setAppName(appName);
        param.setSourceUrl(filePath);
        param.setFileStorageCode(fileProperties.getFileStorageCode());
        final ServiceCallBackDTO serviceCallBackDTO = new ServiceCallBackDTO();
        serviceCallBackDTO.setBizId(bizId);
        serviceCallBackDTO.setBizType(bizType.name());
        serviceCallBackDTO.setCallbackUrl(callBackUrl);
        param.setCallback(serviceCallBackDTO);
        log.info("请求异步转换PDF:{}", JSON.toJSONString(param));
        pdfApi.convert2PdfUrlAsync(param);
    }

}
