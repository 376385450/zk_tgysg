package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TgDataDescription;

import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:19
 */
public interface DataDescriptionDAO extends IService<TgDataDescription> {

    TgDataDescription getByAssetsId(Long assetsId);

    List<TgDataDescription> listByAssetsIds(List<Long> assetsIds);

}
