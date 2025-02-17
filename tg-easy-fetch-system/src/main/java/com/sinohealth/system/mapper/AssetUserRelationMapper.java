package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.biz.homePage.HotAssetsDTO;
import com.sinohealth.system.domain.TgAssetUserRelation;
import com.sinohealth.system.dto.assets.CollectListRequest;
import com.sinohealth.system.vo.CollectListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 16:10
 */
@Mapper
public interface AssetUserRelationMapper extends BaseMapper<TgAssetUserRelation> {

    /**
     * 我的收藏列表
     *
     * @param page
     * @param userId
     * @param collectListRequest
     * @return
     */
    IPage<CollectListVo> collectList(Page page, @Param("userId") Long userId, @Param("request") CollectListRequest collectListRequest);


    IPage<HotAssetsDTO> hotViewAssets(Page page,
                                      @Param("id") Long id,
                                      @Param("createdTime") Date createdTime,
                                      @Param("catalogueIds") List<Integer> catalogueIds,
                                      @Param("source") Integer source);

}
