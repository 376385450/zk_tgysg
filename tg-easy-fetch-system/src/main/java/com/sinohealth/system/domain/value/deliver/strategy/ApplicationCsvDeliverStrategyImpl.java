package com.sinohealth.system.domain.value.deliver.strategy;

import com.alibaba.excel.util.BooleanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.resource.CsvResource;
import com.sinohealth.system.domain.value.deliver.util.HiddenFieldUtils;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgCkStreamDao;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationCsvDeliverStrategyImpl implements ResourceDeliverStrategy<ApplicationDataSource, CsvResource> {

    private final IApplicationService applicationService;
    private final TgApplicationInfoMapper applicationInfoMapper;
    private final TgCkProviderMapper tgCkProviderMapper;
    private final ITableInfoService tableInfoService;

    private final TgCkStreamDao tgCkStreamDao;

    @Override
    public CsvResource deliver(ApplicationDataSource dataSource) throws Exception {
        final Long assetsId = dataSource.getAssetsId();
        final DataPreviewRequest requestDTO = dataSource.getRequestDTO();
        Writer writer = null;
        ICsvListWriter csvWriter = null;
        try {
            UserDataAssets assets = UserDataAssets.newInstance().selectById(assetsId);
            AjaxResult<ApplicationDataDto> dataResult = applicationService.queryAssetsDataFromCk(assetsId, requestDTO);
            if (!dataResult.isSuccess()) {
                log.error("{}", dataResult);
                throw new RuntimeException("无效的数据");
            }

            ApplicationDataDto applicationDataFromCk = dataResult.getData();

            // > 构建csv文件
            String fileName = getFileName(assets);
            DiskFile diskFile = DiskFile.createTmpFile(fileName + ".csv");
            writer = new PrintWriter(Files.newOutputStream(diskFile.getFile().toPath()));
            byte[] bs = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            writer.write(new String(bs));
            csvWriter = getCsvListWriter(writer);

            List<Long> tableIds = applicationDataFromCk.getHeader().stream().map(ApplicationDataDto.Header::getTableId)
                    .distinct().collect(Collectors.toList());
            List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
            Map<Long, String> aliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId,
                    TableInfo::getTableAlias, (front, current) -> current));
            String[] csvHeader = getHeaders(applicationDataFromCk, aliasMap);

            String[] headerFieldNames = getHeaderFieldNames(applicationDataFromCk);

            TgApplicationInfo apply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                    .select(TgApplicationInfo::getExportProjectName)
                    .eq(TgApplicationInfo::getId, assets.getSrcApplicationId()));
            boolean needExportName = BooleanUtils.isTrue(apply.getExportProjectName());

            List<String> heads = Stream.of(csvHeader).collect(Collectors.toList());
            if (needExportName) {
                heads.add(0, ApplicationSqlUtil.PROJECT_NAME_VAL);
            }
            csvWriter.writeHeader(heads.toArray(new String[]{}));

            String whereSql = getWhereSql(applicationService, requestDTO);
            final int QUERY_SIZE = 5000;

            ICsvListWriter finalCsvWriter = csvWriter;
            tgCkStreamDao.fetchBatch(assets.getAssetTableName(), assets.getAssetsSql(), whereSql, QUERY_SIZE, v -> {
                List<List<Object>> objects = HiddenFieldUtils.matchLinesWithProject(headerFieldNames, v, "",
                        needExportName, assets.getProjectName());
                try {
                    for (List<Object> object : objects) {
                        finalCsvWriter.write(object);
                    }
                    finalCsvWriter.flush();
                } catch (Exception e) {
                    log.error("", e);
                }
            });

            return new CsvResource(diskFile);
        } catch (Exception e) {
            log.error("异常", e);
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException ioException) {
                    log.error("", ioException);
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioException) {
                    log.error("", ioException);
                }
            }
            throw e;
        }
    }

    private String getFileName(UserDataAssets assets) {
        return assets.getId() + "_V" + assets.getVersion();
//        return assets.getProjectName();
    }

    private String getWhereSql(IApplicationService applicationService, GetDataInfoRequestDTO requestDTO) {
        return applicationService.handleWhereSql(requestDTO);
    }

    private List<LinkedHashMap<String, Object>> getDataMaps(ApplicationDataSource dataSource, UserDataAssets applicationInfo, String whereSql, GetDataInfoRequestDTO requestDTO) {
        return tgCkProviderMapper.selectApplicationDataFromCk(applicationInfo.getAssetsSql(), whereSql, requestDTO);
    }

    private Long getDataVolume(ApplicationDataSource dataSource, UserDataAssets applicationInfo, String whereSql) {
        return tgCkProviderMapper.selectCountApplicationDataFromCk(applicationInfo.getAssetsSql(), Optional.ofNullable(whereSql).orElse(""));
    }

    private ICsvListWriter getCsvListWriter(Writer writer) throws IOException {
        ICsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        return csvWriter;
    }

    private String[] getHeaderFieldNames(ApplicationDataDto applicationDataFromCk) {
        return applicationDataFromCk.getHeader().stream().map(ApplicationDataDto.Header::getFiledName)
                .filter(HiddenFieldUtils.APPLY_PREDICATE)
                .toArray(String[]::new);
    }
}
