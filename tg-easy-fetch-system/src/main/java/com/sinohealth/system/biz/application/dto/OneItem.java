package com.sinohealth.system.biz.application.dto;

import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.dataassets.dto.compare.CompareResultVO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.util.HistoryApplyUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-01 09:58
 */
@Slf4j
@Data
public class OneItem {
    private String id;
    private String name;
    private String sql;
    private String excludeSql;
    // YYYY-mm
    private String startTime;

    private String endTime;
    /**
     * 区域范围
     */
    private String areaSql;
    /**
     * 区域粒度 省份、城市
     */
    private String areaGra;

    private FilterDTO filter;

    private FilterDTO timeFilter;

    private FilterDTO areaFilter;

    private CompareResultVO compare;

    public FilterDTO parseTimeFilter() {
        SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyyMM");
        SimpleDateFormat yyyy_MM = new SimpleDateFormat("yyyy-MM");

        Date startDate;
        Date endDate;
        try {
            endDate = yyyyMM.parse(this.getEndTime());
            startDate = yyyyMM.parse(this.getStartTime());
        } catch (Exception e) {
            log.error("", e);
            throw new CustomException(e.getMessage());
        }

        //TODO 优化成介于组件
        return HistoryApplyUtil.parseSql(String.format(" period >= '%s' and period <= '%s'",
                yyyy_MM.format(startDate) + "-01", yyyy_MM.format(endDate) + "-31"));
    }

    public String[] parseAreaGra() {
        return StringUtils.split(this.areaGra, "；");
    }
}
