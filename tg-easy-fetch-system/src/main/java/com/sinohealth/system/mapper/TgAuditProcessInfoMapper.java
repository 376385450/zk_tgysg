package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.biz.application.dto.DataApplicationPageDto;
import com.sinohealth.system.biz.application.dto.DocApplicationPageDto;
import com.sinohealth.system.dto.auditprocess.AuditApplicationSearchDto;
import com.sinohealth.system.dto.auditprocess.AuditPageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Author Rudolph
 * @Date 2022-05-16 16:12
 * @Desc
 */

@Mapper
@Repository
public interface TgAuditProcessInfoMapper extends BaseMapper<TgAuditProcessInfo> {


    List<AuditPageDto> queryByHandler(@Param("userId") String userId, @Param("search") AuditApplicationSearchDto search);

    List<TgApplicationInfo> queryApplicationByHandler(@Param("userId") String userId, @Param("search") AuditApplicationSearchDto search);

    List<TgApplicationInfo> queryApplicationByApplicantId(@Param("userId") String userId, @Param("search") AuditApplicationSearchDto searchDto);

    List<DataApplicationPageDto> queryDataApplicationPageDtoByApplicantId(@Param("userId") String userId, @Param("search") AuditApplicationSearchDto searchDto);

    List<TgAuditProcessInfo> queryAuditProcessPageByProcessIdAndMaxVersion(@Param("params") Map<String, Object> params);

    TgApplicationInfo queryApplicationById(Long applicationId);

    TgAuditProcessInfo queryProcessByIdAndVersion(@Param("processId") Long processId, @Param("processVersion") Integer processVersion);

    TgAuditProcessInfo queryProcessByIdAndMaxVersion(@Param("processId") Long processId);

    TgAuditProcessInfo queryProcessByGenericAndMaxVersion();

    List<TgAuditProcessInfo> queryProcessNeedToUpdate(@Param("userId") Long userId);

    List<DocApplicationPageDto> queryDocApplicationPageDtoByApplicationId(@Param("userId") String userId,
                                                                          @Param("search") AuditApplicationSearchDto searchDto);

    List<TgApplicationInfo> queryApplyByHandlerAndType(@Param("userId") String userId,
                                                       @Param("search") AuditApplicationSearchDto search);
}


