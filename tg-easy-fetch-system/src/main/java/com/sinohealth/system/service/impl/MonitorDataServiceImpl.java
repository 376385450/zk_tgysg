package com.sinohealth.system.service.impl;

import com.sinohealth.system.dto.GetTableMonitorDataRequestDTO;
import com.sinohealth.system.dto.TableMonitorDataDTO;
import com.sinohealth.system.monitor.mapper.MonitorDataMapper;
import com.sinohealth.system.service.IMonitorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-14 9:16 上午
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorDataServiceImpl implements IMonitorDataService {

    private final MonitorDataMapper dataMapper;

    private final int BEFORE_DAYS = 30;

    @Override
    public TableMonitorDataDTO getData(GetTableMonitorDataRequestDTO requestDTO) {
        TableMonitorDataDTO dataDTO = new TableMonitorDataDTO()
                .setTableData(this.buildTableData(requestDTO))
                .setApplyData(this.buildApplyData(requestDTO))
                .setCustomerData(this.buildCustomerData(requestDTO));
        return dataDTO;
    }

    /**
     * 本表监控数据
     * @param requestDTO
     * @return
     */
    private TableMonitorDataDTO.TableMonitorTableData buildTableData(GetTableMonitorDataRequestDTO requestDTO) {
        final Long tableId = requestDTO.getTableId();
        Integer tableViewPv = dataMapper.getTableViewPv(tableId);
        Integer tableViewUv = dataMapper.getTableViewUv(tableId);
        List<Map<String, Object>> tableViewTrend = dataMapper.getTableViewTrend(tableId, BEFORE_DAYS);

        Map<Object, Map<String, Object>> trendDateMap = tableViewTrend.stream().collect(Collectors.toMap(it -> it.get("log_date"), Function.identity()));
        List<String> dateRecentDays = buildDateRecentDays(BEFORE_DAYS);
        List<Integer> trendPv = new ArrayList<>(BEFORE_DAYS);
        List<Integer> trendUv = new ArrayList<>(BEFORE_DAYS);
        for (int i = 0; i < dateRecentDays.size(); ++i) {
            String date = dateRecentDays.get(i);
            if (trendDateMap.containsKey(date)) {
                Map<String, Object> valueMap = trendDateMap.get(date);
                trendPv.add(((Long) valueMap.get("pv")).intValue());
                trendUv.add(((Long) valueMap.get("uv")).intValue());
            } else {
                trendPv.add(0);
                trendUv.add(0);
            }
        }
        TableMonitorDataDTO.TableMonitorTableData.TableMonitorTableDataTrend dataTrend = new TableMonitorDataDTO.TableMonitorTableData.TableMonitorTableDataTrend()
                .setDate(dateRecentDays)
                .setPv(trendPv)
                .setUv(trendUv);

        TableMonitorDataDTO.TableMonitorTableData tableData = new TableMonitorDataDTO.TableMonitorTableData()
                .setPv(tableViewPv)
                .setUv(tableViewUv)
                .setTrend(dataTrend);

        return tableData;
    }

    /**
     * 发放数据监控
     * @param requestDTO
     * @return
     */
    private TableMonitorDataDTO.TableMonitorApplyData buildApplyData(GetTableMonitorDataRequestDTO requestDTO) {
        final Long tableId = requestDTO.getTableId();
        Integer total = dataMapper.getApplicationTotal(tableId);
        Integer days = dataMapper.getApplicationDays(tableId, BEFORE_DAYS);
        Integer effect = dataMapper.getApplicationEffect(tableId);
        TableMonitorDataDTO.TableMonitorApplyData applyData = new TableMonitorDataDTO.TableMonitorApplyData()
                .setTotal(total)
                .setDay30(days)
                .setEffect(effect);
        return applyData;
    }

    /**
     * 客户监控数据
     * @param requestDTO
     * @return
     */
    private TableMonitorDataDTO.TableMonitorCustomerData buildCustomerData(GetTableMonitorDataRequestDTO requestDTO) {
        final Long tableId = requestDTO.getTableId();
        Integer apply = dataMapper.getCustomerTableApply(tableId);
        Integer view = dataMapper.getCustomerTableView(tableId);
        Integer download = dataMapper.getCustomerTableDownload(tableId);

        List<Map<String, Object>> downloadTrendRows = dataMapper.getCustomerTableDownloadTrend(tableId, BEFORE_DAYS);
        Map<Object, Map<String, Object>> downloadTrendMap = downloadTrendRows.stream().collect(Collectors.toMap(it -> it.get("log_date"), Function.identity()));
        List<Map<String, Object>> viewTrendRows = dataMapper.getCustomerTableViewTrend(tableId, BEFORE_DAYS);
        Map<Object, Map<String, Object>> viewTrendMap = viewTrendRows.stream().collect(Collectors.toMap(it -> it.get("log_date"), Function.identity()));
        List<Integer> trendDownload = new ArrayList<>(BEFORE_DAYS);
        List<Integer> trendView = new ArrayList<>(BEFORE_DAYS);

        List<String> dateRecentDays = buildDateRecentDays(BEFORE_DAYS);
        for (int i = 0; i < dateRecentDays.size(); ++i) {
            String date = dateRecentDays.get(i);
            if (downloadTrendMap.containsKey(date)) {
                trendDownload.add(((Long) downloadTrendMap.get(date).get("total")).intValue());
            } else {
                trendDownload.add(0);
            }
            if (viewTrendMap.containsKey(date)) {
                trendView.add(((Long) viewTrendMap.get(date).get("total")).intValue());
            } else {
                trendView.add(0);
            }
        }

        TableMonitorDataDTO.TableMonitorCustomerData.TableMonitorCustomerDataTrend dataTrend = new TableMonitorDataDTO.TableMonitorCustomerData.TableMonitorCustomerDataTrend()
                .setDate(dateRecentDays)
                .setView(trendView)
                .setDownload(trendDownload);

        TableMonitorDataDTO.TableMonitorCustomerData customerData = new TableMonitorDataDTO.TableMonitorCustomerData()
                .setApply(apply)
                .setView(view)
                .setDownload(download)
                .setTrend(dataTrend);

        return customerData;

    }


    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private List<String> buildDateRecentDays(int beforeDays) {
        LocalDate before = LocalDate.now(ZoneId.systemDefault()).minusDays(beforeDays);
        return IntStream.rangeClosed(0, beforeDays).mapToObj(value -> before.plusDays(value).format(dateTimeFormatter)).collect(Collectors.toList());
    }

}
