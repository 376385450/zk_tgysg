package com.sinohealth.common.config;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.utils.StringUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-20 15:25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "transfer")
public class TransferProperties {
    private Long applicantId;
    private Long auditId;

    private Long templateId;
    private Long baseTableId;
    /**
     * 特殊需要，在保持tableId不变的前提下，需要替换底层使用到的CK表
     * 用于灰度环境 数据对比？
     */
    private String baseTableReplace;

    /**
     * CMH SKU交付结果表 GP
     */
    private String resultTable;
    /**
     * CMH 长尾交付结果表 GP
     */
    private String tailResultTable;
    /**
     * CMH 品牌 工作流结果表 GP
     */
    private String flowResultTable;

    private Boolean mockSameUser;

    private String whereConvertUrl;

    /**
     * 是否开启ROUND
     * <p>
     * 默认false，只有数据迁移的数据对比验证  数据精度  时才是true。
     *
     * @see CommonConstants#REMOVE_ROUND
     */
    private Boolean closeRound;

    /**
     * @see TransferProperties#baseTableReplace
     */
    public Map<String, String> parseTableReplace() {
        if (StringUtils.isBlank(baseTableReplace)) {
            return Collections.emptyMap();
        }
        String[] pair = baseTableReplace.split("#");
        Map<String, String> cache = new HashMap<>(pair.length / 2);
        for (int i = 0; i < pair.length; i += 2) {
            cache.put(pair[i], pair[i + 1]);
        }
        return cache;
    }
}
