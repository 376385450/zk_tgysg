package com.sinohealth.system.domain.converter;

import com.sinohealth.common.enums.DelFlag;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.label.TgLabelInfo;
import com.sinohealth.system.dto.label.AddLabelRequest;
import com.sinohealth.system.dto.label.DeleteLabelRequest;
import com.sinohealth.system.dto.label.UpdateLabelRequest;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 18:02
 */
public class LabelInfoBeanConverter {

    public static TgLabelInfo toEntity(AddLabelRequest request) {
        TgLabelInfo tgLabelInfo = new TgLabelInfo();
        tgLabelInfo.setName(request.getName());
        tgLabelInfo.setCreateTime(new Date());
        tgLabelInfo.setCreator(String.valueOf(SecurityUtils.getRealName()));
        tgLabelInfo.setUpdateTime(new Date());
        tgLabelInfo.setUpdater(String.valueOf(SecurityUtils.getRealName()));
        return tgLabelInfo;
    }

    public static TgLabelInfo toEntity(UpdateLabelRequest request) {
        TgLabelInfo tgLabelInfo = new TgLabelInfo();
        tgLabelInfo.setId(request.getId());
        tgLabelInfo.setName(request.getName());
        tgLabelInfo.setUpdateTime(new Date());
        tgLabelInfo.setUpdater(String.valueOf(SecurityUtils.getRealName()));
        return tgLabelInfo;
    }

    public static TgLabelInfo toEntity(DeleteLabelRequest request) {
        TgLabelInfo tgLabelInfo = new TgLabelInfo();
        tgLabelInfo.setId(request.getId());
        tgLabelInfo.setDelFlag(DelFlag.DEL.getCode());
        tgLabelInfo.setDeleteTime(new Date());
        return tgLabelInfo;
    }
}
