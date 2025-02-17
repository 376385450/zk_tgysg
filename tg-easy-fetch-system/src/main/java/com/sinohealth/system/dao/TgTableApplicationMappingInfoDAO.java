package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;

import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 09:40
 */
public interface TgTableApplicationMappingInfoDAO extends IService<TgTableApplicationMappingInfo> {

    List<TgTableApplicationMappingInfo> list(List<Long> assetsIds);

    TgTableApplicationMappingInfo getByAssetsId(Long dataAssetsId);

    void deleteById(Integer id);
}
