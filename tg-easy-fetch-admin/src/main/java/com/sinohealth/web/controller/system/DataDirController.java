package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.IgnoreLog;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.EnumVO;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.dir.dto.AssetsSortEditRequest;
import com.sinohealth.system.biz.dir.dto.DirPageQueryRequest;
import com.sinohealth.system.biz.dir.service.AssetsSortService;
import com.sinohealth.system.biz.dir.vo.DataDirListVO;
import com.sinohealth.system.biz.dir.vo.HomeDataDirVO;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.dto.DataDirUpdateReqDTO;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.ITableInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 数据目录Controller
 *
 * @author linweiwu
 * @date 2022-04-16
 */
@Slf4j
@RestController
@RequestMapping({"/system/dir"})
@Api(tags = {"数据地图树状目录接口"})
public class DataDirController extends BaseController {

    @Resource
    private IDataDirService dataDirService;

    @Resource
    private ITableInfoService tableInfoService;
    @Autowired
    private AssetsSortService assetsSortService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置-地图目录管理-添加目录
     */

    @PostMapping("new")
    @ApiOperation(value = "添加一个新的地图目录")
    public AjaxResult<Object> newDir(@RequestBody @Valid DataDir dataDir) {
        try {
            dataDirService.newDir(dataDir);
            Long id = dataDir.getId();
            return AjaxResult.success(InfoConstants.REQUEST_OK, id);
        } catch (Exception ex) {
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }


    /**
     * 设置-地图目录管理-查看目录树
     *
     * @see DataDirApiController#pageQueryDirItem 替代接口
     */

    @GetMapping("dir_tree")
    @ApiOperation(value = "查看目录节点")
    @Deprecated
    public AjaxResult<Object> getDirTree(@RequestParam(defaultValue = "0", required = false) Long dirId,
                                         @RequestParam(defaultValue = "file", required = false) String type,
                                         @RequestParam(value = "name", required = false) String searchName,
                                         @RequestParam(value = "self", required = false) Boolean self) {
        try {
            if (SecurityUtils.isAdmin()) {
                self = false;
            }
            List list = dataDirService.getDirTreeGroupV2(dirId, type, searchName, self);
            return AjaxResult.success(InfoConstants.REQUEST_OK, list);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex.getMessage());
        }
    }


    @GetMapping("applicationTypeList")
    @ApiOperation(value = "资产类型")
    @IgnoreLog
    public AjaxResult<List<EnumVO<String>>> applicationTypeList() {
        return AjaxResult.success(ApplicationConst.DirItemTypeEnum.ALL_TYPE);
    }

    /**
     * 查询全部的一级和二级目录
     *
     * @param firstDirId 有值就查询子目录，空则查询顶级目录
     */

    @GetMapping("queryDir")
    @ApiOperation(value = "查看一二级目录")
    public AjaxResult<DataDirListVO> queryDir(@RequestParam(name = "firstDirId", required = false) Long firstDirId) {
        if (Objects.isNull(firstDirId)) {
            firstDirId = DataDirConst.TOP_PARENT_LEVEL;
        }

        DataDirListVO listVO = dataDirService.selectSonOfParentDir(firstDirId, DataDirConst.Status.ENABLE);
        return AjaxResult.success(listVO);
    }

    /**
     * 分页查询地图目录元素（表单/文档）
     */

    @PostMapping("pageQueryDirItem")
    @ApiOperation(value = "分页查询目录节点")
    public AjaxResult<IPage<HomeDataDirVO>> pageQueryDirItem(@RequestBody DirPageQueryRequest request) {
        return dataDirService.pageQueryDir(request);
    }

    /**
     * 设置-地图目录管理-查看目录下的文件
     */

    @GetMapping("tables")
    @ApiOperation(value = "查看文件节点")
    public AjaxResult<Object> getTableList(@RequestParam(defaultValue = "0", required = false) Long dirId) {
        try {
            List list = dataDirService.listTablesByDirId(dirId);
            return AjaxResult.success(InfoConstants.REQUEST_OK, list);
        } catch (Exception ex) {
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }

    /**
     * 设置-地图目录管理-修改目录树
     */

    @Transactional(rollbackFor = Exception.class)
    @PutMapping("update")
    @ApiOperation(value = "更新目录节点")
    public AjaxResult<Object> updateDirTree(@RequestBody @Valid DataDir dataDir) {
        try {

            if (dataDir.getIcon().equals(CommonConstants.ICON_FILE)
                    && StringUtils.isBlank(dataDir.getDirName())) {
                return AjaxResult.error(InfoConstants.DIRNAME_REQUIREMENT);
            }
            if (!dataDir.getParentId().equals(0L)) {
                // 如果存在父级目录,检测是否为file类型,如果不是, 不让拖动
                DataDir parentDir = DataDir.newInstance().selectById(dataDir.getParentId());
                if (!parentDir.getIcon().equals(CommonConstants.ICON_FILE)) {
                    return AjaxResult.error(InfoConstants.CAN_NOT_MOVE_FILE_INTO_LOCATION);
                }
            }
            int successNums = dataDirService.update(dataDir);
            // 如果是表, 联动更新表 table_info 里面的 dir_id
            if (dataDir.getIcon().equals(CommonConstants.ICON_TABLE)) {
                TableInfo table = tableInfoService.getById(dataDir.getTableId());
                table.setDirId(dataDir.getParentId());
                tableInfoService.updateById(table);
            }
            // 重排目录底下的目录节点
            DataDirListVO dataDirListVO = dataDirService.selectSonOfParentDir(dataDir.getParentId(), null);
            List<DataDir> list = dataDirListVO.getDirs();
            for (int idx = 0, sort = 2; idx < list.size(); idx++, sort += 2) {
                list.get(idx).setSort(sort);
                dataDirService.update(list.get(idx));
            }
            return AjaxResult.success(InfoConstants.REQUEST_OK, successNums);
        } catch (Exception ex) {
            log.error("异常", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, "");
        }
    }


    @PostMapping("/update2")
    @ApiOperation(value = "更新目录节点")
    public AjaxResult updateDirTreeV2(@Valid @RequestBody DataDirUpdateReqDTO reqDTO) {
        dataDirService.updateV2(reqDTO);
        return AjaxResult.success();
    }

    /**
     * 设置-地图目录管理-删除目录树
     */

    @DeleteMapping("delete")
    @ApiOperation(value = "删除目录节点")
    public AjaxResult<Object> deleteDirTree(@RequestParam Long dirId) {
        try {
            Integer deletedNums = dataDirService.delete(dirId);
            return AjaxResult.success(InfoConstants.REQUEST_OK, deletedNums);
        } catch (Exception ex) {
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }

    @PostMapping("/editSort")
    public AjaxResult<Boolean> editSort(@RequestBody @Validated AssetsSortEditRequest request) {
        Boolean lock = null;
        try {
            lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.SORT_MODIFY_LOCK, 0, Duration.ofSeconds(10));
            if (BooleanUtils.isTrue(lock)) {
                return assetsSortService.editSort(request);
            } else {
                return AjaxResult.error("请勿频繁操作");
            }
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        } finally {
            if (Objects.nonNull(lock)) {
                redisTemplate.delete(RedisKeys.SORT_MODIFY_LOCK);
            }
        }
    }

}
