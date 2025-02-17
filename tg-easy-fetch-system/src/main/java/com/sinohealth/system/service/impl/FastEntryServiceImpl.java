package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.FastEntry;
import com.sinohealth.system.domain.vo.FastEntryVO;
import com.sinohealth.system.dto.FastEntryUpsertRequest;
import com.sinohealth.system.mapper.FastEntryMapper;
import com.sinohealth.system.service.FastEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-23 13:50
 */
@Slf4j
@Service
public class FastEntryServiceImpl extends ServiceImpl<FastEntryMapper, FastEntry> implements FastEntryService {
    @Override
    public List<FastEntryVO> queryByUser() {
        Long userId = SecurityUtils.getUserId();
        List<FastEntry> list = this.baseMapper.selectList(new QueryWrapper<FastEntry>().lambda().eq(FastEntry::getUserId, userId));
        return list.stream().map(v -> {
            FastEntryVO vo = new FastEntryVO();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).sorted(Comparator.comparing(FastEntryVO::getSort)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsert(FastEntryUpsertRequest request) {
        Long userId = SecurityUtils.getUserId();
        List<FastEntryVO> list = request.getList();
        List<FastEntry> entryList = new ArrayList<>();

        List<Long> existId = list.stream().map(FastEntryVO::getId).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(existId)) {
            this.baseMapper.delete(new QueryWrapper<FastEntry>().lambda().eq(FastEntry::getUserId, userId).notIn(FastEntry::getId, existId));
        }

        for (int i = 0; i < list.size(); i++) {
            FastEntryVO vo = list.get(i);
            FastEntry entity = new FastEntry();
            BeanUtils.copyProperties(vo, entity);

            entity.setSort(i);
            entity.setUserId(userId);
            entryList.add(entity);
        }

        this.saveOrUpdateBatch(entryList);

    }
}
