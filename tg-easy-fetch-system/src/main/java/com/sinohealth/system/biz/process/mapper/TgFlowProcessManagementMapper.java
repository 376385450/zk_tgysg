package com.sinohealth.system.biz.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.vo.FlowProcessSplitByTemplateVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@DataSource(DataSourceType.MASTER)
public interface TgFlowProcessManagementMapper extends BaseMapper<TgFlowProcessManagement> {
    /**
     * 根据模板id查询全流程信息
     *
     * @param templateIds 模板id
     * @return 全流程信息
     */
    List<FlowProcessSplitByTemplateVO> listByTemplateIds(@Param("templateIds") List<Long> templateIds);
}
