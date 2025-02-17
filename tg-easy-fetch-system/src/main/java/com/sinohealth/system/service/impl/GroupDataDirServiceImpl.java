package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.domain.GroupDataDir;
import com.sinohealth.system.dto.GroupLeaderDto;
import com.sinohealth.system.mapper.GroupDataDirMapper;
import com.sinohealth.system.service.IGroupDataDirService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author jingjun
 * @date 2021-04-16
 */
@Service
public class GroupDataDirServiceImpl extends ServiceImpl<GroupDataDirMapper, GroupDataDir> implements IGroupDataDirService {
    @Override
    public List<Long> getDirId(List<Long> groupIds) {
        return this.baseMapper.getDirId(groupIds);
    }

    @Override
    public List<Long> getDirIdByLeaderId(Long userId) {
        return this.baseMapper.getDirIdByLeaderId(userId);
    }

    @Override
    public List<Long> getDirIdByUserId(Long userId) {
        return this.baseMapper.getDirIdByUserId(userId);
    }

    @Override
    public List<GroupLeaderDto> queryGroupLeader(Long dirId) {
        return this.baseMapper.queryGroupLeader(dirId);
    }
}
