package com.sinohealth.system.biz.dataassets.util;

import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-19 10:30
 */
public class DataAssetsUtil {

    public static Integer getFinalSchedulerId(TgTemplateInfo tgTemplateInfo, TgApplicationInfo info) {
        if (Objects.equals(tgTemplateInfo.getTemplateType(), TemplateTypeEnum.normal.name())) {
            return tgTemplateInfo.getSchedulerId();
        }

        if (ApplicationConfigTypeConstant.SQL_TYPE.equals(info.getConfigType())) {
            return info.getConfigSqlWorkflowId();
        } else if (ApplicationConfigTypeConstant.WORK_FLOW_TYPE.equals(info.getConfigType())) {
            return info.getWorkflowId();
        } else {
            return null;
        }

    }


    public static boolean useAutoProcess(TgTemplateInfo tgTemplateInfo, TgApplicationInfo info) {
        if (Objects.equals(tgTemplateInfo.getTemplateType(), TemplateTypeEnum.normal.name())) {
            return false;
        }
        return ApplicationConfigTypeConstant.SQL_TYPE.equals(info.getConfigType()) && info.getConfigSqlWorkflowId() != null;
    }

    public static Integer getFinalSchedulerId(TgUserDataAssetsDistDto entity) {
        if (Objects.equals(entity.getTemplateType(), TemplateTypeEnum.normal.name())) {
            return entity.getSchedulerId();
        }

        if (ApplicationConfigTypeConstant.SQL_TYPE.equals(entity.getConfigType())) {
            return entity.getConfigSqlWorkflowId();
        } else if (ApplicationConfigTypeConstant.WORK_FLOW_TYPE.equals(entity.getConfigType())) {
            return entity.getWorkflowId();
        } else {
            return null;
        }
    }
}
