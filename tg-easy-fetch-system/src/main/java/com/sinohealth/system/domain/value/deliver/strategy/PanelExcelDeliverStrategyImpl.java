package com.sinohealth.system.domain.value.deliver.strategy;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.system.acl.ArkbiRepository;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.domain.value.deliver.resource.ExcelResource;
import com.sinohealth.system.service.ArkbiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 20:25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PanelExcelDeliverStrategyImpl implements ResourceDeliverStrategy<PanelDataSource, ExcelResource> {

    private final ArkbiRepository arkbiRepository;

    private final ArkbiAnalysisService arkbiAnalysisService;

    @Override
    public ExcelResource deliver(PanelDataSource dataSource) throws Exception {
        ArkbiAnalysis arkbiAnalysis = arkbiAnalysisService.getById(dataSource.getArkbiId());
        InputStream inputStream = arkbiRepository.getExcel(arkbiAnalysis.getAnalysisId());
        DiskFile diskFile = DiskFile.createTmpFile(dataSource.getName() + ".xlsx");
        IoUtil.copy(inputStream, new FileOutputStream(diskFile.getFile()));
        return new ExcelResource(diskFile);
    }

}
