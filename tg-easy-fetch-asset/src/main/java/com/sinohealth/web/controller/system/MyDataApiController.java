package com.sinohealth.web.controller.system;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.service.ISysUserTableService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.impl.DataDirServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jingjun
 * @since 2021/5/17
 */
@RestController
@RequestMapping("/api/system/mydata")
@Api(tags = {"我的数据"})
public class MyDataApiController extends BaseController {

    /**
     * 自定义数据目录
     */
    @Value("${dataset.dirSourceId}")
    private Long dirDataSourceId;

    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private ISysUserTableService userTableService;
    @Autowired
    private DataDirServiceImpl dataDirServiceImpl;

    @GetMapping("/list")
    //@ApiOperation(value = "表列表查询", response = TableInfo.class)
    public TableDataInfo list(@RequestParam(required = false) Long dirId,
                              @RequestParam(value = "tableName", required = false) String tableName,
                              @ApiParam(value = "安全等级 1-3") @RequestParam(value = "safeLevel", required = false) Integer safeLevel,
                              @ApiParam(value = "是否关注 ") @RequestParam(value = "concern", required = false) Boolean concern,
                              @ApiParam(value = "排序字段名") @RequestParam(required = false) String orderField,
                              @ApiParam(value = "排序 正序true,倒序false") @RequestParam(required = false, defaultValue = "false") boolean asc,
                              @ApiParam(value = "表单所属层") @RequestParam(required = false) String dirName,
                              @ApiParam(value = "是否是数据分析") @RequestParam(required = false) boolean isAnalysis) {
        Set<Long> dirIdSet = new HashSet<>();
        if (!StringUtils.isEmpty(dirName)) {
            dirIdSet = dataDirServiceImpl.getDirIdsByName(dirName);
            if (CollectionUtils.isEmpty(dirIdSet)) {
                return getDataTable(new ArrayList<>(0));
            }
        }

        // 如果是数据分析模块调用此接口，需要屏蔽自定义数据库目录
        Set<Long> extDirIdSet = new HashSet<>();
        if (isAnalysis) {
            if (dirId != null && dirId.longValue() == dirDataSourceId) {
                dirId = null;
            }
            if (CollectionUtils.isNotEmpty(dirIdSet)) {
                dirIdSet.removeIf(id -> dirDataSourceId.longValue() == id.longValue());
            }

            extDirIdSet.add(dirDataSourceId);
        }

        if (StringUtils.isEmpty(orderField)) {
            startPage(" t.id desc ");
        } else {
            startPage(String.format("concern".equals(orderField) ? " u.%s %s " : " t.%s %s ", StrUtil.toUnderlineCase(orderField), (asc ? SortParameters.Order.ASC.name() : SortParameters.Order.DESC.name())));
        }

        return getDataTable(tableInfoService.getMyTableList(dirId, tableName, safeLevel, concern, dirIdSet, extDirIdSet));
    }

    @GetMapping("/concern")
    //@ApiOperation(value = "修改关注")
    public AjaxResult list(@RequestParam Long tableId, @ApiParam(value = "是否关注") @RequestParam(defaultValue = "true") boolean concern) {
        userTableService.update(Wrappers.<SysUserTable>update().set("concern", concern).eq("table_id", tableId).eq("user_id", SecurityUtils.getUserId()));
        return AjaxResult.success();
    }

    @GetMapping("/concern2")
    public Object getDirIdsByName() {
        return dataDirServiceImpl.getDirIdsByName("中台");

    }
}
