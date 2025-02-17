package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.system.biz.dir.dto.DirPageQueryRequest;
import com.sinohealth.system.biz.dir.vo.DataDirListVO;
import com.sinohealth.system.biz.dir.vo.HomeDataDirVO;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.dto.DataDirUpdateReqDTO;

import java.util.List;

/**
 * 数据目录Service接口
 *
 * @author jingjun
 * @date 2021-04-16
 */
public interface IDataDirService extends IService<DataDir> {
    DataDirDto getTree(Long id);

    DataDirDto getGroupTree(List<Long> idList, boolean loadTable, List<SysUserTable> userTableList, boolean isFilter);

    DataDirDto getGroupTreeByDirIds(List<Long> dirIdList, boolean loadTable, List<SysUserTable> userTableList, boolean isFilter);

    Integer newDir(DataDir dataDir);

    int update(DataDir dataDir);

    void updateV2(DataDirUpdateReqDTO reqDTO);

    Integer delete(Long DirId);

    /**
     * 地图目录， 我的数据 等多个树形结构
     */
    List getDirTreeGroup(Long id, String type, String searchName, Boolean self);

    List getDirTreeGroupV2(Long id, String type, String searchName, Boolean self);

    List listTablesByDirId(Long dirId);

    DataDirListVO selectSonOfParentDir(Long parentId, Integer status);

    DataDir getByNodeId(Long nodeId);

    AjaxResult<IPage<HomeDataDirVO>> pageQueryDir(DirPageQueryRequest request);

    List<Integer> buildTablePermissions(Long userId, TableInfo tableInfo);
}
