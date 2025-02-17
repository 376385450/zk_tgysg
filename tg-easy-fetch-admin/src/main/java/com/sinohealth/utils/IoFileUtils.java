//package com.sinohealth.utils;
//
//import com.alibaba.excel.EasyExcel;
//import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
//import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
//import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
//import com.sinohealth.common.utils.TgCollectionUtils;
//import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
//import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
//import com.sinohealth.system.biz.dict.util.ExcelUtil;
//import com.sinohealth.system.dto.ApplicationDataDto;
//import com.sinohealth.system.dto.GetDataInfoRequestDTO;
//import com.sinohealth.system.mapper.TgCkProviderMapper;
//import com.sinohealth.system.monitor.event.EventReporterUtil;
//import com.sinohealth.system.service.IApplicationService;
//import lombok.extern.slf4j.Slf4j;
//import org.supercsv.io.CsvListWriter;
//import org.supercsv.io.ICsvListWriter;
//import org.supercsv.prefs.CsvPreference;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * @Author Rudolph
// * @Date 2022-10-26 11:52
// * @Desc
// */
//@Slf4j
//public class IoFileUtils {
//
//
//    public static void exportExcel(Long assetsId, DataPreviewRequest requestDTO, HttpServletResponse response,
//                                   IApplicationService applicationService, TgCkProviderMapper tgCkProviderMapper) throws IOException {
//        try {
//            UserDataAssets assets = new UserDataAssets().selectById(assetsId);
//            ApplicationDataDto applicationDataFromCk = applicationService.queryAssetsDataFromCk(assetsId, requestDTO).getData();
//            // 客户下载次数-埋点
//            EventReporterUtil.operateLogEvent4View(assets.getBaseTableId().toString(), assets.getBaseTableName(),
//                    SecondSubjectTypeEnum.CUSTOMER_TABLE_DOWNLOAD_VIEW, null);
//
//            String fileName = getFileName(response, assets);
//            String[] headers = getHeaders(applicationDataFromCk);
//            ExcelWriterSheetBuilder sheet = getSheet(response, fileName, headers);
//            String whereSql = getWhereSql(applicationService, requestDTO);
//            long dataVolume = getDataVolume(tgCkProviderMapper, assets);
//
//            final int QUERY_SIZE = 100000;
//            int offset = 0;
//
//            while (offset < dataVolume) {
//                List<Object> lines = getContentLines(requestDTO, tgCkProviderMapper, assets, whereSql, QUERY_SIZE, offset);
//                writeContent(sheet, lines);
//                offset += QUERY_SIZE;
//            }
//
//        } catch (Exception e) {
//            log.error("异常", e);
//        }
//    }
//
//    /**
//     * 我的数据-数据下载（复合筛选）
//     */
//    public static void exportCsv(Long assetsId, DataPreviewRequest requestDTO, HttpServletResponse response,
//                                 IApplicationService applicationService, TgCkProviderMapper tgCkProviderMapper) throws IOException {
//
//        UserDataAssets assets = new UserDataAssets().selectById(assetsId);
//        ApplicationDataDto applicationDataFromCk = applicationService.queryAssetsDataFromCk(assetsId, requestDTO).getData();
//
//        // 客户下载次数-埋点
//        EventReporterUtil.operateLogEvent4View(assets.getBaseTableId().toString(), assets.getBaseTableName(),
//                SecondSubjectTypeEnum.CUSTOMER_TABLE_DOWNLOAD_VIEW, null);
//
//        String tableName = assets.getProjectName();
//        ICsvListWriter csvWriter = getCsvListWriter(response, tableName);
//        String[] csvHeader = applicationDataFromCk.getHeader().stream()
//                .map(ApplicationDataDto.Header::getFiledName).toArray(String[]::new);
//        csvWriter.writeHeader(csvHeader);
//
//        String whereSql = getWhereSql(applicationService, requestDTO);
//
//        int offset = 0;
//        final int QUERY_SIZE = 100000;
//        long dataVolume = getDataVolume(tgCkProviderMapper, assets);
//
//        while (offset < dataVolume) {
//            requestDTO.setPageSize(QUERY_SIZE);
//            requestDTO.setPageNum(offset / QUERY_SIZE + 1);
//            List<LinkedHashMap<String, Object>> dataMaps = getDataMaps(tgCkProviderMapper, assets, whereSql, requestDTO);
//
//            for (LinkedHashMap<String, Object> d : dataMaps) {
//                List<Object> line = new ArrayList<>(d.values());
//                csvWriter.write(line);
//            }
//            csvWriter.flush();
//
//            offset += QUERY_SIZE;
//        }
//
//        csvWriter.close();
//    }
//
//
//    //设置导出的数据内容
//    private static List<List<Object>> dataList(List<Map<String, Object>> dataList, String[] dataStrMap) {
//        List<List<Object>> list = new ArrayList<>();
//        for (Map<String, Object> map : dataList) {
//            List<Object> data = new ArrayList<>();
//            for (String s : dataStrMap) {
//                data.add(map.get(s));
//            }
//            list.add(data);
//        }
//        return list;
//    }
//
//    private static void writeContent(ExcelWriterSheetBuilder sheet, List<Object> lines) {
//        log.info("----------------写入Excel信息-------------------");
//        sheet.doWrite(lines);
//        log.info("已写入行数: " + lines.size());
//    }
//
//    private static List<Object> getContentLines(GetDataInfoRequestDTO requestDTO, TgCkProviderMapper tgCkProviderMapper,
//                                                UserDataAssets applicationInfo, String whereSql, int QUERY_SIZE, int offset) {
//        requestDTO.setPageSize(QUERY_SIZE);
//        requestDTO.setPageNum(offset / QUERY_SIZE + 1);
//        List<LinkedHashMap<String, Object>> dataMaps = getDataMaps(tgCkProviderMapper, applicationInfo, whereSql, requestDTO);
//        List<Object> lines = TgCollectionUtils.newArrayList();
//        for (LinkedHashMap<String, Object> d : dataMaps) {
////            List<String> line = d.values().stream().map(Object::toString).collect(Collectors.toList());
//            List<String> line = d.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().substring(0, 2)))
//                    .map(e -> e.getValue().toString()).collect(Collectors.toList());
//            lines.add(line);
//        }
//        return lines;
//    }
//
//    private static List<LinkedHashMap<String, Object>> getDataMaps(TgCkProviderMapper tgCkProviderMapper,
//                                                                   UserDataAssets applicationInfo, String whereSql, GetDataInfoRequestDTO requestDTO) {
//        return tgCkProviderMapper.selectApplicationDataFromCk(applicationInfo.getAssetsSql(), whereSql, requestDTO);
//    }
//
//    private static Long getDataVolume(TgCkProviderMapper tgCkProviderMapper, UserDataAssets applicationInfo) {
//        return tgCkProviderMapper.selectCountApplicationDataFromCk(applicationInfo.getAssetsSql(), "");
//    }
//
//    private static String getWhereSql(IApplicationService applicationService, GetDataInfoRequestDTO requestDTO) {
//        return applicationService.handleWhereSql(requestDTO);
//    }
//
//    private static ExcelWriterSheetBuilder getSheet(HttpServletResponse response, String fileName, String[] headers) throws IOException {
//        return EasyExcel.write(response.getOutputStream()).head(ExcelUtil.head(headers)).sheet(fileName);
//    }
//
//    private static String[] getHeaders(ApplicationDataDto applicationDataFromCk) {
//        return applicationDataFromCk.getHeader().stream().map(a -> ObjectUtils.isNotNull(a.getFiledAlias()) ? a.getFiledAlias() : a.getFiledName()).toArray(String[]::new);
//    }
//
//    private static String getFileName(HttpServletResponse response, UserDataAssets applicationInfo) {
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        String fileName = applicationInfo.getProjectName();
//        response.setHeader("Content-disposition", "attachment;filename=''" + fileName + ".xlsx");
//        return fileName;
//    }
//
//    private static ICsvListWriter getCsvListWriter(HttpServletResponse response, String tableName) throws IOException {
//        ICsvListWriter csvWriter = getListWriter(response, "attachment; filename=数据申请-", tableName);
//        return csvWriter;
//    }
//
//    private static ICsvListWriter getListWriter(HttpServletResponse response, String prefix, String tableName) throws IOException {
//        response.setContentType("text/csv");
//        response.setCharacterEncoding("GBK");
//        String headerKey = "Content-Disposition";
//        String headerValue = prefix + tableName + ".csv";
//        response.setHeader(headerKey, headerValue);
//        ICsvListWriter csvWriter = new CsvListWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
//        return csvWriter;
//    }
//}
