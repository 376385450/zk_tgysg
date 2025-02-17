package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.dto.assets.MenuNameDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/4
 */
@Repository
public interface AssetsCatalogueMapper extends BaseMapper<AssetsCatalogue> {

    List<AssetsCatalogue> selectListInPath(@Param("ids") List<Integer> ids);

    List<MenuNameDto> getFullMenuNames();

    List<MenuNameDto> getLevel12MenuNames();

    Integer getLevel1CatalogueId(Integer catalogueId);
}
