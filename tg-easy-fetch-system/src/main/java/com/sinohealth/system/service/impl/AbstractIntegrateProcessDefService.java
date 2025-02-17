package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.DsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.ReleaseState;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.RetryUtil;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.BeanUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaColumnDTO;
import com.sinohealth.quartz.util.CronUtils;
import com.sinohealth.system.biz.scheduler.dto.request.TypeConvertParam;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgCogradientInfo;
import com.sinohealth.system.dto.ScheduleParam;
import com.sinohealth.system.dto.TgCogradientDetailDto;
import com.sinohealth.system.dto.TgCogradientInfoDto;
import com.sinohealth.system.dto.TgCogradientMonitorDto;
import com.sinohealth.system.mapper.TgCogradientInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ProcessDefCallback;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 易数阁手动创建的流程和自动建的流程放在两个项目中
 * 这个类专门处理自动化流程的代码
 *
 * @author zhangyanping
 * @date 2023/7/5 16:21
 */
@Log4j2
public abstract class AbstractIntegrateProcessDefService {

    public abstract String getUri();

    public abstract String getProjectName();

    public abstract RestTemplate getRestTemplate();

    public abstract TgCogradientInfoMapper getTgCogradientInfoMapper();

    public abstract ITableInfoService getTableInfoService();

    public abstract ISysUserService getSysUserService();

    @Autowired
    private AppProperties appProperties;

    private static final Cache<Integer, String> flowNameCache = Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterAccess(8, TimeUnit.HOURS)
            .build();


    public AjaxResult createProcessDefinition(Long id, Long tableId, String name,
                                              String processDefinitionJson, String crontab, String locations,
                                              String connects, int releaseState, ProcessDefCallback callable) {
        String method;
        TgCogradientInfo tgCogradientInfo;
        String oldName = null;
        if (id == null) {
            method = DsConstants.PROCESS_DEF_ADD;
            tgCogradientInfo = new TgCogradientInfo(tableId, processDefinitionJson, name);
        } else {
            method = DsConstants.PROCESS_DEF_EDIT;
            tgCogradientInfo = getTgCogradientInfoMapper().selectById(id);
            oldName = tgCogradientInfo.getName();
            tgCogradientInfo.setTableId(tableId);
            tgCogradientInfo.setName(name);
            tgCogradientInfo.setProcessDefinitionJson(processDefinitionJson);
            tgCogradientInfo.setUpdateBy(SecurityUtils.getUsername());
            tgCogradientInfo.setUpdateTime(new Date());
        }
        //检验名称
        if (!name.equals(oldName)) {
            if (getTgCogradientInfoMapper().getCountByName(name) > 0) {
                return AjaxResult.error("名称已存在!");
            }
        }

        try {
            Boolean valid = Optional.ofNullable(crontab)
                    .map(v -> JsonUtils.parse(v, ScheduleParam.class))
                    .map(v -> CronUtils.isValid(v.getCrontab()))
                    .orElse(true);
            if (!valid) {
                return AjaxResult.error("Cron表达式错误");
            }
        } catch (Exception e) {
            log.error("", e);
        }

        String url = getUri() + method;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("name", name);
        postParameters.add("id", tgCogradientInfo.getProcessId());
        postParameters.add("processDefinitionJson", processDefinitionJson);
        postParameters.add("connects", connects);
        postParameters.add("locations", locations);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);


        AjaxResult r = getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
        log.info("创建流程结果：{}", r);
        if (r.getCode() == 0) {
            log.info("创建流程成功！！！");
            if (tgCogradientInfo.getProcessId() == null) {
                tgCogradientInfo.setProcessId((Integer) r.getData());
            }
            callable.doAfterCreatedProcess(tgCogradientInfo.getProcessId());


            if (crontab != null && !crontab.equals(tgCogradientInfo.getCrontab())) {
                log.info("设置corn调度任务！！！");
                this.releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.ONLINE);
                tgCogradientInfo.setCrontab(crontab);
                Integer scheduleId = this.saveSchedule(tgCogradientInfo.getScheduleId(), tgCogradientInfo.getProcessId(), crontab);
                tgCogradientInfo.setScheduleId(scheduleId);
            }

