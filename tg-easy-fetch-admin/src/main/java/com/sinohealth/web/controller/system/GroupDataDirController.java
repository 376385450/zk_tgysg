package com.sinohealth.web.controller.system;

import com.sinohealth.common.annotation.Log;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.enums.BusinessType;
import com.sinohealth.system.domain.GroupDataDir;
import com.sinohealth.system.service.IGroupDataDirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 【请填写功能名称】Controller
 * 
 * @author jingjun
 * @date 2021-04-16
 */
@RestController
@RequestMapping("/system/group/dir")
public class GroupDataDirController extends BaseController
{
    @Autowired
    private IGroupDataDirService groupDataDirService;

    /**
     * 查询【请填写功能名称】列表
     */
// @PreAuthorize("@ss.hasPermi('system:dir:list')")
    @GetMapping("/list")
    public TableDataInfo list(GroupDataDir groupDataDir)
    {
        startPage();
        List<GroupDataDir> list = groupDataDirService.list();
        return getDataTable(list);
    }



    /**
     * 获取【请填写功能名称】详细信息
     */
// @PreAuthorize("@ss.hasPermi('system:dir:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(groupDataDirService.getById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
// @PreAuthorize("@ss.hasPermi('system:dir:add')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GroupDataDir groupDataDir)
    {
        return toAjax(groupDataDirService.save(groupDataDir));
    }

    /**
     * 修改【请填写功能名称】
     */
// @PreAuthorize("@ss.hasPermi('system:dir:edit')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GroupDataDir groupDataDir)
    {
        return toAjax(groupDataDirService.update(groupDataDir,null));
    }

    /**
     * 删除【请填写功能名称】
     */
// @PreAuthorize("@ss.hasPermi('system:dir:remove')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(groupDataDirService.removeByIds(Arrays.asList(ids)));
    }
}
