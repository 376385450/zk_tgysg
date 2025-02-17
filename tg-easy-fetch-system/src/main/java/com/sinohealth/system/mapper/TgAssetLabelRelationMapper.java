package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.label.TgAssetLabelRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 19:08
 */
@Mapper
public interface TgAssetLabelRelationMapper extends BaseMapper<TgAssetLabelRelation> {

    /**
     * 通过资产ID删除
     *
     * @param assetId
     */
    void deleteByAssetId(@Param("assetId") Long assetId);
}
