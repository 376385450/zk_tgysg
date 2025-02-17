package com.sinohealth.system.domain.value.deliver.strategy;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.system.acl.ArkbiRepository;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.resource.ImageResource;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.ArkbiAnalysisService;
import com.sinohealth.system.util.ImgWatermarkUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
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
public class ChartAnalysisImageDeliverStrategyImpl implements ResourceDeliverStrategy<CharAnalysisDataSource, ImageResource> {

    private final ArkbiRepository arkbiRepository;

    private final ArkbiAnalysisService arkbiAnalysisService;

    @Override
    public ImageResource deliver(CharAnalysisDataSource dataSource) throws Exception {
        ArkbiAnalysis arkbiAnalysis = arkbiAnalysisService.getById(dataSource.getArkbiId());
        InputStream inputStream = arkbiRepository.getImage(arkbiAnalysis.getAnalysisId());
        DiskFile diskFile = DiskFile.createTmpFile(dataSource.getName() + ".png");
        IoUtil.copy(inputStream, new FileOutputStream(diskFile.getFile()));
        String watermark = ThreadContextHolder.getSysUser().getUserName();
        File watermarkImgFile = ImgWatermarkUtil.watermark(diskFile.getFile(), watermark);
        return new ImageResource(DiskFile.getExistsFile(watermarkImgFile.getAbsolutePath()));
    }

}
