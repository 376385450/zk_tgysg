package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.dto.application.TgNodeMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-06-16 11:58
 * @Desc
 */

@Mapper
@Repository
public interface TgNodeMappingMapper extends BaseMapper<TgNodeMapping> {

    List<TgNodeMapping> queryApplicationMappingByApplicantId(@Param("applicantId") Long applicantId,
                                                             @Param("auditStatus") Integer auditStatus);

    Long queryDirIdByAssetsId(@Param("assetsId") Long assetsId);

    List<TgNodeMapping> queryTableMapping();

    void deleteByNodeIdAndApplicantId(@Param("nodeId") Long NodeId, @Param("applicantId") Long applicantId, @Param("icon") String icon);

    List<TgNodeMapping> queryDocMapping();

    List<TgNodeMapping> queryArkBIMapping(@Param("userId") Long userId, @Param("icon") String icon);
}

