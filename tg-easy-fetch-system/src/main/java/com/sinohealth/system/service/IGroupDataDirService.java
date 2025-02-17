package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.GroupDataDir;
import com.sinohealth.system.dto.GroupLeaderDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 * 
 * @author jingjun
 * @date 2021-04-16
 */
public interface IGroupDataDirService  extends IService<GroupDataDir>
{
    public List<Long> getDirId(List<Long> groupIds);

    public List<Long> getDirIdByLeaderId( Long userId);

    public List<Long> getDirIdByUserId(Long userId);

    public List<GroupLeaderDto> queryGroupLeader(@Param("dirId")Long dirId);
}
