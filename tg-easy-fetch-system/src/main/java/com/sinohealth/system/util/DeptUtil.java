package com.sinohealth.system.util;

/**
 * @Author shallwetalk
 * @Date 2023/11/14
 */
public class DeptUtil {


    public static String showDeptName(String fullDeptName) {
        final String orgAdminTreePathText = fullDeptName;
        final String[] split = orgAdminTreePathText.split("/");
        if (split.length > 2) {
            return split[split.length - 2] + "/" + split[split.length - 1];
        } else {
            return split[split.length - 1];
        }
    }

}
