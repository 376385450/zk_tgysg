package com.sinohealth.system.domain.value.deliver.strategy;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.system.acl.ArkbiRepository;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.resource.CsvResource;
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
 * @date 2022-11-28 20:21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChartAnalysisCsvDeliverStrategyImpl implements ResourceDeliverStrategy<CharAnalysisDataSource, CsvResource> {

    private final ArkbiRepository arkbiRepository;

    private final ArkbiAnalysisService arkbiAnalysisService;

    @Override
    public CsvResource deliver(CharAnalysisDataSource dataSource) throws Exception {
        ArkbiAnalysis arkbiAnalysis = arkbiAnalysisService.getById(dataSource.getArkbiId());
        InputStream inputStream = arkbiRepository.getCsv(arkbiAnalysis.getAnalysisId());
        DiskFile diskFile = DiskFile.createTmpFile(dataSource.getName() + ".csv");
        IoUtil.copy(inputStream, new FileOutputStream(diskFile.getFile()));
        return new CsvResource(diskFile);
    }

}
