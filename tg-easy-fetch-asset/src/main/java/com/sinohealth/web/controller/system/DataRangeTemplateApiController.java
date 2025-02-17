package com.sinohealth.web.controller.system;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.TgDataRangeTemplate;
import com.sinohealth.system.domain.vo.TgDataRangeGroupVO;
import com.sinohealth.system.domain.vo.TgDataRangeModifyVO;
import com.sinohealth.system.domain.vo.TgDataRangeTemplateVO;
import com.sinohealth.system.domain.vo.TgDataRangeVO;
import com.sinohealth.system.service.DataRangeTemplateService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.RangeTemplateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhangyanping
 * @date 2023/5/15 17:25
 */
@Api(value = "/dataRangeTemplate", tags = {"数据范围模版"})
@RestController
@RequestMapping("/api/dataRangeTemplate")
public class DataRangeTemplateApiController extends BaseController {

    @Resource
    private DataRangeTemplateService dataRangeTemplateService;

    
    @ApiOperation(value = "根据ID查询详细")
    @GetMapping("/{id}")
    public AjaxResult<TgDataRangeVO> query(@PathVariable("id") Long id) {
        TgDataRangeVO vo = null;
        TgDataRangeTemplate template = dataRangeTemplateService.getById(id);
        if (template == null) {
            return AjaxResult.success(vo);
        }
        List<TgDataRangeGroupVO> list = JsonUtils.parse(template.getDataRangeConfig(),
                new TypeReference<List<TgDataRangeGroupVO>>() {
                });
        vo = new TgDataRangeVO();
        vo.setDataRangeId(id);
        vo.setGroupList(list);
        return AjaxResult.success(vo);
    }


    
    @ApiOperation(value = "保存/修改模版")
    @PostMapping("/modify")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Long> modify(@RequestBody TgDataRangeVO dataRangeVO) {
        if (dataRangeVO.getGroupList() == null) {
            dataRangeVO.setGroupList(Collections.emptyList());
        }

        RangeTemplateUtil.checkGroupParam(dataRangeVO.getGroupList(), dataRangeVO.getHasCanChoose());

        TgDataRangeTemplate template = new TgDataRangeTemplate();
        template.setId(dataRangeVO.getDataRangeId());

        template.setDataRangeConfig(JsonUtils.format(dataRangeVO.getGroupList()));

        boolean existEmpty = dataRangeVO.getGroupList().stream().flatMap(v -> v.getData().stream())
                .anyMatch(ApplicationSqlUtil::hasEmptyNode);
        if (existEmpty) {
            return AjaxResult.error("请填写完整自定义列 数据范围");
        }

        Date date = new Date();
        template.setCreateTime(date);
        template.setUpdateTime(date);
        dataRangeTemplateService.saveOrUpdate(template);
        return AjaxResult.success(template.getId());
    }

    @ApiOperation(value = "保存/修改模版-复制场景")
    @PostMapping("/modifyCopy")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<TgDataRangeModifyVO> modifyCopy(@RequestBody TgDataRangeVO dataRangeVO) {
        TgDataRangeModifyVO r = new TgDataRangeModifyVO();
        if (dataRangeVO.getGroupList() == null) {
            dataRangeVO.setGroupList(Collections.emptyList());
        }

        TgDataRangeTemplate template = new TgDataRangeTemplate();
        template.setId(dataRangeVO.getDataRangeId());

        template.setDataRangeConfig(JsonUtils.format(dataRangeVO.getGroupList()));

        Set<String> errMsg = new HashSet<>();
        RangeTemplateUtil.checkDataRangeNotNull(dataRangeVO, errMsg);

        Date date = new Date();
        template.setCreateTime(date);
        template.setUpdateTime(date);
        dataRangeTemplateService.saveOrUpdate(template);

        r.setRangeTemplateId(template.getId());
        r.setMessage(String.join(";", errMsg));
        return AjaxResult.success(r);
    }


    
    @ApiOperation(value = "删除模版")
    @DeleteMapping("/delete/{id}")
    public AjaxResult<Boolean> delete(@PathVariable("id") Long id) {
        return AjaxResult.success(dataRangeTemplateService.removeById(id));
    }

    
    @ApiOperation(value = "根据ID数据批量查已使用的定义列")
    @GetMapping("/getCategoryIds")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataRangeIds", value = "1,2,3", required = true, type = "int"),
    })
    public AjaxResult<Set<Long>> getCategoryIds(@RequestParam("dataRangeIds") String idStr) {
        if (StringUtils.isEmpty(idStr)) {
            return AjaxResult.error("id参数必填");
        }

        String[] split = idStr.split(",");
        List<Long> ids = Stream.of(split).map(Long::parseLong).collect(Collectors.toList());

        // 遍历获取所有已使用的自定义列
        Set<Long> set = dataRangeTemplateService.queryFieldIdsByIds(ids);

        return AjaxResult.success(set);
    }


    @ApiOperation(value = "根据项目id查已使用的定义列")
    @GetMapping("/getCategoryIdsByProjectId")
    public AjaxResult<Set<Long>> getCategoryIdsByProjectId(@RequestParam("projectId") Long projectId,
                                                           @RequestParam("bizType") String bizType) {
        return AjaxResult.success(dataRangeTemplateService.queryFieldIdsByIds(projectId, bizType));
    }

    void checkParams(TgDataRangeTemplateVO templateVO, Set<String> nameSet, int i, Map<Long, Integer> categoryIdLevelMap) {
        if (templateVO == null) {
            return;
        }
        if (StringUtils.isEmpty(templateVO.getCategoryName()) || templateVO.getCategoryId() == null) {
            throw new CustomException("名称或者自定义列必填！", 400);
        }

        if (!nameSet.add(templateVO.getCategoryName())) {
            throw new CustomException("名称不能重复！", 400);
        }

        Integer level = categoryIdLevelMap.get(templateVO.getCategoryId());
        if (level == null) {
            categoryIdLevelMap.put(templateVO.getCategoryId(), i);
        } else {
            if (level != i) {
                throw new CustomException("自定义列不能重复！", 400);
            }
        }


        if (!CollectionUtils.isEmpty(templateVO.getChildren())) {
            for (TgDataRangeTemplateVO child : templateVO.getChildren()) {
                checkParams(child, nameSet, i + 1, categoryIdLevelMap);
            }
        }

    }


    void dfs(TgDataRangeTemplateVO templateVO, Set<Long> categoryIds) {
        if (templateVO == null) {
            return;
        }
        if (templateVO.getCategoryId() != null) {
            categoryIds.add(templateVO.getCategoryId());
        }

        if (CollectionUtils.isEmpty(templateVO.getChildren())) {
            return;
        }
        for (TgDataRangeTemplateVO child : templateVO.getChildren()) {
            dfs(child, categoryIds);
        }
    }

}
