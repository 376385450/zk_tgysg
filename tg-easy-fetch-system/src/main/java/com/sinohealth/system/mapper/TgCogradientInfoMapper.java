package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.TgCogradientInfo;
import com.sinohealth.system.dto.TgCogradientDetailDto;
import com.sinohealth.system.dto.TgCogradientInfoDto;
import com.sinohealth.system.dto.TgCogradientMonitorDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TgCogradientInfoMapper extends BaseMapper<TgCogradientInfo> {

    IPage<TgCogradientInfoDto> findList (Page<TgCogradientInfoDto> page, @Param("searchVal") String searchVal);

    void updateState(@Param("id") Integer id,@Param("state") Integer state,@Param("updateBy") String updateBy);

    String getDefIdByTask(@Param("id") Integer id,@Param("tableId") Integer tableId);

    TgCogradientDetailDto getByDefId(Integer defId);

    List<TgCogradientMonitorDto> queryStateCnt();

    String queryAllProcessId();

    int getCountByName(String name);

}
