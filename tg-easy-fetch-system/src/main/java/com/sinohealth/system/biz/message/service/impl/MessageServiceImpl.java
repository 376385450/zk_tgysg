package com.sinohealth.system.biz.message.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.AuditTypeEnum;
import com.sinohealth.common.enums.MessageTypeEnum;
import com.sinohealth.common.spi.alert.utils.JSONUtils;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import com.sinohealth.system.biz.message.constants.TargetPageType;
import com.sinohealth.system.biz.message.dto.MessageDTO;
import com.sinohealth.system.biz.message.service.MessageService;
import com.sinohealth.system.biz.ws.msg.UnReadMsg;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgNoticeRead;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.notice.TgNoticeRelateAsset;
import com.sinohealth.system.dto.assets.TgAssetFrontTreeQueryResult;
import com.sinohealth.system.dto.auditprocess.AuditPageByTypeDto;
import com.sinohealth.system.dto.notice.NoticeReadDTO;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.IAuditProcessService;
import com.sinohealth.system.service.INoticeService;
import com.sinohealth.system.vo.AssetPermissions;
import com.sinohealth.system.vo.TgNoticeInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2024/2/26
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {


    @Autowired
    INoticeService noticeService;

    @Autowired
    TgNoticeReadMapper tgNoticeReadMapper;

    @Autowired
    private TgNoticeRelateAssetMapper tgNoticeRelateAssetMapper;

    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;

    @Autowired
    private IAssetService assetService;

    @Autowired
    private TgMessageRecordDimMapper tgMessageRecordDimMapper;

    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;

    @Autowired
    private IAuditProcessService iAuditProcessService;

    @Autowired
    private WsMsgService wsMsgService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public PageInfo<MessageDTO> getMessageList(Long pageNum, Long pageSize, Integer type, Date queryTime,
                                               Long userId, Boolean readFilter) {
        queryTime = Objects.isNull(queryTime) ? new Date() : queryTime;
        // 获取当前用户已读列表
        final SysUser user = sysUserMapper.selectUserById(userId);
        if (Objects.isNull(user)) {
            final PageInfo<MessageDTO> messageDTOPageInfo = new PageInfo<>();
            messageDTOPageInfo.setList(new ArrayList<MessageDTO>());
            messageDTOPageInfo.setPageSize(0);
            messageDTOPageInfo.setPageNum(0);
            messageDTOPageInfo.setTotal(0);
            return messageDTOPageInfo;
        }

        String deptId = Optional.ofNullable(user.getOrgUserId()).map(SinoipaasUtils::mainEmployeeSelectbyid)
                .orElse(createEmptyOrgUser()).getMainOrganizationId();

        final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
                .eq(TgNoticeRead::getUserId, userId);

        final Set<Long> read = tgNoticeReadMapper.selectList(wq).stream()
                .map(TgNoticeRead::getNoticeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        // type : 1.代办 2.申请 3.资产更新 4.系统升级
        if (MessageTypeEnum.TODO.getType().equals(type)) {
            return this.buildTodoPageList(pageNum, pageSize, user);
        } else if (MessageTypeEnum.APPLICATION.getType().equals(type)) {
            return this.buildApplyPageList(pageNum, pageSize, queryTime, userId, readFilter);
        } else if (MessageTypeEnum.ASSET_UPDATE.getType().equals(type)) {
            return this.buildAssetsUpdatePageList(pageNum, pageSize, queryTime, userId, readFilter, read, deptId);
        } else if (MessageTypeEnum.SYSTEM_UPDATE.getType().equals(type)) {
            return this.buildSystemUpdatePageList(pageNum, pageSize, queryTime, readFilter, read);
        }
        return new PageInfo<>();
    }

    private PageInfo<MessageDTO> buildTodoPageList(Long pageNum, Long pageSize, SysUser user) {
        final List<AuditPageByTypeDto> auditPageByTypeDtos = iAuditProcessService.queryAuditProcessAuditListByUser(user);
        final int maxNum = pageNum.intValue() * pageSize.intValue();
        int total = auditPageByTypeDtos.size();
        final PageInfo<MessageDTO> info = new PageInfo<>();
        info.setPageNum(pageNum.intValue());
        info.setTotal(auditPageByTypeDtos.size());
        info.setPageSize(pageSize.intValue());
        info.setPages(total % pageSize.intValue() == 0 ? total / pageSize.intValue() : total / pageSize.intValue() + 1);
        final List<AuditPageByTypeDto> dtos = new ArrayList<>();
        int fromIndex = pageSize.intValue() * (pageNum.intValue() - 1);
        if (maxNum > total) {
            dtos.addAll(auditPageByTypeDtos.subList(fromIndex, total));
        } else {
            dtos.addAll(auditPageByTypeDtos.subList(fromIndex, maxNum));
        }

        final List<MessageDTO> messageDTOS = dtos.stream().map(a -> {
            final MessageDTO messageDTO = new MessageDTO();
            messageDTO.setTitle("您有一条【" + a.getApplicantName() + "】提交的" + a.getAssetType().getApplicationName() + (a.getDataState().equals("wait_confirm") ? "待确认" : "待审批"));
            messageDTO.setAssetId(a.getAssetId());
            messageDTO.setAssetName(a.getAssetName());
            messageDTO.setType(MessageTypeEnum.TODO.getType());
            messageDTO.setNoticeTime(DateUtils.parseDate(a.getCreateTime()));
            messageDTO.setId(a.getApplicationId().toString());
            messageDTO.setAssetType(a.getAssetType());
            if (a.getAssetType().equals(AssetType.MODEL)) {
                messageDTO.setContent(a.getProjectName());
            } else {
                messageDTO.setContent(a.getServiceType());
            }
            return messageDTO;
        }).collect(Collectors.toList());
        info.setList(messageDTOS);
        return info;
    }

    private PageInfo<MessageDTO> buildSystemUpdatePageList(Long pageNum, Long pageSize, Date queryTime, Boolean readFilter, Set<Long> read) {
        String noticeType = "系统升级";
        IPage<TgNoticeInfoVo> page = new Page<>();
        if (Objects.nonNull(readFilter)) {
            page = noticeService.pageNoticeByType(pageNum, pageSize, noticeType, queryTime, read);
        } else {
            page = noticeService.pageNoticeByType(pageNum, pageSize, noticeType, queryTime, null);
        }
        IPage<MessageDTO> iPage = new Page<>();
        iPage.setCurrent(page.getCurrent());
        iPage.setSize(page.getSize());
        iPage.setTotal(page.getTotal());
        iPage.setPages(page.getPages());
        iPage.setRecords(page.getRecords()
                .stream()
                .map(a -> {
                    final MessageDTO dto = new MessageDTO();
                    dto.setMessageType(MessageTypeEnum.ASSET_UPDATE.getType());
                    dto.setId(a.getId().toString());
                    dto.setTitle(a.getName());
                    dto.setContent(a.getContent());
                    dto.setNoticeTime(a.getCreateTime());
                    dto.setRead(read.contains(a.getId()));
                    return dto;
                }).collect(Collectors.toList()));

        return PageUtil.convert(iPage);
    }

    private PageInfo<MessageDTO> buildAssetsUpdatePageList(Long pageNum, Long pageSize, Date queryTime, Long userId, Boolean readFilter, Set<Long> read, String deptId) {
        String noticeType = "资产更新";

        IPage<MessageDTO> iPage = new Page<>();

        IPage<TgNoticeInfoVo> page;
        if (Objects.nonNull(readFilter)) {
            page = noticeService.pageNoticeByType(pageNum, pageSize, noticeType, queryTime, read);
        } else {
            page = noticeService.pageNoticeByType(pageNum, pageSize, noticeType, queryTime, null);
        }
        iPage.setCurrent(page.getCurrent());
        iPage.setSize(page.getSize());
        iPage.setTotal(page.getTotal());
        iPage.setPages(page.getPages());

        final List<TgNoticeInfoVo> records = page.getRecords();

        if (CollUtil.isEmpty(records)) {
            iPage.setRecords(new ArrayList<>());
            return PageUtil.convert(iPage);
        }

        final LambdaQueryWrapper<TgNoticeRelateAsset> wrapper = Wrappers.<TgNoticeRelateAsset>lambdaQuery()
                .in(TgNoticeRelateAsset::getNoticeId, records.stream().map(TgNoticeInfoVo::getId).collect(Collectors.toList()));
        final List<TgNoticeRelateAsset> tgNoticeRelateAssets = tgNoticeRelateAssetMapper.selectList(wrapper);
        final List<Long> assetsIds = tgNoticeRelateAssets.stream()
                .map(TgNoticeRelateAsset::getAssetId)
                .collect(Collectors.toList());


        final Map<TgAssetInfo, List<AssetPermissionType>> map = new HashMap<>();

        if (CollUtil.isNotEmpty(assetsIds)) {
            final List<TgAssetInfo> assetInfos = tgAssetInfoMapper.selectBatchIds(assetsIds);
            map.putAll(assetService.computePermissions(assetInfos, userId, deptId, true));
        }


        Map<Long, List<TgNoticeRelateAsset>> assetMap = new HashMap<>();
        assetMap.putAll(tgNoticeRelateAssets.stream().collect(Collectors.groupingBy(TgNoticeRelateAsset::getNoticeId)));

        final Map<Long, TgAssetFrontTreeQueryResult> readableMap = new HashMap<>();
        if (Objects.nonNull(SecurityUtils.getUserIdIgnoreError())) {
            final List<TgAssetFrontTreeQueryResult> tgAssetFrontTreeQueryResults = assetService.allReadableAsset();
            readableMap.putAll(tgAssetFrontTreeQueryResults.stream().collect(Collectors.toMap(TgAssetFrontTreeQueryResult::getId, v -> v)));
        }

        iPage.setRecords(records
                .stream()
                .map(a -> {
                    final MessageDTO dto = new MessageDTO();
                    dto.setMessageType(MessageTypeEnum.ASSET_UPDATE.getType());
                    dto.setId(a.getId().toString());
                    dto.setTitle(a.getName());
                    dto.setContent(a.getContent());
                    dto.setNoticeTime(a.getCreateTime());
                    dto.setRead(read.contains(a.getId()));
                    final List<TgNoticeRelateAsset> relateAssets = assetMap.get(a.getId());
                    if (CollUtil.isNotEmpty(relateAssets)) {
                        dto.setAsset(relateAssets.stream()
                                .map(b -> {
                                    final AssetPermissions assetPermissions = new AssetPermissions();
                                    final Map.Entry<TgAssetInfo, List<AssetPermissionType>> tgAssetInfoListEntry = map.entrySet().stream()
                                            .filter(c -> c.getKey().getId().equals(b.getAssetId()))
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
                    return dto;
                }).collect(Collectors.toList()));

        return PageUtil.convert(iPage);
    }

    private PageInfo<MessageDTO> buildApplyPageList(Long pageNum, Long pageSize, Date queryTime, Long userId, Boolean readFilter) {
        IPage<TgNoticeRead> pageQ = new Page<>(pageNum, pageSize);
        final LambdaQueryWrapper<TgNoticeRead> wq1 = Wrappers.<TgNoticeRead>lambdaQuery()
                .eq(TgNoticeRead::getBizType, MessageTypeEnum.APPLICATION.getType())
                .lt(TgNoticeRead::getCreateTime, queryTime)
                .ne(TgNoticeRead::getAuditType, 4)
                .and(w -> w.and(qw -> qw.eq(TgNoticeRead::getUserId, userId)
                                        .eq(Objects.nonNull(readFilter) && !readFilter, TgNoticeRead::getHasRead, 0)
                                )
                                .or(qw -> qw.eq(TgNoticeRead::getAuditUserId, userId)
                                        .in(TgNoticeRead::getAuditType, AuditTypeEnum.auditType)
                                        .eq(Objects.nonNull(readFilter) && !readFilter, TgNoticeRead::getAuditUserHasRead, 0)
                                )
                )
                .orderByDesc(TgNoticeRead::getCreateTime, TgNoticeRead::getId);

        final IPage<TgNoticeRead> page = tgNoticeReadMapper.selectPage(pageQ, wq1);

        final List<TgNoticeRead> records = page.getRecords();

        IPage<MessageDTO> iPage = new Page<>();
        iPage.setCurrent(page.getCurrent());
        iPage.setSize(page.getSize());
        final long total = page.getTotal();
        iPage.setTotal(total);
        iPage.setPages(total % pageSize.intValue() == 0 ? total / pageSize.intValue() : total / pageSize.intValue() + 1);

        final List<Long> ids = records.stream().map(TgNoticeRead::getApplicationId).collect(Collectors.toList());
        if (CollUtil.isEmpty(ids)) {
            return PageUtil.convert(iPage);
        }
        final List<TgApplicationInfo> tgApplicationInfos = tgApplicationInfoMapper.selectBatchIds(ids);
        final Map<Long, TgApplicationInfo> idMap = tgApplicationInfos.stream()
                .collect(Collectors.toMap(TgApplicationInfo::getId, v -> v));

        final List<Long> assetIds = tgApplicationInfos.stream().map(TgApplicationInfo::getNewAssetId)
                .collect(Collectors.toList());

        final Map<Long, TgAssetInfo> assetMap = Lambda.queryMapIfExist(assetIds, tgAssetInfoMapper::selectBatchIds, TgAssetInfo::getId);
//        final Map<Long, TgAssetInfo> assetMap = tgAssetInfoMapper.selectBatchIds(assetIds).stream()
//                .collect(Collectors.toMap(TgAssetInfo::getId, v -> v));


        iPage.setRecords(records.stream()
                .map(a -> {
                    final MessageDTO dto = new MessageDTO();
                    dto.setMessageType(MessageTypeEnum.APPLICATION.getType());
                    dto.setId(a.getId().toString());

                    final TgApplicationInfo apply = idMap.get(a.getApplicationId());
                    JsonBeanConverter.convert2Obj(apply);

                    dto.setUserAsserId(apply.getAssetsId());
                    dto.setApplicationId(a.getApplicationId());
                    dto.setNoticeTime(a.getCreateTime());
                    dto.setVersion(a.getVersion());
                    dto.setNodeId(AssetsNode.buildId(apply.getAssetsId(), ApplicationConst.AssetsIcon.DATA));

                    if (userId.equals(a.getUserId())) {
                        dto.setRead(a.getHasRead() == 1);
                    }
                    if (userId.equals(a.getAuditUserId())) {
                        dto.setRead(a.getAuditUserHasRead() == 1);
                    }

                    final TgAssetInfo tgAssetInfo = assetMap.get(apply.getNewAssetId());
                    if (Objects.isNull(tgAssetInfo)) {
                        return dto;
                    }

                    // 申请人和审核人是同一人时 只保留申请人数据
                    boolean isSetTitle = false;
                    if (userId.equals(a.getUserId())) {
                        if (a.getAuditType().equals(3)) {
                            dto.setTitle("您的提数申请执行成功(申请人)");
                            dto.setType(TargetPageType.ASSETS_DETAIL);
                            isSetTitle = true;
                        } else if (a.getAuditType().equals(1) || a.getAuditType().equals(2)) {
                            dto.setTitle("您的" + tgAssetInfo.getType().getApplicationName() + (a.getAuditType().equals(1) ? "已通过" : "已驳回"));
                            dto.setType(TargetPageType.APPLY_DETAIL);
                            isSetTitle = true;
                        } else if (a.getAuditType().equals(AuditTypeEnum.FAILED.getValue())) {
                            dto.setTitle("您的提数申请执行失败（申请人）");
                            dto.setType(TargetPageType.APPLY_DETAIL);
                            isSetTitle = true;
                        }
                    }
                    if (tgAssetInfo.getType().equals(AssetType.TABLE)) {
                        dto.setUserAsserId(apply.getNewAssetId());
                    }

                    // 审核人
                    if (userId.equals(a.getAuditUserId()) && !isSetTitle) {
                        if (a.getAuditType().equals(AuditTypeEnum.SUCCESS.getValue())) {
                            dto.setTitle("您审批的提数申请执行成功(管理员)");
                        } else if (a.getAuditType().equals(AuditTypeEnum.FAILED.getValue())) {
                            dto.setTitle("您审批的提数申请执行失败（管理员）");
                        }
                        dto.setType(TargetPageType.APPLY_DETAIL);
                        dto.setRead(a.getAuditUserHasRead() == 1);
                    }

                    dto.setAssetId(tgAssetInfo.getId());
                    dto.setAssetName(tgAssetInfo.getAssetName());
                    dto.setAssetType(tgAssetInfo.getType());
                    if (tgAssetInfo.getType().equals(AssetType.MODEL)) {
                        dto.setContent(apply.getProjectName());
                    } else {
                        if (CollUtil.isNotEmpty(apply.getPermission())) {
                            final String serviceType = apply.getPermission().stream()
                                    .map(item -> item.getTypeName().replace("申请", "")).collect(Collectors.joining("/"));
                            dto.setContent(serviceType);
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList()));
        return PageUtil.convert(iPage);
    }

    private SinoPassUserDTO createEmptyOrgUser() {
        return new SinoPassUserDTO();
    }

    @Override
    public UnReadMsg unReadMeg(Long userId) {
        final UnReadMsg unReadMsg = new UnReadMsg();
        unReadMsg.setUserId(userId);
        final PageInfo<MessageDTO> todoInfo = getMessageList(1L, 1L, MessageTypeEnum.TODO.getType(), null, userId, false);
        unReadMsg.setTodoCount(todoInfo.getTotal());

        final PageInfo<MessageDTO> applicationInfo = getMessageList(1L, 1L, MessageTypeEnum.APPLICATION.getType(), null, userId, false);
        unReadMsg.setApplyCount(applicationInfo.getTotal());
//        if (CollectionUtils.isNotEmpty(applicationInfo.getList())) {
//            long actual = applicationInfo.getList().stream().filter(v -> BooleanUtils.isNotTrue(v.isRead())).count();
//            if (!Objects.equals(actual, applicationInfo.getTotal())) {
//                log.error("UnRead", new RuntimeException("已读状态异常"));
//            }
//            unReadMsg.setApplyCount(actual);
//        }

        final PageInfo<MessageDTO> assetUpdateInfo = getMessageList(1L, 1L, MessageTypeEnum.ASSET_UPDATE.getType(), null, userId, false);
        unReadMsg.setUpdateCount(assetUpdateInfo.getTotal());
        final PageInfo<MessageDTO> systemUpdateInfo = getMessageList(1L, 1L, MessageTypeEnum.SYSTEM_UPDATE.getType(), null, userId, false);
        unReadMsg.setUpgradeCount(systemUpdateInfo.getTotal());
//        log.info("UNREAD {} {}", userId, JsonUtils.format(unReadMsg), new RuntimeException());
        return unReadMsg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void readMessage(NoticeReadDTO noticeReadDTO) {
        final Long userId = SecurityUtils.getUserId();
        if (noticeReadDTO.getType().equals(MessageTypeEnum.ASSET_UPDATE.getType())
                || noticeReadDTO.getType().equals(MessageTypeEnum.SYSTEM_UPDATE.getType())) {
            noticeReadDTO.getIds().forEach(a -> {
                final TgNoticeRead tgNoticeRead = new TgNoticeRead();
                tgNoticeRead.setUserId(userId);
                tgNoticeRead.setNoticeId(Long.valueOf(a));
                tgNoticeReadMapper.insert(tgNoticeRead);
            });
        } else {
            final List<Long> ids = noticeReadDTO.getIds().stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            final List<TgNoticeRead> tgNoticeReads = tgNoticeReadMapper.selectBatchIds(ids);
            log.info("{}阅读接口：{}", userId, JSONUtils.toJsonString(tgNoticeReads));
            for (TgNoticeRead tgNoticeRead : tgNoticeReads) {
                if (Objects.nonNull(tgNoticeRead.getUserId()) && tgNoticeRead.getUserId().equals(userId)) {
                    tgNoticeRead.setHasRead(1);
                }
                if (tgNoticeRead.getAuditUserId().equals(userId)) {
                    tgNoticeRead.setAuditUserHasRead(1);
                }
                tgNoticeReadMapper.updateById(tgNoticeRead);
            }
        }
        wsMsgService.pushUnReadMsg(userId);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void markAllRead(NoticeReadDTO noticeReadDTO) {

        // 获取当前用户已读列表
        final Long userId = SecurityUtils.getUserId();

        final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
                .eq(TgNoticeRead::getUserId, userId);

        final Set<Long> read = tgNoticeReadMapper.selectList(wq).stream()
                .map(TgNoticeRead::getNoticeId)
                .collect(Collectors.toSet());

        final List<Integer> types = noticeReadDTO.getTypes();

        for (Integer type : types) {
            if (type.equals(MessageTypeEnum.APPLICATION.getType())) {

                // 通知
                final LambdaQueryWrapper<TgNoticeRead> wq1 = Wrappers.<TgNoticeRead>lambdaQuery()
                        .eq(TgNoticeRead::getBizType, MessageTypeEnum.APPLICATION.getType())
                        .and(w -> {
                            w.and(qw -> {
                                        qw.eq(TgNoticeRead::getUserId, userId)
                                                .eq(TgNoticeRead::getHasRead, 0);
                                    })
                                    .or(qw -> {
                                        qw.eq(TgNoticeRead::getAuditUserId, userId)
                                                .eq(TgNoticeRead::getAuditType, 3)
                                                .eq(TgNoticeRead::getAuditUserHasRead, 0);
                                    });
                        })
                        .orderByDesc(TgNoticeRead::getCreateTime);

                final List<TgNoticeRead> tgNoticeReads = tgNoticeReadMapper.selectList(wq1);
                if (CollUtil.isNotEmpty(tgNoticeReads)) {
                    // 申请人通知
                    final List<TgNoticeRead> userNotice = tgNoticeReads.stream().filter(a -> userId.equals(a.getUserId())).collect(Collectors.toList());
                    // 审批人通知
                    final List<TgNoticeRead> auditNotice = tgNoticeReads.stream().filter(a -> userId.equals(a.getAuditUserId())).collect(Collectors.toList());
                    final List<Long> userNoticeIds = userNotice.stream().map(TgNoticeRead::getId).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(userNoticeIds)) {
                        final LambdaUpdateWrapper<TgNoticeRead> updateWrapper = Wrappers.<TgNoticeRead>lambdaUpdate()
                                .set(TgNoticeRead::getHasRead, 1)
                                .in(TgNoticeRead::getId, userNoticeIds);
                        tgNoticeReadMapper.update(null, updateWrapper);
                    }

                    final List<Long> auditNoticeIds = auditNotice.stream().map(TgNoticeRead::getId).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(auditNoticeIds)) {
                        final LambdaUpdateWrapper<TgNoticeRead> updateWrapper = Wrappers.<TgNoticeRead>lambdaUpdate()
                                .set(TgNoticeRead::getAuditUserHasRead, 1)
                                .in(TgNoticeRead::getId, auditNoticeIds);
                        tgNoticeReadMapper.update(null, updateWrapper);
                    }
                }

            } else if (type.equals(MessageTypeEnum.ASSET_UPDATE.getType()) || type.equals(MessageTypeEnum.SYSTEM_UPDATE.getType())) {
                String noticeType = "资产更新";
                if (type.equals(MessageTypeEnum.SYSTEM_UPDATE.getType())) {
                    noticeType = "系统升级";
                }
                final List<TgNoticeInfoVo> notice = noticeService.getAllNotice(noticeType, new Date());
                final List<TgNoticeInfoVo> unread = notice.stream()
                        .filter(a -> !read.contains(a.getId()))
                        .collect(Collectors.toList());

                for (TgNoticeInfoVo tgNoticeInfoVo : unread) {
                    final TgNoticeRead tgNoticeRead = new TgNoticeRead();
                    tgNoticeRead.setNoticeId(tgNoticeInfoVo.getId());
                    tgNoticeRead.setUserId(userId);
                    tgNoticeReadMapper.insert(tgNoticeRead);
                }

            }
        }
        wsMsgService.pushUnReadMsg(userId);
        return null;
    }

}
