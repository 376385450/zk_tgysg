package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.DelFlag;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgNoticeRead;
import com.sinohealth.system.domain.converter.NoticeInfoBeanConverter;
import com.sinohealth.system.domain.notice.TgNoticeInfo;
import com.sinohealth.system.domain.notice.TgNoticeRelateAsset;
import com.sinohealth.system.dto.assets.TgAssetFrontTreeQueryResult;
import com.sinohealth.system.dto.notice.*;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.mapper.TgNoticeInfoMapper;
import com.sinohealth.system.mapper.TgNoticeReadMapper;
import com.sinohealth.system.mapper.TgNoticeRelateAssetMapper;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.INoticeService;
import com.sinohealth.system.vo.AssetPermissions;
import com.sinohealth.system.vo.TgNoticeInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:16
 */
@Service
@Slf4j
public class NoticeServiceImpl implements INoticeService {

    @Resource
    private TgNoticeInfoMapper noticeInfoMapper;

    @Autowired
    private IAssetService assetService;

    @Autowired
    private TgNoticeRelateAssetMapper tgNoticeRelateAssetMapper;

    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;

    @Autowired
    private TgNoticeReadMapper tgNoticeReadMapper;

    @Autowired
    private WsMsgService wsMsgService;

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return
     */
    @Override
    public AjaxResult<PageInfo<TgNoticeInfoVo>> pageQuery(PageQueryNoticeRequest pageRequest) {
        Set<Long> read = new HashSet<>();
        if (Objects.nonNull(pageRequest.getRead())) {
            if (!pageRequest.getRead()) {
                // 取未读
                final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
                        .eq(TgNoticeRead::getUserId, SecurityUtils.getUserId());

                read.addAll(tgNoticeReadMapper.selectList(wq).stream()
                        .map(TgNoticeRead::getNoticeId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()));

                pageRequest.setReadList(read);
            }
        }
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        String deptId = o.getMainOrganizationId();
        final List<TgAssetFrontTreeQueryResult> tgAssetFrontTreeQueryResults = assetService.allReadableAsset();
        final Map<Long, TgAssetFrontTreeQueryResult> readableMap = tgAssetFrontTreeQueryResults.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
        IPage<TgNoticeInfoVo> page =
                noticeInfoMapper.pageQuery(new Page(pageRequest.getPageNum(), pageRequest.getPageSize()), pageRequest);

        final List<TgNoticeInfoVo> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return AjaxResult.success(PageUtil.convert(page));
        }
        final List<Long> noticeIds = records.stream()
                .map(TgNoticeInfoVo::getId)
                .collect(Collectors.toList());

        final LambdaQueryWrapper<TgNoticeRelateAsset> wrapper = Wrappers.<TgNoticeRelateAsset>lambdaQuery()
                .in(TgNoticeRelateAsset::getNoticeId, noticeIds);
        final List<TgNoticeRelateAsset> tgNoticeRelateAssets = tgNoticeRelateAssetMapper.selectList(wrapper);
        final List<Long> assetsIds = tgNoticeRelateAssets.stream()
                .map(TgNoticeRelateAsset::getAssetId)
                .collect(Collectors.toList());

        Map<Long, List<TgNoticeRelateAsset>> assetMap = new HashMap<>();
        assetMap.putAll(tgNoticeRelateAssets.stream().collect(Collectors.groupingBy(TgNoticeRelateAsset::getNoticeId)));


