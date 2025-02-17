package com.sinohealth.system.biz.table.vo;

import com.sinohealth.common.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
public class TableInfoDiffVO {
    /**
     * 字段信息
     */
    private LinkedHashMap<String, Object> fieldValue;

    /**
     * 变动标识1
     */
    private String mark;

    /**
     * 变动标识2
     */
    private Set<String> markDescription;

    /**
     * 变化明细
     */
    private List<tableInfoChangeDetail> changeDetails;

    /**
     * 新版本标签
     */
    private List<String> newTags;

    /**
     * 旧版本标签
     */
    private List<String> oldTags;

    /**
     * 唯一标识
     */
    private String uniqueKey;

    @Setter
    @Getter
    @ToString
    public static class tableInfoChangeDetail {
        /**
         * 变动类型【标签、指标】
         */
        private String type;

        /**
         * 变化列中文名
         */
        private String changeFieldCommand;

        /**
         * 变化列英文名
         */
        private String changeField;

        /**
         * 列顺序
         */
        private Integer sort;

        /**
         * 变化列_新值
         */
        private String newValue;

        /**
         * 变化列_旧值
         */
        private String oldValue;
    }

    public void setMark(String mark) {
        if (!Objects.equals(this.mark, "减少")) {
            this.mark = mark;
        }
    }

    public void addMarkDescription(String description) {
        if (Objects.isNull(this.markDescription)) {
            this.markDescription = new LinkedHashSet<>();
        }
        if (Objects.equals(description, "ID合并") || Objects.equals(description, "被回收站打包")) {
            this.markDescription = Collections.singleton(description);
        }
        if (!markDescription.contains("ID合并") && !markDescription.contains("被回收站打包")) {
            this.markDescription.add(description);
        }
    }

    public void addChangeDetails(tableInfoChangeDetail detail) {
        if (Objects.isNull(this.changeDetails)) {
            this.changeDetails = new ArrayList<>();
        }
        this.changeDetails.add(detail);
    }

    public void addNewTags(String tag) {
        if (Objects.isNull(this.newTags)) {
            this.newTags = new ArrayList<>();
        }
        this.newTags.add(tag);
    }

    public void addOldTags(String tag) {
        if (Objects.isNull(this.oldTags)) {
            this.oldTags = new ArrayList<>();
        }
        this.oldTags.add(tag);
    }

    public String getMark() {
        return StringUtils.isNotBlank(mark) ? mark : "无变化";
    }
}
