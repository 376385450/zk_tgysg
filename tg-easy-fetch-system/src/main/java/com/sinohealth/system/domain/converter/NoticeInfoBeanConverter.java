package com.sinohealth.system.domain.converter;

import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.notice.TgNoticeInfo;
import com.sinohealth.system.dto.notice.AddNoticeRequest;
import com.sinohealth.system.dto.notice.UpdateNoticeRequest;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 15:09
 */
public class NoticeInfoBeanConverter {

    public static TgNoticeInfo toEntity(AddNoticeRequest addNoticeRequest) {
        TgNoticeInfo tgNoticeInfo = new TgNoticeInfo();
        tgNoticeInfo.setNoticeType(addNoticeRequest.getNoticeType());
        tgNoticeInfo.setName(addNoticeRequest.getName());
        tgNoticeInfo.setContent(addNoticeRequest.getContent());
        tgNoticeInfo.setIsTop(addNoticeRequest.getIsTop());
        tgNoticeInfo.setCreateTime(new Date());
        tgNoticeInfo.setCreator(SecurityUtils.getRealName());
        tgNoticeInfo.setUpdateTime(new Date());
        tgNoticeInfo.setUpdater(SecurityUtils.getRealName());
        return tgNoticeInfo;
    }

    public static TgNoticeInfo toEntity(UpdateNoticeRequest updateRequest) {
        TgNoticeInfo tgNoticeInfo = new TgNoticeInfo();
        tgNoticeInfo.setId(updateRequest.getId());
        tgNoticeInfo.setNoticeType(updateRequest.getNoticeType());
        tgNoticeInfo.setName(updateRequest.getName());
        tgNoticeInfo.setContent(updateRequest.getContent());
        tgNoticeInfo.setIsTop(updateRequest.getIsTop());
        tgNoticeInfo.setUpdateTime(new Date());
        tgNoticeInfo.setUpdater(SecurityUtils.getRealName());
        return tgNoticeInfo;
    }
}
