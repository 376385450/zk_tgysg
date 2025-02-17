package com.sinohealth.system.biz.dataassets.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.github.pagehelper.Page;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.dao.TgDeliverCustomerRecordDAO;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.dto.TgDeliverCustomerRecordDTO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventRequest;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-02-29 18:33
 */
@Service
public class AssetsAdapter {

    @Autowired
    private TgDeliverCustomerRecordDAO deliverCustomerRecordDAO;
    private final static TimedCache<String, Object> shortCache = CacheUtil.newTimedCache(20_000);

    //预览：【状态】=“启用”
    //交付：【申请人】=当前用户 且 【状态】=“启用”
    //更新：【申请人】=当前用户 且 【状态】=“启用” 且 该份数据已分配至客户账号  且  该份申请数据对应宽表有最新“成功”状态的同步任务
    //更多：【更多】中可查看的操作功能>=1,则展示
    //数据说明文档：【申请人】=当前用户 且 【状态】=“启用”
    //交付记录：【申请人】=当前用户 且 【状态】=“启用”
    //重新申请：【申请人】=当前用户 且 【状态】=“启用”  且  （该份数据≠另存的项目数据 或 该份数据≠BI分析后的图表/仪表板）
    //复制项目：【申请人】=当前用户 且 【状态】=“启用”  且  该份数据是BI分析后的图表/仪表板
    //删除：【申请人】=当前用户 且 【状态】=“启用”  且 （该份数据=另存的项目数据 或 该份数据=BI分析后的图表/仪表板） 且 数据分配客户次数=0
    public List<Integer> buildActions(UserDataAssets assets) {
        boolean expire = LocalDateTime.now().isAfter(assets.getDataExpire());
        Long userId = SecurityUtils.getUserId();
        List<Integer> actions = new ArrayList<>();
        boolean isFile = ApplicationConfigTypeConstant.isFile(assets.getConfigType());

        actions.add(DataDirConst.ActionType.PREVIEW);
        // TODO 交付客户 需要处理，不允许可读用户直接拥有该权限
        actions.add(DataDirConst.ActionType.DELIVER);

        // 资产创建人
        if (!Objects.equals(assets.getApplicantId(), userId)) {
            return actions;
        }

//        actions.add(DataDirConst.ActionType.DOC);
        actions.add(DataDirConst.ActionType.DELIVER_RECORD);

        // 非另存项目
        if (Objects.isNull(assets.getCopyFromId())) {
            // V1.9.8 关闭手动更新，待考虑
            // 文件类型无法手动更新
//            if (!expire && !isFile) {
//                actions.add(DataDirConst.ActionType.MANUAL_UPGRADE);
//            }
            if (BooleanUtils.isNotTrue(assets.getDeprecated())) {
                actions.add(DataDirConst.ActionType.DEPRECATED);
            }
            actions.add(DataDirConst.ActionType.RE_APPLY);
        } else {
            // 另存类型项目
            String cacheKey = assets.getId() + "";
            Object cache = shortCache.get(cacheKey);
            if (Objects.isNull(cache)) {
                // 未分配客户 时 才能删除
                DataDeliverCustomerEventRequest request = new DataDeliverCustomerEventRequest();
                request.setAssetsId(assets.getId());
                Page<TgDeliverCustomerRecordDTO> pageData = deliverCustomerRecordDAO.queryParentList(request);
                if (Objects.isNull(pageData) || pageData.isEmpty()) {
                    actions.add(DataDirConst.ActionType.DELETE);
                    shortCache.put(cacheKey, true);
                } else {
                    shortCache.put(cacheKey, false);
                }
            } else {
                if ((boolean) cache) {
                    actions.add(DataDirConst.ActionType.DELETE);
                }
            }
        }
        return actions;

    }
}
