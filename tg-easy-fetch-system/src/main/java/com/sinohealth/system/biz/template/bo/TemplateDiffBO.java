package com.sinohealth.system.biz.template.bo;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-22 14:45
 */

public class TemplateDiffBO {
    public static final TemplateDiffBO success = new TemplateDiffBO();


    private List<String> diff;

    public boolean hasDiff() {
        return CollectionUtils.isEmpty(diff);
    }
}
