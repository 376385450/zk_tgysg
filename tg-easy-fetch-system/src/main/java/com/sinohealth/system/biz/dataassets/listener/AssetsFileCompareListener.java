package com.sinohealth.system.biz.dataassets.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.Sets;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompareFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kuangcp
 * 2024-07-18 21:10
 */
@Slf4j
public class AssetsFileCompareListener extends AnalysisEventListener<Map<Integer, Object>> {

    private final AssetsCompareFile file;
    private final String fileName;

    //    private final String projectHead = "项目名称";
//    private Integer projectIdx = 0;
//    private String projectVal = "";
    private final String periodHead = "时间";
    private Integer periodIdx = 0;
    private List<String> periodVal = new ArrayList<>();
    private final String prodCodeHead = "品类";
    private Integer prodCodeIdx = 0;
    private List<String> prodCodeVal = new ArrayList<>();
    private Set<String> exceptHead = Sets.newHashSet(
            // 检查变化的 标签
            "产品ID", "处方性质", "中西药属性", "剂型", "对象", "品类",
            "分类一", "分类二", "分类三", "分类四", "通用名", "品牌", "商品名", "品名(含属性)", "厂家", "规格", "集团权益", "简写品牌", "简写厂家", "装量", "日服用量"
            // 必须标签
            , "项目名称", "时间", "区域",
            // 检查变化的 指标
            "放大销售额");

    public AssetsFileCompareListener(AssetsCompareFile file, String fileName) {
        this.file = file;
        this.fileName = fileName;
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        super.invokeHeadMap(headMap, context);

        exceptHead.removeAll(headMap.values());
        if (CollectionUtils.isNotEmpty(exceptHead)) {
            String no = String.join(",", exceptHead);
            throw new CustomException(no + " 列缺失，请检查Excel: " + fileName);
        }

        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            if (Objects.equals(entry.getValue(), periodHead)) {
                periodIdx = entry.getKey();
            }
            if (Objects.equals(entry.getValue(), prodCodeHead)) {
                prodCodeIdx = entry.getKey();
            }
//            if (Objects.equals(entry.getValue(), projectHead)) {
//                projectIdx = entry.getKey();
//            }
        }
    }

    /**
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context analysis context
     */
    @Override
    public void invoke(Map<Integer, Object> data, AnalysisContext context) {
//        log.info("data={}", data);
        Object pv = data.get(periodIdx);
        periodVal.add(pv.toString());
//        Object pro = data.get(projectIdx);
//        if (StringUtils.isBlank(projectVal)) {
//            projectVal = pro.toString();
//        }

        Object cv = data.get(prodCodeIdx);
        prodCodeVal.add(cv.toString());
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (Objects.nonNull(file)) {
            String val = prodCodeVal.stream().distinct().collect(Collectors.joining("、"));
            file.setProdCode(val);
            periodVal.stream().max(Comparator.comparing(String::toString)).ifPresent(file::setDataPeriod);
//            file.setProjectName(projectVal);
        }
    }
}
