package com.sinohealth.system.domain.vo;

import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

/**
 * @author zhangyanping
 * @date 2023/5/15 16:16
 */
@Data
@ToString
public class TgDataRangeTemplateVO {

    /**
     * id
     */
    private String id;
    /**
     * pid用于形成树关系
     */
    private String pid;
    /**
     * 分类ID （UI 第一行 自定义列）
     *
     * @see FieldDict#id
     */
    private Long categoryId;
    /**
     * 前端：自定义列下标
     */
    private Integer selectIndex;
    /**
     * 分类中文名称
     */
    private String categoryChineseName;
    /**
     * 分类英文名称
     */
    private String categoryEnName;
    /**
     * 关联粒度 （UI 第二行）
     */
    private String granularity;

    /**
     * 用户手动输入的分类名称 （UI 第三行 自定义名称）
     */
    private String categoryName;
    /**
     * 子条件
     */
    private List<TgDataRangeTemplateVO> children;
    /**
     * 数据范围信息
     */
    private FilterDTO dataRangeInfo;

    /**
     * 附加字段
     */
    private String categoryPlaceholder;

    /**
     * 附加字段
     */
    private String granularityPlaceholder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TgDataRangeTemplateVO that = (TgDataRangeTemplateVO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(categoryId, that.categoryId)
                && Objects.equals(categoryName, that.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, categoryId, categoryName);
    }
}
