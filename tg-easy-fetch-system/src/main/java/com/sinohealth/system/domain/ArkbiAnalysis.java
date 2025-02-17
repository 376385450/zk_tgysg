package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.utils.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 表：arkbi_analysis
 */
@Data
@Slf4j
@Accessors(chain = true)
public class ArkbiAnalysis implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 资产id,仪表板可能为多值,逗号分隔。并用#分隔资产版本，缺省最新版本
     * <p>
     * 例如： 1#4,2#7
     *
     * @see ArkbiAnalysis#fillAssetsVersion
     * @see ArkbiAnalysis#getAssetsIds
     */
    private String assetsId;

    /**
     * BI分析ID
     */
    private String analysisId;

    private String name;

    /**
     * 编辑（创建）链接
     */
    private String editUrl;

    /**
     * 编辑（修改）链接
     */
    private String previewUrl;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 状态,0:图表未保存，1:图表已保存
     */
    private Integer status;

    /**
     * 类型,dashboard:仪表板,chart:图表
     */
    private String type;

    /**
     * bi分享链接
     */
    private String shareUrl;

    /**
     * 分享链接密码
     */
    private String shareUrlPassword;

    /**
     * 父级ID,代表这条数据是从这个父级复制(推送到外网)而来
     */
    private Long parentId;

    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private Long applicantId;

    public boolean fillAssetsVersion(Collection<String> assetsVersions) {
        if (CollectionUtils.isEmpty(assetsVersions)) {
            log.error("EMPTY: old={}", this.assetsId);
            this.assetsId = "";
            return true;
        }
        String newVal = String.join(",", assetsVersions);
        boolean same = StringUtils.equals(this.assetsId, newVal);
        if (same) {
            return false;
        }
        this.assetsId = newVal;
        return true;
    }

    public List<Long> getAssetsIds() {
        if (StringUtils.isBlank(this.assetsId)) {
            return Collections.emptyList();
        }
        return Stream.of(this.assetsId.split(",")).map(v -> {
            String[] tmp = v.split("#");
            return Long.valueOf(tmp[0]);
        }).collect(Collectors.toList());
    }

    public Integer getFirstVersion() {
        if (StringUtils.isBlank(this.assetsId)) {
            return null;
        }
        return Stream.of(this.assetsId.split(",")).map(v -> {
            String[] tmp = v.split("#");
            if (tmp.length > 1) {
                return Integer.valueOf(tmp[1]);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).findFirst().orElse(null);

    }
}