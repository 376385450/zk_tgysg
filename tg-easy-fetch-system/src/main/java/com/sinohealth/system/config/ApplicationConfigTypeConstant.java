package com.sinohealth.system.config;

import java.util.Objects;

/**
 * 通用模版资产审核配置-配置类型
 *
 * @author zhangyanping
 * @date 2023/6/13 16:17
 */
public interface ApplicationConfigTypeConstant {

    /**
     * 配置-SQL类型
     */
    Integer SQL_TYPE = 0;
    /**
     * 配置-工作流模式
     */
    Integer WORK_FLOW_TYPE = 1;

    /**
     * 配置 文件模式
     */
    Integer FILE_TYPE = 2;

    static boolean isFile(Integer type) {
        return Objects.equals(type, FILE_TYPE);
    }

    static String getDesc(Integer type) {
        if (Objects.isNull(type)) {
            return "工作流";
        }
        if (Objects.equals(type, SQL_TYPE)) {
            return "SQL";
        }
        if (Objects.equals(type, WORK_FLOW_TYPE)) {
            return "工作流";
        }
        if (Objects.equals(type, FILE_TYPE)) {
            return "文件";
        }
        return "工作流";
    }

}
