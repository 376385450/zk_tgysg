package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface TgTemplateInfoMapper extends BaseMapper<TgTemplateInfo> {
    List<TgTemplateInfo> queryTemplatePage(@Param("params") Map<String, Object> params);
    List<TgTemplateInfo> queryRelateTemplatePage(@Param("params") Map<String, Object> params);

    List<TemplateAuditProcessEasyDto> queryProcessesByBaseTableId(@Param("baseTableId") Long baseTableId);

    List<TemplateAuditProcessEasyDto> queryProcessesByBaseTableIds(@Param("baseTableIds") List<Long> baseTableIds);

}
