package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.system.domain.personalservice.PersonalServiceView;
import com.sinohealth.system.dto.personalservice.PageQueryServiceRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 13:55
 */
@Repository
public interface PersonalServiceMapper {

    /**
     * 查询我的服务
     *
     * @param pageRequest
     */
    IPage<PersonalServiceView> queryDataView(IPage<PersonalServiceView> page, @Param("pageRequest") PageQueryServiceRequest pageRequest);

    /**
     * 查询我的服务
     *
     * @param userId
     * @param assetId
     * @return
     */
    List<PersonalServiceView> queryValidApplyByAssetId(@Param("userId") Long userId, @Param("assetId") Long assetId);
}
