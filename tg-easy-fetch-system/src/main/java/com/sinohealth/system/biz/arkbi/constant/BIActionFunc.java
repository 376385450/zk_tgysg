package com.sinohealth.system.biz.arkbi.constant;

import com.sinohealth.arkbi.param.RestoreViewParam;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-10 16:52
 */
@FunctionalInterface
public interface BIActionFunc {

    boolean handleView(String userToken, List<String> viewIds, List<RestoreViewParam> views);
}
