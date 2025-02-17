package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TgIntelligenceUserMapping;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author shallwetalk
 * @Date 2023/8/5
 */

@Mapper
@Repository
public interface TgIntelligenceUserMappingMapper extends BaseMapper<TgIntelligenceUserMapping> {
}
