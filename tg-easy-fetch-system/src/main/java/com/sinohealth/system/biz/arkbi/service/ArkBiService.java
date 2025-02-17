package com.sinohealth.system.biz.arkbi.service;

import com.sinohealth.arkbi.param.DeleteType;
import com.sinohealth.arkbi.vo.UserBaseInfoVo;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;
import com.sinohealth.system.biz.dataassets.dto.request.ArkBiEditRequest;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-10 15:35
 */
public interface ArkBiService {

    /**
     * 获取BI用户信息
     *
     * @param userId 易数阁用户id
     */
    UserBaseInfoVo getBIUserBaseInfoVo(Long userId) throws Exception;

    /**
     * 检查并清理过期的视图
     */
    void checkDeleteExpireView();

    /**
     * 删除视图
     *
     * @param type 软 可恢复 硬 不可恢复
     */
    boolean deleteView(List<UserDataAssetsBiView> views, DeleteType type);

    void editArkBi(ArkBiEditRequest request);


    boolean deleteChart(Long id);
}
