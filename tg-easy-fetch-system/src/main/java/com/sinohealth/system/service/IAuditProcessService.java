package com.sinohealth.system.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.system.biz.application.dto.SyncApplyDetailVO;
import com.sinohealth.system.biz.audit.dto.AuditRequest;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.dto.auditprocess.AuditPageByTypeDto;
import com.sinohealth.system.dto.auditprocess.AuditPageDto;
import com.sinohealth.system.vo.TgApplicationInfoDetailVO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public interface IAuditProcessService {

    Object add(TgAuditProcessInfo auditProcessInfo);

    Object query(Map<String, Object> params);

    AjaxResult delete(Map<String, Object> params);

    Object queryAuditProcessApplicationList(Map<String, Object> params);

    Integer qeuryAuditProcessApplicationListCount(Map<String, Object> params);

    TgApplicationInfoDetailVO queryAuditProcessApplicationDetail(Long applicationId);

    SyncApplyDetailVO querySyncDetail(Long applicationId);

    List<AuditPageDto> queryAuditProcessAuditList(SysUser user, Map<String, Object> params);

    Object qeuryAuditProcessAuditDetail(Long applicationId);

    Pair<Boolean, Long> overLimit(Long applicationId);

    Object auditProcess(AuditRequest node);

    void updateGeneric(TgAuditProcessInfo auditProcessInfo);

    TgAuditProcessInfo queryProcessByIdAndVersion(Long processId, Integer processVersion);

    TgAuditProcessInfo queryGenericProcess();

    TgAuditProcessInfo queryCurrentProcess(Long processId);

    boolean extendsAuditProcess(Long oldUserId, Long newUserId);


    AjaxResult<List<AuditPageByTypeDto>> queryAuditProcessAuditListByType(SysUser user, Map<String, Object> params);


    List<AuditPageByTypeDto> queryAuditProcessAuditListByUser(SysUser user);

}
