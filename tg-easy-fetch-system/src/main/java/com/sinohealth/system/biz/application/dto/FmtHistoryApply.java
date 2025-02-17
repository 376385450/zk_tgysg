package com.sinohealth.system.biz.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 前端预处理后的历史项目JSON
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-09-14 16:43
 */
@Slf4j
@Data
public class FmtHistoryApply {

    /**
     * Excel 中的id
     */
    private Long id;

    @JsonProperty("需求名称")
    private String projectName;

    @JsonProperty("项目名")
    private String newProjectName;

    @JsonProperty("长尾模板名")
    private String tailTemplateName;

    @JsonProperty("申请人")
    private String applyUser;

    /**
     * @see DeliverTimeTypeEnum
     */
    @JsonProperty("时间周期")
    private String deliverTimeType;

    @JsonProperty("客户")
    private String customer;

    /**
     * 省份、城市
     */
    @JsonProperty("区域粒度")
    private String areaGra;

    @JsonProperty("区域范围")
    private String areaSql;

    private FilterDTO areaFilter;

    /**
     * 可以忽略
     */
    @JsonProperty("产品范围")
    private String productSql;

    /**
     * 前端处理后的筛选树 产品范围
     */
    private FilterDTO newFilter;

    @JsonProperty("产品范围-需剔除部分")
    private String productExFilter;

    @JsonProperty("时间颗粒度")
    private String timeGra;

    /**
     * @see OneItem#parseTimeFilter()
     */
    // YYYY-mm
    @JsonProperty("开始时间")
    private String startTime;

    @JsonProperty("结束时间")
    private String endTime;

    private FilterDTO timeFilter;

    @JsonProperty("指标-固定维度默认全部指标")
    private String metrics;

    /**
     * @see RequireAttrType
     */
    @JsonProperty("需求性质")
    private String requireAttr;

    @JsonProperty("合同编号")
    private String contractNo;

    /**
     * 多市场粒度 ；分隔
     */
    public String[] parseAreaGra() {
        return StringUtils.split(this.areaGra, "；");
    }

    public void handleSeasonFilter() {
        if (Objects.equals(this.getTimeGra(), "季度")) {
            FilterDTO.FilterItemDTO item = this.getTimeFilter().getFilters().get(0).getFilters().get(1).getFilterItem();
            try {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                String target = "2023-12-31";
                Date targetEnd = fmt.parse(target);
                Date end = fmt.parse(item.getValue().toString());
                if (end.after(targetEnd)) {
                    item.setValue(target);
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }
}
