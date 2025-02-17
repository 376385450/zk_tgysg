package com.sinohealth.system.domain.value.deliver.strategy;

import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.ckpg.CustomerCKProperties;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.datasource.CustomerApplyDataSource;
import com.sinohealth.system.domain.value.deliver.resource.CsvResource;
import com.sinohealth.system.domain.value.deliver.util.HiddenFieldUtils;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.mapper.OutsideClickhouseMapper;
import com.sinohealth.system.mapper.TgCkStreamDao;
import com.sinohealth.system.monitor.event.EventReporterUtil;
import com.sinohealth.system.service.IApplicationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Objects;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:23
 */
@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerApplyCsvDeliverStrategyImpl extends AbstractCustomerApplyDeliverStrategy<CustomerApplyDataSource, CsvResource> {

    private final IApplicationService applicationService;

    private final OutsideClickhouseMapper outsideClickhouseMapper;

    private final TgTableApplicationMappingInfoDAO tgTableApplicationMappingInfoDAO;

    private final TgCkStreamDao tgCkStreamDao;

    private final CustomerCKProperties customerCKProperties;

    @Override
    public CsvResource deliver(CustomerApplyDataSource dataSource) throws Exception {
        final Long assetsId = dataSource.getAssetsId();
        final GetDataInfoRequestDTO requestDTO = dataSource.getRequestDTO();
        Writer writer = null;
        ICsvListWriter csvWriter = null;
        try {
            UserDataAssets assets = UserDataAssets.newInstance().selectById(assetsId);
            if (Objects.nonNull(assets.getBaseTableId())) {
                // 客户表单下载次数-埋点
                EventReporterUtil.operateLogEvent4View(assets.getBaseTableId().toString(),
                        assets.getBaseTableName(), SecondSubjectTypeEnum.CUSTOMER_TABLE_DOWNLOAD_VIEW, null);
            }
            // 客户提数下载次数-埋点
            EventReporterUtil.operateLogEvent4View(assets.getId().toString(), assets.getProjectName(),
                    SecondSubjectTypeEnum.CUSTOMER_APPLY_DOWNLOAD_VIEW, null);

            // > 构建csv文件
            String fileName = getFileName(assets);
            DiskFile diskFile = DiskFile.createTmpFile(fileName + ".csv");
            writer = new PrintWriter(Files.newOutputStream(diskFile.getFile().toPath()));
            csvWriter = getCsvListWriter(writer);

            Pair<String[], String[]> headersPair = getHeaders(assetsId);
            csvWriter.writeHeader(headersPair.getKey());

            String whereSql = getWhereSql(applicationService, requestDTO);


            ICsvListWriter finalCsvWriter = csvWriter;
            TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(assets.getId());
            String tableName = mappingInfo.getDataTableName();

            final int QUERY_SIZE = 10000;
            tgCkStreamDao.fetchCustomBatch(tableName, whereSql, QUERY_SIZE, v -> {
                List<Object> objects = HiddenFieldUtils.hiddenForCustomer(headersPair.getValue(), v, "");
                try {
                    for (Object object : objects) {
                        finalCsvWriter.write((List) object);
                    }
                    finalCsvWriter.flush();
                } catch (Exception e) {
                    log.error("", e);
                }
            });

//            int offset = 0;
//
//            long dataVolume = getDataVolume(dataSource, assets, whereSql);
//
//            while (offset < dataVolume) {
//                requestDTO.setPageSize(QUERY_SIZE);
//                requestDTO.setPageNum(offset / QUERY_SIZE + 1);
//                List<LinkedHashMap<String, Object>> dataMaps = getDataMaps(dataSource, assets, whereSql, requestDTO);
//
//                List<Object> objects = HiddenFieldUtils.hiddenForCustomer(headersPair.getValue(), dataMaps, "");
//                for (Object object : objects) {
//                    csvWriter.write((List) object);
//                }
////                for (LinkedHashMap<String, Object> d : dataMaps) {
////                    List<Object> line = new ArrayList<>(d.values());
////                    csvWriter.write(line);
////                }
//                csvWriter.flush();
//
//                offset += QUERY_SIZE;
//            }
//
//            csvWriter.close();
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

    private String getFileName(UserDataAssets applicationInfo) {
        return applicationInfo.getProjectName();
    }

    private String getWhereSql(IApplicationService applicationService, GetDataInfoRequestDTO requestDTO) {
        return applicationService.handleWhereSql(requestDTO);
    }

//    private Pair<String[], String[]> getHeaders(Long applyId) {
//        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.get(applyId);
//        if (Objects.isNull(mappingInfo)) {
//            throw new RuntimeException("数据同步失败");
//        }
//        List<AuthTableFieldDTO> fields = outsideClickhouseMapper.getFields(customerCKProperties.getDatabase(), mappingInfo.getDataTableName());
//        String[] aliasHeader = fields.stream()
//                .filter(HiddenFieldUtils.CUSTOMER_PREDICATE)
//                .map(a -> ObjectUtils.isNotNull(a.getFieldAlias()) ? a.getFieldAlias() : a.getFieldName())
//                .toArray(String[]::new);
//        String[] nameHeader = fields.stream()
//                .filter(HiddenFieldUtils.CUSTOMER_PREDICATE)
//                .map(AuthTableFieldDTO::getFieldName)
//                .toArray(String[]::new);
//        return Pair.of(aliasHeader, nameHeader);
//    }

    private List<LinkedHashMap<String, Object>> getDataMaps(CustomerApplyDataSource dataSource, UserDataAssets applicationInfo, String whereSql, GetDataInfoRequestDTO requestDTO) {
        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(applicationInfo.getId());
        String tableName = mappingInfo.getDataTableName();
        String selectDataSQL = outsideClickhouseMapper.buildSelectDataSQL(tableName, whereSql, requestDTO.getPageSize(), requestDTO.getOffset(), requestDTO.getSortBy(), requestDTO.getSortingField());
        return outsideClickhouseMapper.selectBySQL(selectDataSQL);
    }

    private Long getDataVolume(CustomerApplyDataSource dataSource, UserDataAssets applicationInfo, String whereSql) {
        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(applicationInfo.getId());
        String tableName = mappingInfo.getDataTableName();
        String countSQL = outsideClickhouseMapper.buildCountSQL(tableName, whereSql);
        return outsideClickhouseMapper.getDataCount(countSQL);
    }

    private ICsvListWriter getCsvListWriter(Writer writer) throws IOException {
        ICsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.STANDARD_PREFERENCE);
        return csvWriter;
    }

}