            boolean online = releaseState == ReleaseState.ONLINE.getCode();
            if (online) {
                log.info("设置任务上线！！！");
                this.releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.ONLINE);
                setScheduleState(tgCogradientInfo.getScheduleId(), ReleaseState.ONLINE);
            } else {
                this.releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.OFFLINE);
            }


            tgCogradientInfo.setStatus(releaseState);
            if (tgCogradientInfo.getId() != null) {
                getTgCogradientInfoMapper().updateById(tgCogradientInfo);
            } else {
                getTgCogradientInfoMapper().insert(tgCogradientInfo);

            }
            //新增时处理表的负责人字段
            if (DsConstants.PROCESS_DEF_ADD.equals(method) && tableId != null) {
                TableInfo tableInfo = getTableInfoService().getById(tableId);
                if (StringUtils.isEmpty(tableInfo.getLeaderName())) {
                    tableInfo.setLeaderName(SecurityUtils.getUsername());
                    getTableInfoService().saveOrUpdate(tableInfo);
                }
            }


        } else {
            log.info("创建流程失败！！！:{}", r.getMsg());
            r = AjaxResult.error("任务创建失败");
        }
        return r;
    }


    public AjaxResult releaseProcessDefinition(int id, ReleaseState releaseState) {
        String url = getUri() + DsConstants.PROCESS_DEF_RELEASE;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
        postParameters.add("processId", id);
        postParameters.add("releaseState", releaseState);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        return getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
    }


    public IPage<TgCogradientInfoDto> queryListPaging(Page<TgCogradientInfoDto> page, String searchVal) {
        // 查询列表
        final IPage<TgCogradientInfoDto> iPage = getTgCogradientInfoMapper().findList(page, searchVal);
        if (Objects.isNull(iPage)) {
            return null;
        }
        Map<String, String> nameMap = new HashMap<>();

        final List<TgCogradientInfoDto> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(tgCogradientInfoDto -> {
                Map map = this.getTaskInfo(tgCogradientInfoDto.getProcessId());
                if (map != null) {
                    tgCogradientInfoDto.setSyncCnt((Integer) map.get("cnt"));
                    tgCogradientInfoDto.setSyncStatus((Integer) map.get("state"));
                }
                if (StringUtils.isNotEmpty(tgCogradientInfoDto.getCrontab())) {
                    ScheduleParam scheduleParam = JsonUtils.parse(tgCogradientInfoDto.getCrontab(), ScheduleParam.class);
                    tgCogradientInfoDto.setCron(scheduleParam.getCrontab());
                    tgCogradientInfoDto.setExeTime(CronUtils.getSelfFireDateList(scheduleParam.getCrontab(), 3));
                }

                //处理创建人
                if (StringUtils.isNotEmpty(tgCogradientInfoDto.getCreateBy())) {
                    String viewName = nameMap.get(tgCogradientInfoDto.getCreateBy());
                    if (Objects.nonNull(viewName)) {
                        tgCogradientInfoDto.setCreateByOri(viewName);
                    } else {
                        SysUser user = getSysUserService().selectUserByUserName(tgCogradientInfoDto.getCreateBy());
                        if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                            if (sinoPassUserDTO != null) {
                                nameMap.put(tgCogradientInfoDto.getCreateBy(), sinoPassUserDTO.getViewName());
                                tgCogradientInfoDto.setCreateByOri(sinoPassUserDTO.getViewName());
                            }
                        }
                    }
                }
            });
        }
        return iPage;
    }


    public AjaxResult execProcessInstance(MultiValueMap<String, Object> postParameters) {
        String url = getUri() + DsConstants.PROCESS_DEF_START;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        postParameters.add("projectName", getProjectName());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        return getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
    }


    public AjaxResult<Object> release(int taskId, Integer releaseState) {
        TgCogradientInfo tgCogradientInfo = getTgCogradientInfoMapper().selectById(taskId);
        if (tgCogradientInfo != null) {
            if (releaseState.equals(ReleaseState.OFFLINE.getCode())) {
                releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.OFFLINE);
            } else {
                releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.ONLINE);
                setScheduleState(tgCogradientInfo.getScheduleId(), ReleaseState.ONLINE);
            }
            getTgCogradientInfoMapper().updateState(taskId, releaseState, SecurityUtils.getUsername());
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    public AjaxResult<Object> releaseByProcessId(int processId, Integer releaseState) {
        return this.releaseByProcessId(processId, releaseState, SecurityUtils.getUsername());
    }

    public AjaxResult<Object> releaseByProcessId(int processId, Integer releaseState, String username) {
        List<TgCogradientInfo> infos = getTgCogradientInfoMapper().selectList(new QueryWrapper<TgCogradientInfo>()
                .lambda().eq(TgCogradientInfo::getProcessId, processId));
        if (CollectionUtils.isNotEmpty(infos)) {
            TgCogradientInfo tgCogradientInfo = infos.get(0);
            if (releaseState.equals(ReleaseState.OFFLINE.getCode())) {
                releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.OFFLINE);
            } else {
                releaseProcessDefinition(tgCogradientInfo.getProcessId(), ReleaseState.ONLINE);
                setScheduleState(tgCogradientInfo.getScheduleId(), ReleaseState.ONLINE);
            }
            getTgCogradientInfoMapper().updateState(tgCogradientInfo.getId(), releaseState, username);
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    public AjaxResult delete(Integer id) {
        TgCogradientInfo tgCogradientInfo = getTgCogradientInfoMapper().selectById(id);
        String url = getUri() + DsConstants.PROCESS_DEF_DELETE + "?processDefinitionId={processDefinitionId}";
        AjaxResult r = getRestTemplate().getForObject(url, AjaxResult.class, getProjectName(), tgCogradientInfo.getProcessId());
        if (r.getCode() == 0) {
            getTgCogradientInfoMapper().deleteById(id);
            return AjaxResult.success();
        } else {
            return AjaxResult.error(r.getMsg());
        }
    }


    public IPage<TgCogradientDetailDto> querySyncDetail(Integer processId, Integer simState,
                                                        Integer pageNo, Integer pageSize) {
        // 查询列表
        final IPage<TgCogradientDetailDto> iPage = new Page<>();
        String url = getUri() + DsConstants.PROCESS_TASK_DEFIDS_SIM;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
        postParameters.add("defIds", processId);
        postParameters.add("simState", simState);
        postParameters.add("pageNo", pageNo);
        postParameters.add("pageSize", pageSize);

        // TODO 支持endTime查询
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        AjaxResult result = getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
        if (result != null && result.getCode() == 0) {
            List<TgCogradientDetailDto> tgCogradientDetailDtos = new ArrayList<>();

            Map<String, Object> resultMap = (Map<String, Object>) result.getData();

            iPage.setTotal((Integer) resultMap.get("total"));
            List<Map<String, Object>> list = (List<Map<String, Object>>) resultMap.get("totalList");

            for (Map<String, Object> entity : list) {
                // TODO 为什么会出现对象缓存
                TgCogradientDetailDto tmp = getTgCogradientInfoMapper().getByDefId((Integer) entity.get("defId"));
                TgCogradientDetailDto tgCogradientDetailDto = new TgCogradientDetailDto();
                BeanUtils.copyProperties(tmp, tgCogradientDetailDto);

                tgCogradientDetailDto.setSubmitTime(convertTime((String) entity.get("submitTime")));
                tgCogradientDetailDto.setEndTime(convertTime((String) entity.get("endTime")));
                tgCogradientDetailDto.setState((Integer) entity.get("state"));
                tgCogradientDetailDto.setTaskId((Integer) entity.get("id"));

                tgCogradientDetailDtos.add(tgCogradientDetailDto);
            }
            iPage.setRecords(tgCogradientDetailDtos);

        }
        return iPage;
    }

    private String convertTime(String time) {
        if (Objects.isNull(time)) {
            return null;
        }
        String[] parts = time.split("\\.");
        return parts[0].replace("T", " ");
    }


    public IPage<TgCogradientDetailDto> querySyncDetail(Integer id, Integer tableId, Integer state, Integer pageNo, Integer pageSize) {
        // 查询列表
        String defIds = getTgCogradientInfoMapper().getDefIdByTask(id, tableId);
        final IPage<TgCogradientDetailDto> iPage = new Page<>();
        String url = getUri() + DsConstants.PROCESS_TASK_DEFIDS;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<String, Object>();
        postParameters.add("defIds", defIds);
        postParameters.add("state", state);
        postParameters.add("pageNo", pageNo);
        postParameters.add("pageSize", pageSize);

        // TODO 支持endTime查询
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        AjaxResult result = getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
        if (result != null && result.getCode() == 0) {
            List<TgCogradientDetailDto> tgCogradientDetailDtos = new ArrayList<>();

            Map<String, Object> resultMap = (Map<String, Object>) result.getData();

            iPage.setTotal((Integer) resultMap.get("total"));
            List<Map<String, Object>> list = (List<Map<String, Object>>) resultMap.get("totalList");

            for (Map<String, Object> entity : list) {
                // TODO 为什么会出现对象缓存
                TgCogradientDetailDto tmp = getTgCogradientInfoMapper().getByDefId((Integer) entity.get("defId"));
                TgCogradientDetailDto tgCogradientDetailDto = new TgCogradientDetailDto();
                BeanUtils.copyProperties(tmp, tgCogradientDetailDto);

                tgCogradientDetailDto.setSubmitTime((String) entity.get("submitTime"));
                tgCogradientDetailDto.setEndTime((String) entity.get("endTime"));
                tgCogradientDetailDto.setState((Integer) entity.get("state"));
                tgCogradientDetailDto.setTaskId((Integer) entity.get("id"));
                String cnt = (String) entity.get("dataCnt");
                try {
                    if (StringUtils.isNotEmpty(cnt)) {
                        String[] array = cnt.split("\\|");
                        if (array.length > 1) {
                            tgCogradientDetailDto.setTotalCnt(Long.parseLong(array[1]));
                            tgCogradientDetailDto.setChangeCnt(Long.parseLong(array[1]) - Long.parseLong(array[0]));
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                }

                tgCogradientDetailDtos.add(tgCogradientDetailDto);
            }
            iPage.setRecords(tgCogradientDetailDtos);

        }
        return iPage;
    }


    public AjaxResult countTaskState(String startTime, String endTime) {
        // 查询列表
        String defIds = getTgCogradientInfoMapper().queryAllProcessId();
        String url = getUri() + DsConstants.PROCESS_TASK_STATE;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("defIds", defIds);
        postParameters.add("startTime", startTime);
        postParameters.add("endTime", endTime);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        AjaxResult r = getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
        if (r != null && r.getCode() == 0) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) r.getData();
            List<TgCogradientMonitorDto> dtoList = getTgCogradientInfoMapper().queryStateCnt();
            for (TgCogradientMonitorDto tgCogradientMonitorDto : dtoList) {
                String[] array = tgCogradientMonitorDto.getProcessIds().split(",");
                for (Map entity : list) {
                    if (Arrays.asList(array).contains(String.valueOf(entity.get("defId")))) {
                        tgCogradientMonitorDto.setRunningCnt(tgCogradientMonitorDto.getRunningCnt() + (Integer) entity.get("runCnt"));
                        tgCogradientMonitorDto.setFailCnt(tgCogradientMonitorDto.getFailCnt() + (Integer) entity.get("failCnt"));
                        tgCogradientMonitorDto.setSuccessCnt(tgCogradientMonitorDto.getSuccessCnt() + (Integer) entity.get("successCnt"));
                    }
                }
            }
            return AjaxResult.success(dtoList);
        } else {
            return AjaxResult.success();
        }
    }


    private Integer saveSchedule(Integer id, Integer processDefineId, String schedule) {
        String method;
        Integer scheduleId = null;
        if (id == null) {
            method = DsConstants.PROCESS_DEF_SCHE_CREATE;
        } else {
            method = DsConstants.PROCESS_DEF_SCHE_UPDATE;
            setScheduleState(id, ReleaseState.OFFLINE);
        }
        String url = getUri() + method;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("processDefinitionId", processDefineId);
        postParameters.add("id", id);
        postParameters.add("schedule", schedule);
        postParameters.add("warningType", appProperties.getFinalWarningType());
        postParameters.add("warningGroupId", appProperties.getFinalWarnGroupId());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        AjaxResult r = getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
        if (r.isDolphinSuccess()) {
            Map<String, Object> result = (Map<String, Object>) r.getData();
            scheduleId = (Integer) result.get("id");
            setScheduleState(scheduleId, ReleaseState.ONLINE);
        } else {
            log.warn("scheduler: result={}", r);
        }
        return scheduleId;
    }


    private void setScheduleState(Integer id, ReleaseState scheduleStatus) {
        String method;
        if (scheduleStatus.equals(ReleaseState.OFFLINE)) {
            method = DsConstants.PROCESS_DEF_SCHE_OFFLINE;
        } else {
            method = DsConstants.PROCESS_DEF_SCHE_ONLINE;
        }
        String url = getUri() + method;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("id", id);
        postParameters.add("scheduleStatus", scheduleStatus);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(postParameters, headers);
        getRestTemplate().postForObject(url, request, AjaxResult.class, getProjectName());
    }

    public Map getTaskInfo(Integer id) {
        String url = getUri() + DsConstants.PROCESS_TASK_DEFID + "?defId={defId}";
        Map<String, Object> postParameters = new HashMap<>();
        postParameters.put("defId", id);
        postParameters.put("projectName", getProjectName());
        AjaxResult r = getRestTemplate().getForObject(url, AjaxResult.class, postParameters);
        if (r.getCode() == 0) {
            return (Map) r.getData();
        }
        return null;
    }

    public List<MetaColumnDTO> convertType(TypeConvertParam param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(JsonUtils.format(param), headers);

        AjaxResult queryRes = getRestTemplate().postForObject(getUri() + DsConstants.SyncTask.CONVERT_TYPE, request, AjaxResult.class);
        if (Objects.isNull(queryRes) || queryRes.getCode() != 0) {
            log.error("res={}", queryRes);
            throw new CustomException("字段转换失败");
        }
        ArrayList data = (ArrayList) queryRes.getData();
        String json = JsonUtils.format(data);
        return JsonUtils.parse(json, new TypeReference<List<MetaColumnDTO>>() {
        });
    }

    public AjaxResult queryLog(int taskInstId, int skipLineNum, int limit) {
        String url = getUri() + DsConstants.PROCESS_TASK_LOG +
                "?taskInstanceId={taskInstId}&skipLineNum={skipLineNum}&limit={limit}";
        return getRestTemplate().getForObject(url, AjaxResult.class, taskInstId, skipLineNum, limit);
    }


    public ResponseEntity<byte[]> getLogBytes(int taskInstId) {
        String url = getUri() + DsConstants.PROCESS_TASK_LOG_DOWNLOAD +
                "?taskInstanceId={taskInstId}";
        byte[] logBytes = getRestTemplate().getForObject(url, byte[].class, taskInstId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                .body(logBytes);
    }


    public AjaxResult queryProcessDefinitionList(int pageNo, int pageSize, String searchVal, Integer releaseState) {
        String url = getUri() + DsConstants.PROCESS_DEF_LIST_PAGE +
                "?pageNo={pageNo}&pageSize={pageSize}&searchVal={searchVal}&releaseState={releaseState}";
        return RetryUtil.withRetry(() ->
                getRestTemplate().getForObject(url, AjaxResult.class, getProjectName(), pageNo, pageSize, searchVal, releaseState));
    }

    public AjaxResult queryProcessById(Integer processId) {
        String url = getUri() + DsConstants.PROCESS_DEF_DETAIL + "?processId={processId}";
        return getRestTemplate().getForObject(url, AjaxResult.class, getProjectName(), processId);
    }

    public String queryProcessNameById(Integer processId) {
        try {
            AjaxResult processResult = queryProcessById(processId);
            if (!processResult.isDolphinSuccess()) {
                log.warn("processResult={}", processResult);
                return null;
            }

            Map<String, Object> resultMap = (Map<String, Object>) processResult.getData();
            return Optional.ofNullable(resultMap.get("name")).map(Object::toString).orElse(null);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public String queryProcessNameByIdWithCache(Integer processId) {
        if (Objects.isNull(processId)) {
            return null;
        }
        return flowNameCache.get(processId, this::queryProcessNameById);
    }


    public AjaxResult queryProcessInstanceStatus(Integer processDefinitionId, Integer pageNo, Integer pageSize, String stateType) {
        String url = getUri() + DsConstants.PROCESS_INS_PAGE +
                "?processDefinitionId={processDefinitionId}&pageNo={pageNo}&pageSize={pageSize}&stateType={stateType}";
        return getRestTemplate().getForObject(url, AjaxResult.class, getProjectName(), processDefinitionId, pageNo, pageSize, stateType);
    }

    public AjaxResult queryProcessInstanceStatusByUUID(String uid) {
        String url = getUri() + DsConstants.PROCESS_INS_UID_DETAIL + "?uids={uid}";
        return RetryUtil.withRetry(() ->
                getRestTemplate().getForObject(url, AjaxResult.class, getProjectName(), uid));
//        return getRestTemplate().getForObject(url, AjaxResult.class, getProjectName(), uid);
    }


    public AjaxResult wrapException(Supplier<AjaxResult> func) {
        try {
            return func.get();
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("访问尚书台异常");
        }
    }
}
