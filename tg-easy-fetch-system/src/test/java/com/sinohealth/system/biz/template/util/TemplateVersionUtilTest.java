package com.sinohealth.system.biz.template.util;

import com.sinohealth.system.domain.TgTemplateInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-22 14:51
 */
@Slf4j
public class TemplateVersionUtilTest {

    @Test
    public void testNeedSaveSnapshot() throws Exception {
        TgTemplateInfo a = new TgTemplateInfo();
        TgTemplateInfo b = new TgTemplateInfo();

        a.setCustomExt("[{\"name\":\"关联分析1\",\"note\":\"关联分析设置2\",\"tips\":\"测试自适应设置项2\",\"options\":[87,88,89],\"subType\":\"field\",\"required\":0,\"customGranularity\":true,\"bool\":true,\"select\":[87,88]}]");
        b.setCustomExt("[{\"name\":\"关联分析1\",\"note\":\"xx\",\"tips\":\"xx\",\"options\":[87,88,89],\"subType\":\"field\",\"required\":0,\"customGranularity\":true,\"bool\":true,\"select\":[87,88]}]");
        List<String> result = TemplateVersionUtil.needSaveSnapshot(a, b);
        log.info("result={}", result);
    }
}
