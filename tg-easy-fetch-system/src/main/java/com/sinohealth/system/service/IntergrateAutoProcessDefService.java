package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.ReleaseState;
import com.sinohealth.system.dto.TgCogradientDetailDto;
import com.sinohealth.system.dto.TgCogradientInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public interface IntergrateAutoProcessDefService {


    AjaxResult createProcessDefinition(
            Long id,
            Long tableId,
            String name,
            String processDefinitionJson,
            String crontab,
            String locations,
            String connects,
            int releaseState, ProcessDefCallback callable);


    AjaxResult releaseProcessDefinition(int id, ReleaseState releaseState);

    IPage<TgCogradientInfoDto> queryListPaging(Page<TgCogradientInfoDto> page, String searchVal);

    AjaxResult execProcessInstance(MultiValueMap<String, Object> postParameters);

    AjaxResult release(int taskId, Integer releaseState);

    AjaxResult delete(Integer id);

    IPage<TgCogradientDetailDto> querySyncDetail(Integer id, Integer tableId, Integer state, Integer pageNo, Integer pageSize);

    AjaxResult countTaskState(String startTime, String endTime);

    AjaxResult queryLog(int taskInstId, int skipLineNum, int limit);

    ResponseEntity getLogBytes(int taskInstId);

    /**
     * @param pageNo       1开始
     * @param searchVal    搜索文本
     * @param releaseState 1上线 0下线
     */
    AjaxResult queryProcessDefinitionList(int pageNo, int pageSize, String searchVal, Integer releaseState);

    /**
     * @param processId 流程id
     */
    AjaxResult queryProcessById(Integer processId);

    /**
     * 查询流程定义下的实例
     */
    AjaxResult queryProcessInstanceStatus(Integer processDefinitionId, Integer pageNo, Integer pageSize, String stateType);

}
