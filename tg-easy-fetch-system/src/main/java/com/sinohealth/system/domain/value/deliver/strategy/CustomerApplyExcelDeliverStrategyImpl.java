package com.sinohealth.system.domain.value.deliver.strategy;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.google.common.collect.Lists;
import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dict.util.ExcelUtil;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.TgDataDescription;
import com.sinohealth.system.domain.ckpg.CustomerCKProperties;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.datasource.CustomerApplyDataSource;
import com.sinohealth.system.domain.value.deliver.resource.ExcelResource;
import com.sinohealth.system.domain.value.deliver.util.HiddenFieldUtils;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.mapper.OutsideClickhouseMapper;
import com.sinohealth.system.mapper.TgCkStreamDao;
import com.sinohealth.system.monitor.event.EventReporterUtil;
import com.sinohealth.system.service.DataDescriptionService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.util.EasyExcelUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:23
 */
@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerApplyExcelDeliverStrategyImpl extends AbstractCustomerApplyDeliverStrategy<CustomerApplyDataSource, ExcelResource> {

    private final IApplicationService applicationService;

    private final OutsideClickhouseMapper outsideClickhouseMapper;

    private final TgTableApplicationMappingInfoDAO tgTableApplicationMappingInfoDAO;

    private final DataDescriptionService dataDescriptionService;

    private final TgCkStreamDao tgCkStreamDao;

    private final CustomerCKProperties customerCKProperties;

    /**
     * 使用文件流
     *
     * @param dataSource 数据来源
     * @return
     * @throws Exception
     */
    @Override
    public ExcelResource deliver(CustomerApplyDataSource dataSource) throws Exception {
        final Long assetsId = dataSource.getAssetsId();
        // 客户下载次数-埋点
        FileOutputStream fileOutputStream = null;
        ExcelWriter excelWriter = null;
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
            String fileName = getFileName(assets);
            // 文件名
            DiskFile diskFile = DiskFile.createTmpFile(fileName + ".xlsx");
            fileOutputStream = new FileOutputStream(diskFile.getFile());
            excelWriter = EasyExcelUtil.appendConfig(EasyExcel.write(fileOutputStream));
            // 表单数据sheet
            writeFormDataSheet(dataSource, assets, excelWriter);
            // 数据说明文档sheet
            writeDescriptionSheet(dataSource, excelWriter);
            return new ExcelResource(diskFile);
        } catch (Exception e) {
            log.error("异常", e);
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ioException) {
                    log.error("", ioException);
                }
            }
            throw e;
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 表单数据sheet
     */
    private void writeFormDataSheet(CustomerApplyDataSource dataSource, UserDataAssets assets, ExcelWriter excelWriter) {
        final Long assetsId = dataSource.getAssetsId();
        final GetDataInfoRequestDTO requestDTO = dataSource.getRequestDTO();
        String sheetName = getFileName(assets);
        Pair<String[], String[]> headersPair = super.getHeaders(assetsId);
        WriteSheet sheet = EasyExcel.writerSheet(0, sheetName).head(ExcelUtil.head(headersPair.getKey())).useDefaultStyle(false).build();
        String whereSql = getWhereSql(requestDTO);

        final int QUERY_SIZE = 10000;
        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(assets.getId());
        String tableName = mappingInfo.getDataTableName();

        tgCkStreamDao.fetchCustomBatch(tableName, whereSql, QUERY_SIZE, v -> {
            List<List<Object>> objects = HiddenFieldUtils.matchLines(headersPair.getValue(), v, "");
            try {
                excelWriter.write(objects, sheet);
            } catch (Exception e) {
                log.error("", e);
            }
        });

//        long dataVolume = getDataVolume(dataSource, assets, whereSql);
//        int offset = 0;
//        while (offset < dataVolume) {
//            List<Object> lines = getContentLines(dataSource, requestDTO, assets, whereSql, QUERY_SIZE, headersPair.getValue(), offset);
//            excelWriter.write(lines, sheet);
//            offset += QUERY_SIZE;
//        }
    }

    private void writeDescriptionSheet(CustomerApplyDataSource dataSource, ExcelWriter excelWriter) throws Exception {
        TgDataDescription description = dataDescriptionService.getByAssetsId(dataSource.getAssetsId());
        if (Objects.isNull(description)) {
            return;
        }

        String sheetName = description.getDocName() + "-数据说明文档";
        String[] headers1 = new String[]{"数据说明", "数据说明"};
        WriteSheet sheet = EasyExcel.writerSheet(1, sheetName).useDefaultStyle(false).build();
        // 数据说明表格
        WriteTable dataDescriptionTable = EasyExcel.writerTable(0).head(ExcelUtil.head(headers1)).automaticMergeHead(true)
                .useDefaultStyle(false)
                .build();
        List<List<String>> descLines = description.getDataDesc().getList().stream()
                .map(quota -> Lists.newArrayList(quota.getKey(), quota.getValue())).collect(Collectors.toList());
        excelWriter.write(descLines, sheet, dataDescriptionTable);

        // 基础指标表格
        String[] headers2 = new String[]{"基础指标", "基础指标"};
        WriteTable baseTargetTable = EasyExcel.writerTable(1).head(ExcelUtil.head(headers2)).automaticMergeHead(true)
                .useDefaultStyle(false)
                .build();
        List<ArrayList<String>> quotaLines = description.getBaseTarget().getList().stream().map(quota -> {
            return Lists.newArrayList(quota.getKey(), quota.getValue());
        }).collect(Collectors.toList());
        excelWriter.write(quotaLines, sheet, baseTargetTable);
    }


    private String getFileName(UserDataAssets applicationInfo) {
        String fileName = applicationInfo.getProjectName();
        return fileName;
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

    private String getWhereSql(GetDataInfoRequestDTO requestDTO) {
        return applicationService.handleWhereSql(requestDTO);
    }


    private void writeContent(ExcelWriterSheetBuilder sheet, List<Object> lines) {
        log.info("----------------写入Excel信息-------------------");
        sheet.doWrite(lines);
        log.info("已写入行数: " + lines.size());
    }

    private ExcelWriterSheetBuilder getSheet(OutputStream outputStream, String sheetName, String[] headers) throws IOException {
        return EasyExcel.write(outputStream).head(ExcelUtil.head(headers)).sheet(sheetName);
    }


    private List<Object> getContentLines(CustomerApplyDataSource dataSource, GetDataInfoRequestDTO requestDTO,
                                         UserDataAssets applicationInfo, String whereSql, int QUERY_SIZE,
                                         String[] fieldHeader, int offset) {
        requestDTO.setPageSize(QUERY_SIZE);
        requestDTO.setPageNum(offset / QUERY_SIZE + 1);
        List<LinkedHashMap<String, Object>> dataMaps = getDataMaps(dataSource, applicationInfo, whereSql, requestDTO);
        // 因为有类型，需要传null否则会有类型转换错误
        return HiddenFieldUtils.hiddenForCustomer(fieldHeader, dataMaps, null);
    }


    private List<LinkedHashMap<String, Object>> getDataMaps(CustomerApplyDataSource dataSource,
                                                            UserDataAssets applicationInfo, String whereSql, GetDataInfoRequestDTO requestDTO) {
        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(applicationInfo.getId());
        String tableName = mappingInfo.getDataTableName();
        String selectDataSQL = outsideClickhouseMapper.buildSelectDataSQL(tableName, whereSql, requestDTO.getPageSize(),
                requestDTO.getOffset(), requestDTO.getSortBy(), requestDTO.getSortingField());
        return outsideClickhouseMapper.selectBySQL(selectDataSQL);
    }

    private Long getDataVolume(CustomerApplyDataSource dataSource, UserDataAssets applicationInfo, String whereSql) {
        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(applicationInfo.getId());
        String tableName = mappingInfo.getDataTableName();
        String countSQL = outsideClickhouseMapper.buildCountSQL(tableName, whereSql);
        return outsideClickhouseMapper.getDataCount(countSQL);
    }

}

