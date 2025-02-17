package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.mapper.SysUserTableMapper;
import com.sinohealth.system.service.ISysUserTableService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author dataplatform
 * @date 2021-04-21
 */
@Service
public class SysUserTableServiceImpl extends ServiceImpl<SysUserTableMapper, SysUserTable> implements ISysUserTableService {

    @Override
    public List<SysUserTable> groupByUserTable(){
        return this.baseMapper.groupByUserTable();
    }

    @Override
    public Long getCountTableByUserId(Long userId) {
        return baseMapper.getCountTableByUserId(userId);
    }
}
