package com.sinohealth.system.domain.value.deliver.strategy;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.system.acl.ArkbiRepository;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.MultipartFileBuilder;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.domain.value.deliver.resource.PdfResource;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.ArkbiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 20:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PanelPdfDeliverStrategyImpl implements ResourceDeliverStrategy<PanelDataSource, PdfResource> {

    private final ArkbiRepository arkbiRepository;

    private final ArkbiAnalysisService arkbiAnalysisService;

    private final OfficeRepository officeRepository;

    @Override
    public PdfResource deliver(PanelDataSource dataSource) throws Exception {
        ArkbiAnalysis arkbiAnalysis = arkbiAnalysisService.getById(dataSource.getArkbiId());
        InputStream inputStream = arkbiRepository.getPdf(arkbiAnalysis.getAnalysisId());
        String pdfFileName = dataSource.getName() + ".pdf";
        // pdf添加水印
        String watermark = ThreadContextHolder.getSysUser().getUserName();
        InputStream watermarkInputStream = officeRepository.watermark2Bytes(MultipartFileBuilder.build(inputStream, pdfFileName), watermark);
        //
        DiskFile diskFile = DiskFile.createTmpFile(pdfFileName);
        IoUtil.copy(watermarkInputStream, new FileOutputStream(diskFile.getFile()));
        return new PdfResource(diskFile);
    }

}