        if (CollUtil.isNotEmpty(assetsIds)) {
            final List<TgAssetInfo> assetInfos = tgAssetInfoMapper.selectBatchIds(assetsIds);
            final Map<TgAssetInfo, List<AssetPermissionType>> map = assetService.computePermissions(assetInfos, userId, deptId, true);
            for (TgNoticeInfoVo n : page.getRecords()) {
                final List<TgNoticeRelateAsset> relateAssets = assetMap.get(n.getId());
                if (CollUtil.isNotEmpty(relateAssets)) {
                    n.setAsset(relateAssets.stream()
                            .map(a-> {
                                final AssetPermissions assetPermissions = new AssetPermissions();
                                final Map.Entry<TgAssetInfo, List<AssetPermissionType>> tgAssetInfoListEntry = map.entrySet().stream()
                                        .filter(b -> b.getKey().getId().equals(a.getAssetId()))
                                        .findFirst().get();
                                final TgAssetInfo assetInfo = tgAssetInfoListEntry.getKey();
                                assetPermissions.setType(assetInfo.getType());
                                assetPermissions.setProcessId(assetInfo.getProcessId());
                                assetPermissions.setAssetName(assetInfo.getAssetName());
                                assetPermissions.setAssetId(assetInfo.getId());
                                assetPermissions.setRelatedId(assetInfo.getRelatedId());
                                assetPermissions.setAssetBindingDataName(assetInfo.getAssetBindingDataName());
                                assetPermissions.setHasReadPermission(Objects.nonNull(readableMap.get(assetInfo.getId())));
                                assetPermissions.setPermission(tgAssetInfoListEntry.getValue());
                                return assetPermissions;
                            }).collect(Collectors.toList()));
                }
            }
        }
        return AjaxResult.success(PageUtil.convert(page));
    }

    /**
     * 新增公告
     *
     * @param addRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Object> add(AddNoticeRequest addRequest) {
        TgNoticeInfo tgNoticeInfo = NoticeInfoBeanConverter.toEntity(addRequest);
        int insertResult = noticeInfoMapper.insert(tgNoticeInfo);
        if (addRequest.getNoticeType().equals("系统升级")) {
            wsMsgService.pushAnnouncementMsg(tgNoticeInfo.getId());
        } else {
            wsMsgService.pushAssetsMsg(tgNoticeInfo.getId());
        }
        if (CollUtil.isNotEmpty(addRequest.getAssetIds())) {
            // 新增关联
            addRequest.getAssetIds().forEach(a->{
                final TgNoticeRelateAsset tgNoticeRelateAsset = new TgNoticeRelateAsset();
                tgNoticeRelateAsset.setAssetId(a);
                tgNoticeRelateAsset.setNoticeId(tgNoticeInfo.getId());
                tgNoticeRelateAssetMapper.insert(tgNoticeRelateAsset);
            });
        }
        log.info("新增公告，名称：{}，结果：{}", addRequest.getName(), insertResult);
        return AjaxResult.success(tgNoticeInfo.getId());
    }

    /**
     * 更细公告
     *
     * @param updateRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Object> update(UpdateNoticeRequest updateRequest) {
        final LambdaUpdateWrapper<TgNoticeInfo> wq = Wrappers.<TgNoticeInfo>lambdaUpdate()
                .eq(TgNoticeInfo::getId, updateRequest.getId())
                .set(TgNoticeInfo::getName, updateRequest.getName())
                .set(TgNoticeInfo::getNoticeType, updateRequest.getNoticeType())
                .set(TgNoticeInfo::getContent, updateRequest.getContent())
                .set(TgNoticeInfo::getUpdateTime, new Date())
                .set(TgNoticeInfo::getUpdater, SecurityUtils.getRealName())
                .set(TgNoticeInfo::getIsTop, updateRequest.getIsTop());
        int updateResult = noticeInfoMapper.update(null, wq);

        if (updateRequest.getNoticeType().equals("系统升级")) {
            wsMsgService.pushAnnouncementMsg(updateRequest.getId());
        } else {
            wsMsgService.pushAssetsMsg(updateRequest.getId());
        }

        final LambdaQueryWrapper<TgNoticeRelateAsset> eq = Wrappers.<TgNoticeRelateAsset>lambdaQuery()
                .eq(TgNoticeRelateAsset::getNoticeId, updateRequest.getId());

        tgNoticeRelateAssetMapper.delete(eq);

        if (CollUtil.isNotEmpty(updateRequest.getAssetIds())) {
            // 新增关联
            updateRequest.getAssetIds().forEach(a->{
                final TgNoticeRelateAsset tgNoticeRelateAsset = new TgNoticeRelateAsset();
                tgNoticeRelateAsset.setAssetId(a);
                tgNoticeRelateAsset.setNoticeId(updateRequest.getId());
                tgNoticeRelateAssetMapper.insert(tgNoticeRelateAsset);
            });
        }

        log.info("更新公告，ID：{}，结果：{}", updateRequest.getId(), updateResult);
        return AjaxResult.success();
    }

    /**
     * 更新公告的置顶状态
     *
     * @param updateTopRequest
     * @return
     */
    @Override
    public AjaxResult<Object> updateIsTop(UpdateNoticeIsTopRequest updateTopRequest) {
        TgNoticeInfo tgNoticeInfo = new TgNoticeInfo();
        tgNoticeInfo.setId(updateTopRequest.getId());
        tgNoticeInfo.setIsTop(updateTopRequest.getIsTop());
        int updateResult = noticeInfoMapper.updateById(tgNoticeInfo);
        log.info("更新公告，ID：{}，结果：{}", tgNoticeInfo.getId(), updateResult);
        return AjaxResult.success();
    }

    /**
     * 删除公告
     *
     * @param deleteNotice
     * @return
     */
    @Override
    public AjaxResult<Object> delete(DeleteNoticeRequest deleteNotice) {
        TgNoticeInfo tgNoticeInfo = new TgNoticeInfo();
        tgNoticeInfo.setId(deleteNotice.getId());
        tgNoticeInfo.setDelFlag(DelFlag.DEL.getCode());
        tgNoticeInfo.setDeleteTime(new Date());
        int deleteResult = noticeInfoMapper.updateById(tgNoticeInfo);
        log.info("删除公告，ID：{}，结果：{}", tgNoticeInfo.getId(), deleteResult);
        wsMsgService.pushUnReadMsg();
        return AjaxResult.success();
    }

    @Override
    public IPage<TgNoticeInfoVo> pageNoticeByType(Long pageNum, Long pageSize, String type, Date queryTime, Collection<Long> id) {
        IPage<TgNoticeInfo> page = new Page<>(pageNum, pageSize);
        final LambdaQueryWrapper<TgNoticeInfo> wq = Wrappers.<TgNoticeInfo>lambdaQuery()
                .eq(TgNoticeInfo::getNoticeType, type)
                .eq(TgNoticeInfo::getDelFlag, DelFlag.NOT_DEL.getCode())
                .lt(TgNoticeInfo::getUpdateTime, queryTime)
                .notIn(CollUtil.isNotEmpty(id), TgNoticeInfo::getId, id)
                .orderByDesc(TgNoticeInfo::getCreateTime);
        final IPage<TgNoticeInfo> infoIPage = noticeInfoMapper.selectPage(page, wq);

        IPage<TgNoticeInfoVo> iPage = new Page<>();

        if (CollUtil.isNotEmpty(infoIPage.getRecords())) {
            iPage.setRecords(infoIPage.getRecords().stream()
                    .map(a-> {
                        final TgNoticeInfoVo vo = new TgNoticeInfoVo();
                        BeanUtils.copyProperties(a, vo);
                        return vo;
                    }).collect(Collectors.toList()));
        }
        iPage.setPages(infoIPage.getPages());
        iPage.setSize(infoIPage.getSize());
        iPage.setTotal(infoIPage.getTotal());
        iPage.setCurrent(infoIPage.getCurrent());

        return iPage;
    }

    @Override
    public List<TgNoticeInfoVo> getAllNotice(String type, Date queryTime) {
        final LambdaQueryWrapper<TgNoticeInfo> wq = Wrappers.<TgNoticeInfo>lambdaQuery()
                .eq(TgNoticeInfo::getNoticeType, type)
                .eq(TgNoticeInfo::getDelFlag, DelFlag.NOT_DEL.getCode())
                .le(TgNoticeInfo::getUpdateTime, queryTime);

        final List<TgNoticeInfo> tgNoticeInfos = noticeInfoMapper.selectList(wq);

        if (CollUtil.isNotEmpty(tgNoticeInfos)) {
            return tgNoticeInfos.stream()
                    .map(a-> {
                        final TgNoticeInfoVo vo = new TgNoticeInfoVo();
                        BeanUtils.copyProperties(a, vo);
                        return vo;
                    }).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
