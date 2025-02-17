package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsDirRequest;
import com.sinohealth.system.biz.dataassets.dto.request.DataDirRequest;
import com.sinohealth.system.biz.dir.dto.DirPageQueryRequest;
import com.sinohealth.system.biz.dir.entity.DataDirView;
import com.sinohealth.system.dto.DataDirDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据目录Mapper接口
 *
 * @author jingjun
 * @date 2021-04-16
 */
@Mapper
public interface DataDirMapper extends BaseMapper<DataDir> {

    List<Node> selectTreeData(@Param("target") Integer target, @Param("applicantId") Long applicantId,
                              @Param("searchStatus") Integer searchStatus, @Param("searchProjectName") String searchProjectName,
                              @Param("searchBaseTable") String searchBaseTable,
                              @Param("baseTableId") Long baseTableId,
                              @Param("icon") String icon,
                              @Param("expireType") String expireType);

    /**
     * @see DataDirDto 返回类型
     * @see DataDirMapper#queryAssetsData
     */
    List<Node> selectAssetsData(@Param("request") DataDirRequest request);

    /**
     * @see DataDirDto 返回类型
     */
    List<Node> queryAssetsData(@Param("request") AssetsDirRequest request);

    Integer insertAndGetId(@Param("dataDir") DataDir dataDir, @Param("target") Integer target);

    List<DataDir> selectSonOfParentDir(@Param("parentId") Long parentId,
                                       @Param("target") Integer target,
                                       @Param("status") Integer status);

    List<Long> getDirIdsByName(@Param("name") String name, @Param("target") Integer target, @Param("applicantId") Long applicantId);

    List<DataDir> getDataDirsByTarget(@Param("target") Integer target, @Param("applicantId") Long applicantId);

    List<DataDirDto> getDataDirs();

    List<Long> getAllNodeIds(Long id);

    DataDir getByApplicationId(@Param("applicationId") Long applicationId, @Param("icon") String icon);

    IPage<DataDirView> pageQueryDirView(@Param("page") IPage page, @Param("param") DirPageQueryRequest param);

    /**
     * 在根目录下的文件
     */
    Integer existOutOfDir();
}
