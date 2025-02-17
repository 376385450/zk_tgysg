package com.sinohealth.system.service.impl;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.anno.MessagePointCut;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author Rudolph
 * @Date 2022-06-16 18:52
 * @Desc
 */
@Component
@Slf4j
public class ApplicationServiceAspect {

    @Autowired
    private AuditProcessServiceImpl auditProcessService;

    @MessagePointCut
    public void getApplication4Insert(TgApplicationInfo tgApplicationInfo) {
        log.info(">>>>>>>>>>>>>> 新增申请切点");
        auditProcessService.handleException(tgApplicationInfo);
    }

    @DataSource(DataSourceType.MASTER)
    public TgApplicationInfo getApplication(Long id) {
        TgApplicationInfo info = TgApplicationInfo.newInstance().selectById(id);
        if (Objects.isNull(info)) {
            log.warn("无效的id: id={}", id);
            return null;
        }
        return JsonBeanConverter.convert2Obj(info);
    }

    @DataSource(DataSourceType.MASTER)
    public UserDataAssets getDataAssets(Long id) {
        UserDataAssets info = new UserDataAssets().selectById(id);
        if (Objects.isNull(info)) {
            log.warn("无效的id: id={}", id);
            return null;
        }
        return info;
    }
}
