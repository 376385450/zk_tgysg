package com.sinohealth.system.biz.dataassets.listener;

import cn.hutool.core.util.ReUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.ConverterUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-23 19:44
 */
@Slf4j
public class AssetsCompareResultListener extends AnalysisEventListener<Map<Integer, Object>> {

    private final Map<Integer, WriteSheet> sheetMap = new HashMap<>();
    private WriteSheet curSheet = null;
    private final int batch_size = 1000;
    private final List<Map<Integer, Object>> cache = new ArrayList<>(batch_size);
    private boolean hasData = false;

    private final ExcelWriter excelWriter;
    private final String prodCodeHead = "分类一";
    private Integer prodCodeIdx = 0;

    /**
     * 按需合并
     */
    private final Set<String> sort1List;
    private boolean needFilterSort1 = false;

    public AssetsCompareResultListener(ExcelWriter excelWriter) {
        this.excelWriter = excelWriter;
        this.sort1List = Collections.emptySet();
    }

    public AssetsCompareResultListener(ExcelWriter excelWriter, Set<String> sort1List) {
        this.excelWriter = excelWriter;
        this.sort1List = sort1List;
        this.needFilterSort1 = CollectionUtils.isNotEmpty(sort1List);
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        Map<Integer, String> headStrMap = ConverterUtils.convertToStringMap(headMap, context);

        prodCodeIdx = -1;
        for (Map.Entry<Integer, String> entry : headStrMap.entrySet()) {
            if (Objects.equals(entry.getValue(), prodCodeHead)) {
                prodCodeIdx = entry.getKey();
            }
        }
        invokeHeadMap(headStrMap, context);

//        log.info("HEAD: {}", JsonUtils.format(headStrMap));

        ReadSheet readSheet = context.readSheetHolder().getReadSheet();
        Integer sheetNo = readSheet.getSheetNo();
        if (sheetMap.containsKey(sheetNo)) {
            curSheet = sheetMap.get(sheetNo);
        } else {
            WriteSheet sheet = EasyExcel.writerSheet(sheetNo, readSheet.getSheetName())
                    .head(headBlock(headStrMap)).build();
            sheetMap.put(sheetNo, sheet);
            curSheet = sheet;
        }
//        log.info("Entry {}", readSheet.getSheetName());
    }

    @Override
    public void invoke(Map<Integer, Object> data, AnalysisContext context) {
        hasData = true;
        cache.add(data);
        if (cache.size() >= batch_size) {
            excelWriter.write(dataRow(cache), curSheet);
            cache.clear();
        }
    }

    private List<List<String>> headBlock(Map<Integer, String> headStrMap) {
        List<List<String>> list = new ArrayList<>();
        headStrMap.forEach((k, v) -> {
            List<String> headList = new ArrayList<>();
            headList.add(v);
            list.add(headList);
        });
        return list;
    }

    private List<List<Object>> dataRow(List<Map<Integer, Object>> list) {
        List<List<Object>> rowList = new ArrayList<>();
        for (Map<Integer, Object> entry : list) {
            List<Object> cols = new ArrayList<>();
            if (prodCodeIdx != -1) {
                Object prodVal = entry.get(prodCodeIdx);
                if (needFilterSort1 && Objects.nonNull(prodVal) && !sort1List.contains(prodVal.toString())) {
                    continue;
                }
            }
            entry.forEach((k, v) -> appendCellVal(v, cols));

            rowList.add(cols);
        }

        return rowList;
    }

    private static void appendCellVal(Object v, List<Object> cols) {
        if (v instanceof String) {
            String cell = v.toString();
            try {
                if (ReUtil.isMatch("^[-+]?[0-9]*\\.?[0-9]+$", cell)) {
                    cols.add(Double.parseDouble(cell));
                } else {
                    cols.add(cell);
                }
            } catch (Exception e) {
                log.error("", e);
                cols.add(cell);
            }
        } else {
            cols.add(v);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!cache.isEmpty()) {
            excelWriter.write(dataRow(cache), curSheet);
            cache.clear();
        } else if (!hasData) {
            excelWriter.write(dataRow(cache), curSheet);
        }

        hasData = false;
    }
}
