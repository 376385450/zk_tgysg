package com.sinohealth.system.biz.scheduler.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.ReleaseState;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaColumnDTO;
import com.sinohealth.system.biz.scheduler.dto.request.TypeConvertParam;
import com.sinohealth.system.dto.TgCogradientDetailDto;
import com.sinohealth.system.service.ProcessDefCallback;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 数据交换 尚书台封装
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-11-05 18:10
 */
public interface IntegrateSyncProcessDefService {
    AjaxResult createProcessDefinition(
            Long id,
            Long tableId,
            String name,
            String processDefinitionJson,
            String crontab,
            String locations,
            String connects,
            int releaseState, ProcessDefCallback callable);

    AjaxResult execProcessInstance(MultiValueMap<String, Object> postParameters);

    /**
     * @param processId 流程id
     */
    AjaxResult queryProcessById(Integer processId);

    /**
     * @param processId
     * @param releaseState
     * @see ReleaseState
     */
    AjaxResult<Object> releaseByProcessId(int processId, Integer releaseState);

    /**
     * @param processId 工作流id
     * @param simState  简化状态
     * @param pageNo
     * @param pageSize
     * @see org.apache.dolphinscheduler.common.enums.SimState 尚书台项目
     */
    IPage<TgCogradientDetailDto> querySyncDetail(Integer processId, Integer simState, Integer pageNo, Integer pageSize);

    Map getTaskInfo(Integer id);

    List<MetaColumnDTO> convertType(TypeConvertParam param);

    AjaxResult wrapException(Supplier<AjaxResult> func);

    AjaxResult queryProcessInstanceStatus(Integer processDefinitionId, Integer pageNo, Integer pageSize, String stateType);
}
